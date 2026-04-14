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

/**
 * REST-Controller für Bestellungen.
 * Basis-URL: /api/bestellungen
 * KUNDE sieht nur eigene Bestellungen, ADMIN sieht alle.
 */
@RestController
@RequestMapping(path = "/api/bestellungen")
public class BestellungController {

    @Autowired
    private BestellungService bestellungService;

    /**
     * Prüft ob der aktuelle User Besitzer der Bestellung oder Admin ist.
     *
     * @param user  Der eingeloggte User
     * @param owner Der Besitzer der Bestellung (kann null sein)
     * @return true wenn Admin oder Besitzer
     */
    private boolean isOwnerOrAdmin(AppUser user, AppUser owner) {
        return user.getRole() == Role.ADMIN
                || (owner != null && owner.getId().equals(user.getId()));
    }

    /**
     * Gibt alle Bestellungen zurück.
     * ADMIN sieht alle, KUNDE nur seine eigenen.
     *
     * @param currentUser Der eingeloggte User (via JWT)
     * @return HTTP 200 mit Bestellungen oder HTTP 401 wenn nicht eingeloggt
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

    /**
     * Gibt nur die eigenen Bestellungen des eingeloggten Users zurück.
     *
     * @param currentUser Der eingeloggte User (via JWT)
     * @return HTTP 200 mit eigenen Bestellungen oder HTTP 401 wenn nicht eingeloggt
     */
    @GetMapping("/meine")
    public ResponseEntity<List<Bestellung>> getMeineBestellungen(
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bestellungService.findByOwner(currentUser));
    }

    /**
     * Gibt eine Bestellung anhand ihrer ID zurück.
     * KUNDE kann nur eigene Bestellungen abrufen.
     *
     * @param id          Die ID der Bestellung
     * @param currentUser Der eingeloggte User (via JWT)
     * @return HTTP 200 mit der Bestellung, HTTP 401, HTTP 403 oder HTTP 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bestellung> getBestellungById(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Bestellung b = bestellungService.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        if (!isOwnerOrAdmin(currentUser, b.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(b);
    }

    /**
     * Gibt alle Bestellungen mit einem bestimmten Status zurück. Nur für ADMIN.
     *
     * @param status Der Bestellstatus als String (OFFEN, BEZAHLT, STORNIERT)
     * @return HTTP 200 mit Bestellungen oder HTTP 400 bei ungültigem Status
     */
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

    /**
     * Gibt alle Bestellungen eines Kunden nach Name zurück. Nur für ADMIN.
     *
     * @param kundenName Der Name des Kunden
     * @return HTTP 200 mit Bestellungen (kann leer sein)
     */
    @GetMapping("/kunde/{kundenName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Bestellung>> getBestellungenByKunde(@PathVariable String kundenName) {
        return ResponseEntity.ok(bestellungService.findByKundenName(kundenName));
    }

    /**
     * Erstellt eine neue Bestellung für den eingeloggten User.
     * Status und Erstellzeit werden automatisch gesetzt.
     *
     * @param bestellung  Die neue Bestellung
     * @param currentUser Der eingeloggte User (wird als Owner gesetzt)
     * @return HTTP 201 mit der erstellten Bestellung
     */
    @PostMapping
    public ResponseEntity<Bestellung> createBestellung(
            @Valid @RequestBody Bestellung bestellung,
            @AuthenticationPrincipal AppUser currentUser) {

        Bestellung saved = bestellungService.create(bestellung, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Aktualisiert eine Bestellung vollständig.
     * KUNDE kann nur eigene Bestellungen ändern.
     *
     * @param id          Die ID der Bestellung
     * @param bestellung  Die neuen Bestelldaten
     * @param currentUser Der eingeloggte User (via JWT)
     * @return HTTP 200 mit aktualisierter Bestellung, HTTP 401, HTTP 403 oder HTTP 404
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
        if (existing == null) return ResponseEntity.notFound().build();
        if (!isOwnerOrAdmin(currentUser, existing.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return bestellungService.update(id, bestellung)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Ändert den Status einer Bestellung.
     * KUNDE darf nur auf STORNIERT setzen, ADMIN kann jeden Status setzen.
     *
     * @param id          Die ID der Bestellung
     * @param status      Der neue Status als String
     * @param currentUser Der eingeloggte User (via JWT)
     * @return HTTP 200 mit aktualisierter Bestellung, HTTP 400 bei ungültigem Status, HTTP 401, HTTP 403 oder HTTP 404
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBestellungStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Bestellung bestellung = bestellungService.findById(id).orElse(null);
        if (bestellung == null) return ResponseEntity.notFound().build();
        if (!isOwnerOrAdmin(currentUser, bestellung.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (currentUser.getRole() != Role.ADMIN && !status.equalsIgnoreCase(BestellStatus.STORNIERT.name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return bestellungService.updateStatus(id, status)
                    .map(updated -> ResponseEntity.ok().body((Object) updated))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Ungültiger Status: " + status);
        }
    }

    /**
     * Löscht eine Bestellung anhand ihrer ID.
     * KUNDE kann nur eigene Bestellungen löschen.
     *
     * @param id          Die ID der zu löschenden Bestellung
     * @param currentUser Der eingeloggte User (via JWT)
     * @return HTTP 204 bei Erfolg, HTTP 401, HTTP 403, HTTP 404 oder HTTP 409
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBestellung(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Bestellung bestellung = bestellungService.findById(id).orElse(null);
        if (bestellung == null) return ResponseEntity.notFound().build();
        if (!isOwnerOrAdmin(currentUser, bestellung.getOwner())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            bestellungService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Bestellung kann nicht gelöscht werden.");
        }
    }
}
