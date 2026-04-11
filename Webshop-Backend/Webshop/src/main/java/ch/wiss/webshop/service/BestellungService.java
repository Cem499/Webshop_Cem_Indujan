package ch.wiss.webshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.model.Bestellung.BestellStatus;
import ch.wiss.webshop.repository.BestellpositionRepository;
import ch.wiss.webshop.repository.BestellungRepository;
import ch.wiss.webshop.repository.ProduktRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class BestellungService {

    @Autowired
    private BestellungRepository bestellungRepository;

    @Autowired
    private BestellpositionRepository bestellpositionRepository;

    @Autowired
    private ProduktRepository produktRepository;

    public List<Bestellung> findAll() {
        return bestellungRepository.findAll();
    }

    public List<Bestellung> findByOwner(AppUser owner) {
        return bestellungRepository.findByOwnerOrderByErstelltAmDesc(owner);
    }

    public Optional<Bestellung> findById(Long id) {
        return bestellungRepository.findById(id);
    }

    public List<Bestellung> findByStatus(BestellStatus status) {
        return bestellungRepository.findByStatus(status);
    }

    public List<Bestellung> findByKundenName(String kundenName) {
        return bestellungRepository.findByKundenName(kundenName);
    }

    /**
     * Erstellt eine neue Bestellung und setzt Standardwerte sowie den Owner.
     */
    public Bestellung create(Bestellung bestellung, AppUser currentUser) {
        if (bestellung.getErstelltAm() == null) {
            bestellung.setErstelltAm(LocalDateTime.now());
        }
        if (bestellung.getStatus() == null) {
            bestellung.setStatus(BestellStatus.OFFEN);
        }
        if (bestellung.getGesamtbetrag() == null) {
            bestellung.setGesamtbetrag(BigDecimal.ZERO);
        }
        if (currentUser != null) {
            bestellung.setOwner(currentUser);
        }
        return bestellungRepository.save(bestellung);
    }

    /**
     * Aktualisiert eine bestehende Bestellung. Owner bleibt unverändert.
     * Gibt Optional.empty() zurück wenn die Bestellung nicht existiert.
     */
    public Optional<Bestellung> update(Long id, Bestellung bestellung) {
        Optional<Bestellung> existing = bestellungRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        bestellung.setId(id);
        bestellung.setOwner(existing.get().getOwner());
        return Optional.of(bestellungRepository.save(bestellung));
    }

    /**
     * Ändert den Status einer Bestellung.
     * Gibt Optional.empty() zurück wenn die Bestellung nicht existiert.
     * Wirft IllegalArgumentException bei ungültigem Status-String.
     */
    public Optional<Bestellung> updateStatus(Long id, String status) {
        Optional<Bestellung> bestellungOpt = bestellungRepository.findById(id);
        if (bestellungOpt.isEmpty()) {
            return Optional.empty();
        }
        BestellStatus bestellStatus = BestellStatus.valueOf(status.toUpperCase());
        Bestellung bestellung = bestellungOpt.get();
        bestellung.setStatus(bestellStatus);
        return Optional.of(bestellungRepository.save(bestellung));
    }

    /**
     * Löscht eine Bestellung und stellt den Produktbestand aller Positionen wieder
     * her.
     * Gibt false zurück wenn die Bestellung nicht existiert.
     */
    public boolean delete(Long id) {
        Optional<Bestellung> existing = bestellungRepository.findById(id);
        if (existing.isEmpty()) {
            return false;
        }
        List<Bestellposition> positionen = bestellpositionRepository.findByBestellungId(id);
        for (Bestellposition position : positionen) {
            Produkt produkt = position.getProdukt();
            produkt.erhoeheBestand(position.getMenge());
            produktRepository.save(produkt);
        }
        bestellpositionRepository.deleteAll(positionen);
        bestellungRepository.deleteById(id);
        return true;
    }
}
