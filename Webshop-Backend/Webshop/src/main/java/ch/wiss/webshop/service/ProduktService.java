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

@Service
@Transactional
public class ProduktService {

    @Autowired
    private ProduktRepository produktRepository;

    @Autowired
    private KategorieRepository kategorieRepository;

    public List<Produkt> findAll() {
        return produktRepository.findAll();
    }

    public Optional<Produkt> findById(Long id) {
        return produktRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return produktRepository.findById(id).isPresent();
    }

    public boolean kategorieExistiertById(Long kategorieId) {
        return kategorieRepository.findById(kategorieId).isPresent();
    }

    public List<Produkt> findByKategorieId(Long kategorieId) {
        return produktRepository.findByKategorieId(kategorieId);
    }

    public List<Produkt> sucheByName(String name) {
        return produktRepository.findByNameContainingIgnoreCase(name);
    }

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
