package ch.wiss.webshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.service.BestellpositionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/bestellpositionen")
public class BestellpositionController {

    @Autowired
    private BestellpositionService bestellpositionService;

    @GetMapping
    public ResponseEntity<List<Bestellposition>> getAllBestellpositionen() {
        return ResponseEntity.ok(bestellpositionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bestellposition> getBestellpositionById(@PathVariable Long id) {
        return bestellpositionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/bestellung/{bestellungId}")
    public ResponseEntity<List<Bestellposition>> getBestellpositionenByBestellung(@PathVariable Long bestellungId) {
        if (!bestellpositionService.bestellungExistiertById(bestellungId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bestellpositionService.findByBestellungId(bestellungId));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellposition(@PathVariable Long id) {
        if (!bestellpositionService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
