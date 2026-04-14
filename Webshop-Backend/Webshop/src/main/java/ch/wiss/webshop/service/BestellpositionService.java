package ch.wiss.webshop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.model.Bestellung;
import ch.wiss.webshop.model.Bestellposition;
import ch.wiss.webshop.model.Produkt;
import ch.wiss.webshop.repository.BestellpositionRepository;
import ch.wiss.webshop.repository.BestellungRepository;
import ch.wiss.webshop.repository.ProduktRepository;
import jakarta.transaction.Transactional;

/**
 * Service für Bestellpositions-Geschäftslogik.
 * Verwaltet Bestandsänderungen beim Erstellen, Ändern und Löschen von Positionen
 * und hält den Gesamtbetrag der Bestellung aktuell.
 */
@Service
@Transactional
public class BestellpositionService {

    @Autowired
    private BestellpositionRepository bestellpositionRepository;

    @Autowired
    private BestellungRepository bestellungRepository;

    @Autowired
    private ProduktRepository produktRepository;

    /**
     * Gibt alle Bestellpositionen zurück.
     *
     * @return Liste aller Bestellpositionen
     */
    public List<Bestellposition> findAll() {
        return bestellpositionRepository.findAll();
    }

    /**
     * Sucht eine Bestellposition anhand ihrer ID.
     *
     * @param id Die ID der Bestellposition
     * @return Optional mit der Position oder leer wenn nicht gefunden
     */
    public Optional<Bestellposition> findById(Long id) {
        return bestellpositionRepository.findById(id);
    }

    /**
     * Prüft ob eine Bestellung mit dieser ID existiert.
     *
     * @param bestellungId Die zu prüfende Bestell-ID
     * @return true wenn die Bestellung existiert
     */
    public boolean bestellungExistiertById(Long bestellungId) {
        return bestellungRepository.existsById(bestellungId);
    }

    /**
     * Gibt alle Positionen einer bestimmten Bestellung zurück.
     *
     * @param bestellungId Die ID der Bestellung
     * @return Liste der Positionen dieser Bestellung
     */
    public List<Bestellposition> findByBestellungId(Long bestellungId) {
        return bestellpositionRepository.findByBestellungId(bestellungId);
    }

    /**
     * Erstellt eine neue Bestellposition:
     * - Prüft ob Bestellung und Produkt existieren
     * - Prüft und reduziert den Bestand
     * - Setzt den Einzelpreis automatisch vom Produkt
     * - Aktualisiert den Gesamtbetrag der Bestellung
     *
     * Gibt Optional.empty() zurück wenn Bestellung oder Produkt nicht gefunden.
     * Wirft IllegalStateException wenn der Bestand nicht ausreicht.
     */
    public Optional<Bestellposition> create(Bestellposition position) {
        Optional<Bestellung> bestellung = bestellungRepository.findById(position.getBestellung().getId());
        if (bestellung.isEmpty()) {
            return Optional.empty();
        }

        Optional<Produkt> produkt = produktRepository.findById(position.getProdukt().getId());
        if (produkt.isEmpty()) {
            return Optional.empty();
        }

        if (!produkt.get().pruefeBestand(position.getMenge())) {
            throw new IllegalStateException("Nicht genügend Bestand verfügbar");
        }

        produkt.get().reduziereBestand(position.getMenge());
        produktRepository.save(produkt.get());

        position.setEinzelpreis(produkt.get().getPreis());
        position.setBestellung(bestellung.get());
        position.setProdukt(produkt.get());

        Bestellposition savedPosition = bestellpositionRepository.save(position);
        aktualisiereGesamtbetrag(bestellung.get());

        return Optional.of(savedPosition);
    }

    /**
     * Aktualisiert eine Bestellposition:
     * - Passt den Bestand entsprechend der Mengendifferenz an
     * - Aktualisiert den Gesamtbetrag der Bestellung
     *
     * Gibt Optional.empty() zurück wenn Position, Bestellung oder Produkt nicht
     * gefunden.
     * Wirft IllegalStateException wenn der Bestand nicht ausreicht.
     */
    public Optional<Bestellposition> update(Long id, Bestellposition position) {
        Optional<Bestellposition> existingOpt = bestellpositionRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<Bestellung> bestellung = bestellungRepository.findById(position.getBestellung().getId());
        if (bestellung.isEmpty()) {
            return Optional.empty();
        }

        Optional<Produkt> produkt = produktRepository.findById(position.getProdukt().getId());
        if (produkt.isEmpty()) {
            return Optional.empty();
        }

        int mengeDifferenz = position.getMenge() - existingOpt.get().getMenge();
        if (mengeDifferenz > 0) {
            if (!produkt.get().pruefeBestand(mengeDifferenz)) {
                throw new IllegalStateException("Nicht genügend Bestand verfügbar");
            }
            produkt.get().reduziereBestand(mengeDifferenz);
        } else if (mengeDifferenz < 0) {
            produkt.get().erhoeheBestand(Math.abs(mengeDifferenz));
        }
        produktRepository.save(produkt.get());

        position.setEinzelpreis(produkt.get().getPreis());
        position.setId(id);
        position.setBestellung(bestellung.get());
        position.setProdukt(produkt.get());

        Bestellposition updatedPosition = bestellpositionRepository.save(position);
        aktualisiereGesamtbetrag(bestellung.get());

        return Optional.of(updatedPosition);
    }

    /**
     * Löscht eine Bestellposition, stellt den Bestand wieder her
     * und aktualisiert den Gesamtbetrag der Bestellung.
     * Gibt false zurück wenn die Position nicht existiert.
     */
    public boolean delete(Long id) {
        Optional<Bestellposition> positionOpt = bestellpositionRepository.findById(id);
        if (positionOpt.isEmpty()) {
            return false;
        }

        Bestellposition position = positionOpt.get();
        Produkt produkt = position.getProdukt();
        produkt.erhoeheBestand(position.getMenge());
        produktRepository.save(produkt);

        Bestellung bestellung = position.getBestellung();
        bestellpositionRepository.deleteById(id);
        aktualisiereGesamtbetrag(bestellung);

        return true;
    }

    /**
     * Berechnet den Gesamtbetrag der Bestellung neu aus allen aktuellen Positionen
     * und speichert das Ergebnis.
     *
     * @param bestellung Die Bestellung deren Gesamtbetrag aktualisiert werden soll
     */
    private void aktualisiereGesamtbetrag(Bestellung bestellung) {
        List<Bestellposition> positionen = bestellpositionRepository.findByBestellungId(bestellung.getId());
        BigDecimal gesamtbetrag = positionen.stream()
                .map(p -> p.getEinzelpreis().multiply(BigDecimal.valueOf(p.getMenge())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        bestellung.setGesamtbetrag(gesamtbetrag);
        bestellungRepository.save(bestellung);
    }
}
