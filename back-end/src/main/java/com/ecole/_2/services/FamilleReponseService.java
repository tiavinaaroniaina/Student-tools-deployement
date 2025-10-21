package com.ecole._2.services;

import com.ecole._2.models.Famille;
import com.ecole._2.models.FamilleReponse;
import com.ecole._2.models.Reponse;
import com.ecole._2.repositories.FamilleRepository;
import com.ecole._2.repositories.FamilleReponseRepository;
import com.ecole._2.repositories.ReponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FamilleReponseService {

    @Autowired
    private FamilleReponseRepository familleReponseRepository;

    @Autowired
    private FamilleRepository familleRepository;

    @Autowired
    private ReponseRepository reponseRepository;

    public Map<Long, Long> getReponsesByFamille(Long familleId) {
        List<FamilleReponse> familleReponses = familleReponseRepository.findByFamilleId(familleId);
        return familleReponses.stream()
                .collect(Collectors.toMap(fr -> fr.getReponse().getQuestion().getId(), fr -> fr.getReponse().getId()));
    }

    @Transactional
    public int saveAndCalculateScore(Long familleId, List<Long> reponseIds) {
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new IllegalArgumentException("Famille non trouvée avec l'ID: " + familleId));

        // Supprimer les anciennes réponses pour cette famille
        familleReponseRepository.deleteByFamilleId(familleId);

        int totalScore = 0;

        for (Long reponseId : reponseIds) {
            Reponse reponse = reponseRepository.findById(reponseId)
                    .orElseThrow(() -> new IllegalArgumentException("Réponse non trouvée avec l'ID: " + reponseId));

            FamilleReponse familleReponse = new FamilleReponse();
            familleReponse.setFamille(famille);
            familleReponse.setReponse(reponse);
            familleReponseRepository.save(familleReponse);

            totalScore += reponse.getScore();
        }

        return totalScore;
    }
}
