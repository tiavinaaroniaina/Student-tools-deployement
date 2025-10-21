package com.ecole._2.controller;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecole._2.models.CursusUserList;
import com.ecole._2.models.User;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.UserCursusService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BlackholeController {

    private static final Logger logger = LoggerFactory.getLogger(BlackholeController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private UserCursusService userCursusService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private com.ecole._2.repositories.UserCandidaturesRepository userCandidaturesRepository;

    @GetMapping("/blackhole")
    public List<BlackholeStudent> getBlackholeStudents(HttpSession session) {
        User user = (User) session.getAttribute("userResponse");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
    
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : droits administrateur requis.");
        }

        String tokenAdmin = apiService.getAccessToken();

        // Récupération filtrée par cursus_id=21, campus_id=65, page=2, size=100
        CursusUserList cursusUserList = userCursusService.getAllCursusUsersByCampus(tokenAdmin, 65, 2, 100);

        LocalDate today = LocalDate.now();

        List<BlackholeStudent> students = cursusUserList.getCursusUsers()
                .stream()
                .filter(cu -> cu != null 
                        && cu.getUser() != null
                        && cu.getUser().getEmail() != null
                        && cu.getUser().getLogin() != null
                        && cu.getBlackholed_at() != null 
                        && !cu.getBlackholed_at().isEmpty())
                .map(cu -> {
                    LocalDate blackholeDate;
                    try {
                        blackholeDate = LocalDate.parse(cu.getBlackholed_at());
                    } catch (DateTimeParseException e1) {
                        try {
                            blackholeDate = ZonedDateTime.parse(cu.getBlackholed_at()).toLocalDate();
                        } catch (DateTimeParseException e2) {
                            logger.warn("Impossible de parser la date de blackhole pour l'utilisateur {}: {}", 
                                        cu.getUser().getLogin(), cu.getBlackholed_at());
                            return null;
                        }
                    }

                    long daysUntilBlackhole = ChronoUnit.DAYS.between(today, blackholeDate);

                    // Ne pas afficher si > 21 jours ou date passée
                    if (daysUntilBlackhole > 21 || daysUntilBlackhole < 0) {
                        return null;
                    }

                    String badgeColor = getBadgeColor(daysUntilBlackhole);

                    // Formatter la date en yyyy-MM-dd pour enlever l'heure
                    String formattedDate = blackholeDate.format(DATE_FORMATTER);

                    return new BlackholeStudent(
                            cu.getUser().getLogin(),
                            cu.getUser().getEmail(),
                            formattedDate,
                            badgeColor,
                            daysUntilBlackhole
                    );
                })
                .filter(bs -> bs != null)
                .sorted(Comparator.comparingLong(BlackholeStudent::getDaysUntilBlackhole))
                .collect(Collectors.toList());

        // DEBUG : afficher les étudiants filtrés
        students.forEach(bs -> logger.info("Blackhole student: {}, Email: {}, Days until: {}", 
                bs.getLogin(), bs.getEmail(), bs.getDaysUntilBlackhole()));

        return students;
    }

    @GetMapping("/safe-users")
    public List<SafeUser> getSafeUsers(HttpSession session) {
        User user = (User) session.getAttribute("userResponse");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
    
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : droits administrateur requis.");
        }

        String tokenAdmin = apiService.getAccessToken();

        List<com.ecole._2.models.CursusUser> allUsers = userCursusService.getAllCursusUsers(tokenAdmin, 65);

        LocalDate today = LocalDate.now();

        return allUsers.stream()
                .filter(cu -> cu != null && cu.getUser() != null && cu.getUser().getLogin() != null)
                .map(cu -> {
                    String contactPhone1 = userCandidaturesRepository.findByUserId(cu.getUser().getId())
                                            .map(com.ecole._2.models.UserCandidatures::getContactPhone1)
                                            .orElse(null);
                    return new SafeUser(
                        cu.getUser().getLogin(),
                        cu.getUser().getEmail(),
                        cu.getUser().getFirst_name(),
                        cu.getUser().getLast_name(),
                        contactPhone1
                    );
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/notify")
    public void notifyStudent(@RequestBody NotificationRequest request, HttpSession session) {
        User user = (User) session.getAttribute("userResponse");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }
    
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé : droits administrateur requis.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tiavinaaroniaina@gmail.com");
           //  message.setTo(request.getEmail());
           message.setTo("mioraralevason@gmail.com");  // Utilise l'email de la requête
            message.setSubject("Blackhole Notification - 42 Antananarivo");
            message.setText(
                String.format(
                    "Dear %s,\n\nYou are scheduled to be blackholed on %s. Please take necessary actions to avoid this status.\n\nBest regards,\n42 Antananarivo Team",
                    request.getLogin(),
                    getBlackholeDateForStudent(request.getLogin())
                )
            );
            mailSender.send(message);
            logger.info("Notification email sent to {} for user {}", request.getEmail(), request.getLogin());
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", request.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send notification email.");
        }
    }

    private String getBlackholeDateForStudent(String login) {
        String tokenAdmin = apiService.getAccessToken();
        CursusUserList cursusUserList = userCursusService.getAllCursusUsersByCampus(tokenAdmin, 65, 2, 100);
        return cursusUserList.getCursusUsers()
                .stream()
                .filter(cu -> cu.getUser() != null && cu.getUser().getLogin() != null)
                .filter(cu -> cu.getUser().getLogin().equals(login) && cu.getBlackholed_at() != null)
                .map(cu -> {
                    try {
                        return LocalDate.parse(cu.getBlackholed_at()).format(DATE_FORMATTER);
                    } catch (DateTimeParseException e1) {
                        try {
                            return ZonedDateTime.parse(cu.getBlackholed_at()).toLocalDate().format(DATE_FORMATTER);
                        } catch (DateTimeParseException e2) {
                            return "Unknown";
                        }
                    }
                })
                .findFirst()
                .orElse("Unknown");
    }

    private boolean isAdmin(User user) {
        return "admin".equalsIgnoreCase(user.getKind()) || 
               "root".equalsIgnoreCase(user.getLogin()) || 
               "supervisor".equalsIgnoreCase(user.getLogin());
    }

    private String getBadgeColor(long days) {
        if (days <= 7) return "red";       // 1 semaine
        if (days <= 14) return "orange";   // 2 semaines
        if (days <= 21) return "yellow";   // 3 semaines
        return "none";
    }

    public static class BlackholeStudent {
        private String login;
        private String email;
        private String blackholedAt;
        private String badgeColor;
        private long daysUntilBlackhole;

        public BlackholeStudent(String login, String email, String blackholedAt, String badgeColor, long daysUntilBlackhole) {
            this.login = login;
            this.email = email;
            this.blackholedAt = blackholedAt;
            this.badgeColor = badgeColor;
            this.daysUntilBlackhole = daysUntilBlackhole;
        }

        public String getLogin() { return login; }
        public String getEmail() { return email; }
        public String getBlackholedAt() { return blackholedAt; }
        public String getBadgeColor() { return badgeColor; }
        public long getDaysUntilBlackhole() { return daysUntilBlackhole; }
    }

    public static class NotificationRequest {
        private String login;
        private String email;

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class SafeUser {
        private String login;
        private String email;
        private String firstName;
        private String lastName;
        private String contactPhone1;

        public SafeUser(String login, String email, String firstName, String lastName, String contactPhone1) {
            this.login = login;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.contactPhone1 = contactPhone1;
        }

        public String getLogin() { return login; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getContactPhone1() { return contactPhone1; }
    }
}