package com.ecole._2.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BourseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Object[]> findFamillesElligibles() {
        return entityManager.createNativeQuery("select * from familles_elligibles_pour_bourse()").getResultList();
    }

    public List<Object[]> findFamillesElligiblesParCategorie() {
        return entityManager.createNativeQuery("select * from familles_elligibles_par_categorie()").getResultList();
    }
}
