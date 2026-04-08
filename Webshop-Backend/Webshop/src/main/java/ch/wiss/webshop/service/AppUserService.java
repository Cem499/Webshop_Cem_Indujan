package ch.wiss.webshop.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.wiss.webshop.dto.LoginRequestDTO;
import ch.wiss.webshop.dto.LoginResponseDTO;
import ch.wiss.webshop.dto.RegisterRequestDTO;
import ch.wiss.webshop.dto.RegisterResponseDTO;
import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Role;
import ch.wiss.webshop.repository.AppUserRepository;

/**
 * Service für Benutzer-Authentifizierung, Registrierung und Verwaltung.
 *
 * <h2>Warum @Transactional?</h2>
 * <p>Die Annotation {@code @Transactional} auf Klassenebene bewirkt, dass jede Methode
 * in einer <strong>atomaren Datenbanktransaktion</strong> ausgeführt wird. Das bedeutet:</p>
 * <ul>
 *   <li><strong>Atomarität:</strong> Entweder werden ALLE Datenbankoperationen einer Methode
 *       ausgeführt, oder keine. Bei einem Fehler (z. B. Constraint-Verletzung) wird die
 *       gesamte Transaktion zurückgerollt (Rollback).</li>
 *   <li><strong>Konsistenz:</strong> Die Datenbank bleibt immer in einem konsistenten Zustand.
 *       Beispiel Registrierung: Wenn nach dem Passwort-Hashen das Speichern fehlschlägt,
 *       wird nichts in der Datenbank verändert.</li>
 *   <li><strong>Isolation:</strong> Parallele Transaktionen sehen keine halbfertigen Daten
 *       von anderen Transaktionen.</li>
 * </ul>
 * <p>Ohne {@code @Transactional} könnte z. B. bei der Registrierung folgendes passieren:
 * Das Passwort wird gehasht, aber der Datenbankfehler beim Speichern wird nicht
 * rückgängig gemacht – die Daten befänden sich in einem inkonsistenten Zustand.</p>
 */
@Service
@Transactional
public class AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // =========================================================================
    // Registrierung
    // =========================================================================

    /**
     * Registriert einen neuen Benutzer im System.
     *
     * <p><strong>Transaktions-Relevanz:</strong> Diese Methode prüft zunächst Unique-Constraints
     * (E-Mail, Username), hasht dann das Passwort und speichert den Benutzer. Dank
     * {@code @Transactional} werden alle diese Schritte atomar ausgeführt – bei einem
     * Fehler wird alles zurückgerollt.</p>
     *
     * <p>Neue Benutzer erhalten automatisch die Rolle {@code KUNDE}. Nur der
     * {@link ch.wiss.webshop.config.DataInitializer} kann ADMIN-Benutzer erstellen.</p>
     *
     * @param request Registrierungsdaten (username, email, password)
     * @return {@link RegisterResponseDTO} mit ID, Anzeigename, E-Mail, Rolle und Bestätigungsmeldung
     * @throws IllegalArgumentException wenn E-Mail oder Username bereits vergeben sind
     */
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        // Prüfen ob Username bereits vergeben
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Benutzername ist bereits vergeben: " + request.getUsername());
        }

        // Prüfen ob E-Mail bereits registriert
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "E-Mail ist bereits registriert: " + request.getEmail());
        }

        // Passwort hashen (BCrypt) – NIEMALS im Klartext speichern!
        AppUser user = new AppUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.KUNDE  // Neue Registrierungen erhalten immer KUNDE-Rolle
        );

        AppUser savedUser = appUserRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getDisplayName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Registrierung erfolgreich"
        );
    }

    /**
     * Registriert einen neuen Benutzer mit explizit angegebener Rolle.
     *
     * <p>Diese Methode wird intern (z. B. vom {@link ch.wiss.webshop.config.DataInitializer})
     * verwendet, um Test-Benutzer mit beliebigen Rollen zu erstellen.</p>
     *
     * @param username    Anzeigename (muss eindeutig sein)
     * @param email       E-Mail (muss eindeutig sein)
     * @param rawPassword Klartext-Passwort (wird BCrypt-gehasht gespeichert)
     * @param role        Gewünschte Rolle (ADMIN oder KUNDE)
     * @return Der gespeicherte {@link AppUser}
     * @throws IllegalArgumentException wenn Username oder E-Mail bereits vergeben sind
     */
    public AppUser registerUser(String username, String email, String rawPassword, Role role) {
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Benutzername bereits vergeben: " + username);
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-Mail bereits registriert: " + email);
        }

        AppUser user = new AppUser(username, email, passwordEncoder.encode(rawPassword), role);
        return appUserRepository.save(user);
    }

    // =========================================================================
    // Authentifizierung
    // =========================================================================

    /**
     * Authentifiziert einen Benutzer via Spring Security und gibt ein JWT-Token zurück.
     *
     * <p>Verwendet den {@link AuthenticationManager} von Spring Security, der intern:</p>
     * <ol>
     *   <li>Den Benutzer via {@link AppUserDetailsService} lädt</li>
     *   <li>Das Passwort via BCrypt vergleicht</li>
     *   <li>Bei falschen Credentials eine {@link BadCredentialsException} wirft</li>
     * </ol>
     *
     * @param request Login-Daten (email, password)
     * @return {@link LoginResponseDTO} mit JWT-Token und Benutzerinfos
     * @throws org.springframework.security.core.AuthenticationException bei falschen Credentials
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Spring Security übernimmt die Passwort-Prüfung (AppUserDetailsService unterstützt E-Mail und Username)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        // User per E-Mail oder Username suchen (gleiche Logik wie im AppUserDetailsService)
        AppUser user = appUserRepository.findByEmail(request.getUsernameOrEmail())
                .or(() -> appUserRepository.findByUsername(request.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalStateException(
                        "Benutzer nach Authentifizierung nicht gefunden – inkonsistenter Zustand"));

        String token = jwtService.generateToken(user);
        long expiresIn = jwtService.getJwtExpiration() / 1000; // Millisekunden → Sekunden

        return new LoginResponseDTO(
                token,
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole().name(),
                expiresIn
        );
    }

    /**
     * Authentifiziert einen Benutzer durch direkte Passwort-Prüfung (ohne AuthenticationManager).
     *
     * <p>Prüft das Klartext-Passwort gegen den BCrypt-Hash in der Datenbank.
     * Nützlich für interne Prüfungen oder Tests.</p>
     *
     * @param email       E-Mail des Benutzers
     * @param rawPassword Klartext-Passwort
     * @return Der authentifizierte {@link AppUser}
     * @throws UsernameNotFoundException wenn kein Benutzer mit dieser E-Mail gefunden wurde
     * @throws BadCredentialsException   wenn das Passwort falsch ist
     */
    public AppUser authenticateUser(String email, String rawPassword) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kein Benutzer mit E-Mail '" + email + "' gefunden"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Falsches Passwort für Benutzer: " + email);
        }

        return user;
    }

    // =========================================================================
    // Suche / Lookup
    // =========================================================================

    /**
     * Sucht einen Benutzer anhand des Anzeigenamens.
     *
     * @param username Der Anzeigename
     * @return Optional mit dem Benutzer, oder leer wenn nicht gefunden
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    /**
     * Sucht einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse
     * @return Optional mit dem Benutzer, oder leer wenn nicht gefunden
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
}
