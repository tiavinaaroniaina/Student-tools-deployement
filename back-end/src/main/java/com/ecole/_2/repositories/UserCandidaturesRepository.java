package com.ecole._2.repositories;

import com.ecole._2.models.UserCandidatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCandidaturesRepository extends JpaRepository<UserCandidatures, Integer> {
    Optional<UserCandidatures> findByUserId(String userId);
}
