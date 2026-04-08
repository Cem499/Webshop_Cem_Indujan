package ch.wiss.webshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Bestellung.BestellStatus;

@Repository
public interface BestellungRepository extends JpaRepository<Bestellung, Long> {
    
    /**
     * Findet alle Bestellungen mit einem bestimmten Status.
     *
     * @param status Der Bestellstatus
     * @return Liste der Bestellungen
     */
    List<Bestellung> findByStatus(BestellStatus status);
    
    /**
     * Findet alle Bestellungen eines Kunden.
     *
     * @param kundenName Der Name des Kunden
     * @return Liste der Bestellungen
     */
    List<Bestellung> findByKundenName(String kundenName);
    
    /**
     * Findet alle Bestellungen eines Kunden sortiert nach Erstelldatum (neueste zuerst).
     *
     * @param kundenEmail Die E-Mail des Kunden
     * @return Liste der Bestellungen
     */
    List<Bestellung> findByKundenEmailOrderByErstelltAmDesc(String kundenEmail);

    /**
     * Findet alle Bestellungen eines bestimmten Users (für KUNDE-Ansicht).
     *
     * @param owner Der eingeloggte User
     * @return Nur die eigenen Bestellungen des Users
     */
    List<Bestellung> findByOwnerOrderByErstelltAmDesc(AppUser owner);
}