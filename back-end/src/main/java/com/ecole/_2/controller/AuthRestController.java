package com.ecole._2.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.services.OAuth42Service;
import com.ecole._2.services.User42Service;
import com.ecole._2.utils.CheckingUtils;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthRestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    @Autowired
    private OAuth42Service oauth42Service;

    @Autowired
    private User42Service user42Service;

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session
    ) {
        Map<String, String> response = new HashMap<>();
        logger.info("Starting authentication process via API with code: {}", code);

        TokenResponse tokenResponse = oauth42Service.getAccessToken(code);
        if (tokenResponse == null) {
            logger.error("Failed to retrieve access token");
            response.put("error", "token_failed");
            response.put("message", "Échec de la récupération du jeton d'accès.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        User userResponse = user42Service.getUserInfo(tokenResponse.getAccessToken());
        if (userResponse == null) {
            logger.error("Failed to retrieve user info");
            response.put("error", "user_failed");
            response.put("message", "Échec de la récupération des informations utilisateur.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        session.setAttribute("tokenResponse", tokenResponse);
        session.setAttribute("userResponse", userResponse);
        session.setAttribute("code", code);
        session.setAttribute("state", state);

        logger.info("Authenticated user: {} (ID: {})", userResponse.getLogin(), userResponse.getId());

        String kind = CheckingUtils.determineUserKind(userResponse);
        session.setAttribute("kind", kind);

        if ("student".equals(kind)) {
            boolean hasCursus21 = userResponse.getCursus_users() != null &&
                                 userResponse.getCursus_users().stream()
                                             .anyMatch(cu -> cu.getCursus_id() == 21);
            if (hasCursus21) {
                logger.info("Student {} (ID: {}) with cursus_id 21 logged in successfully.", userResponse.getLogin(), userResponse.getId());
                response.put("status", "success");
                response.put("message", "Connexion réussie.");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                logger.warn("Student {} (ID: {}) does not have cursus_id 21. Access denied.", userResponse.getLogin(), userResponse.getId());
                response.put("error", "cursus_id_mismatch");
                response.put("message", "Accès refusé : vous n'avez pas le cursus_id requis (21).");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } else if ("admin".equals(kind)) {
            logger.info("Admin {} (ID: {}) logged in successfully.", userResponse.getLogin(), userResponse.getId());
            response.put("status", "success");
            response.put("message", "Connexion réussie.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            logger.warn("User {} (ID: {}) has an unknown kind: {}. Access denied.", userResponse.getLogin(), userResponse.getId(), kind);
            response.put("error", "unauthorized_user_type");
            response.put("message", "Accès refusé : type d'utilisateur non autorisé.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/api/user")
    public User getUser(HttpSession session) {
        User userResponse = (User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        userResponse.setKind((String) session.getAttribute("kind"));
        return userResponse;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "Logged out successfully";
    }
}
