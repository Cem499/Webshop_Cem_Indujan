package ch.wiss.webshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.wiss.webshop.model.Bestellposition;

@Repository
public interface BestellpositionRepository extends JpaRepository<Bestellposition, Long> {
    
    /**
     * Findet alle Bestellpositionen einer bestimmten Bestellung.
     *
     * @param bestellungId Die ID der Bestellung
     * @return Liste der Bestellpositionen
     */
    List<Bestellposition> findByBestellungId(Long bestellungId);
    
    /**
     * Findet alle Bestellpositionen eines bestimmten Produkts.
     *
     * @param produktId Die ID des Produkts
     * @return Liste der Bestellpositionen
     */
    List<Bestellposition> findByProduktId(Long produktId);
}