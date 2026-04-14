package ch.wiss.webshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.wiss.webshop.model.Produkt;

/**
 * Repository für Produkt-Entitäten.
 * Spring Data JPA generiert automatisch SQL aus den Methodennamen.
 */
@Repository
public interface ProduktRepository extends JpaRepository<Produkt, Long> {

    /**
     * Findet alle Produkte einer bestimmten Kategorie.
     *
     * @param kategorieId Die ID der Kategorie
     * @return Liste der Produkte
     */
    List<Produkt> findByKategorieId(Long kategorieId);

    /**
     * Findet alle Produkte mit Bestand größer als 0.
     *
     * @return Liste der verfügbaren Produkte
     */
    List<Produkt> findByBestandGreaterThan(int bestand);

    /**
     * Sucht Produkte nach Name (case-insensitive, teilweise Übereinstimmung).
     *
     * @param name Der Suchbegriff
     * @return Liste der gefundenen Produkte
     */
    List<Produkt> findByNameContainingIgnoreCase(String name);
}