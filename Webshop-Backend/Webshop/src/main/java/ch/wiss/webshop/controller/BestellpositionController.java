package ch.wiss.webshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.service.BestellpositionService;
import jakarta.validation.Valid;

/**
 * REST-Controller für Bestellpositionen.
 * Basis-URL: /api/bestellpositionen
 * Jede Position verknüpft eine Bestellung mit einem Produkt und verwaltet den Bestand automatisch.
 */
@RestController
@RequestMapping(path = "/api/bestellpositionen")
public class BestellpositionController {

    @Autowired
    private BestellpositionService bestellpositionService;

    /**
     * Gibt alle Bestellpositionen zurück.
     *
     * @return HTTP 200 mit Liste aller Bestellpositionen
     */
    @GetMapping
    public ResponseEntity<List<Bestellposition>> getAllBestellpositionen() {
        return ResponseEntity.ok(bestellpositionService.findAll());
    }

    /**
     * Gibt eine Bestellposition anhand ihrer ID zurück.
     *
     * @param id Die ID der Bestellposition
     * @return HTTP 200 mit der Bestellposition oder HTTP 404 wenn nicht gefunden
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bestellposition> getBestellpositionById(@PathVariable Long id) {
        return bestellpositionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Gibt alle Positionen einer bestimmten Bestellung zurück.
     *
     * @param bestellungId Die ID der Bestellung
     * @return HTTP 200 mit Positionsliste oder HTTP 404 wenn Bestellung nicht gefunden
     */
    @GetMapping("/bestellung/{bestellungId}")
    public ResponseEntity<List<Bestellposition>> getBestellpositionenByBestellung(@PathVariable Long bestellungId) {
        if (!bestellpositionService.bestellungExistiertById(bestellungId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bestellpositionService.findByBestellungId(bestellungId));
    }

    /**
     * Erstellt eine neue Bestellposition und reduziert den Produktbestand.
     * Schlägt fehl wenn der Bestand nicht ausreicht.
     *
     * @param position Die neue Bestellposition mit Bestellung, Produkt und Menge
     * @return HTTP 201 bei Erfolg, HTTP 400 wenn Bestellung/Produkt nicht gefunden, HTTP 409 bei unzureichendem Bestand
     */
    @PostMapping
    public ResponseEntity<?> createBestellposition(@Valid @RequestBody Bestellposition position) {
        try {
            return bestellpositionService.create(position)
                    .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body((Object) saved))
                    .orElseGet(() -> ResponseEntity.badRequest().body("Bestellung oder Produkt nicht gefunden"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Aktualisiert eine Bestellposition und passt den Produktbestand entsprechend der Mengendifferenz an.
     *
     * @param id       Die ID der Bestellposition
     * @param position Die neuen Positionsdaten
     * @return HTTP 200 mit aktualisierter Position, HTTP 404 wenn nicht gefunden, HTTP 409 bei unzureichendem Bestand
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBestellposition(
            @PathVariable Long id,
            @Valid @RequestBody Bestellposition position) {

        try {
            return bestellpositionService.update(id, position)
                    .map(updated -> ResponseEntity.ok().body((Object) updated))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Löscht eine Bestellposition und stellt den Produktbestand wieder her.
     *
     * @param id Die ID der zu löschenden Bestellposition
     * @return HTTP 204 bei Erfolg oder HTTP 404 wenn nicht gefunden
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellposition(@PathVariable Long id) {
        if (!bestellpositionService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
