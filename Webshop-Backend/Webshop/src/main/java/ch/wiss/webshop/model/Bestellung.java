package ch.wiss.webshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Repräsentiert eine Bestellung im Webshop.
 * Speichert Status, Gesamtbetrag, Erstellzeit sowie Kunden- und Lieferdaten.
 */
@Entity
@Table(name = "bestellungen")
public class Bestellung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Status darf nicht null sein")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BestellStatus status;

    @NotNull(message = "Gesamtbetrag darf nicht null sein")
    @DecimalMin(value = "0.0", inclusive = true, message = "Gesamtbetrag darf nicht negativ sein")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal gesamtbetrag;

    @NotNull(message = "Erstellzeit darf nicht null sein")
    @Column(name = "erstellt_am", nullable = false)
    private LocalDateTime erstelltAm;

    @NotBlank(message = "Kundenname darf nicht leer sein")
    @Size(min = 2, max = 150, message = "Kundenname muss zwischen 2 und 150 Zeichen lang sein")
    @Column(name = "kunde_name", nullable = false, length = 150)
    private String kundenName;

    @Email(message = "Ungültige E-Mail-Adresse")
    @Size(max = 150, message = "E-Mail darf maximal 150 Zeichen lang sein")
    @Column(name = "kunde_email", length = 150)
    private String kundenEmail;

    @Size(max = 150, message = "Straße darf maximal 150 Zeichen lang sein")
    @Column(name = "liefer_strasse", length = 150)
    private String lieferStrasse;

    @Size(max = 20, message = "PLZ darf maximal 20 Zeichen lang sein")
    @Column(name = "liefer_plz", length = 20)
    private String lieferPlz;

    @Size(max = 100, message = "Stadt darf maximal 100 Zeichen lang sein")
    @Column(name = "liefer_stadt", length = 100)
    private String lieferStadt;

    @Size(max = 100, message = "Land darf maximal 100 Zeichen lang sein")
    @Column(name = "liefer_land", length = 100)
    private String lieferLand;

    // Verknüpfung mit dem eingeloggten User der die Bestellung aufgegeben hat.
    // Nullable damit bestehende Bestellungen ohne Owner weiterhin funktionieren.
    // JsonIgnore verhindert zirkuläre Referenz in der JSON-Serialisierung.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnore
    private AppUser owner;

    /**
     * Standard-Konstruktor für JPA.
     */
    public Bestellung() {
        this.erstelltAm = LocalDateTime.now();
        this.gesamtbetrag = BigDecimal.ZERO;
        this.status = BestellStatus.OFFEN;
    }

    /**
     * Konstruktor mit allen Parametern.
     *
     * @param id            Die eindeutige ID der Bestellung
     * @param status        Der Status der Bestellung
     * @param gesamtbetrag  Der Gesamtbetrag der Bestellung
     * @param erstelltAm    Der Zeitpunkt der Bestellerstellung
     * @param kundenName    Der Name des Kunden
     * @param kundenEmail   Die E-Mail des Kunden
     * @param lieferStrasse Die Lieferstraße
     * @param lieferPlz     Die Liefer-PLZ
     * @param lieferStadt   Die Lieferstadt
     * @param lieferLand    Das Lieferland
     */
    public Bestellung(long id, BestellStatus status, BigDecimal gesamtbetrag, LocalDateTime erstelltAm,
                      String kundenName, String kundenEmail, String lieferStrasse, String lieferPlz,
                      String lieferStadt, String lieferLand) {
        this.id = id;
        this.status = status;
        this.gesamtbetrag = gesamtbetrag;
        this.erstelltAm = erstelltAm;
        this.kundenName = kundenName;
        this.kundenEmail = kundenEmail;
        this.lieferStrasse = lieferStrasse;
        this.lieferPlz = lieferPlz;
        this.lieferStadt = lieferStadt;
        this.lieferLand = lieferLand;
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
    public BestellStatus getStatus() {
        return status;
    }

    /**
     * Setter-Methode
     */
    public void setStatus(BestellStatus status) {
        this.status = status;
    }

    /**
     * Getter-Methode
     */
    public BigDecimal getGesamtbetrag() {
        return gesamtbetrag;
    }

    /**
     * Setter-Methode
     */
    public void setGesamtbetrag(BigDecimal gesamtbetrag) {
        this.gesamtbetrag = gesamtbetrag;
    }

    /**
     * Getter-Methode
     */
    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    /**
     * Setter-Methode
     */
    public void setErstelltAm(LocalDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
    }

    /**
     * Getter-Methode
     */
    public String getKundenName() {
        return kundenName;
    }

    /**
     * Setter-Methode
     */
    public void setKundenName(String kundenName) {
        this.kundenName = kundenName;
    }

    /**
     * Getter-Methode
     */
    public String getKundenEmail() {
        return kundenEmail;
    }

    /**
     * Setter-Methode
     */
    public void setKundenEmail(String kundenEmail) {
        this.kundenEmail = kundenEmail;
    }

    /**
     * Getter-Methode
     */
    public String getLieferStrasse() {
        return lieferStrasse;
    }

    /**
     * Setter-Methode
     */
    public void setLieferStrasse(String lieferStrasse) {
        this.lieferStrasse = lieferStrasse;
    }

    /**
     * Getter-Methode
     */
    public String getLieferPlz() {
        return lieferPlz;
    }

    /**
     * Setter-Methode
     */
    public void setLieferPlz(String lieferPlz) {
        this.lieferPlz = lieferPlz;
    }

    /**
     * Getter-Methode
     */
    public String getLieferStadt() {
        return lieferStadt;
    }

    /**
     * Setter-Methode
     */
    public void setLieferStadt(String lieferStadt) {
        this.lieferStadt = lieferStadt;
    }

    /**
     * Getter-Methode
     */
    public String getLieferLand() {
        return lieferLand;
    }

    /**
     * Setter-Methode
     */
    public void setLieferLand(String lieferLand) {
        this.lieferLand = lieferLand;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Bestellung [id=" + id + ", status=" + status + 
               ", gesamtbetrag=" + gesamtbetrag + ", kundenName=" + kundenName + "]";
    }

    /**
     * Enum für den Bestellstatus.
     */
    public enum BestellStatus {
        OFFEN("offen"),
        BEZAHLT("bezahlt"),
        STORNIERT("storniert");

        private final String wert;

        BestellStatus(String wert) {
            this.wert = wert;
        }

        public String getWert() {
            return wert;
        }
    }
}