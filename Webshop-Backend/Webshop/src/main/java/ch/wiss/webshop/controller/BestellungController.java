package ch.wiss.webshop.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.model.Role;
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

    /**
     * Gibt Bestellungen zurück – gefiltert nach Rolle:
     * ADMIN sieht alle, KUNDE nur seine eigenen.
     */
    @GetMapping
    public ResponseEntity<List<Bestellung>> getAllBestellungen(
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (currentUser.getRole() == Role.ADMIN) {
            // ADMIN sieht alle Bestellungen aller Kunden
            return ResponseEntity.ok(bestellungRepository.findAll());
        } else {
            // KUNDE sieht nur seine eigenen Bestellungen
            return ResponseEntity.ok(
                    bestellungRepository.findByOwnerOrderByErstelltAmDesc(currentUser));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bestellung> getBestellungById(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        Optional<Bestellung> bestellung = bestellungRepository.findById(id);
        if (bestellung.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // KUNDE darf nur seine eigene Bestellung sehen
        Bestellung b = bestellung.get();
        if (currentUser.getRole() != Role.ADMIN
                && (b.getOwner() == null || !b.getOwner().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(b);
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

    /**
     * Neue Bestellung erstellen – Owner wird automatisch auf den eingeloggten User gesetzt.
     */
    @PostMapping
    public ResponseEntity<Bestellung> createBestellung(
            @Valid @RequestBody Bestellung bestellung,
            @AuthenticationPrincipal AppUser currentUser) {

        if (bestellung.getErstelltAm() == null) {
            bestellung.setErstelltAm(LocalDateTime.now());
        }
        if (bestellung.getStatus() == null) {
            bestellung.setStatus(BestellStatus.OFFEN);
        }
        if (bestellung.getGesamtbetrag() == null) {
            bestellung.setGesamtbetrag(BigDecimal.ZERO);
        }

        // Owner setzen damit der User seine Bestellung später findet
        if (currentUser != null) {
            bestellung.setOwner(currentUser);
        }

        Bestellung savedBestellung = bestellungRepository.save(bestellung);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBestellung);
    }

    /**
     * Bestellung aktualisieren – ADMIN kann alles, KUNDE nur seine eigene.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Bestellung> updateBestellung(
            @PathVariable Long id,
            @Valid @RequestBody Bestellung bestellung,
            @AuthenticationPrincipal AppUser currentUser) {

        Optional<Bestellung> existing = bestellungRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Bestellung existingBestellung = existing.get();

        // KUNDE darf nur seine eigene Bestellung bearbeiten
        if (currentUser.getRole() != Role.ADMIN
                && (existingBestellung.getOwner() == null
                    || !existingBestellung.getOwner().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        bestellung.setId(id);
        bestellung.setOwner(existingBestellung.getOwner()); // Owner nicht überschreiben
        Bestellung updatedBestellung = bestellungRepository.save(bestellung);
        return ResponseEntity.ok(updatedBestellung);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBestellungStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Optional<Bestellung> bestellungOpt = bestellungRepository.findById(id);
        if (bestellungOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            BestellStatus bestellStatus = BestellStatus.valueOf(status.toUpperCase());
            Bestellung bestellung = bestellungOpt.get();
            bestellung.setStatus(bestellStatus);
            return ResponseEntity.ok(bestellungRepository.save(bestellung));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ungültiger Status: " + status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellung(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        Optional<Bestellung> existing = bestellungRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // KUNDE darf nur seine eigene Bestellung löschen
        Bestellung bestellung = existing.get();
        if (currentUser.getRole() != Role.ADMIN
                && (bestellung.getOwner() == null
                    || !bestellung.getOwner().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Bestellposition> positionen = bestellpositionRepository.findByBestellungId(id);
            for (Bestellposition position : positionen) {
                Produkt produkt = position.getProdukt();
                produkt.erhoeheBestand(position.getMenge());
                produktRepository.save(produkt);
            }
            bestellpositionRepository.deleteAll(positionen);
            bestellungRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Bestellung kann nicht gelöscht werden.");
        }
    }
}
