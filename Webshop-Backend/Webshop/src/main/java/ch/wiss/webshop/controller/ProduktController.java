package ch.wiss.webshop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.dao.DataIntegrityViolationException;

import ch.wiss.webshop.model.Kategorie;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.repository.KategorieRepository;
import ch.wiss.webshop.repository.ProduktRepository;
import jakarta.validation.Valid;

/**
 * REST-Controller für Produkte.
 * Verwaltet CRUD-Operationen für Produkte.
 */
@RestController
@RequestMapping(path = "/api/produkte")
public class ProduktController {

    @Autowired
    private ProduktRepository produktRepository;

    @Autowired
    private KategorieRepository kategorieRepository;

    /**
     * Gibt alle Produkte zurück.
     *
     * @return Liste aller Produkte
     */
    @GetMapping
    public ResponseEntity<List<Produkt>> getAllProdukte() {
        List<Produkt> produkte = produktRepository.findAll();
        return ResponseEntity.ok(produkte);
    }

    /**
     * Gibt ein Produkt anhand seiner ID zurück.
     *
     * @param id Die ID des Produkts
     * @return Das Produkt oder 404 wenn nicht gefunden
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produkt> getProduktById(@PathVariable Long id) {
        Optional<Produkt> produkt = produktRepository.findById(id);
        
        return produkt.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Gibt alle Produkte einer bestimmten Kategorie zurück.
     *
     * @param kategorieId Die ID der Kategorie
     * @return Liste der Produkte
     */
    @GetMapping("/kategorie/{kategorieId}")
    public ResponseEntity<List<Produkt>> getProdukteBykategorie(@PathVariable Long kategorieId) {
        if (kategorieRepository.findById(kategorieId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Produkt> produkte = produktRepository.findByKategorieId(kategorieId);
        return ResponseEntity.ok(produkte);
    }

    /**
     * Sucht Produkte nach Name.
     *
     * @param name Der Suchbegriff
     * @return Liste der gefundenen Produkte
     */
    @GetMapping("/suche")
    public ResponseEntity<List<Produkt>> sucheProdukte(@RequestParam String name) {
        List<Produkt> produkte = produktRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(produkte);
    }

    /**
     * Gibt alle verfügbaren Produkte zurück (Bestand > 0).
     *
     * @return Liste der verfügbaren Produkte
     */
    @GetMapping("/verfuegbar")
    public ResponseEntity<List<Produkt>> getVerfuegbareProdukte() {
        List<Produkt> produkte = produktRepository.findByBestandGreaterThan(0);
        return ResponseEntity.ok(produkte);
    }

    /**
     * Erstellt ein neues Produkt.
     *
     * @param produkt Das zu erstellende Produkt
     * @return Das erstellte Produkt mit Status 201
     */
    @PostMapping
    public ResponseEntity<?> createProdukt(@Valid @RequestBody Produkt produkt) {
        // Validierung: Kategorie muss existieren
        Optional<Kategorie> kategorie = kategorieRepository.findById(produkt.getKategorie().getId());
        if (kategorie.isEmpty()) {
            return ResponseEntity.badRequest().body("Kategorie nicht gefunden");
        }
        
        produkt.setKategorie(kategorie.get());
        Produkt savedProdukt = produktRepository.save(produkt);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProdukt);
    }

    /**
     * Aktualisiert ein bestehendes Produkt.
     *
     * @param id      Die ID des zu aktualisierenden Produkts
     * @param produkt Die aktualisierten Daten
     * @return Das aktualisierte Produkt oder 404 wenn nicht gefunden
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProdukt(
            @PathVariable Long id,
            @Valid @RequestBody Produkt produkt) {
        
        if (produktRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Validierung: Kategorie muss existieren
        Optional<Kategorie> kategorie = kategorieRepository.findById(produkt.getKategorie().getId());
        if (kategorie.isEmpty()) {
            return ResponseEntity.badRequest().body("Kategorie nicht gefunden");
        }
        
        produkt.setId(id);
        produkt.setKategorie(kategorie.get());
        Produkt updatedProdukt = produktRepository.save(produkt);
        return ResponseEntity.ok(updatedProdukt);
    }

    /**
     * Aktualisiert den Bestand eines Produkts.
     *
     * @param id      Die ID des Produkts
     * @param bestand Der neue Bestand
     * @return Das aktualisierte Produkt oder 404 wenn nicht gefunden
     */
    @PatchMapping("/{id}/bestand")
    public ResponseEntity<?> updateBestand(
            @PathVariable Long id,
            @RequestParam int bestand) {
        
        Optional<Produkt> produktOpt = produktRepository.findById(id);
        if (produktOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (bestand < 0) {
            return ResponseEntity.badRequest().body("Bestand darf nicht negativ sein");
        }
        
        Produkt produkt = produktOpt.get();
        produkt.setBestand(bestand);
        Produkt updatedProdukt = produktRepository.save(produkt);
        return ResponseEntity.ok(updatedProdukt);
    }

    /**
     * Löscht ein Produkt.
     *
     * @param id Die ID des zu löschenden Produkts
     * @return 204 No Content bei Erfolg, 404 wenn nicht gefunden, 409 bei Constraint-Verletzung
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProdukt(@PathVariable Long id) {
        if (produktRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            produktRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Produkt kann nicht gelöscht werden, da es noch in Bestellungen verwendet wird.");
        }
    }
}