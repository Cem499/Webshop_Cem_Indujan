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
 * Alle Methoden laufen in einer Datenbanktransaktion.
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

    /**
     * Registriert einen neuen Benutzer mit der Rolle KUNDE.
     * Das Passwort wird BCrypt-gehasht gespeichert.
     *
     * @param request Registrierungsdaten (username, email, password)
     * @return RegisterResponseDTO mit ID, Name, E-Mail, Rolle und Bestätigung
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

        // Passwort hashen – niemals im Klartext speichern
        AppUser user = new AppUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.KUNDE
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
     * Registriert einen Benutzer mit einer bestimmten Rolle.
     * Wird intern z.B. vom DataInitializer für Testbenutzer verwendet.
     *
     * @param username    Anzeigename
     * @param email       E-Mail-Adresse
     * @param rawPassword Klartext-Passwort wird BCrypt-gehasht gespeichert
     * @param role        Rolle (ADMIN oder KUNDE)
     * @return Der gespeicherte AppUser
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

    /**
     * Authentifiziert einen Benutzer und gibt ein JWT Token zurück.
     *
     * @param request Login-Daten (email, password)
     * @return LoginResponseDTO mit Token und Benutzerinfos
     * @throws org.springframework.security.core.AuthenticationException bei falschen Zugangsdaten
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Spring Security prüft E-Mail und Passwort via AppUserDetailsService
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        // User per E-Mail oder Username suchen
        AppUser user = appUserRepository.findByEmail(request.getUsernameOrEmail())
                .or(() -> appUserRepository.findByUsername(request.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalStateException(
                        "Benutzer nach Authentifizierung nicht gefunden – inkonsistenter Zustand"));

        String token = jwtService.generateToken(user);
        long expiresIn = jwtService.getJwtExpiration() / 1000; // Millisekunden in Sekunden

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
     * Prüft E-Mail und Passwort direkt gegen die Datenbank.
     * Nützlich für interne Prüfungen oder Tests.
     *
     * @param email       E-Mail des Benutzers
     * @param rawPassword Klartext-Passwort
     * @return Der authentifizierte AppUser
     * @throws UsernameNotFoundException wenn kein Benutzer mit dieser E-Mail gefunden wurde
     * @throws BadCredentialsException wenn das Passwort falsch ist
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

    /**
     * Sucht einen Benutzer anhand des Anzeigenamens.
     *
     * @param username Der Anzeigename
     * @return Optional mit dem Benutzer oder leer wenn nicht gefunden
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    /**
     * Sucht einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse
     * @return Optional mit dem Benutzer oder leer wenn nicht gefunden
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
}
