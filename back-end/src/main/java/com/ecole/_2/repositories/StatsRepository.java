package com.ecole._2.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ecole._2.models.UserAbsenceConsecutive;
import com.ecole._2.models.UserHourAverages;
import com.ecole._2.models.UserPresenceRate;

@Repository
public class StatsRepository {

    private static final Logger logger = LoggerFactory.getLogger(StatsRepository.class);
    private final DatabaseConnection dbConnection;

    public StatsRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public List<UserHourAverages> getAllMoyenneHeures() {
        List<UserHourAverages> results = new ArrayList<>();
        String sql = "SELECT * FROM moyenne_heures_utilisateurs()";
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(new UserHourAverages(
                    rs.getString("login"),
                    rs.getBigDecimal("moyenne_heure_depuis_debut"),
                    rs.getBigDecimal("moyenne_heure_depuis_3_mois"),
                    rs.getBigDecimal("moyenne_heure_depuis_1_mois"),
                    rs.getBigDecimal("moyenne_heure_depuis_1_semaine")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error executing moyenne_heures_utilisateurs: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve user hour averages", e);
        }
        return results;
    }

    public List<UserPresenceRate> getUserPresenceRate(String startDate, String endDate) {
        String sql = "SELECT user_id AS userId, login AS login, displayname AS displayname, " +
                     "first_name AS firstName, last_name AS lastName, jours_present AS joursPresent, " +
                     "jours_totaux AS joursTotaux, taux_presence AS tauxPresence " +
                     "FROM taux_presence_par_utilisateur(?, ?)";
        return executeQueryForUsers(sql, startDate, endDate, null);
    }

    public List<UserPresenceRate> getUserPresenceRateByUserId(String startDate, String endDate, String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be empty");
        }
        String sql = "SELECT user_id AS userId, login AS login, displayname AS displayname, " +
                     "first_name AS firstName, last_name AS lastName, jours_present AS joursPresent, " +
                     "jours_totaux AS joursTotaux, taux_presence AS tauxPresence " +
                     "FROM taux_presence_par_utilisateur(?, ?) " +
                     "WHERE user_id = ?";
        return executeQueryForUsers(sql, startDate, endDate, userId);
    }

  public List<UserAbsenceConsecutive> getUtilisateursAbsentsConsecutifs(
        Integer granulariteMin, Integer granulariteMax, String dateDebut) {

    String sql = "SELECT user_id, login, displayname, first_name, last_name, " +
                 "jours_absents_consecutifs, total_absences " +
                 "FROM utilisateurs_absents_consecutifs_2(?, ?, ?)";
    List<UserAbsenceConsecutive> results = new ArrayList<>();

    try (Connection connection = dbConnection.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {

        if (granulariteMin != null) stmt.setInt(1, granulariteMin);
        else stmt.setNull(1, java.sql.Types.INTEGER);

        if (granulariteMax != null) stmt.setInt(2, granulariteMax);
        else stmt.setNull(2, java.sql.Types.INTEGER);

        if (dateDebut != null) stmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(dateDebut)));
        else stmt.setNull(3, java.sql.Types.DATE);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UserAbsenceConsecutive u = new UserAbsenceConsecutive();
                u.setUserId(rs.getString("user_id"));
                u.setLogin(rs.getString("login"));
                u.setDisplayname(rs.getString("displayname"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setJoursAbsentsConsecutifs(rs.getInt("jours_absents_consecutifs"));
                u.setTotalAbsences(rs.getInt("total_absences"));
                results.add(u);
            }
        }

    } catch (SQLException e) {
        logger.error("Error executing query for utilisateurs_absents_consecutifs_2: {}", e.getMessage());
        throw new RuntimeException("Failed to retrieve utilisateurs absents consecutifs", e);
    }

    return results;
}

    public Double getGlobalPresenceRate(String startDate, String endDate) {
        String sql = "SELECT taux_presence_global(?, ?)";
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));
            rs = stmt.executeQuery();

            if (rs.next()) {
                double result = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : result;
            } else {
                logger.warn("No result returned for taux_presence_global with startDate: {}, endDate: {}", startDate, endDate);
                return 0.0;
            }
        } catch (SQLException e) {
            logger.error("Error executing query for taux_presence_global: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve global presence rate", e);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    private List<UserPresenceRate> executeQueryForUsers(String sql, String startDate, String endDate, String userId) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<UserPresenceRate> results = new ArrayList<>();

        try {
            connection = dbConnection.getConnection();
            stmt = connection.prepareStatement(sql);
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            stmt.setDate(1, java.sql.Date.valueOf(start));
            stmt.setDate(2, java.sql.Date.valueOf(end));
            if (userId != null) {
                stmt.setString(3, userId);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            logger.error("Error executing query for user presence: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve user presence rates", e);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    private UserPresenceRate mapRow(ResultSet rs) throws SQLException {
        UserPresenceRate taux = new UserPresenceRate();
        taux.setUserId(rs.getString("userId"));
        taux.setLogin(rs.getString("login"));
        taux.setDisplayname(rs.getString("displayname"));
        taux.setFirstName(rs.getString("firstName"));
        taux.setLastName(rs.getString("lastName"));
        taux.setJoursPresent(rs.getInt("joursPresent"));
        taux.setJoursTotaux(rs.getInt("joursTotaux"));
        taux.setTauxPresence(rs.getDouble("tauxPresence"));
        return taux;
    }

    private void closeResources(Connection connection, PreparedStatement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { logger.error("Error closing ResultSet: {}", e.getMessage()); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { logger.error("Error closing PreparedStatement: {}", e.getMessage()); }
        dbConnection.closeConnection(connection);
    }
}
