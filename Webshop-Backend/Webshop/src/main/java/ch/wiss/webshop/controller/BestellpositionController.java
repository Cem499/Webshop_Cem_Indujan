package ch.wiss.webshop.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.repository.BestellpositionRepository;
import ch.wiss.webshop.repository.BestellungRepository;
import ch.wiss.webshop.repository.ProduktRepository;
import jakarta.validation.Valid;

/**
 * REST-Controller für Bestellpositionen.
 * Verwaltet CRUD-Operationen für Bestellpositionen und kümmert sich um die
 * automatische Bestandsverwaltung sowie die Berechnung des Gesamtbetrags.
 */
@RestController
@RequestMapping(path = "/api/bestellpositionen")
public class BestellpositionController {

    @Autowired
    private BestellpositionRepository bestellpositionRepository;

    @Autowired
    private BestellungRepository bestellungRepository;

    @Autowired
    private ProduktRepository produktRepository;

    /**
     * Gibt alle Bestellpositionen zurück.
     *
     * @return ResponseEntity mit Liste aller Bestellpositionen
     */
    @GetMapping
    public ResponseEntity<List<Bestellposition>> getAllBestellpositionen() {
        List<Bestellposition> positionen = bestellpositionRepository.findAll();
        return ResponseEntity.ok(positionen);
    }

    /**
     * Gibt eine Bestellposition anhand ihrer ID zurück.
     *
     * @param id Die ID der Bestellposition
     * @return ResponseEntity mit der Bestellposition oder 404 wenn nicht gefunden
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bestellposition> getBestellpositionById(@PathVariable Long id) {
        Optional<Bestellposition> position = bestellpositionRepository.findById(id);
        
        return position.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Gibt alle Bestellpositionen einer bestimmten Bestellung zurück.
     *
     * @param bestellungId Die ID der Bestellung
     * @return ResponseEntity mit Liste der Bestellpositionen oder 404 wenn Bestellung nicht gefunden
     */
    @GetMapping("/bestellung/{bestellungId}")
    public ResponseEntity<List<Bestellposition>> getBestellpositionenByBestellung(@PathVariable Long bestellungId) {
        if (bestellungRepository.findById(bestellungId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Bestellposition> positionen = bestellpositionRepository.findByBestellungId(bestellungId);
        return ResponseEntity.ok(positionen);
    }

    /**
     * Erstellt eine neue Bestellposition.
     * Prüft die Verfügbarkeit des Produkts, reduziert den Bestand,
     * setzt den Einzelpreis automatisch vom Produkt und aktualisiert
     * den Gesamtbetrag der Bestellung.
     *
     * @param position Die zu erstellende Bestellposition
     * @return ResponseEntity mit der erstellten Bestellposition (Status 201) oder Fehlermeldung
     */
    @PostMapping
    public ResponseEntity<?> createBestellposition(@Valid @RequestBody Bestellposition position) {
        Optional<Bestellung> bestellung = bestellungRepository.findById(position.getBestellung().getId());
        if (bestellung.isEmpty()) {
            return ResponseEntity.badRequest().body("Bestellung nicht gefunden");
        }
        
        Optional<Produkt> produkt = produktRepository.findById(position.getProdukt().getId());
        if (produkt.isEmpty()) {
            return ResponseEntity.badRequest().body("Produkt nicht gefunden");
        }
        
        if (!produkt.get().pruefeBestand(position.getMenge())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Nicht genügend Bestand verfügbar");
        }
        
        produkt.get().reduziereBestand(position.getMenge());
        produktRepository.save(produkt.get());
        
        position.setEinzelpreis(produkt.get().getPreis());
        
        position.setBestellung(bestellung.get());
        position.setProdukt(produkt.get());
        
        Bestellposition savedPosition = bestellpositionRepository.save(position);
        
        aktualisiereBestellungGesamtbetrag(bestellung.get());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPosition);
    }

    /**
     * Aktualisiert eine bestehende Bestellposition.
     * Passt den Produktbestand entsprechend der Mengenänderung an,
     * aktualisiert den Einzelpreis und berechnet den neuen Gesamtbetrag.
     *
     * @param id       Die ID der zu aktualisierenden Bestellposition
     * @param position Die aktualisierten Daten
     * @return ResponseEntity mit der aktualisierten Bestellposition oder 404/Fehlermeldung
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBestellposition(
            @PathVariable Long id,
            @Valid @RequestBody Bestellposition position) {
        
        Optional<Bestellposition> existingPositionOpt = bestellpositionRepository.findById(id);
        if (existingPositionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Bestellposition existingPosition = existingPositionOpt.get();
        
        Optional<Bestellung> bestellung = bestellungRepository.findById(position.getBestellung().getId());
        if (bestellung.isEmpty()) {
            return ResponseEntity.badRequest().body("Bestellung nicht gefunden");
        }
        
        Optional<Produkt> produkt = produktRepository.findById(position.getProdukt().getId());
        if (produkt.isEmpty()) {
            return ResponseEntity.badRequest().body("Produkt nicht gefunden");
        }
        
        int mengeDifferenz = position.getMenge() - existingPosition.getMenge();
        
        if (mengeDifferenz > 0) {
            if (!produkt.get().pruefeBestand(mengeDifferenz)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Nicht genügend Bestand verfügbar");
            }
            produkt.get().reduziereBestand(mengeDifferenz);
        } else if (mengeDifferenz < 0) {
            produkt.get().erhoeheBestand(Math.abs(mengeDifferenz));
        }
        produktRepository.save(produkt.get());
        
        position.setEinzelpreis(produkt.get().getPreis());
        
        position.setId(id);
        position.setBestellung(bestellung.get());
        position.setProdukt(produkt.get());
        
        Bestellposition updatedPosition = bestellpositionRepository.save(position);
        
        aktualisiereBestellungGesamtbetrag(bestellung.get());
        
        return ResponseEntity.ok(updatedPosition);
    }

    /**
     * Löscht eine Bestellposition.
     * Gibt den Produktbestand wieder frei und aktualisiert den Gesamtbetrag der Bestellung.
     *
     * @param id Die ID der zu löschenden Bestellposition
     * @return ResponseEntity mit Status 204 (No Content) bei Erfolg oder 404 wenn nicht gefunden
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellposition(@PathVariable Long id) {
        Optional<Bestellposition> positionOpt = bestellpositionRepository.findById(id);
        if (positionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Bestellposition position = positionOpt.get();
        Bestellung bestellung = position.getBestellung();
        Produkt produkt = position.getProdukt();
        
        produkt.erhoeheBestand(position.getMenge());
        produktRepository.save(produkt);
        
        bestellpositionRepository.deleteById(id);
        
        aktualisiereBestellungGesamtbetrag(bestellung);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Berechnet und aktualisiert den Gesamtbetrag einer Bestellung.
     * Summiert alle Bestellpositionen (Einzelpreis × Menge) und speichert
     * das Ergebnis in der Bestellung.
     *
     * @param bestellung Die Bestellung deren Gesamtbetrag aktualisiert werden soll
     */
    private void aktualisiereBestellungGesamtbetrag(Bestellung bestellung) {
        List<Bestellposition> positionen = bestellpositionRepository.findByBestellungId(bestellung.getId());
       
        BigDecimal gesamtbetrag = positionen.stream()
                .map(p -> p.getEinzelpreis().multiply(BigDecimal.valueOf(p.getMenge())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        bestellung.setGesamtbetrag(gesamtbetrag);
        bestellungRepository.save(bestellung);
    }
}