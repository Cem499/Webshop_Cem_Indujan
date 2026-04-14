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

/**
 * REST-Controller für Kategorien.
 * Basis-URL: /api/kategorien
 * GET-Endpunkte sind öffentlich, Schreib-Endpunkte erfordern Authentifizierung.
 */
@RestController
@RequestMapping(path = "/api/kategorien")
public class KategorieController {

    @Autowired
    private KategorieService kategorieService;

    /**
     * Gibt alle Kategorien zurück.
     *
     * @return HTTP 200 mit Liste aller Kategorien
     */
    @GetMapping
    public ResponseEntity<List<Kategorie>> getAllKategorien() {
        return ResponseEntity.ok(kategorieService.findAll());
    }

    /**
     * Gibt eine Kategorie anhand ihrer ID zurück.
     *
     * @param id Die ID der Kategorie
     * @return HTTP 200 mit der Kategorie oder HTTP 404 wenn nicht gefunden
     */
    @GetMapping("/{id}")
    public ResponseEntity<Kategorie> getKategorieById(@PathVariable Long id) {
        return kategorieService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Gibt eine Kategorie anhand ihres Namens zurück.
     *
     * @param name Der Name der Kategorie
     * @return HTTP 200 mit der Kategorie oder HTTP 404 wenn nicht gefunden
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Kategorie> getKategorieByName(@PathVariable String name) {
        return kategorieService.findByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Erstellt eine neue Kategorie.
     * Kategoriename muss systemweit eindeutig sein.
     *
     * @param kategorie Die zu erstellende Kategorie
     * @return HTTP 201 mit der erstellten Kategorie oder HTTP 409 wenn Name bereits existiert
     */
    @PostMapping
    public ResponseEntity<?> createKategorie(@Valid @RequestBody Kategorie kategorie) {
        if (kategorieService.existsByName(kategorie.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Kategorie mit diesem Namen existiert bereits");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(kategorieService.save(kategorie));
    }

    /**
     * Aktualisiert eine bestehende Kategorie vollständig.
     *
     * @param id        Die ID der zu aktualisierenden Kategorie
     * @param kategorie Die neuen Kategoriedaten
     * @return HTTP 200 mit der aktualisierten Kategorie oder HTTP 404 wenn nicht gefunden
     */
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

    /**
     * Löscht eine Kategorie anhand ihrer ID.
     * Schlägt fehl wenn noch Produkte dieser Kategorie existieren.
     *
     * @param id Die ID der zu löschenden Kategorie
     * @return HTTP 204 bei Erfolg, HTTP 404 wenn nicht gefunden, HTTP 409 bei referenzierten Produkten
     */
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
