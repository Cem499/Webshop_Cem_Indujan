package ch.wiss.webshop.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Produkt-Klasse.
 * Testet wichtige Business-Logik-Methoden mit detaillierten Ausgaben.
 */
class ProduktTest {

    /**
     * Test 1: Bestand prüfen - genügend vorhanden
     */
    @Test
    void testPruefeBestand_GenugVorhanden_ReturnsTrue() {
        // Arrange
        Kategorie kategorie = new Kategorie("Elektronik", "Test");
        Produkt produkt = new Produkt("Laptop", "Test Laptop",
                                     new BigDecimal("999.99"), 10, kategorie);

        // Act
        boolean result = produkt.pruefeBestand(5);

        // Console-Ausgabe
        System.out.println("=== Test 1: Bestand prüfen - genügend vorhanden ===");
        System.out.println("Aktueller Bestand: " + produkt.getBestand());
        System.out.println("Angeforderte Menge: 5");
        System.out.println("Ergebnis pruefeBestand(5): " + result);
        System.out.println("Erwartetes Ergebnis: true");
        System.out.println("Test bestanden: " + (result == true));
        System.out.println();

        // Assert
        assertTrue(result, "Bestand sollte ausreichen für 5 Stück");
    }

    /**
     * Test 2: Bestand prüfen - nicht genug vorhanden
     */
    @Test
    void testPruefeBestand_NichtGenug_ReturnsFalse() {
        // Arrange
        Kategorie kategorie = new Kategorie("Elektronik", "Test");
        Produkt produkt = new Produkt("Laptop", "Test Laptop",
                                     new BigDecimal("999.99"), 5, kategorie);

        // Act
        boolean result = produkt.pruefeBestand(10);

        // Console-Ausgabe
        System.out.println("=== Test 2: Bestand prüfen - nicht genug vorhanden ===");
        System.out.println("Aktueller Bestand: " + produkt.getBestand());
        System.out.println("Angeforderte Menge: 10");
        System.out.println("Ergebnis pruefeBestand(10): " + result);
        System.out.println("Erwartetes Ergebnis: false");
        System.out.println("Test bestanden: " + (result == false));
        System.out.println();

        // Assert
        assertFalse(result, "Bestand sollte nicht ausreichen für 10 Stück");
    }

    /**
     * Test 3: Bestand reduzieren - erfolgreich
     */
    @Test
    void testReduziereBestand_Erfolgreich() {
        // Arrange
        Kategorie kategorie = new Kategorie("Elektronik", "Test");
        Produkt produkt = new Produkt("Laptop", "Test Laptop",
                                     new BigDecimal("999.99"), 10, kategorie);

        // Console-Ausgabe vor Reduktion
        System.out.println("=== Test 3: Bestand reduzieren - erfolgreich ===");
        System.out.println("Bestand vorher: " + produkt.getBestand());
        System.out.println("Reduziere um: 3");

        // Act
        produkt.reduziereBestand(3);

        // Console-Ausgabe nach Reduktion
        System.out.println("Bestand nachher: " + produkt.getBestand());
        System.out.println("Erwarteter Bestand: 7");
        System.out.println("Test bestanden: " + (produkt.getBestand() == 7));
        System.out.println();

        // Assert
        assertEquals(7, produkt.getBestand(), "Bestand sollte von 10 auf 7 reduziert werden");
    }

    /**
     * Test 4: Bestand erhöhen
     */
    @Test
    void testErhoeheBestand() {
        // Arrange
        Kategorie kategorie = new Kategorie("Elektronik", "Test");
        Produkt produkt = new Produkt("Laptop", "Test Laptop",
                                     new BigDecimal("999.99"), 10, kategorie);

        // Console-Ausgabe vor Erhöhung
        System.out.println("=== Test 4: Bestand erhöhen ===");
        System.out.println("Bestand vorher: " + produkt.getBestand());
        System.out.println("Erhöhe um: 5");

        // Act
        produkt.erhoeheBestand(5);

        // Console-Ausgabe nach Erhöhung
        System.out.println("Bestand nachher: " + produkt.getBestand());
        System.out.println("Erwarteter Bestand: 15");
        System.out.println("Test bestanden: " + (produkt.getBestand() == 15));
        System.out.println();

        // Assert
        assertEquals(15, produkt.getBestand(), "Bestand sollte von 10 auf 15 erhöht werden");
    }

    /**
     * Test 5: Bestand reduzieren - Exception bei zu wenig Bestand
     */
    @Test
    void testReduziereBestand_ZuWenig_WirftException() {
        // Arrange
        Kategorie kategorie = new Kategorie("Elektronik", "Test");
        Produkt produkt = new Produkt("Laptop", "Test Laptop",
                                     new BigDecimal("999.99"), 5, kategorie);

        // Console-Ausgabe
        System.out.println("=== Test 5: Bestand reduzieren - Exception bei zu wenig Bestand ===");
        System.out.println("Aktueller Bestand: " + produkt.getBestand());
        System.out.println("Versuche zu reduzieren um: 10");
        System.out.println("Erwartetes Verhalten: IllegalArgumentException wird geworfen");

        // Act & Assert
        try {
            produkt.reduziereBestand(10);
            System.out.println("FEHLER: Keine Exception geworfen!");
            System.out.println("Test bestanden: false");
        } catch (IllegalArgumentException e) {
            System.out.println("Exception erfolgreich geworfen: " + e.getMessage());
            System.out.println("Test bestanden: true");
        }
        System.out.println();

        assertThrows(IllegalArgumentException.class, () -> {
            produkt.reduziereBestand(10);
        }, "Sollte Exception werfen wenn nicht genug Bestand vorhanden");
    }
}