package ch.wiss.webshop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Kategorie;
import ch.wiss.webshop.service.KategorieService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/kategorien")
public class KategorieController {

    @Autowired
    private KategorieService kategorieService;

    @GetMapping
    public ResponseEntity<List<Kategorie>> getAllKategorien() {
        return ResponseEntity.ok(kategorieService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Kategorie> getKategorieById(@PathVariable Long id) {
        return kategorieService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Kategorie> getKategorieByName(@PathVariable String name) {
        return kategorieService.findByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createKategorie(@Valid @RequestBody Kategorie kategorie) {
        if (kategorieService.existsByName(kategorie.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Kategorie mit diesem Namen existiert bereits");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(kategorieService.save(kategorie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Kategorie> updateKategorie(
            @PathVariable Long id,
            @Valid @RequestBody Kategorie kategorie) {

        if (kategorieService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        kategorie.setId(id);
        return ResponseEntity.ok(kategorieService.save(kategorie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKategorie(@PathVariable Long id) {
        if (kategorieService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            kategorieService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Kategorie kann nicht gelöscht werden, da sie noch von Produkten verwendet wird.");
        }
    }
}
