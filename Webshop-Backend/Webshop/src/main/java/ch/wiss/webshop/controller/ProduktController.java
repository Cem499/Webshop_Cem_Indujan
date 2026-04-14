package ch.wiss.webshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.service.ProduktService;
import jakarta.validation.Valid;

/**
 * REST-Controller für Produkte.
 * Basis-URL: /api/produkte
 * GET-Endpunkte sind öffentlich, Schreib-Endpunkte erfordern Authentifizierung.
 */
@RestController
@RequestMapping(path = "/api/produkte")
public class ProduktController {

    @Autowired
    private ProduktService produktService;

    /**
     * Gibt alle Produkte zurück.
     *
     * @return HTTP 200 mit Liste aller Produkte
     */
    @GetMapping
    public ResponseEntity<List<Produkt>> getAllProdukte() {
        return ResponseEntity.ok(produktService.findAll());
    }

    /**
     * Gibt ein Produkt anhand seiner ID zurück.
     *
     * @param id Die ID des Produkts
     * @return HTTP 200 mit dem Produkt oder HTTP 404 wenn nicht gefunden
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produkt> getProduktById(@PathVariable Long id) {
        return produktService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Gibt alle Produkte einer bestimmten Kategorie zurück.
     *
     * @param kategorieId Die ID der Kategorie
     * @return HTTP 200 mit Produktliste oder HTTP 404 wenn Kategorie nicht gefunden
     */
    @GetMapping("/kategorie/{kategorieId}")
    public ResponseEntity<List<Produkt>> getProdukteBykategorie(@PathVariable Long kategorieId) {
        if (!produktService.kategorieExistiertById(kategorieId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produktService.findByKategorieId(kategorieId));
    }

    /**
     * Sucht Produkte nach Name (case-insensitive, Teilübereinstimmung).
     *
     * @param name Der Suchbegriff
     * @return HTTP 200 mit gefundenen Produkten (kann leer sein)
     */
    @GetMapping("/suche")
    public ResponseEntity<List<Produkt>> sucheProdukte(@RequestParam String name) {
        return ResponseEntity.ok(produktService.sucheByName(name));
    }

    /**
     * Gibt alle Produkte mit Bestand größer als 0 zurück.
     *
     * @return HTTP 200 mit verfügbaren Produkten
     */
    @GetMapping("/verfuegbar")
    public ResponseEntity<List<Produkt>> getVerfuegbareProdukte() {
        return ResponseEntity.ok(produktService.findVerfuegbare());
    }

    /**
     * Erstellt ein neues Produkt.
     *
     * @param produkt Das zu erstellende Produkt
     * @return HTTP 201 mit dem erstellten Produkt oder HTTP 400 wenn Kategorie nicht gefunden
     */
    @PostMapping
    public ResponseEntity<?> createProdukt(@Valid @RequestBody Produkt produkt) {
        return produktService.save(produkt)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body((Object) saved))
                .orElseGet(() -> ResponseEntity.badRequest().body("Kategorie nicht gefunden"));
    }

    /**
     * Aktualisiert ein bestehendes Produkt vollständig.
     *
     * @param id      Die ID des zu aktualisierenden Produkts
     * @param produkt Die neuen Produktdaten
     * @return HTTP 200 mit dem aktualisierten Produkt, HTTP 404 oder HTTP 400 wenn Kategorie fehlt
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProdukt(
            @PathVariable Long id,
            @Valid @RequestBody Produkt produkt) {

        if (!produktService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        produkt.setId(id);
        return produktService.save(produkt)
                .map(updated -> ResponseEntity.ok().body((Object) updated))
                .orElseGet(() -> ResponseEntity.badRequest().body("Kategorie nicht gefunden"));
    }

    /**
     * Aktualisiert nur den Bestand eines Produkts.
     *
     * @param id      Die ID des Produkts
     * @param bestand Der neue Bestandswert (muss >= 0 sein)
     * @return HTTP 200 mit aktualisierten Produkt, HTTP 400 bei negativem Bestand, HTTP 404 wenn nicht gefunden
     */
    @PatchMapping("/{id}/bestand")
    public ResponseEntity<?> updateBestand(
            @PathVariable Long id,
            @RequestParam int bestand) {

        if (bestand < 0) {
            return ResponseEntity.badRequest().body("Bestand darf nicht negativ sein");
        }
        return produktService.updateBestand(id, bestand)
                .map(updated -> ResponseEntity.ok().body((Object) updated))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Löscht ein Produkt anhand seiner ID.
     * Schlägt fehl wenn das Produkt noch in Bestellungen referenziert wird.
     *
     * @param id Die ID des zu löschenden Produkts
     * @return HTTP 204 bei Erfolg, HTTP 404 wenn nicht gefunden, HTTP 409 bei referenzierten Bestellungen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProdukt(@PathVariable Long id) {
        if (!produktService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            produktService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Produkt kann nicht gelöscht werden, da es noch in Bestellungen verwendet wird.");
        }
    }
}
