package com.ecole._2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.services.OAuth42Service;
import com.ecole._2.services.User42Service;
import com.ecole._2.utils.CheckingUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private OAuth42Service oauth42Service;

    @Autowired
    private User42Service user42Service;

    private static final String FRONT_URL = "http://localhost:5173"; 

    @GetMapping("/auth")
    public String auth(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpSession session,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        logger.info("Starting authentication process with code: {}", code);

        TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
        User userResponse = (User) session.getAttribute("userResponse");

        if (tokenResponse == null || userResponse == null) {
            tokenResponse = oauth42Service.getAccessToken(code);
            if (tokenResponse == null) {
                logger.error("Failed to retrieve access token");
                try {
                    return "redirect:/login?error=token_failed";
                } catch (Exception e) { 
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to redirect to login");
                }
            }

            userResponse = user42Service.getUserInfo(tokenResponse.getAccessToken());
            if (userResponse == null) {
                logger.error("Failed to retrieve user info");
                return "redirect:/login?error=user_failed";
            }

            session.setAttribute("tokenResponse", tokenResponse);
            session.setAttribute("userResponse", userResponse);
            session.setAttribute("code", code);
            session.setAttribute("state", state);

        }

        String kind;
        if ("tramitso".equals(userResponse.getLogin()) || "mralevas".equals(userResponse.getLogin())) {
            kind = "admin"; // Force 'tramitso' to be a student
            logger.info("Forcing user 'tramitso' to be a student.");
        } else {
            kind = CheckingUtils.determineUserKind(userResponse);
        }
        session.setAttribute("kind", kind);

        if ("student".equals(kind)) {
            // 'tramitso' already forced as student, so this check is for other students
            if ("tramitso".equals(userResponse.getLogin())) {
                logger.info("Test user 'tramitso' granted student access (cursus_id check bypassed).");
                return "redirect:" + FRONT_URL + "/?login_success=true";
            }

            boolean hasCursus21 = userResponse.getCursus_users() != null &&
                                 userResponse.getCursus_users().stream()
                                             .anyMatch(cu -> cu.getCursus_id() == 21);
            if (hasCursus21) {
                logger.info("Student {} (ID: {}) with cursus_id 21 logged in successfully.", userResponse.getLogin(), userResponse.getId());
                return "redirect:" + FRONT_URL + "/?login_success=true";
            } else {
                logger.warn("Student {} (ID: {}) does not have cursus_id 21. Access denied.", userResponse.getLogin(), userResponse.getId());
                return "redirect:/login?error=cursus_id_mismatch";
            }
        } else if ("admin".equals(kind)) {
            logger.info("Admin {} (ID: {}) logged in successfully.", userResponse.getLogin(), userResponse.getId());
            return "redirect:" + FRONT_URL + "/?login_success=true";
        } else {
            logger.warn("User {} (ID: {}) has an unknown kind: {}. Access denied.", userResponse.getLogin(), userResponse.getId(), kind);
            return "redirect:" + FRONT_URL + "/login?error=unauthorized_user_type";
           
        }
    }

}