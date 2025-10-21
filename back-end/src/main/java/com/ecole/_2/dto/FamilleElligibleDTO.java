package com.ecole._2.dto;

public class FamilleElligibleDTO {

    private String nom_famille;
    private String login_utilisateur;
    private String categorie_nom;
    private Long score_categorie;


    public FamilleElligibleDTO(String nom_famille, String login_utilisateur, String categorie_nom, Long score_categorie) {
        this.nom_famille = nom_famille;
        this.login_utilisateur = login_utilisateur;
        this.categorie_nom = categorie_nom;
        this.score_categorie = score_categorie;
    }

    // Getters and Setters

    public String getNom_famille() {
        return nom_famille;
    }

    public void setNom_famille(String nom_famille) {
        this.nom_famille = nom_famille;
    }

    public String getLogin_utilisateur() {
        return login_utilisateur;
    }

    public void setLogin_utilisateur(String login_utilisateur) {
        this.login_utilisateur = login_utilisateur;
    }

    public String getCategorie_nom() {
        return categorie_nom;
    }

    public void setCategorie_nom(String categorie_nom) {
        this.categorie_nom = categorie_nom;
    }

    public Long getScore_categorie() {
        return score_categorie;
    }

    public void setScore_categorie(Long score_categorie) {
        this.score_categorie = score_categorie;
    }
}
