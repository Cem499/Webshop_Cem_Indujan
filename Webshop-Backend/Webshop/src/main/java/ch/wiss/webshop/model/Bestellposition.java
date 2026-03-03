package ch.wiss.webshop.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Repräsentiert eine Position innerhalb einer Bestellung.
 * Verknüpft Bestellungen mit Produkten und hält Menge und Einzelpreis fest.
 */
@Entity
@Table(name = "bestellpositionen")
public class Bestellposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Bestellung darf nicht null sein")
    @ManyToOne
    @JoinColumn(name = "bestellung_id", nullable = false)
    private Bestellung bestellung;

    @NotNull(message = "Produkt darf nicht null sein")
    @ManyToOne
    @JoinColumn(name = "produkt_id", nullable = false)
    private Produkt produkt;

    @Min(value = 1, message = "Menge muss mindestens 1 sein")
    private int menge;

    @DecimalMin(value = "0.01", message = "Einzelpreis muss größer als 0 sein")
    private BigDecimal einzelpreis;

    /**
     * Standard-Konstruktor für JPA.
     */
    public Bestellposition() {
    }

    /**
     * Konstruktor mit allen Parametern.
     *
     * @param id          Die eindeutige ID der Bestellposition
     * @param bestellung  Die zugehörige Bestellung
     * @param produkt     Das bestellte Produkt
     * @param menge       Die bestellte Menge
     * @param einzelpreis Der Einzelpreis zum Zeitpunkt der Bestellung
     */
    public Bestellposition(long id, Bestellung bestellung, Produkt produkt, int menge, BigDecimal einzelpreis) {
        this.id = id;
        this.bestellung = bestellung;
        this.produkt = produkt;
        this.menge = menge;
        this.einzelpreis = einzelpreis;
    }

    /**
     * Getter-Methode
     */
    public long getId() {
        return id;
    }

    /**
     * Setter-Methode
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Getter-Methode
     */
    public Bestellung getBestellung() {
        return bestellung;
    }

    /**
     * Setter-Methode
     */
    public void setBestellung(Bestellung bestellung) {
        this.bestellung = bestellung;
    }

    /**
     * Getter-Methode
     */
    public Produkt getProdukt() {
        return produkt;
    }

    /**
     * Setter-Methode
     */
    public void setProdukt(Produkt produkt) {
        this.produkt = produkt;
    }

    /**
     * Getter-Methode
     */
    public int getMenge() {
        return menge;
    }

    /**
     * Setter-Methode
     */
    public void setMenge(int menge) {
        this.menge = menge;
    }

    /**
     * Getter-Methode
     */
    public BigDecimal getEinzelpreis() {
        return einzelpreis;
    }

    /**
     * Setter-Methode
     */
    public void setEinzelpreis(BigDecimal einzelpreis) {
        this.einzelpreis = einzelpreis;
    }

    @Override
    public String toString() {
        return "Bestellposition [id=" + id + ", bestellung=" + bestellung.getId() +
               ", produkt=" + produkt.getName() + ", menge=" + menge +
               ", einzelpreis=" + einzelpreis + "]";
    }
}