package ch.wiss.webshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.wiss.webshop.model.AppUser;

/**
 * Repository für {@link AppUser}-Entitäten.
 *
 * <p>Spring Data JPA generiert automatisch SQL-Abfragen aus den Methodennamen
 * (Query Derivation). Dadurch ist kein manuelles SQL notwendig.</p>
 *
 * <p><strong>Wichtig:</strong> In diesem System wird die E-Mail als Spring-Security-
 * Username verwendet (siehe {@link AppUser#getUsername()}). Die Methoden
 * {@code findByEmail} und {@code existsByEmail} werden daher für die Authentifizierung
 * genutzt. Die {@code findByUsername}-Methoden beziehen sich auf den Anzeigenamen.</p>
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Sucht einen Benutzer anhand des Anzeigenamens (username-Feld, nicht E-Mail).
     *
     * @param username Der Anzeigename des Benutzers
     * @return Den Benutzer oder {@link Optional#empty()} wenn nicht gefunden
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Sucht einen Benutzer anhand seiner E-Mail-Adresse.
     *
     * <p>Wird vom {@link ch.wiss.webshop.service.AppUserDetailsService} zur
     * Spring-Security-Authentifizierung verwendet, da die E-Mail der Principal ist.</p>
     *
     * @param email Die E-Mail-Adresse des Benutzers
     * @return Den Benutzer oder {@link Optional#empty()} wenn nicht gefunden
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Prüft, ob ein Benutzer mit diesem Anzeigenamen existiert.
     * Wird bei der Registrierung zur Eindeutigkeitsprüfung verwendet.
     *
     * @param username Der zu prüfende Anzeigename
     * @return {@code true} wenn der Username bereits vergeben ist
     */
    boolean existsByUsername(String username);

    /**
     * Prüft, ob ein Benutzer mit dieser E-Mail existiert.
     * Wird bei der Registrierung zur Eindeutigkeitsprüfung verwendet.
     *
     * @param email Die zu prüfende E-Mail-Adresse
     * @return {@code true} wenn die E-Mail bereits registriert ist
     */
    boolean existsByEmail(String email);
}
