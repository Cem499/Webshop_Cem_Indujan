package ch.wiss.webshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Role;
import ch.wiss.webshop.repository.AppUserRepository;

/**
 * Initialisiert Test-Benutzer beim Start der Anwendung.
 *
 * <p>
 * Implementiert {@link CommandLineRunner}, sodass Spring Boot diese Klasse
 * automatisch nach dem vollständigen Starten des Anwendungskontexts ausführt.
 * Prüft, ob die Standard-Benutzer bereits existieren, und legt sie an falls
 * nicht.
 * </p>
 *
 * <h2>Test-Benutzer</h2>
 * <table border="1">
 * <tr>
 * <th>Rolle</th>
 * <th>Username</th>
 * <th>E-Mail</th>
 * <th>Passwort</th>
 * </tr>
 * <tr>
 * <td>ADMIN</td>
 * <td>admin</td>
 * <td>admin@webshop.ch</td>
 * <td>admin123</td>
 * </tr>
 * <tr>
 * <td>KUNDE</td>
 * <td>kunde</td>
 * <td>kunde@webshop.ch</td>
 * <td>kunde123</td>
 * </tr>
 * </table>
 *
 * <p>
 * Passwörter werden BCrypt-gehasht gespeichert. Diese Klasse läuft sowohl in
 * der
 * Produktion als auch in den Tests (mit H2-Datenbank).
 * </p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Wird beim Anwendungsstart ausgeführt.
     * Erstellt Test-Benutzer, falls sie noch nicht existieren.
     *
     * @param args Kommandozeilenargumente (werden nicht verwendet)
     */
    @Override
    public void run(String... args) {
        log.info("DataInitializer: Prüfe Test-Benutzer...");

        createUserIfNotExists(
                "admin",
                "admin@webshop.ch",
                "admin123",
                Role.ADMIN);

        createUserIfNotExists(
                "kunde",
                "kunde@webshop.ch",
                "kunde123",
                Role.KUNDE);

        log.info("DataInitializer: Initialisierung abgeschlossen.");
    }

    /**
     * Erstellt einen Benutzer, wenn er noch nicht existiert.
     *
     * <p>
     * Prüft anhand der E-Mail, ob der Benutzer bereits in der Datenbank vorhanden
     * ist.
     * Falls ja, wird er übersprungen (kein Update). Falls nein, wird er mit BCrypt-
     * gehashtem Passwort erstellt.
     * </p>
     *
     * @param username    Anzeigename des Benutzers
     * @param email       E-Mail-Adresse (muss eindeutig sein)
     * @param rawPassword Klartext-Passwort (wird BCrypt-gehasht gespeichert)
     * @param role        Rolle (ADMIN oder KUNDE)
     */
    private void createUserIfNotExists(String username, String email,
            String rawPassword, Role role) {
        if (!appUserRepository.existsByEmail(email)) {
            AppUser user = new AppUser(
                    username,
                    email,
                    passwordEncoder.encode(rawPassword),
                    role);
            appUserRepository.save(user);
            log.info("DataInitializer: Benutzer '{}' ({}) wurde erstellt.", username, role);
        } else {
            log.info("DataInitializer: Benutzer '{}' existiert bereits – wird übersprungen.", email);
        }
    }
}
