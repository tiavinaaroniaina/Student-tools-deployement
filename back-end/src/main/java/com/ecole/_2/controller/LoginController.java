package com.ecole._2.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

    @Value("${app.client_id}")
    private String CLIENT_ID;

    @Value("${app.client_secret}")
    private String CLIENT_SECRET;

    @Value("${app.redirect_uri}")
    private String REDIRECT_URI;

    private final String RESPONSE_TYPE = "code";
    private final String SCOPE = "public";
    private final String STATE = "123456";

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        String error = request.getParameter("error");
        if (error != null) {
            String errorMessage;
            switch (error) {
                case "token_failed":
                    errorMessage = "Échec de la récupération du jeton d'accès.";
                    break;
                case "cursus_id_mismatch":
                    errorMessage = "Accès refusé : vous n'avez pas le cursus_id requis (21).";
                    break;
                case "unauthorized_user_type":
                    errorMessage = "Accès refusé : type d'utilisateur non autorisé.";
                    break;
                default:
                    errorMessage = "Une erreur inconnue est survenue lors de l'authentification.";
                    break;
            }
            model.addAttribute("errorMessage", errorMessage);
            return "login"; // Return the name of the login view (e.g., login.html)
        }

        String redirectUriEncoded = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.toString());
        String authUrl = "https://api.intra.42.fr/oauth/authorize" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + redirectUriEncoded +
                "&response_type=" + RESPONSE_TYPE +
                "&scope=" + SCOPE +
                "&state=" + STATE;

        response.sendRedirect(authUrl);
        return null; // Redirect handled by sendRedirect
    }

    @GetMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        return login(request, response, model);
    }
}
