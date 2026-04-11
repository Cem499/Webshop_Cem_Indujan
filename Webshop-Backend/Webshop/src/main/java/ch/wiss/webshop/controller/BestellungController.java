package ch.wiss.webshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Role;
import ch.wiss.webshop.model.Bestellung.BestellStatus;
import ch.wiss.webshop.service.BestellungService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/bestellungen")
public class BestellungController {

    @Autowired
    private BestellungService bestellungService;

    /**
     * Gibt Bestellungen zurück – ADMIN sieht alle, KUNDE nur seine eigenen.
     */
    @GetMapping
    public ResponseEntity<List<Bestellung>> getAllBestellungen(
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(bestellungService.findAll());
        }
        return ResponseEntity.ok(bestellungService.findByOwner(currentUser));
    }

    @GetMapping("/meine")
    public ResponseEntity<List<Bestellung>> getMeineBestellungen(
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bestellungService.findByOwner(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bestellung> getBestellungById(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Bestellung b = bestellungService.findById(id).orElse(null);
        if (b == null)
            return ResponseEntity.notFound().build();
        if (currentUser.getRole() != Role.ADMIN
                && (b.getOwner() == null || !b.getOwner().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(b);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Bestellung>> getBestellungenByStatus(@PathVariable String status) {
        try {
            BestellStatus bestellStatus = BestellStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(bestellungService.findByStatus(bestellStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/kunde/{kundenName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Bestellung>> getBestellungenByKunde(@PathVariable String kundenName) {
        return ResponseEntity.ok(bestellungService.findByKundenName(kundenName));
    }

    @PostMapping
    public ResponseEntity<Bestellung> createBestellung(
            @Valid @RequestBody Bestellung bestellung,
            @AuthenticationPrincipal AppUser currentUser) {

        Bestellung saved = bestellungService.create(bestellung, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Bestellung aktualisieren – ADMIN kann alles, KUNDE nur seine eigene.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Bestellung> updateBestellung(
            @PathVariable Long id,
            @Valid @RequestBody Bestellung bestellung,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Bestellung existing = bestellungService.findById(id).orElse(null);
        if (existing == null)
            return ResponseEntity.notFound().build();
        if (currentUser.getRole() != Role.ADMIN
                && (existing.getOwner() == null
                        || !existing.getOwner().getId().equals(currentUser.getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return bestellungService.update(id, bestellung)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBestellungStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return bestellungService.findById(id).map(bestellung -> {
            if (currentUser.getRole() != Role.ADMIN) {
                if (bestellung.getOwner() == null || !bestellung.getOwner().getId().equals(currentUser.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                if (!status.equalsIgnoreCase("STORNIERT")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            try {
                return bestellungService.updateStatus(id, status)
                        .map(updated -> ResponseEntity.ok().body((Object) updated))
                        .orElseGet(() -> ResponseEntity.notFound().build());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Ungültiger Status: " + status);
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellung(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return bestellungService.findById(id).map(bestellung -> {
            if (currentUser.getRole() != Role.ADMIN
                    && (bestellung.getOwner() == null
                            || !bestellung.getOwner().getId().equals(currentUser.getId()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            try {
                bestellungService.delete(id);
                return ResponseEntity.noContent().build();
            } catch (DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Bestellung kann nicht gelöscht werden.");
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
