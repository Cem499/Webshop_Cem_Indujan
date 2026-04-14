package ch.wiss.webshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.wiss.webshop.model.Kategorie;

/**
 * Repository für Kategorie-Entitäten.
 * Spring Data JPA generiert automatisch SQL aus den Methodennamen.
 */
@Repository
public interface KategorieRepository extends JpaRepository<Kategorie, Long> {

    /**
     * Findet eine Kategorie anhand ihres Namens.
     *
     * @param name Der Name der Kategorie
     * @return Optional mit der Kategorie
     */
    Optional<Kategorie> findByName(String name);
}