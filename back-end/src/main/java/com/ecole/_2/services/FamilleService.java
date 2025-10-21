package com.ecole._2.services;

import com.ecole._2.models.Famille;
import com.ecole._2.repositories.FamilleRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FamilleService {

    @Autowired
    private FamilleRepository familleRepository;

    public Optional<Famille> getFamilleByUserId(String userId) {
        return familleRepository.findByUserId(userId);
    }

    public Famille createFamille(Famille famille) {
        // On pourrait ajouter ici des logiques de validation
        // Par exemple, vérifier si une famille existe déjà pour cet utilisateur
        Optional<Famille> existingFamille = familleRepository.findByUserId(famille.getUserId());
        if (existingFamille.isPresent()) {
            // Gérer le cas où la famille existe déjà, par exemple en lançant une exception
            throw new IllegalStateException("Une famille est déjà associée à cet utilisateur.");
        }
        return familleRepository.save(famille);
    }
}
