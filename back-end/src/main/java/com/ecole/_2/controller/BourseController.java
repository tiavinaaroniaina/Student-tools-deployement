package com.ecole._2.controller;

import com.ecole._2.dto.FamilleElligibleDTO;
import com.ecole._2.services.BourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bourse")
public class BourseController {

    @Autowired
    private BourseService bourseService;

    @GetMapping("/elligibles")
    public ResponseEntity<List<FamilleElligibleDTO>> getFamillesElligibles() {
        List<FamilleElligibleDTO> familles = bourseService.getFamillesElligibles();
        return ResponseEntity.ok(familles);
    }

    @GetMapping("/elligibles_par_categorie")
    public ResponseEntity<List<FamilleElligibleDTO>> getFamillesElligiblesParCategorie() {
        List<FamilleElligibleDTO> familles = bourseService.getFamillesElligiblesParCategorie();
        return ResponseEntity.ok(familles);
    }
}
