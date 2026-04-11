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
 * Erstellt Testbenutzer beim App-Start falls sie noch nicht existieren.
 * Admin: admin@webshop.ch / admin123
 * Kunde: kunde@webshop.ch / kunde123
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Wird beim App-Start ausgeführt und erstellt die Testbenutzer.
     *
     * @param args Kommandozeilenargumente (nicht verwendet)
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
     * Erstellt einen Benutzer wenn er noch nicht existiert.
     *
     * @param username    Anzeigename
     * @param email       E-Mail-Adresse
     * @param rawPassword Klartext-Passwort wird BCrypt-gehasht gespeichert
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
