package com.ecole._2.controller;

import com.ecole._2.models.LocationStat;
import com.ecole._2.models.TokenResponse;
import com.ecole._2.models.User;
import com.ecole._2.models.UserFreeze;
import com.ecole._2.models.UserLocationStat;
import com.ecole._2.models.Milestone;
import com.ecole._2.models.CursusUser;
import com.ecole._2.services.ApiService;
import com.ecole._2.services.CampusUsersService;
import com.ecole._2.services.FreezeUserService;
import com.ecole._2.services.UserCursusService;
import com.ecole._2.services.UserLocationStatsService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CheckingController {

    private static final Logger logger = LoggerFactory.getLogger(CheckingController.class);

    @Autowired
    private CampusUsersService campusUsersService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private UserLocationStatsService userLocationStatsService;

    @Autowired
    private FreezeUserService freezeUserService;

    @Autowired
    private UserCursusService userCursusService;

    private static final String CAMPUS_ID = "65";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/check")
    public String checkPage(Model model, HttpSession session) {
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            String today = LocalDate.now().format(DATE_FORMATTER);
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);

        } catch (Exception e) {
            logger.error("Error loading checking page", e);
            model.addAttribute("error", "Erreur lors du chargement de la page: " + e.getMessage());
            return "error-page";
        }
        return "checking-admin";
    }

    @PostMapping("/check")
    public String checkStudents(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "pool", required = false) String pool,
            @RequestParam(value = "year", required = false) String year,
            Model model,
            HttpSession session) {

        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

            if (!isValidDateRange(startDate, endDate)) {
                model.addAttribute("error", "Dates invalides. La date de début doit être antérieure ou égale à la date de fin.");
                model.addAttribute("userResponse", userResponse);
                model.addAttribute("kind", session.getAttribute("kind"));
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("pool", pool);
                model.addAttribute("year", year);
                return "checking-admin";
            }

            List<User> userList = (List<User>) session.getAttribute("userList");
            if (userList == null) {
                logger.warn("User list is null in session");
                userList = new ArrayList<>();
            }
            if (pool != null && !pool.isEmpty() && year != null && !year.isEmpty()) {
                userList = User.filterUsersByPool(userList, pool, year);
            }

            List<UserLocationStat> userLocationStats = new ArrayList<>();
            String token = apiService.getAccessToken();
            for (User u : userList) {
                try {
                    UserLocationStat stat = userLocationStatsService.getUserLocationStats(u.getId(), token, startDate, endDate);
                    Map<String, Object> userData = apiService.getUser(u.getId(), token);
                    String userName = (String) userData.getOrDefault("login", u.getId());
                    stat.setUserName(userName);
                    userLocationStats.add(stat);
                } catch (Exception e) {
                    logger.warn("Could not fetch stats or user data for user {}: {}", u.getId(), e.getMessage());
                    UserLocationStat stat = new UserLocationStat(u.getId(), null);
                    stat.setUserName(u.getId());
                    userLocationStats.add(stat);
                }
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("pool", pool);
            model.addAttribute("year", year);
            model.addAttribute("dayCount", userLocationStats.size());
            model.addAttribute("userLocationStats", userLocationStats);
            model.addAttribute("searchPerformed", true);

        } catch (Exception e) {
            logger.error("Error during student checking process", e);
            model.addAttribute("error", "Erreur lors de la vérification des présences: " + e.getMessage());
        }

        return "checking-admin";
    }

    @GetMapping("/checkUser")
    public String checkUserPage(Model model, HttpSession session) {
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("kind", session.getAttribute("kind"));
            String today = LocalDate.now().format(DATE_FORMATTER);
            model.addAttribute("startDate", today);
            model.addAttribute("endDate", today);
            model.addAttribute("login", "");

        } catch (Exception e) {
            logger.error("Error loading checking page for user", e);
            model.addAttribute("error", "Erreur lors du chargement de la page: " + e.getMessage());
            return "error-page";
        }
        return "checking-user";
    }

    @PostMapping("/checkUser")
    public String checkSingleUser(
            @RequestParam("login") String login,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model,
            HttpSession session) {

        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            User userResponse = (User) session.getAttribute("userResponse");
            if (tokenResponse == null || userResponse == null) {
                logger.warn("No token or user in session, redirecting to login");
                return "redirect:/login";
            }

            if (!isValidDateRange(startDate, endDate)) {
                model.addAttribute("error", "Dates invalides. La date de début doit être antérieure ou égale à la date de fin.");
                model.addAttribute("userResponse", userResponse);
                model.addAttribute("kind", session.getAttribute("kind"));
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("login", "");
                return "checking-user";
            }

            UserLocationStat userStat;
            String token = apiService.getAccessToken();
            String userId = apiService.getIdUsers(login, token);
            try {
                userStat = userLocationStatsService.getUserLocationStats(userId, token, startDate, endDate);
                userStat.setUserName(login);
                List<LocationStat> locationStats = userStat.filterStatsBetween(startDate, endDate);
                userStat.setStats(locationStats);
            } catch (Exception e) {
                logger.warn("Could not fetch stats or user data for user {}: {}", userId, e.getMessage());
                userStat = new UserLocationStat(userId, null);
                userStat.setUserName(userId);
            }

            model.addAttribute("userResponse", userResponse);
            model.addAttribute("userLocationStats", List.of(userStat));
            model.addAttribute("dayCount", userStat.getNbDays(startDate, endDate));
            model.addAttribute("hourCount", userStat.getTotalHours(startDate, endDate));
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("login", login);

        } catch (Exception e) {
            logger.error("Error checking single user", e);
            model.addAttribute("error", "Erreur lors de la vérification: " + e.getMessage());
        }

        return "checking-user";
    }

    @GetMapping("/calendar")
    @ResponseBody
    public Map<String, Object> getCalendar(
            @RequestParam("year") int year,
            @RequestParam(value = "month", required = false, defaultValue = "0") int month,
            @RequestParam(value = "login", required = false) String login,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            TokenResponse tokenResponse = (TokenResponse) session.getAttribute("tokenResponse");
            if (tokenResponse == null) {
                logger.warn("No token in session for /calendar request");
                response.put("error", "Authentication required");
                return response;
            }

            String token = apiService.getAccessToken();
            String tokenV3 = apiService.getAccessTokenV3();
            logger.info("Fetching calendar for year: {}, month: {}, login: {}", year, month, login);

            String userId = login != null && !login.isEmpty() ? apiService.getIdUsers(login, token) : null;
            if (userId == null && login != null && !login.isEmpty()) {
                logger.warn("User not found for login: {}", login);
                response.put("error", "User not found for login: " + login);
                return response;
            }

            // Vérifier si l'utilisateur est un pisciner
            if (userId != null && userCursusService.isPiscinerOnly(userId, token)) {
                logger.info("Login {} is a pisciner. Fetching presence data only.", login);
                LocalDate startDatePisciner = (month > 0) ? LocalDate.of(year, month, 1) : LocalDate.of(year, 1, 1);
                LocalDate endDatePisciner = (month > 0) ? startDatePisciner.withDayOfMonth(startDatePisciner.lengthOfMonth()) : LocalDate.of(year, 12, 31);

                UserLocationStat userStat = userLocationStatsService.getUserLocationStats(
                        userId, token, startDatePisciner.format(DATE_FORMATTER), endDatePisciner.format(DATE_FORMATTER));
                
                List<Map<String, String>> presenceStats = userStat.getStats().stream()
                        .filter(stat -> stat.getDuration() != null && !stat.getDuration().isZero())
                        .map(stat -> {
                            Map<String, String> statData = new HashMap<>();
                            statData.put("date", stat.getDate().format(DATE_FORMATTER));
                            Duration duration = stat.getDuration();
                            long hours = duration.toHours();
                            long minutes = duration.toMinutesPart();
                            String formattedDuration = hours + "h" + (minutes > 0 ? " " + minutes + "m" : "");
                            statData.put("duration", formattedDuration);
                            return statData;
                        })
                        .collect(Collectors.toList());

                List<String> presenceDays = presenceStats.stream()
                        .map(stat -> stat.get("date"))
                        .collect(Collectors.toList());

                response.put("presence", presenceDays);
                response.put("presenceStats", presenceStats);
                // For pisciner, other fields are empty
                response.put("milestones", new ArrayList<>());
                response.put("blackholed_at", null);
                response.put("milestoneDates", new ArrayList<>());
                response.put("freezeAndBonusEvents", new ArrayList<>());

                logger.info("Returning calendar for pisciner {}", login);
                return response;
            }

            List<Map<String, Object>> milestones = new ArrayList<>();
            List<Map<String, Object>> milestoneDates = new ArrayList<>();
            String formattedBlackholedAt = null;
            if (userId != null) {
                CursusUser cursusUser = apiService.getCursusUser(userId, token);
                List<Milestone> userMilestones = userCursusService.getUserMilestones(userId, tokenV3);
                
                // Fallback: Directly fetch milestones from API if UserCursusService fails
                if (userMilestones.isEmpty() || userMilestones.stream().allMatch(m -> m.getValidatedAt() == null)) {
                    logger.warn("UserCursusService returned empty or invalid milestones for userId: {}, falling back to direct API call", userId);
                    RestTemplate restTemplate = new RestTemplate();
                    String apiUrl = "https://pace-system.42.fr/api/v1/users/" + userId + "/milestones";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(tokenV3);
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    List<Map<String, Object>> rawMilestones = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, List.class).getBody();
                    logger.info("Raw milestones from direct API: {}", rawMilestones);
                    
                    userMilestones = rawMilestones.stream().map(m -> {
                        Milestone milestone = new Milestone();
                        milestone.setLevel(((Number) m.get("level")).intValue());
                        milestone.setDeadline((String) m.get("deadline"));
                        milestone.setValidatedAt((String) m.get("validated_at"));
                        return milestone;
                    }).collect(Collectors.toList());
                }

                if (userMilestones.isEmpty()) {
                    logger.warn("No milestones found for userId: {}", userId);
                    response.put("milestones", milestones);
                    response.put("blackholed_at", null);
                    response.put("milestoneDates", milestoneDates);
                } else {
                    logger.info("Processed milestones from UserCursusService or API: {}", userMilestones);
                    milestones = userMilestones.stream()
                            .map(m -> {
                                Map<String, Object> milestoneData = new HashMap<>();
                                milestoneData.put("level", m.getLevel());
                                milestoneData.put("deadline", m.getDeadline());
                                milestoneData.put("validated_at", m.getValidatedAt());
                                String date = m.getValidatedAt() != null ? m.getValidatedAt() : m.getDeadline();
                                milestoneData.put("date", date);
                                logger.info("Processed milestone: level={}, deadline={}, validated_at={}, date={}",
                                        m.getLevel(), m.getDeadline(), m.getValidatedAt(), date);
                                return milestoneData;
                            })
                            .collect(Collectors.toList());
                    response.put("milestones", milestones);

                    formattedBlackholedAt = cursusUser.getBlackholed_at() != null
                            ? formatMilestoneDate(cursusUser.getBlackholed_at())
                            : null;
                    response.put("blackholed_at", formattedBlackholedAt);

                    milestoneDates = userMilestones.stream()
                            .filter(m -> m.getValidatedAt() != null || m.getDeadline() != null)
                            .map(m -> {
                                Map<String, Object> milestoneInfo = new HashMap<>();
                                milestoneInfo.put("level", m.getLevel());
                                String date = m.getValidatedAt() != null ? m.getValidatedAt() : m.getDeadline();
                                milestoneInfo.put("date", date);
                                logger.info("Processed milestoneDate: level={}, date={}", m.getLevel(), date);
                                return milestoneInfo;
                            })
                            .collect(Collectors.toList());
                    response.put("milestoneDates", milestoneDates);
                }
            } else {
                response.put("milestones", milestones);
                response.put("blackholed_at", null);
                response.put("milestoneDates", milestoneDates);
            }

            LocalDate startDate;
            LocalDate endDate;

            if (month > 0) {
                startDate = LocalDate.of(year, month, 1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            } else {
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
            }

            List<LocationStat> stats = new ArrayList<>();
            List<Map<String, String>> presenceStats = new ArrayList<>();
            List<Map<String, Object>> freezeAndBonusEvents = new ArrayList<>();
            if (userId != null) {
                UserLocationStat userStat = userLocationStatsService.getUserLocationStats(
                        userId, token, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
                stats = userStat.filterStatsBetween(startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
                presenceStats = stats.stream()
                        .filter(stat -> stat.getDuration() != null && !stat.getDuration().isZero())
                        .map(stat -> {
                            Map<String, String> statData = new HashMap<>();
                            statData.put("date", stat.getDate().format(DATE_FORMATTER));
                            Duration duration = stat.getDuration();
                            long hours = duration.toHours();
                            long minutes = duration.toMinutesPart();
                            String formattedDuration = hours + "h" + (minutes > 0 ? " " + minutes + "m" : "");
                            statData.put("duration", formattedDuration);
                            return statData;
                        })
                        .collect(Collectors.toList());

                List<UserFreeze> freezes = freezeUserService.getUserFreezes(userId, tokenV3);
                freezeAndBonusEvents = freezes.stream()
                        .filter(freeze -> freeze.getBeginDate() != null && freeze.getExpectedEndDate() != null)
                        .filter(freeze -> "regular".equals(freeze.getCategory()) || "bonus".equals(freeze.getCategory()))
                        .map(freeze -> {
                            Map<String, Object> event = new HashMap<>();
                            String eventType = "regular".equals(freeze.getCategory()) ? "freeze" : "bonus";
                            event.put("id", freeze.getId());
                            event.put("title", "freeze".equals(eventType) ? "Freeze Period" : "Bonus Period");
                            event.put("start", freeze.getBeginDate());
                            event.put("end", freeze.getExpectedEndDate());
                            event.put("type", eventType);
                            event.put("status", freeze.getStatus());
                            event.put("reason", freeze.getReason());
                            event.put("staffDescription", freeze.getStaffDescription());
                            event.put("color", "freeze".equals(eventType) ? "#ff4d4d" : "#4CAF50");
                            return event;
                        })
                        .filter(event -> {
                            try {
                                LocalDate eventStart = LocalDate.parse((String) event.get("start"), DATE_FORMATTER);
                                return !eventStart.isBefore(startDate) && !eventStart.isAfter(endDate);
                            } catch (DateTimeParseException e) {
                                logger.warn("Invalid date format in freeze event: {}", event.get("start"));
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }

            List<String> presenceDays = presenceStats.stream()
                    .map(stat -> stat.get("date"))
                    .collect(Collectors.toList());

            response.put("presence", presenceDays);
            response.put("presenceStats", presenceStats);
            response.put("freezeAndBonusEvents", freezeAndBonusEvents);

            logger.info("Returning {} presence days, {} presence stats, {} milestone dates, and {} freeze/bonus events for user {}",
                    presenceDays.size(), presenceStats.size(), milestoneDates.size(), freezeAndBonusEvents.size(), login);
            return response;

        } catch (Exception e) {
            logger.error("Error fetching calendar data for user {}: {}", login, e.getMessage(), e);
            response.put("error", "Erreur lors de la récupération des données du calendrier: " + e.getMessage());
            return response;
        }
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            return !end.isBefore(start);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format: startDate={}, endDate={}", startDate, endDate);
            return false;
        }
    }

    private String formatMilestoneDate(String dateStr) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(dateStr);
            return zdt.toLocalDate().format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format for milestone: {}", dateStr);
            return null;
        }
    }
}