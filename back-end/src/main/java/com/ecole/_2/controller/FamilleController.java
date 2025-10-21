package com.ecole._2.controller;

import com.ecole._2.models.Famille;
import com.ecole._2.services.FamilleService;
import java.util.Optional;
import com.ecole._2.services.FamilleReponseService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/famille")
public class FamilleController {

    @Autowired
    private FamilleService familleService;

    @Autowired
    private FamilleReponseService familleReponseService;

    @GetMapping("/{userId}")
    public ResponseEntity<Famille> getFamilleByUserId(@PathVariable String userId) {
        Optional<Famille> famille = familleService.getFamilleByUserId(userId);
        return famille.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createFamille(@RequestBody Famille famille) {
        try {
            Famille nouvelleFamille = familleService.createFamille(famille);
            return new ResponseEntity<>(nouvelleFamille, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/{familleId}/reponses")
    public ResponseEntity<Map<Long, Long>> getReponsesByFamille(@PathVariable Long familleId) {
        Map<Long, Long> reponses = familleReponseService.getReponsesByFamille(familleId);
        return ResponseEntity.ok(reponses);
    }

    @PostMapping("/{familleId}/reponses")
    public ResponseEntity<?> submitReponses(@PathVariable Long familleId, @RequestBody List<Long> reponseIds) {
        try {
            int totalScore = familleReponseService.saveAndCalculateScore(familleId, reponseIds);
            return ResponseEntity.ok(totalScore);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
