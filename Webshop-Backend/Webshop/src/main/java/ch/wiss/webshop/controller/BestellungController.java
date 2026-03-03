package ch.wiss.webshop.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.model.Bestellung.BestellStatus;
import ch.wiss.webshop.repository.BestellpositionRepository;
import ch.wiss.webshop.repository.BestellungRepository;
import ch.wiss.webshop.repository.ProduktRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/bestellungen")
public class BestellungController {

    @Autowired
    private BestellungRepository bestellungRepository;

    @Autowired
    private BestellpositionRepository bestellpositionRepository;

    @Autowired
    private ProduktRepository produktRepository;

    @GetMapping
    public ResponseEntity<List<Bestellung>> getAllBestellungen() {
        List<Bestellung> bestellungen = bestellungRepository.findAll();
        return ResponseEntity.ok(bestellungen);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bestellung> getBestellungById(@PathVariable Long id) {
        Optional<Bestellung> bestellung = bestellungRepository.findById(id);
        return bestellung.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Bestellung>> getBestellungenByStatus(@PathVariable String status) {
        try {
            BestellStatus bestellStatus = BestellStatus.valueOf(status.toUpperCase());
            List<Bestellung> bestellungen = bestellungRepository.findByStatus(bestellStatus);
            return ResponseEntity.ok(bestellungen);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/kunde/{kundenName}")
    public ResponseEntity<List<Bestellung>> getBestellungenByKunde(@PathVariable String kundenName) {
        List<Bestellung> bestellungen = bestellungRepository.findByKundenName(kundenName);
        return ResponseEntity.ok(bestellungen);
    }

    @PostMapping
    public ResponseEntity<Bestellung> createBestellung(@Valid @RequestBody Bestellung bestellung) {
        if (bestellung.getErstelltAm() == null) {
            bestellung.setErstelltAm(LocalDateTime.now());
        }
        if (bestellung.getStatus() == null) {
            bestellung.setStatus(BestellStatus.OFFEN);
        }
        if (bestellung.getGesamtbetrag() == null) {
            bestellung.setGesamtbetrag(BigDecimal.ZERO);
        }
        
        Bestellung savedBestellung = bestellungRepository.save(bestellung);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBestellung);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bestellung> updateBestellung(@PathVariable Long id, @Valid @RequestBody Bestellung bestellung) {
        if (bestellungRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        bestellung.setId(id);
        Bestellung updatedBestellung = bestellungRepository.save(bestellung);
        return ResponseEntity.ok(updatedBestellung);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBestellungStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Bestellung> bestellungOpt = bestellungRepository.findById(id);
        if (bestellungOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            BestellStatus bestellStatus = BestellStatus.valueOf(status.toUpperCase());
            Bestellung bestellung = bestellungOpt.get();
            bestellung.setStatus(bestellStatus);
            Bestellung updatedBestellung = bestellungRepository.save(bestellung);
            return ResponseEntity.ok(updatedBestellung);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ungültiger Status: " + status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellung(@PathVariable Long id) {
        if (bestellungRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Zuerst Bestellpositionen laden und Bestand zurückgeben
            List<Bestellposition> positionen = bestellpositionRepository.findByBestellungId(id);
            for (Bestellposition position : positionen) {
                Produkt produkt = position.getProdukt();
                produkt.erhoeheBestand(position.getMenge());
                produktRepository.save(produkt);
            }
            
            // Dann Bestellpositionen löschen
            bestellpositionRepository.deleteAll(positionen);
            
            // Dann Bestellung löschen
            bestellungRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Bestellung kann nicht gelöscht werden.");
        }
    }
}