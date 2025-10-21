package com.ecole._2.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ecole._2.models.CursusUser;
import com.ecole._2.models.CursusUserList;
import com.ecole._2.models.Milestone;
import com.ecole._2.models.User; // Assumons que vous avez un modèle User pour gérer les informations de l'utilisateur

@Service
public class UserCursusService {

    private static final Logger logger = LoggerFactory.getLogger(UserCursusService.class);

    private static final String BASE_URL = "https://api.intra.42.fr/v2/users/";
    private static final String BASE_URL_ALL_CURSUS_USERS = "https://api.intra.42.fr/v2/cursus_users";
    private static final String MILESTONE_BASE_URL = "https://pace-system.42.fr/api/v1/users/";
    private static final int MAIN_CURSUS_ID = 21; // 42cursus
    private static final int SECONDARY_CURSUS_ID = 9; // 42 (piscine)

    /**
     * Récupère l'ID de l'utilisateur à partir de son login
     */
    private String getUserIdByLogin(String login, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + login;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Anti rate-limit
        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        try {
            ResponseEntity<User> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    User.class
            );

            User user = response.getBody();
            if (user != null && user.getId() != null) {
                return user.getId();
            } else {
                logger.error("Utilisateur avec login {} introuvable ou sans ID", login);
                return null;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'ID pour le login {}: {}", login, e.getMessage());
            return null;
        }
    }

    /**
     * Vérifie si un utilisateur est un piscineux (cursus_id = 9) mais pas un étudiant (cursus_id = 21)
     */
    public boolean isPiscinerOnly(String userId, String accessToken) {
        try {
            List<CursusUser> cursusList = getAllCursusForUser(userId, accessToken);

            boolean hasCursus9 = cursusList.stream()
                    .anyMatch(cu -> cu.getCursus() != null && cu.getCursus().getId() == SECONDARY_CURSUS_ID);
            boolean hasCursus21 = cursusList.stream()
                    .anyMatch(cu -> cu.getCursus() != null && cu.getCursus().getId() == MAIN_CURSUS_ID);

            return hasCursus9 && !hasCursus21;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des cursus pour l'userId {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Récupère tous les cursus d'un utilisateur spécifique sans filtre
     */
    public List<CursusUser> getAllCursusForUser(String userId, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + userId + "/cursus_users";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        ResponseEntity<CursusUser[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CursusUser[].class
        );

        CursusUser[] body = response.getBody() != null ? response.getBody() : new CursusUser[0];

        return Arrays.asList(body);
    }

    /**
     * Récupère les cursus d'un utilisateur spécifique (filtré sur 42cursus uniquement)
     */
    public CursusUserList getUserCursus(String userId, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + userId + "/cursus_users";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Anti rate-limit
        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        ResponseEntity<CursusUser[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CursusUser[].class
        );

        CursusUser[] body = response.getBody() != null ? response.getBody() : new CursusUser[0];

        // Filtrer uniquement cursus_id = 21
        List<CursusUser> filtered = Arrays.stream(body)
                .filter(cu -> cu.getCursus() != null && cu.getCursus().getId() == MAIN_CURSUS_ID)
                .collect(Collectors.toList());

        filtered.forEach(cu -> {
            if (cu.getUser() != null && cu.getCursus() != null) {
                logger.info("Login: {} | Email: {} | Cursus: {}", 
                            cu.getUser().getLogin(), 
                            cu.getUser().getEmail(),
                            cu.getCursus().getName());
            }
        });

        return new CursusUserList(userId, filtered);
    }

    /**
     * Récupère tous les cursus utilisateurs filtrés par campus_id si fourni
     * mais ne garde que cursus_id = 21
     * Adapté pour support page et taille (page[number], page[size])
     */
    public CursusUserList getAllCursusUsersByCampus(String accessToken, Integer campusId, int pageNumber, int pageSize) {
        RestTemplate restTemplate = new RestTemplate();

        // Construire l'URL avec filtre exact
        String url = BASE_URL_ALL_CURSUS_USERS
                + "?filter[cursus_id]=" + MAIN_CURSUS_ID
                + (campusId != null ? "&filter[campus_id]=" + campusId : "")
                + "&page[number]=" + pageNumber
                + "&page[size]=" + pageSize;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        ResponseEntity<CursusUser[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CursusUser[].class
        );

        CursusUser[] body = response.getBody() != null ? response.getBody() : new CursusUser[0];

        // Filtrer uniquement cursus_id = 21 (sécurité)
        List<CursusUser> filtered = Arrays.stream(body)
                .filter(cu -> cu.getCursus() != null && cu.getCursus().getId() == MAIN_CURSUS_ID)
                .collect(Collectors.toList());

        filtered.forEach(cu -> {
            if (cu.getUser() != null && cu.getCursus() != null) {
                logger.info("Login: {} | Email: {} | Cursus: {}", 
                            cu.getUser().getLogin(), 
                            cu.getUser().getEmail(),
                            cu.getCursus().getName());
            }
        });

        return new CursusUserList(campusId != null ? String.valueOf(campusId) : "000000", filtered);
    }

    public List<CursusUser> getAllCursusUsers(String accessToken, Integer campusId) {
        List<CursusUser> allUsers = new ArrayList<>();
        int pageNumber = 1;
        int pageSize = 100;
        boolean hasMore = true;
    
        while (hasMore) {
            CursusUserList page = getAllCursusUsersByCampus(accessToken, campusId, pageNumber, pageSize);
            if (page != null) {
                List<CursusUser> usersOnPage = page.getCursusUsers();
                if (usersOnPage != null && !usersOnPage.isEmpty()) {
                    allUsers.addAll(usersOnPage);
                    pageNumber++;
                } else {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
        }
        return allUsers;
    }

    /**
     * Récupère les milestones d'un utilisateur spécifique depuis l'API pace-system
     */
    public List<Milestone> getUserMilestones(String userId, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = MILESTONE_BASE_URL + userId + "/milestones";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Anti rate-limit
        try {
            Thread.sleep(125);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during delay", e);
        }

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            logger.info("Raw API response for milestones: {}", rawResponse.getBody());

            ResponseEntity<Milestone[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Milestone[].class
            );

            Milestone[] body = response.getBody() != null ? response.getBody() : new Milestone[0];

            List<Milestone> milestones = Arrays.stream(body)
                    .collect(Collectors.toList());

            milestones.forEach(m -> {
                logger.info("UserId: {} | Milestone: Level {}, Deadline: {}, ValidatedAt: {}", 
                            userId, 
                            m.getLevel(),
                            m.getDeadline(),
                            m.getValidatedAt() != null ? m.getValidatedAt() : "Not validated");
            });

            return milestones;
        } catch (Exception e) {
            logger.error("Error fetching milestones for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }
}