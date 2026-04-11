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

@RestController
@RequestMapping(path = "/api/produkte")
public class ProduktController {

    @Autowired
    private ProduktService produktService;

    @GetMapping
    public ResponseEntity<List<Produkt>> getAllProdukte() {
        return ResponseEntity.ok(produktService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produkt> getProduktById(@PathVariable Long id) {
        return produktService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/kategorie/{kategorieId}")
    public ResponseEntity<List<Produkt>> getProdukteBykategorie(@PathVariable Long kategorieId) {
        if (!produktService.kategorieExistiertById(kategorieId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(produktService.findByKategorieId(kategorieId));
    }

    @GetMapping("/suche")
    public ResponseEntity<List<Produkt>> sucheProdukte(@RequestParam String name) {
        return ResponseEntity.ok(produktService.sucheByName(name));
    }

    @GetMapping("/verfuegbar")
    public ResponseEntity<List<Produkt>> getVerfuegbareProdukte() {
        return ResponseEntity.ok(produktService.findVerfuegbare());
    }

    @PostMapping
    public ResponseEntity<?> createProdukt(@Valid @RequestBody Produkt produkt) {
        return produktService.save(produkt)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body((Object) saved))
                .orElseGet(() -> ResponseEntity.badRequest().body("Kategorie nicht gefunden"));
    }

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
