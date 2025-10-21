package com.ecole._2.services;

import com.ecole._2.dto.FamilleElligibleDTO;
import com.ecole._2.repositories.BourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BourseService {

    @Autowired
    private BourseRepository bourseRepository;

    @Autowired
    private ApiService apiService;

    public List<FamilleElligibleDTO> getFamillesElligibles() {
        List<Object[]> results = bourseRepository.findFamillesElligibles();
        List<FamilleElligibleDTO> dtos = new ArrayList<>();
        String accessToken = apiService.getAccessToken(); // Get token once

        for (Object[] result : results) {
            String nomFamille = (String) result[0];
            String userId = String.valueOf(result[1]);
            String loginUtilisateur = userId; // default fallback

            try {
                Map<String, Object> userData = apiService.getUser(userId, accessToken);
                if (userData != null && userData.containsKey("login")) {
                    loginUtilisateur = (String) userData.get("login");
                }
            } catch (Exception e) {
                System.err.println("Error fetching login for user ID " + userId + ": " + e.getMessage());
            }

            dtos.add(new FamilleElligibleDTO(nomFamille, loginUtilisateur, null, null));
        }
        return dtos;
    }

    public List<FamilleElligibleDTO> getFamillesElligiblesParCategorie() {
        List<Object[]> results = bourseRepository.findFamillesElligiblesParCategorie();
        List<FamilleElligibleDTO> dtos = new ArrayList<>();
        String accessToken = apiService.getAccessToken(); // Get token once

        for (Object[] result : results) {
            String nomFamille = (String) result[0];
            String userId = String.valueOf(result[1]); // On suppose que c’est bien l’ID utilisateur
            String loginUtilisateur = userId; // Fallback si le login n’est pas récupéré
            String categorieNom = (String) result[2];
            Long scoreCategorie = ((Number) result[3]).longValue();

            try {
                Map<String, Object> userData = apiService.getUser(userId, accessToken);
                if (userData != null && userData.containsKey("login")) {
                    loginUtilisateur = (String) userData.get("login");
                }
            } catch (Exception e) {
                System.err.println("Error fetching login for user ID " + userId + ": " + e.getMessage());
            }

            dtos.add(new FamilleElligibleDTO(nomFamille, loginUtilisateur, categorieNom, scoreCategorie));
        }

        return dtos;
    }
}
