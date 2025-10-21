package com.ecole._2.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.ecole._2.models.CursusUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    @Value("${app.client_id}")
    private String uid;

    @Value("${app.client_secret}")
    private String secret;

    private static final String BASE_URL = "https://api.intra.42.fr";
    private static final String ACCESS_TOKEN_URL = "https://api.intra.42.fr/oauth/token";
    private static final String ACCESS_TOKEN_url_V3 = "https://auth.42.fr/auth/realms/staff-42/protocol/openid-connect/token"; 
    private static final String USERS_URL = "https://api.intra.42.fr/v2/campus/65/users/";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- Méthode V2 standard ---
    public String getAccessToken() {
        try {
            logger.info("Requesting access token from 42 API");

            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("grant_type", "client_credentials");
            data.add("scope", "public projects profile tig elearning forum");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(uid, secret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(data, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(ACCESS_TOKEN_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Access token successfully obtained");
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.has("access_token")) {
                    return jsonNode.get("access_token").asText();
                } else {
                    throw new RuntimeException("Access token not found in response");
                }
            } else {
                logger.error("Error obtaining token: {}", response.getStatusCode());
                throw new RuntimeException("Error obtaining access token: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error obtaining token: {}", e.getMessage());
            throw new RuntimeException("Authentication error with 42 API: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error obtaining token: {}", e.getMessage());
            throw new RuntimeException("Error obtaining token: " + e.getMessage(), e);
        }
    }

    // --- Nouvelle méthode V3 ---
    public String getAccessTokenV3() {
        try {
            logger.info("Requesting V3 access token from 42 API");

            String clientId = "staff-42-antananarivo";
            String clientSecret = "8I7OkQdSg3EqbDv5B0E7zzRYBW3yD0qe";
            String userLogin = "--10";
            String userPassword = "matcuT-qeczav-jemto8";

            String authString = clientId + ":" + clientSecret;
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes("UTF-8"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("username", userLogin);
            body.add("password", userPassword);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(ACCESS_TOKEN_url_V3, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("access_token")) {
                    String token = jsonNode.get("access_token").asText();
                    logger.info("V3 Access token successfully obtained");
                    return token;
                } else {
                    logger.error("V3 access token not found in response: {}", response.getBody());
                    throw new RuntimeException("V3 Access token not found in response");
                }
            } else {
                logger.error("Error obtaining V3 token: {}", response.getStatusCode());
                throw new RuntimeException("Error obtaining V3 access token: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error obtaining V3 token: {}", e.getMessage());
            throw new RuntimeException("Authentication error with 42 API V3: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error obtaining V3 token: {}", e.getMessage());
            throw new RuntimeException("Error obtaining V3 token: " + e.getMessage(), e);
        }
    }

    // --- Méthodes existantes V2 ---
    public String getIdUsers(String login, String token) {
        try {
            logger.info("Searching for user: {}", login);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> request = new HttpEntity<>(headers);

            String url = USERS_URL + "?filter[login]=" + login;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                logger.debug("User search response: {}", responseBody);

                if (responseBody.trim().isEmpty()) {
                    throw new RuntimeException("Empty API response for user: " + login);
                }

                JsonNode jsonNode = objectMapper.readTree(responseBody);
                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    String userId = jsonNode.get(0).get("id").asText();
                    logger.info("User ID found: {} for login: {}", userId, login);
                    return userId;
                } else {
                    throw new RuntimeException("User not found: " + login);
                }
            } else {
                logger.error("Error searching for user: {}", response.getStatusCode());
                throw new RuntimeException("Error retrieving user ID: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error searching for user: {}", e.getMessage());
            throw new RuntimeException("API error retrieving user ID: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error searching for user: {}", e.getMessage());
            throw new RuntimeException("API error retrieving user ID: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getUser(String userId, String token) {
        try {
            logger.info("Retrieving user data: {}", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> request = new HttpEntity<>(headers);

            String url = BASE_URL + "/v2/users/" + userId;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();

                if (responseBody.trim().isEmpty()) {
                    throw new RuntimeException("Empty API response for user ID: " + userId);
                }

                Map<String, Object> userData = objectMapper.readValue(responseBody, Map.class);
                logger.info("User data successfully retrieved for ID: {}", userId);
                return userData;
            } else {
                logger.error("Error retrieving user: {}", response.getStatusCode());
                throw new RuntimeException("Error retrieving user: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error retrieving user: {}", e.getMessage());
            throw new RuntimeException("Error retrieving user: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage());
            throw new RuntimeException("Error retrieving user: " + e.getMessage(), e);
        }
    }

    public CursusUser getCursusUser(String userId, String token) {
        try {
            logger.info("Retrieving CursusUser data for userId: {}", userId);

            Map<String, Object> userData = getUser(userId, token);
            List<Map<String, Object>> cursusUsers = (List<Map<String, Object>>) userData.get("cursus_users");
            if (cursusUsers == null || cursusUsers.isEmpty()) {
                logger.warn("No CursusUser data found for userId: {}", userId);
                return null;
            }

            Map<String, Object> mainCursus = cursusUsers.stream()
                    .filter(cu -> cu.get("cursus_id") != null && ((Number) cu.get("cursus_id")).intValue() == 21)
                    .findFirst()
                    .orElse(cursusUsers.get(0));
            CursusUser cursusUser = objectMapper.convertValue(mainCursus, CursusUser.class);

            String projectsUrl = BASE_URL + "/v2/users/" + userId + "/projects_users?filter[status]=finished";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> projectsResponse = restTemplate.exchange(projectsUrl, HttpMethod.GET, request, String.class);
            List<CursusUser.Milestone> milestones = new ArrayList<>();

            if (projectsResponse.getStatusCode().is2xxSuccessful() && projectsResponse.getBody() != null) {
                JsonNode projectsJson = objectMapper.readTree(projectsResponse.getBody());
                if (projectsJson.isArray()) {
                    int currentLevel = (int) cursusUser.getLevel();
                    String latestDate = null;

                    List<JsonNode> sortedProjects = new ArrayList<>();
                    for (JsonNode project : projectsJson) {
                        if (project.has("validated?") && project.get("validated?").asBoolean() &&
                            project.has("final_mark") && project.has("marked_at") && !project.get("marked_at").isNull()) {
                            sortedProjects.add(project);
                        }
                    }
                    sortedProjects.sort((a, b) -> a.get("marked_at").asText().compareTo(b.get("marked_at").asText()));

                    if (!sortedProjects.isEmpty()) {
                        JsonNode latestProject = sortedProjects.get(sortedProjects.size() - 1);
                        latestDate = latestProject.get("marked_at").asText();
                        milestones.add(new CursusUser.Milestone(currentLevel, latestDate));
                    } else {
                        latestDate = cursusUser.getBegin_at();
                        milestones.add(new CursusUser.Milestone(currentLevel, latestDate));
                    }
                }
            }

            cursusUser.setMilestones(milestones);
            return cursusUser;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error retrieving CursusUser: " + e.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving CursusUser: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getUserCandidature(String userId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            String url = BASE_URL + "/v2/users/" + userId + "/user_candidature";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> candidatureData = objectMapper.readValue(response.getBody(), Map.class);
                return candidatureData;
            } else {
                throw new RuntimeException("Error retrieving candidature data: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error retrieving candidature data: " + e.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving candidature data: " + e.getMessage(), e);
        }
    }
}
