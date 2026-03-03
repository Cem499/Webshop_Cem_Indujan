package ch.wiss.webshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Repräsentiert eine Produktkategorie im Webshop.
 * Jedes Produkt gehört zu genau einer Kategorie.
 */
@Entity
@Table(name = "kategorien")
public class Kategorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 2, max = 100, message = "Name muss zwischen 2 und 100 Zeichen lang sein")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 255, message = "Beschreibung darf maximal 255 Zeichen lang sein")
    @Column(length = 255)
    private String beschreibung;

    /**
     * Standard-Konstruktor für JPA.
     */
    public Kategorie() {
    }

    /**
     * Konstruktor mit allen Parametern.
     *
     * @param id            Die eindeutige ID der Kategorie
     * @param name          Der Name der Kategorie
     * @param beschreibung  Die Beschreibung der Kategorie
     */
    public Kategorie(long id, String name, String beschreibung) {
        this.id = id;
        this.name = name;
        this.beschreibung = beschreibung;
    }

    /**
     * Konstruktor ohne ID (für neue Kategorien).
     *
     * @param name          Der Name der Kategorie
     * @param beschreibung  Die Beschreibung der Kategorie
     */
    public Kategorie(String name, String beschreibung) {
        this.name = name;
        this.beschreibung = beschreibung;
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

    @Override
    public String toString() {
        return "Kategorie [id=" + id + ", name=" + name + "]";
    }
}