package ch.wiss.webshop.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ch.wiss.webshop.dto.LoginRequestDTO;
import ch.wiss.webshop.dto.LoginResponseDTO;
import ch.wiss.webshop.dto.RegisterRequestDTO;
import ch.wiss.webshop.dto.RegisterResponseDTO;
import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.repository.AppUserRepository;
import ch.wiss.webshop.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST-Controller für Authentifizierung.
 * Endpoints: POST /api/auth/register, POST /api/auth/login, GET /api/auth/me
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentifizierung", description = "Login, Registrierung und eigene Benutzerdaten")
public class AuthController {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * Registriert einen neuen Benutzer mit der Rolle KUNDE.
     *
     * @param request Registrierungsdaten (username, email, password)
     * @return HTTP 201 mit RegisterResponseDTO
     */
    @PostMapping("/register")
    @Operation(summary = "Neuen Benutzer registrieren (Rolle: KUNDE)")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        RegisterResponseDTO response = appUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Meldet einen Benutzer an und gibt ein JWT Token zurück.
     *
     * @param request Login-Daten (email, password)
     * @return HTTP 200 mit LoginResponseDTO oder HTTP 401 bei falschen Zugangsdaten
     */
    @PostMapping("/login")
    @Operation(summary = "Benutzer anmelden und JWT Token erhalten")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = appUserService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Bei falschen Credentials: 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Ungültige E-Mail oder Passwort"));
        }
    }

    /**
     * Gibt die Daten des aktuell eingeloggten Benutzers zurück.
     *
     * @param principal Spring Security Principal (E-Mail des eingeloggten Benutzers)
     * @return HTTP 200 mit Benutzerdaten oder HTTP 401 wenn nicht eingeloggt
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Eigene Benutzerdaten abrufen",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> me(Principal principal) {
        AppUser user = appUserRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Benutzer '" + principal.getName() + "' im SecurityContext nicht in DB gefunden"));

        RegisterResponseDTO response = new RegisterResponseDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole().name(),
                "Eingeloggt als " + user.getDisplayName()
        );
        return ResponseEntity.ok(response);
    }
}
