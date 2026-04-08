package ch.wiss.webshop.controller;

import java.security.Principal;

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
 *
 * <p>Stellt folgende Endpoints bereit:</p>
 * <ul>
 *   <li>{@code POST /api/auth/register} – Neuen Benutzer registrieren (Rolle: KUNDE)</li>
 *   <li>{@code POST /api/auth/login}    – Einloggen und JWT-Token erhalten</li>
 *   <li>{@code GET  /api/auth/me}       – Daten des aktuell eingeloggten Benutzers abrufen</li>
 * </ul>
 *
 * <p>Alle {@code /api/auth/**}-Pfade sind in der {@link ch.wiss.webshop.config.SecurityConfig}
 * als {@code permitAll()} konfiguriert. Das bedeutet, sie sind ohne Token erreichbar.
 * Der {@code /me}-Endpoint erzwingt jedoch Authentifizierung via {@code @PreAuthorize}.</p>
 *
 * <p>Fehlerbehandlung:</p>
 * <ul>
 *   <li>Duplikat-Username / E-Mail → {@code IllegalArgumentException} →
 *       {@link ch.wiss.webshop.exception.GlobalExceptionHandler} → HTTP 400</li>
 *   <li>Falsches Passwort → {@code AuthenticationException} → HTTP 401</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentifizierung", description = "Login, Registrierung und eigene Benutzerdaten")
public class AuthController {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    // =========================================================================
    // POST /api/auth/register
    // =========================================================================

    /**
     * Registriert einen neuen Benutzer mit der Rolle KUNDE.
     *
     * <p>Bei Duplikat-Username oder Duplikat-E-Mail wird eine
     * {@code IllegalArgumentException} geworfen, die vom
     * {@link ch.wiss.webshop.exception.GlobalExceptionHandler} als HTTP 400 behandelt wird.
     * Kein try-catch hier – der GlobalExceptionHandler übernimmt die Fehlerbehandlung.</p>
     *
     * @param request Registrierungsdaten (username, email, password) – mit @Valid validiert
     * @return HTTP 201 mit {@link RegisterResponseDTO}
     */
    @PostMapping("/register")
    @Operation(summary = "Neuen Benutzer registrieren (Rolle: KUNDE)")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        RegisterResponseDTO response = appUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // POST /api/auth/login
    // =========================================================================

    /**
     * Meldet einen Benutzer an und gibt ein JWT-Token zurück.
     *
     * <p>Spring Security's {@code AuthenticationManager} prüft E-Mail + Passwort.
     * Bei Erfolg generiert der {@link ch.wiss.webshop.service.JwtService} ein Token,
     * das im {@code Authorization: Bearer <token>} Header für weitere Requests
     * mitgesendet werden muss.</p>
     *
     * @param request Login-Daten (email, password)
     * @return HTTP 200 mit {@link LoginResponseDTO} (Token + Benutzerinfos),
     *         oder HTTP 401 bei falschen Credentials
     */
    @PostMapping("/login")
    @Operation(summary = "Benutzer anmelden und JWT-Token erhalten")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = appUserService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Bei falschen Credentials: 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Ungültige E-Mail oder Passwort");
        }
    }

    // =========================================================================
    // GET /api/auth/me
    // =========================================================================

    /**
     * Gibt die Daten des aktuell eingeloggten Benutzers zurück.
     *
     * <p>Der {@link ch.wiss.webshop.security.JwtAuthenticationFilter} extrahiert die E-Mail
     * aus dem JWT-Token und setzt sie als {@link Principal}. Mit dieser E-Mail wird der
     * Benutzer aus der Datenbank geladen.</p>
     *
     * <p>{@code @PreAuthorize("isAuthenticated()")} stellt sicher, dass dieser Endpoint
     * nur mit gültigem JWT-Token erreichbar ist – auch wenn {@code /api/auth/**} in der
     * SecurityConfig als {@code permitAll()} markiert ist.</p>
     *
     * @param principal Spring Security Principal (E-Mail des eingeloggten Benutzers)
     * @return HTTP 200 mit {@link RegisterResponseDTO} (Benutzerinfos ohne Token),
     *         oder HTTP 401 wenn nicht eingeloggt
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
