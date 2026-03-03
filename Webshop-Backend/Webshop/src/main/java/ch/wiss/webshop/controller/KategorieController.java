package ch.wiss.webshop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Kategorie;
import ch.wiss.webshop.repository.KategorieRepository;
import jakarta.validation.Valid;

/**
 * REST-Controller für Kategorien.
 * Verwaltet CRUD-Operationen für Kategorien.
 */
@RestController
@RequestMapping(path = "/api/kategorien")
public class KategorieController {

    @Autowired
    private KategorieRepository kategorieRepository;

    /**
     * Gibt alle Kategorien zurück.
     *
     * @return Liste aller Kategorien
     */
    @GetMapping
    public ResponseEntity<List<Kategorie>> getAllKategorien() {
        List<Kategorie> kategorien = kategorieRepository.findAll();
        return ResponseEntity.ok(kategorien);
    }

    /**
     * Gibt eine Kategorie anhand ihrer ID zurück.
     *
     * @param id Die ID der Kategorie
     * @return Die Kategorie oder 404 wenn nicht gefunden
     */
    @GetMapping("/{id}")
    public ResponseEntity<Kategorie> getKategorieById(@PathVariable Long id) {
        Optional<Kategorie> kategorie = kategorieRepository.findById(id);
        
        return kategorie.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Sucht eine Kategorie nach Name.
     *
     * @param name Der Name der Kategorie
     * @return Die Kategorie oder 404 wenn nicht gefunden
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Kategorie> getKategorieByName(@PathVariable String name) {
        Optional<Kategorie> kategorie = kategorieRepository.findByName(name);
        
        return kategorie.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Erstellt eine neue Kategorie.
     *
     * @param kategorie Die zu erstellende Kategorie
     * @return Die erstellte Kategorie mit Status 201
     */
    @PostMapping
    public ResponseEntity<?> createKategorie(@Valid @RequestBody Kategorie kategorie) {
        // Prüfe ob Kategorie mit gleichem Namen bereits existiert
        Optional<Kategorie> existing = kategorieRepository.findByName(kategorie.getName());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Kategorie mit diesem Namen existiert bereits");
        }
        
        Kategorie savedKategorie = kategorieRepository.save(kategorie);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedKategorie);
    }

    /**
     * Aktualisiert eine bestehende Kategorie.
     *
     * @param id        Die ID der zu aktualisierenden Kategorie
     * @param kategorie Die aktualisierten Daten
     * @return Die aktualisierte Kategorie oder 404 wenn nicht gefunden
     */
    @PutMapping("/{id}")
    public ResponseEntity<Kategorie> updateKategorie(
            @PathVariable Long id,
            @Valid @RequestBody Kategorie kategorie) {
        
        if (kategorieRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        kategorie.setId(id);
        Kategorie updatedKategorie = kategorieRepository.save(kategorie);
        return ResponseEntity.ok(updatedKategorie);
    }

    /**
     * Löscht eine Kategorie.
     *
     * @param id Die ID der zu löschenden Kategorie
     * @return 204 No Content bei Erfolg, 404 wenn nicht gefunden, 409 bei Constraint-Verletzung
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKategorie(@PathVariable Long id) {
        if (kategorieRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            kategorieRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Kategorie kann nicht gelöscht werden, da sie noch von Produkten verwendet wird.");
        }
    }
}