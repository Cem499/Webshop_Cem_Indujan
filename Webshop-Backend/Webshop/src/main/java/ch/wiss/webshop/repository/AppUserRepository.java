package ch.wiss.webshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.wiss.webshop.model.AppUser;

/**
 * Repository für AppUser-Entitäten.
 * Spring Data JPA generiert automatisch SQL aus den Methodennamen.
 * Als Spring Security Username wird die E-Mail verwendet, nicht der
 * Anzeigename.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Sucht einen Benutzer anhand des Anzeigenamens.
     *
     * @param username Der Anzeigename
     * @return Den Benutzer oder leer wenn nicht gefunden
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Sucht einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse
     * @return Den Benutzer oder leer wenn nicht gefunden
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Prüft ob ein Benutzer mit diesem Anzeigenamen existiert.
     *
     * @param username Der zu prüfende Anzeigename
     * @return true wenn der Username bereits vergeben ist
     */
    boolean existsByUsername(String username);

    /**
     * Prüft ob ein Benutzer mit dieser E-Mail existiert.
     *
     * @param email Die zu prüfende E-Mail-Adresse
     * @return true wenn die E-Mail bereits registriert ist
     */
    boolean existsByEmail(String email);
}
