package com.ecole._2.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "famille")
public class Famille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("user_id")
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "animateur_nom")
    private String animateurNom;

    @Column(name = "nombre_personnes")
    private Integer nombrePersonnes;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private Timestamp dateCreation;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAnimateurNom() {
        return animateurNom;
    }

    public void setAnimateurNom(String animateurNom) {
        this.animateurNom = animateurNom;
    }

    public Integer getNombrePersonnes() {
        return nombrePersonnes;
    }

    public void setNombrePersonnes(Integer nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
}
