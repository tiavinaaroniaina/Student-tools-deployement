package com.ecole._2.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecole._2.models.UserAbsenceConsecutive;
import com.ecole._2.models.UserHourAverages;
import com.ecole._2.models.UserPresenceRate;
import com.ecole._2.services.StatsService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/stats")
public class StatsController {
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);
    private final StatsService statsService;

    @Autowired
    private JavaMailSender mailSender;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/monthly-presence")
    public List<Map<String, Object>> getMonthlyPresence(
            @RequestParam(value = "year", required = false) Integer year,
            HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        var kind = (String) session.getAttribute("kind");

        if (!"admin".equalsIgnoreCase(kind)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator rights required.");
        }

        if (year != null) {
            return statsService.getMonthlyPresenceByYear(year);
        } else {
            return statsService.getYearlyMonthlyPresence();
        }
    }

    @GetMapping("/moyennes-heures")
    public List<UserHourAverages> getAllUserHourAverages(HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        var kind = (String) session.getAttribute("kind");

        if (!"admin".equalsIgnoreCase(kind)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator rights required.");
        }

        return statsService.getAllMoyenneHeures();
    }

    @GetMapping("/users")
    public List<UserPresenceRate> getUserPresenceRates(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "userId", required = false) String userId,
            HttpSession session) {
        
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        var kind = (String) session.getAttribute("kind");

        if (!"admin".equalsIgnoreCase(kind)) {
            userId = userResponse.getId();
        }

        return statsService.getUserPresenceRates(startDate, endDate, userId);
    }

    @GetMapping("/global")
    public Double getTauxGlobal(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            HttpSession session) {  
        
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return statsService.getTauxGlobal(startDate, endDate);
    }

    @GetMapping("/absences")
    public List<UserAbsenceConsecutive> getAbsentUsers(
            @RequestParam(value = "granulariteMin", required = false) Integer granulariteMin,
            @RequestParam(value = "granulariteMax", required = false) Integer granulariteMax,
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            HttpSession session) {

        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return statsService.getUtilisateursAbsentsConsecutifs(granulariteMin, granulariteMax, dateDebut);
    }

    @PostMapping("/notify")
    public void notifyAbsentUser(@RequestBody NotificationRequest request, HttpSession session) {
        var userResponse = (com.ecole._2.models.User) session.getAttribute("userResponse");
        if (userResponse == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        if (!"admin".equalsIgnoreCase((String) session.getAttribute("kind"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: administrator rights required.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tiavinaaroniaina@gmail.com");
            message.setTo("mioraralevason@gmail.com");
            message.setSubject("Absence Notification - 42 Antananarivo");
            message.setText(
                String.format(
                    "Dear %s,\n\nYou have been absent for several consecutive days. Please contact the administration to address your attendance.\n\nBest regards,\n42 Antananarivo Team",
                    request.getLogin()
                )
            );
            mailSender.send(message);
            logger.info("Notification email sent to {} for user {}", request.getEmail(), request.getLogin());
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", request.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send notification email.");
        }
    }

    public static class NotificationRequest {
        private String login;
        private String email;

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
