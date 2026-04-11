package ch.wiss.webshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.model.Kategorie;
import ch.wiss.webshop.repository.KategorieRepository;
import jakarta.transaction.Transactional;

/**
 * Service für Kategorien.
 * Kapselt die Repository-Aufrufe für den KategorieController.
 */
@Service
@Transactional
public class KategorieService {

    @Autowired
    private KategorieRepository kategorieRepository;

    /**
     * Gibt alle Kategorien zurück.
     *
     * @return Liste aller Kategorien
     */
    public List<Kategorie> findAll() {
        return kategorieRepository.findAll();
    }

    /**
     * Sucht eine Kategorie anhand ihrer ID.
     *
     * @param id Die ID der Kategorie
     * @return Optional mit der Kategorie oder leer wenn nicht gefunden
     */
    public Optional<Kategorie> findById(Long id) {
        return kategorieRepository.findById(id);
    }

    /**
     * Sucht eine Kategorie anhand ihres Namens.
     *
     * @param name Der Name der Kategorie
     * @return Optional mit der Kategorie oder leer wenn nicht gefunden
     */
    public Optional<Kategorie> findByName(String name) {
        return kategorieRepository.findByName(name);
    }

    /**
     * Prüft ob eine Kategorie mit diesem Namen bereits existiert.
     *
     * @param name Der zu prüfende Name
     * @return true wenn eine Kategorie mit diesem Namen existiert
     */
    public boolean existsByName(String name) {
        return kategorieRepository.findByName(name).isPresent();
    }

    /**
     * Speichert eine neue oder aktualisiert eine bestehende Kategorie.
     *
     * @param kategorie Die zu speichernde Kategorie
     * @return Die gespeicherte Kategorie mit generierter ID
     */
    public Kategorie save(Kategorie kategorie) {
        return kategorieRepository.save(kategorie);
    }

    /**
     * Löscht eine Kategorie anhand ihrer ID.
     *
     * @param id Die ID der zu löschenden Kategorie
     */
    public void deleteById(Long id) {
        kategorieRepository.deleteById(id);
    }
}
