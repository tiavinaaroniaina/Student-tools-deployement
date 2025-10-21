package com.ecole._2.services;

import com.ecole._2.models.UserFreeze;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class FreezeUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(FreezeUserService.class);
    private static final String BASE_URL = "https://freeze.42.fr/api/v2/users/";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<UserFreeze> getUserFreezes(String userId, String accessToken) {
        try {
            logger.info("=== START FETCHING USER FREEZES ===");
            logger.info("User ID: {}", userId);
            logger.info("Token present: {}", accessToken != null ? "YES" : "NO");
            
            RestTemplate restTemplate = new RestTemplate();
            String url = BASE_URL + userId + "/freezes";
            logger.info("Request URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.info("Sending request to Freeze API...");
            ResponseEntity<UserFreezeResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserFreezeResponse.class
            );
            
            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response headers: {}", response.getHeaders());
            
            UserFreezeResponse freezeResponse = response.getBody();
            if (freezeResponse == null || freezeResponse.getItems() == null) {
                logger.warn("WARNING: Response body or items is null");
                return Arrays.asList();
            }
            
            List<UserFreeze> freezes = freezeResponse.getItems();
            logger.info("Number of freezes fetched: {}", freezes.size());
            
            int maxToLog = Math.min(5, freezes.size());
            for (int i = 0; i < maxToLog; i++) {
                UserFreeze freeze = freezes.get(i);
                logger.info("Freeze {}: {}", i + 1, logUserFreeze(freeze));
            }
            
            if (freezes.size() > 5) {
                logger.info("... and {} more freezes", freezes.size() - 5);
            }
            
            logger.info("=== END FETCHING USER FREEZES ===");
            logger.info("Final result - User ID: {}, Number of freezes: {}", userId, freezes.size());
            
            return freezes;
            
        } catch (HttpClientErrorException e) {
            logger.error("HTTP client error ({}): {}", e.getStatusCode(), e.getMessage());
            logger.error("Error response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Freeze API authentication error: " + e.getStatusCode());
            
        } catch (HttpServerErrorException e) {
            logger.error("HTTP server error ({}): {}", e.getStatusCode(), e.getMessage());
            logger.error("Error response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Freeze API server error: " + e.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Unexpected error while fetching user freezes", e);
            throw new RuntimeException("Error fetching user freezes: " + e.getMessage(), e);
        }
    }
    
    private String logUserFreeze(UserFreeze freeze) {
        if (freeze == null) {
            return "null";
        }
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("UserFreeze{");
            
            if (freeze.getId() != null) sb.append("id=").append(freeze.getId()).append(", ");
            if (freeze.getReason() != null) sb.append("reason=").append(freeze.getReason()).append(", ");
            if (freeze.getCategory() != null) sb.append("category=").append(freeze.getCategory()).append(", ");
            if (freeze.getStatus() != null) sb.append("status=").append(freeze.getStatus()).append(", ");
            sb.append("isFreeFreeze=").append(freeze.isFreeFreeze()).append(", ");
            if (freeze.getBeginDate() != null) sb.append("beginDate=").append(freeze.getBeginDate()).append(", ");
            if (freeze.getExpectedEndDate() != null) sb.append("expectedEndDate=").append(freeze.getExpectedEndDate());
            
            sb.append("}");
            return sb.toString();
            
        } catch (Exception e) {
            logger.warn("Error while logging UserFreeze: {}", e.getMessage());
            return "UserFreeze{serialization error}";
        }
    }
    
    // Helper class to deserialize the API response
    private static class UserFreezeResponse {
        private List<UserFreeze> items;
        
        public List<UserFreeze> getItems() {
            return items;
        }
        
        public void setItems(List<UserFreeze> items) {
            this.items = items;
        }
    }

    public int getUsedFreezeDays(String userId, String accessToken) {
        try {
            logger.info("=== START FETCHING USED FREEZE DAYS ===");
            logger.info("User ID: {}", userId);

            RestTemplate restTemplate = new RestTemplate();
            String url = BASE_URL + userId;
            logger.info("Request URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info("Sending request to Freeze API for user info...");
            ResponseEntity<UserFreezeInfo> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserFreezeInfo.class
            );

            logger.info("Response status: {}", response.getStatusCode());

            UserFreezeInfo userInfo = response.getBody();
            if (userInfo == null) {
                logger.warn("WARNING: Response body is null");
                return 0;
            }

            logger.info("Used freeze days fetched: {}", userInfo.getUsedFreezeDays());
            logger.info("=== END FETCHING USED FREEZE DAYS ===");

            return userInfo.getUsedFreezeDays();

        } catch (HttpClientErrorException e) {
            logger.error("HTTP client error ({}): {}", e.getStatusCode(), e.getMessage());
            logger.error("Error response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Freeze API authentication error: " + e.getStatusCode());

        } catch (HttpServerErrorException e) {
            logger.error("HTTP server error ({}): {}", e.getStatusCode(), e.getMessage());
            logger.error("Error response body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Freeze API server error: " + e.getStatusCode());

        } catch (Exception e) {
            logger.error("Unexpected error while fetching used freeze days", e);
            throw new RuntimeException("Error fetching used freeze days: " + e.getMessage(), e);
        }
    }

    private static class UserFreezeInfo {
        private int used_freeze_days;

        public int getUsedFreezeDays() {
            return used_freeze_days;
        }

        public void setUsed_freeze_days(int used_freeze_days) {
            this.used_freeze_days = used_freeze_days;
        }
    }
}