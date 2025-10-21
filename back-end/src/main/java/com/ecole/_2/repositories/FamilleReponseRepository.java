package com.ecole._2.repositories;

import com.ecole._2.models.FamilleReponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilleReponseRepository extends JpaRepository<FamilleReponse, Long> {
    List<FamilleReponse> findByFamilleId(Long familleId);

    @Modifying
    @Query("DELETE FROM FamilleReponse fr WHERE fr.famille.id = :familleId")
    void deleteByFamilleId(@Param("familleId") Long familleId);
}
