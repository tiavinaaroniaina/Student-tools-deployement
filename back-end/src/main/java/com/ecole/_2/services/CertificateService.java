package com.ecole._2.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateService {
    
    @Autowired
    private ApiService apiService;
    
    @Value("${certificate.logo:classpath:static/pdf/logo.png}")
    private String logoPath;
    
    @Value("${certificate.tampon:classpath:static/pdf/tampon.png}")
    private String tamponPath;
    
    @Value("${certificate.signature.dg:classpath:static/pdf/signature_dg.png}")
    private String signatureDgPath;
    
    @Value("${certificate.signature.dp:classpath:static/pdf/signature_dp.png}")
    private String signatureDpPath;
    
    @Value("${certificate.signature.ap:classpath:static/pdf/signature_ap.png}")
    private String signatureApPath;
    
    @Value("${certificate.dg.name:LACOSTE Antony}")
    private String dgName;

    @Value("${certificate.dp.name:SEBASTIAN Yan San}")
    private String dpName;

    @Value("${certificate.ap.name:SURREL Antony}")
    private String apName;

    @Value("${certificate.dg.poste.fr:Directeur}")
    private String posteDgFr;

    @Value("${certificate.dp.poste.fr:Directeur Pédagogique et IT}")
    private String posteDpFr;

    @Value("${certificate.ap.poste.fr:Assistant Pédagogique et IT}")
    private String posteApFr;

    @Value("${certificate.dg.poste.en:Director}")
    private String posteDgEn;

    @Value("${certificate.dp.poste.en:Educational and IT Director}")
    private String posteDpEn;

    @Value("${certificate.ap.poste.en:Educational and IT Assistant}")
    private String posteApEn;

    @Value("${certificate.etablissement:42 ANTANANARIVO}")
    private String etablissement;
    
    @Value("${certificate.etablissement.adresse:Building Titan VI Zone Galaxy, Andraharo 101 Antananarivo}")
    private String etablissementAdresse;
    
    private static final Map<String, String> COUNTRY_TO_NATIONALITY = new HashMap<>();
    
    static {
        COUNTRY_TO_NATIONALITY.put("France", "Française");
        COUNTRY_TO_NATIONALITY.put("Madagascar", "Malgache");
        COUNTRY_TO_NATIONALITY.put("Spain", "Espagnole");
        COUNTRY_TO_NATIONALITY.put("Italy", "Italienne");
        COUNTRY_TO_NATIONALITY.put("Germany", "Allemande");
    }

    public Resource generateCertificate(String login, String sousignerPar, boolean signer, String lang) {
        try {
            if (login == null || login.trim().isEmpty()) {
                throw new IllegalArgumentException("Login cannot be empty");
            }
            if (!login.matches("^[a-zA-Z0-9_]+$")) {
                throw new IllegalArgumentException("Login must only contain letters, numbers or underscores");
            }

            String sousignerNormalized = "none";
            if (sousignerPar != null) {
                String s = sousignerPar.trim().toUpperCase();
                if (s.equals("DG") || s.equals("DP") || s.equals("AP")) {
                    sousignerNormalized = s;
                } else if (s.equalsIgnoreCase("none")) {
                    sousignerNormalized = "none";
                } else {
                    sousignerNormalized = "none";
                }
            }

            if (lang == null || (!"fr".equalsIgnoreCase(lang) && !"en".equalsIgnoreCase(lang))) {
                throw new IllegalArgumentException("lang must be 'fr' or 'en'");
            }

            String token = apiService.getAccessToken();
            String userId = apiService.getIdUsers(login, token);
            Map<String, Object> userData = apiService.getUser(userId, token);
            Map<String, Object> candidatureData = apiService.getUserCandidature(userId, token);

            ByteArrayOutputStream outputStream = createPdfDocument(userData, candidatureData, sousignerNormalized, signer, lang.toLowerCase());
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Error generating certificate for " + login + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error generating certificate for " + login + ": " + e.getMessage(), e);
        }
    }

    private ByteArrayOutputStream createPdfDocument(Map<String, Object> userData, Map<String, Object> candidatureData,
                                                  String sousignerPar, boolean signer, String lang) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 56.7f, 56.7f, 113.4f, 113.4f);
        
        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    try {
                        drawImages(writer.getDirectContent(), sousignerPar, signer);
                    } catch (Exception e) {
                        System.err.println("Image drawing error: " + e.getMessage());
                    }
                }
            });
            document.open();

            if ("en".equalsIgnoreCase(lang)) {
                addContentEnglish(document, userData, candidatureData, sousignerPar, signer);
            } else {
                addContentFrench(document, userData, candidatureData, sousignerPar, signer);
            }
            
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return outputStream;
    }

    private void addContentFrench(Document document, Map<String, Object> userData,
                                 Map<String, Object> candidatureData, String sousignerPar, boolean signer) throws DocumentException {
        try {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font footerFont2 = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(39, 221, 245));

            Paragraph title = new Paragraph("Certificat de scolarité", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            String nom = getStringValue(userData, "last_name", "Inconnu");
            String prenom = getStringValue(userData, "first_name", "Inconnu");
            String dateNaissance = formatDateNaissance(getStringValue(candidatureData, "birth_date", "2000-01-01"));
            String lieuNaissance = getStringValue(candidatureData, "birth_city", "Inconnu");
            String adresse = getStringValue(candidatureData, "postal_street", "Inconnu");
            String zipCode = getStringValue(candidatureData, "postal_zip_code", "00000");
            String ville = getStringValue(candidatureData, "postal_city", "Inconnu");
            String nationalite = COUNTRY_TO_NATIONALITY.getOrDefault(
                getStringValue(candidatureData, "country", "Inconnu"), "Inconnue");

            String gender = getStringValue(candidatureData, "gender", "");
            String monsieurMadame = "male".equals(gender) ? "Monsieur" : "female".equals(gender) ? "Madame" : "Monsieur/Madame";
            String interesse = "male".equals(gender) ? "l'intéressé" : "female".equals(gender) ? "l'intéressée" : "l'intéressé";
            String inscrit = "male".equals(gender) ? "inscrit" : "female".equals(gender) ? "inscrite" : "inscrit";

            String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String currentYear = String.valueOf(LocalDate.now().getYear());

            // Determine signer's name and position based on sousignerPar
            String signerName = "none";
            String signerPoste = "";
            if (!"none".equals(sousignerPar)) {
                if ("DG".equals(sousignerPar)) {
                    signerName = dgName;
                    signerPoste = posteDgFr;
                } else if ("DP".equals(sousignerPar)) {
                    signerName = dpName;
                    signerPoste = posteDpFr;
                } else if ("AP".equals(sousignerPar)) {
                    signerName = apName;
                    signerPoste = posteApFr;
                }
            }

            Paragraph content = new Paragraph();
            content.setAlignment(Element.ALIGN_LEFT);
            content.setSpacingAfter(12f);
            if (!"none".equals(sousignerPar)) {
                content.add(new Chunk("Je soussigné, Monsieur ", bodyFont));
                content.add(new Chunk(signerName + ", " + signerPoste, boldFont));
                content.add(new Chunk(" de l'établissement " + etablissement + ", domicilié au " + etablissementAdresse + ", atteste que l'élève :", bodyFont));
            } else {
                content.add(new Chunk("L'établissement " + etablissement + ", domicilié au " + etablissementAdresse + ", atteste que l'élève :", bodyFont));
            }
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk(monsieurMadame, boldFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Nom", boldFont));
            content.add(new Chunk(" : " + nom, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Prénom", boldFont));
            content.add(new Chunk(" : " + prenom, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Date de naissance", boldFont));
            content.add(new Chunk(" : " + dateNaissance, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Lieu de naissance", boldFont));
            content.add(new Chunk(" : " + lieuNaissance, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Nationalité", boldFont));
            content.add(new Chunk(" : " + nationalite, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Adresse", boldFont));
            content.add(new Chunk(" : " + adresse + ", " + zipCode + " " + ville, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Est régulièrement " + inscrit + " pour l'année " + currentYear + "/" + (Integer.parseInt(currentYear) + 1) +
              " à l'école " + etablissement + ", école gratuite sans frais d'écolage, et n'y a pas encore fini ses études.", bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Cette attestation est délivrée le " + currentDate +
                " à la demande de " + interesse + " pour servir et faire valoir ce que de droit.", bodyFont));
            document.add(content);

            Paragraph footer = new Paragraph();
            footer.add(new Chunk("BY ", footerFont));
            Chunk etablissementChunk = new Chunk(etablissement, footerFont2);
            etablissementChunk.setUnderline(0.5f, -2f);
            footer.add(etablissementChunk);
            footer.add(Chunk.NEWLINE);
            footer.add(new Chunk(etablissementAdresse, footerFont));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40f);
            document.add(footer);
        } catch (Exception e) {
            throw new DocumentException("Error creating French content: " + e.getMessage(), e);
        }
    }

    private void addContentEnglish(Document document, Map<String, Object> userData,
                                  Map<String, Object> candidatureData, String sousignerPar, boolean signer) throws DocumentException {
        try {
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font footerFont2 = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(39, 221, 245));

            Paragraph title = new Paragraph("School Certificate", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            String nom = getStringValue(userData, "last_name", "Unknown");
            String prenom = getStringValue(userData, "first_name", "Unknown");
            String dateNaissance = formatDateNaissance(getStringValue(candidatureData, "birth_date", "2000-01-01"));
            String lieuNaissance = getStringValue(candidatureData, "birth_city", "Unknown");
            String adresse = getStringValue(candidatureData, "postal_street", "Unknown");
            String zipCode = getStringValue(candidatureData, "postal_zip_code", "00000");
            String ville = getStringValue(candidatureData, "postal_city", "Unknown");
            String nationalite = COUNTRY_TO_NATIONALITY.getOrDefault(
                getStringValue(candidatureData, "country", "Inconnu"), "Inconnue");

            String gender = getStringValue(candidatureData, "gender", "");
            String mrMrs = "male".equals(gender) ? "Mr." : "female".equals(gender) ? "Mrs." : "Mr./Mrs.";
            String enrolled = "male".equals(gender) ? "enrolled" : "female".equals(gender) ? "enrolled" : "enrolled";

            String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String currentYear = String.valueOf(LocalDate.now().getYear());

            // Determine signer's name and position based on sousignerPar
            String signerName = "none";
            String signerPoste = "";
            if (!"none".equals(sousignerPar)) {
                if ("DG".equals(sousignerPar)) {
                    signerName = dgName;
                    signerPoste = posteDgEn;
                } else if ("DP".equals(sousignerPar)) {
                    signerName = dpName;
                    signerPoste = posteDpEn;
                } else if ("AP".equals(sousignerPar)) {
                    signerName = apName;
                    signerPoste = posteApEn;
                }
            }

            Paragraph content = new Paragraph();
            content.setAlignment(Element.ALIGN_LEFT);
            content.setSpacingAfter(12f);
            if (!"none".equals(sousignerPar)) {
                content.add(new Chunk("I, the undersigned, Mr. ", bodyFont));
                content.add(new Chunk(signerName + ", " + signerPoste, boldFont));
                content.add(new Chunk(" of the institution " + etablissement + ", located at " + etablissementAdresse + ", hereby certify that the student:", bodyFont));
            } else {
                content.add(new Chunk("The institution " + etablissement + ", located at " + etablissementAdresse + ", hereby certifies that the student:", bodyFont));
            }
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk(mrMrs, boldFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Last name", boldFont));
            content.add(new Chunk(" : " + nom, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("First name", boldFont));
            content.add(new Chunk(" : " + prenom, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Date of birth", boldFont));
            content.add(new Chunk(" : " + dateNaissance, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Place of birth", boldFont));
            content.add(new Chunk(" : " + lieuNaissance, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Nationality", boldFont));
            content.add(new Chunk(" : " + nationalite, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Address", boldFont));
            content.add(new Chunk(" : " + adresse + ", " + zipCode + " " + ville, bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("Is regularly " + enrolled + " for the year " + currentYear +
                " at " + etablissement + ", a free school with no tuition fees, and has not yet completed their studies.", bodyFont));
            content.add(Chunk.NEWLINE);
            content.add(Chunk.NEWLINE);
            content.add(new Chunk("This certificate is issued on " + currentDate + " at the request of the interested party to serve and assert their legal rights.", bodyFont));
            document.add(content);

            Paragraph footer = new Paragraph();
            footer.add(new Chunk("BY ", footerFont));
            Chunk etablissementChunk = new Chunk(etablissement, footerFont2);
            etablissementChunk.setUnderline(0.5f, -2f);
            footer.add(etablissementChunk);
            footer.add(Chunk.NEWLINE);
            footer.add(new Chunk(etablissementAdresse, footerFont));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40f);
            document.add(footer);
        } catch (Exception e) {
            throw new DocumentException("Error creating English content: " + e.getMessage(), e);
        }
    }

    private void drawImages(PdfContentByte canvas, String sousignerPar, boolean signer) {
        try {
            float pageWidth = PageSize.A4.getWidth();
            float pageHeight = PageSize.A4.getHeight();

            try {
                if (logoPath != null && !logoPath.equals("classpath:static/pdf/logo.png") && !logoPath.equals("Inconnu.png")) {
                    Image logo = loadImageFromClasspath(logoPath);
                    if (logo != null) {
                        logo.scaleToFit(pageWidth - 113.4f, 113.4f);
                        float logoWidth = logo.getScaledWidth();
                        float logoX = (pageWidth - logoWidth) / 2;
                        float logoY = pageHeight - logo.getScaledHeight();
                        logo.setAbsolutePosition(logoX, logoY);
                        canvas.addImage(logo);
                    }
                }
            } catch (Exception e) {
                System.err.println("Logo error: " + e.getMessage());
            }

            float tamponX = 0;
            try {
                if (tamponPath != null && !tamponPath.equals("classpath:static/pdf/tampon.png") && !tamponPath.equals("Inconnu.png")) {
                    Image tampon = loadImageFromClasspath(tamponPath);
                    if (tampon != null) {
                        tampon.scaleToFit(170.1f, 105.8f);
                        float tamponWidth = tampon.getScaledWidth();
                        tamponX = pageWidth - tamponWidth - 56.7f;
                        float tamponY = 170.1f;
                        tampon.setAbsolutePosition(tamponX, tamponY);
                        canvas.addImage(tampon);
                    }
                }
            } catch (Exception e) {
                System.err.println("Tampon error: " + e.getMessage());
            }

            try {
                String signaturePath = null;
                if (signer && !"none".equals(sousignerPar)) {
                    if ("DG".equals(sousignerPar)) {
                        signaturePath = signatureDgPath;
                    } else if ("DP".equals(sousignerPar)) {
                        signaturePath = signatureDpPath;
                    } else if ("AP".equals(sousignerPar)) {
                        signaturePath = signatureApPath;
                    }
                }

                if (signaturePath != null && !signaturePath.equals("classpath:static/pdf/signature_dg.png") 
                    && !signaturePath.equals("classpath:static/pdf/signature_dp.png") 
                    && !signaturePath.equals("classpath:static/pdf/signature_ap.png") 
                    && !signaturePath.equals("Inconnu.png")) {
                    Image signature = loadImageFromClasspath(signaturePath);
                    if (signature != null) {
                        signature.scaleToFit(170.1f, 85.05f);
                        signature.setAbsolutePosition(tamponX, 170.1f);
                        canvas.addImage(signature);
                    }
                }
            } catch (Exception e) {
                System.err.println("Signature error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("General drawing error: " + e.getMessage());
        }
    }

    private Image loadImageFromClasspath(String imagePath) {
        try {
            System.out.println("Chargement de l'image: " + imagePath);
            
            String resourcePath = imagePath;
            
            if (imagePath.startsWith("classpath:")) {
                resourcePath = imagePath.substring(10);
            }
            
            System.out.println("Chemin de ressource final: " + resourcePath);
            
            try {
                ClassPathResource resource = new ClassPathResource(resourcePath);
                System.out.println("Ressource existe: " + resource.exists());
                System.out.println("URI de la ressource: " + resource.getURI());
                
                if (resource.exists()) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        byte[] imageBytes = inputStream.readAllBytes();
                        System.out.println("Taille des données de l'image: " + imageBytes.length + " bytes");
                        return Image.getInstance(imageBytes);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur avec ClassPathResource: " + e.getMessage());
            }
            
            try {
                InputStream stream = getClass().getResourceAsStream("/" + resourcePath);
                if (stream == null) {
                    stream = getClass().getResourceAsStream(resourcePath);
                }
                
                if (stream != null) {
                    try (InputStream inputStream = stream) {
                        byte[] imageBytes = inputStream.readAllBytes();
                        System.out.println("Image chargée via getResourceAsStream, taille: " + imageBytes.length + " bytes");
                        return Image.getInstance(imageBytes);
                    }
                } else {
                    System.out.println("Stream null pour le chemin: " + resourcePath);
                }
            } catch (Exception e) {
                System.err.println("Erreur avec getResourceAsStream: " + e.getMessage());
            }
            
            System.out.println("Impossible de charger l'image: " + imagePath);
            return null;
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement de l'image: " + imagePath + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String formatDateNaissance(String birthDate) {
        try {
            if (birthDate == null || birthDate.trim().isEmpty()) {
                return "01/01/2000";
            }
            LocalDate date = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return "01/01/2000";
        }
    } 

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }
}