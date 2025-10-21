package com.ecole._2.models;

import java.math.BigDecimal;

public class UserHourAverages {
    private String login;
    private BigDecimal moyenneHeureDepuisDebut;
    private BigDecimal moyenneHeureDepuis3Mois;
    private BigDecimal moyenneHeureDepuis1Mois;
    private BigDecimal moyenneHeureDepuis1Semaine;

    public UserHourAverages() {}

    public UserHourAverages(String login, BigDecimal moyenneHeureDepuisDebut, BigDecimal moyenneHeureDepuis3Mois, BigDecimal moyenneHeureDepuis1Mois, BigDecimal moyenneHeureDepuis1Semaine) {
        this.login = login;
        this.moyenneHeureDepuisDebut = moyenneHeureDepuisDebut;
        this.moyenneHeureDepuis3Mois = moyenneHeureDepuis3Mois;
        this.moyenneHeureDepuis1Mois = moyenneHeureDepuis1Mois;
        this.moyenneHeureDepuis1Semaine = moyenneHeureDepuis1Semaine;
    }

    // Getters and Setters

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public BigDecimal getMoyenneHeureDepuisDebut() {
        return moyenneHeureDepuisDebut;
    }

    public void setMoyenneHeureDepuisDebut(BigDecimal moyenneHeureDepuisDebut) {
        this.moyenneHeureDepuisDebut = moyenneHeureDepuisDebut;
    }

    public BigDecimal getMoyenneHeureDepuis3Mois() {
        return moyenneHeureDepuis3Mois;
    }

    public void setMoyenneHeureDepuis3Mois(BigDecimal moyenneHeureDepuis3Mois) {
        this.moyenneHeureDepuis3Mois = moyenneHeureDepuis3Mois;
    }

    public BigDecimal getMoyenneHeureDepuis1Mois() {
        return moyenneHeureDepuis1Mois;
    }

    public void setMoyenneHeureDepuis1Mois(BigDecimal moyenneHeureDepuis1Mois) {
        this.moyenneHeureDepuis1Mois = moyenneHeureDepuis1Mois;
    }

    public BigDecimal getMoyenneHeureDepuis1Semaine() {
        return moyenneHeureDepuis1Semaine;
    }

    public void setMoyenneHeureDepuis1Semaine(BigDecimal moyenneHeureDepuis1Semaine) {
        this.moyenneHeureDepuis1Semaine = moyenneHeureDepuis1Semaine;
    }
}
