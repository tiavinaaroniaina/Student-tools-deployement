package com.ecole._2.repositories;

import com.ecole._2.models.Famille;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilleRepository extends JpaRepository<Famille, Long> {

    // Méthode pour trouver une famille par l'ID de l'utilisateur
    Optional<Famille> findByUserId(String userId);
}
