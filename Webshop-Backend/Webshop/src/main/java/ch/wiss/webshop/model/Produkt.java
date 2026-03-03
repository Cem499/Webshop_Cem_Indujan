package ch.wiss.webshop.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Repräsentiert ein Produkt im Webshop.
 * Jedes Produkt gehört zu genau einer Kategorie.
 */
@Entity
@Table(name = "produkte")
public class Produkt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 150, message = "Name muss zwischen 2 und 150 Zeichen lang sein")
    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String beschreibung;

    @NotNull(message = "Preis darf nicht null sein")
    @DecimalMin(value = "0.01", message = "Preis muss größer als 0 sein")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preis;

    @Min(value = 0, message = "Bestand darf nicht negativ sein")
    @Column(nullable = false)
    private int bestand;

    @NotNull(message = "Kategorie darf nicht null sein")
    @ManyToOne
    @JoinColumn(name = "kategorie_id", nullable = false)
    private Kategorie kategorie;

    /**
     * Standard-Konstruktor für JPA.
     */
    public Produkt() {
    }

    /**
     * Konstruktor mit allen Parametern.
     *
     * @param id           Die eindeutige ID des Produkts
     * @param name         Der Name des Produkts
     * @param beschreibung Die Beschreibung des Produkts
     * @param preis        Der Preis des Produkts
     * @param bestand      Der verfügbare Bestand
     * @param kategorie    Die zugehörige Kategorie
     */
    public Produkt(long id, String name, String beschreibung, BigDecimal preis, int bestand, Kategorie kategorie) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
        this.preis = preis;
        this.bestand = bestand;
        this.kategorie = kategorie;
    }

    /**
     * Konstruktor ohne ID (für neue Produkte).
     *
     * @param name         Der Name des Produkts
     * @param beschreibung Die Beschreibung des Produkts
     * @param preis        Der Preis des Produkts
     * @param bestand      Der verfügbare Bestand
     * @param kategorie    Die zugehörige Kategorie
     */
    public Produkt(String name, String beschreibung, BigDecimal preis, int bestand, Kategorie kategorie) {
        this.name = name;
        this.beschreibung = beschreibung;
        this.preis = preis;
        this.bestand = bestand;
        this.kategorie = kategorie;
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
    public String getName() {
        return name;
    }

    /**
     * Setter-Methode
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter-Methode
     */
    public String getBeschreibung() {
        return beschreibung;
    }

    /**
     * Setter-Methode
     */
    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    /**
     * Getter-Methode
     */
    public BigDecimal getPreis() {
        return preis;
    }

    /**
     * Setter-Methode
     */
    public void setPreis(BigDecimal preis) {
        this.preis = preis;
    }

    /**
     * Getter-Methode
     */
    public int getBestand() {
        return bestand;
    }

    /**
     * Setter-Methode
     */
    public void setBestand(int bestand) {
        this.bestand = bestand;
    }

    /**
     * Getter-Methode
     */
    public Kategorie getKategorie() {
        return kategorie;
    }

    /**
     * Setter-Methode
     */
    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

    /**
     * Prüft ob genügend Bestand für die angeforderte Menge vorhanden ist.
     *
     * @param menge Die angeforderte Menge
     * @return true wenn genügend Bestand vorhanden ist, sonst false
     */
    public boolean pruefeBestand(int menge) {
        return menge <= this.bestand && menge > 0;
    }

    /**
     * Reduziert den Bestand um die angegebene Menge.
     *
     * @param menge Die zu reduzierende Menge
     * @throws IllegalArgumentException wenn nicht genügend Bestand vorhanden ist
     */
    public void reduziereBestand(int menge) {
        if (!pruefeBestand(menge)) {
            throw new IllegalArgumentException("Nicht genügend Bestand für Produkt: " + this.name + 
                " (verfügbar: " + this.bestand + ", angefordert: " + menge + ")");
        }
        this.bestand = this.bestand - menge;
    }

    /**
     * Erhöht den Bestand um die angegebene Menge.
     *
     * @param menge Die zu erhöhende Menge
     */
    public void erhoeheBestand(int menge) {
        if (menge > 0) {
        	this.bestand = this.bestand + menge;
        } 
    }

    
    @Override
    public String toString() {
        return "Produkt [id=" + id + ", name=" + name + ", preis=" + preis + 
               ", bestand=" + bestand + ", kategorie=" + kategorie.getName() + "]";
    }
}