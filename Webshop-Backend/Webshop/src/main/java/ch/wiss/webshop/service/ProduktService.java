package ch.wiss.webshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.model.Kategorie;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.repository.KategorieRepository;
import ch.wiss.webshop.repository.ProduktRepository;
import jakarta.transaction.Transactional;

/**
 * Service für Produkt-Geschäftslogik.
 * Kapselt alle Datenbankzugriffe für den ProduktController.
 */
@Service
@Transactional
public class ProduktService {

    @Autowired
    private ProduktRepository produktRepository;

    @Autowired
    private KategorieRepository kategorieRepository;

    /**
     * Gibt alle Produkte zurück.
     *
     * @return Liste aller Produkte
     */
    public List<Produkt> findAll() {
        return produktRepository.findAll();
    }

    /**
     * Sucht ein Produkt anhand seiner ID.
     *
     * @param id Die ID des Produkts
     * @return Optional mit dem Produkt oder leer wenn nicht gefunden
     */
    public Optional<Produkt> findById(Long id) {
        return produktRepository.findById(id);
    }

    /**
     * Prüft ob ein Produkt mit dieser ID existiert.
     *
     * @param id Die zu prüfende ID
     * @return true wenn das Produkt existiert
     */
    public boolean existsById(Long id) {
        return produktRepository.existsById(id);
    }

    /**
     * Prüft ob eine Kategorie mit dieser ID existiert.
     *
     * @param kategorieId Die zu prüfende Kategorie-ID
     * @return true wenn die Kategorie existiert
     */
    public boolean kategorieExistiertById(Long kategorieId) {
        return kategorieRepository.existsById(kategorieId);
    }

    /**
     * Gibt alle Produkte einer bestimmten Kategorie zurück.
     *
     * @param kategorieId Die ID der Kategorie
     * @return Liste der Produkte in dieser Kategorie
     */
    public List<Produkt> findByKategorieId(Long kategorieId) {
        return produktRepository.findByKategorieId(kategorieId);
    }

    /**
     * Sucht Produkte nach Name (case-insensitive, Teilübereinstimmung).
     *
     * @param name Der Suchbegriff
     * @return Liste der gefundenen Produkte
     */
    public List<Produkt> sucheByName(String name) {
        return produktRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Gibt alle Produkte mit Bestand größer als 0 zurück.
     *
     * @return Liste der verfügbaren Produkte
     */
    public List<Produkt> findVerfuegbare() {
        return produktRepository.findByBestandGreaterThan(0);
    }

    /**
     * Speichert ein Produkt. Setzt die vollständige Kategorie-Entität anhand der
     * ID.
     * Gibt Optional.empty() zurück wenn die Kategorie nicht existiert.
     */
    public Optional<Produkt> save(Produkt produkt) {
        Optional<Kategorie> kategorie = kategorieRepository.findById(produkt.getKategorie().getId());
        if (kategorie.isEmpty()) {
            return Optional.empty();
        }
        produkt.setKategorie(kategorie.get());
        return Optional.of(produktRepository.save(produkt));
    }

    /**
     * Aktualisiert den Bestand eines Produkts.
     * Gibt Optional.empty() zurück wenn das Produkt nicht existiert.
     */
    public Optional<Produkt> updateBestand(Long id, int bestand) {
        Optional<Produkt> produktOpt = produktRepository.findById(id);
        if (produktOpt.isEmpty()) {
            return Optional.empty();
        }
        Produkt produkt = produktOpt.get();
        produkt.setBestand(bestand);
        return Optional.of(produktRepository.save(produkt));
    }

    public void deleteById(Long id) {
        produktRepository.deleteById(id);
    }
}
