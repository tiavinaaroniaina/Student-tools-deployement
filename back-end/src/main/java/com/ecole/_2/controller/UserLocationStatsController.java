package com.ecole._2.controller;

import com.ecole._2.models.CursusUser;
import com.ecole._2.models.Freeze;
import com.ecole._2.models.User;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CampusUserService;
import com.ecole._2.services.FreezeUserService;
import com.ecole._2.services.UserCursusService;
import com.ecole._2.services.UserLocationStatsService;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserLocationStatsController {

    private static final Logger logger = LoggerFactory.getLogger(UserLocationStatsController.class);

    @Autowired
    private UserLocationStatsService userLocationStatsService;

    @Autowired
    private UserCursusService userCursusService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private CampusUserService campusUserService;

    @Autowired
    private FreezeUserService freezeUserService;

    private static final String DEFAULT_USER_ID = "211018";

    @GetMapping("/freeze")
    public Map<String, Object> getFreezeData(
            @RequestParam(value = "login", required = false) String login,
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("userResponse");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String kind = (String) session.getAttribute("kind");
        String userId = determineUserId(login, kind, session);

        String tokenAdmin = apiService.getAccessToken();
        
        String tokenAdminV3 = apiService.getAccessTokenV3();
        CursusUser userCursus = userCursusService.getUserCursus(userId, tokenAdmin).filterByGrade("Cadet");
        UserLocationStat locationStats = userLocationStatsService.getUserLocationStats(userId, tokenAdmin, null, null);
        dateDebut = userCursus.getBegin_at();
        long nbDays = locationStats.getNbDays(dateDebut, null);
        long nbOpenDays = locationStats.getNbOpenDays(dateDebut, null);
        double totalHours = locationStats.getTotalHours(dateDebut, null);
        Freeze freeze = new Freeze();
        freeze.setA(nbDays);
        freeze.setB(nbOpenDays);
        freeze.setC(totalHours);
        freeze.setD(userCursus.getMilestone());

        response.put("nbDays", nbDays);
        response.put("nbOpenDays", nbOpenDays);
        response.put("totalHours", totalHours);
        response.put("isAdmin", isAdminUser(user));
        response.put("freeze", freeze.calculFreeze());
        response.put("userCursus", userCursus);
        response.put("locationStats", locationStats);
        response.put("used_freeze_days", freezeUserService.getUsedFreezeDays(userId, tokenAdminV3));

        return response;
    }

    private String determineUserId(String login, String kind, HttpSession session) {
        if (kind == null || !"admin".equals(kind)) {
            User sessionUser = (User) session.getAttribute("userResponse");
            return (sessionUser != null && sessionUser.getId() != null) ? sessionUser.getId() : DEFAULT_USER_ID;
        }

        if ("admin".equals(kind)) {
            if (login != null && !login.trim().isEmpty()) {
                try {
                    return apiService.getIdUsers(login.trim(), apiService.getAccessToken());
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + login);
                }
            } else {
                return "203988";
            }
        }
        return null;
    }

    private boolean isAdminUser(User user) {
        String[] adminLogins = {"admin", "root", "supervisor"};
        if (user.getLogin() != null) {
            for (String adminLogin : adminLogins) {
                if (user.getLogin().toLowerCase().contains(adminLogin)) return true;
            }
        }
        return false;
    }
}