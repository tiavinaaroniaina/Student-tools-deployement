package com.ecole._2.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ecole._2.models.UserAbsenceConsecutive;
import com.ecole._2.models.UserHourAverages;
import com.ecole._2.models.UserPresenceRate;
import com.ecole._2.repositories.StatsRepository;

@Service
public class StatsService {

    private final StatsRepository statsRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public List<Map<String, Object>> getYearlyMonthlyPresence() {
        List<Map<String, Object>> monthlyPresenceData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 2; i >= 0; i--) { // Loop for the last 3 months
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = today.minusMonths(i).withDayOfMonth(monthStart.lengthOfMonth());

            String startDateStr = monthStart.format(DATE_FORMATTER);
            String endDateStr = monthEnd.format(DATE_FORMATTER);

            Double globalRate = getTauxGlobal(startDateStr, endDateStr);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", YearMonth.from(monthStart).toString());
            monthData.put("presenceRate", globalRate);
            monthlyPresenceData.add(monthData);
        }
        return monthlyPresenceData;
    }

    public List<Map<String, Object>> getMonthlyPresenceByYear(Integer year) {
        List<Map<String, Object>> monthlyPresenceData = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            String startDateStr = monthStart.format(DATE_FORMATTER);
            String endDateStr = monthEnd.format(DATE_FORMATTER);

            Double globalRate = getTauxGlobal(startDateStr, endDateStr);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", YearMonth.from(monthStart).toString());
            monthData.put("presenceRate", globalRate);
            monthlyPresenceData.add(monthData);
        }
        return monthlyPresenceData;
    }

    public List<UserHourAverages> getAllMoyenneHeures() {
        return statsRepository.getAllMoyenneHeures();
    }

    public List<UserPresenceRate> getUserPresenceRates(String startDate, String endDate, String userId) {
        validateDates(startDate, endDate);
        if (StringUtils.hasText(userId)) {
            return statsRepository.getUserPresenceRateByUserId(startDate, endDate, userId);
        } else {
            return statsRepository.getUserPresenceRate(startDate, endDate);
        }
    }

    public Double getTauxGlobal(String startDate, String endDate) {
        validateDates(startDate, endDate);
        Double result = statsRepository.getGlobalPresenceRate(startDate, endDate);
        if (result == null) {
            throw new IllegalStateException("Global presence rate could not be retrieved for the given date range.");
        }
        return result;
    }

    // ================== CORRECTION ABSENCES ==================
  public List<UserAbsenceConsecutive> getUtilisateursAbsentsConsecutifs(
        Integer granulariteMin, Integer granulariteMax, String dateDebut) {
    if (dateDebut != null) {
        validateSingleDate(dateDebut);
    }
    return statsRepository.getUtilisateursAbsentsConsecutifs(granulariteMin, granulariteMax, dateDebut);
}


    // ========== VALIDATION DES DATES ==========
    private void validateDates(String startDate, String endDate) {
        if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
            throw new IllegalArgumentException("Start date and end date must not be empty.");
        }
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must not be after end date.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd", e);
        }
    }

    private void validateSingleDate(String date) {
        if (!StringUtils.hasText(date)) {
            throw new IllegalArgumentException("Date must not be empty.");
        }
        try {
            LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd", e);
        }
    }
}