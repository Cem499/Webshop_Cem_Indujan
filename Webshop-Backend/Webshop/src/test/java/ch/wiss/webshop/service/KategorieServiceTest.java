package ch.wiss.webshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.wiss.webshop.model.Kategorie;
import ch.wiss.webshop.repository.KategorieRepository;

/**
 * Unit-Tests für KategorieService.
 * Verwendet Mockito um das Repository zu isolieren – kein Datenbankzugriff.
 */
@ExtendWith(MockitoExtension.class)
class KategorieServiceTest {

    @Mock
    private KategorieRepository kategorieRepository;

    @InjectMocks
    private KategorieService kategorieService;

    /**
     * Test: findAll gibt alle Kategorien aus dem Repository zurück.
     * Erwartet eine Liste mit genau 2 Einträgen.
     */
    @Test
    void testFindAll_GibtAlleKategorienZurueck() {
        System.out.println("=== Test: Alle Kategorien abrufen ===");

        Kategorie k1 = new Kategorie(1L, "Elektronik", "Elektronische Geräte");
        Kategorie k2 = new Kategorie(2L, "Kleidung", "Mode und Bekleidung");
        when(kategorieRepository.findAll()).thenReturn(Arrays.asList(k1, k2));

        List<Kategorie> result = kategorieService.findAll();

        System.out.println("Anzahl Kategorien: " + result.size() + " (erwartet: 2)");
        assertEquals(2, result.size());
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: findById gibt die Kategorie zurück wenn sie existiert.
     * Erwartet Optional mit Name "Elektronik".
     */
    @Test
    void testFindById_GefundeneKategorie() {
        System.out.println("=== Test: Kategorie nach ID suchen - gefunden ===");

        Kategorie k = new Kategorie(1L, "Elektronik", "Elektronische Geräte");
        when(kategorieRepository.findById(1L)).thenReturn(Optional.of(k));

        Optional<Kategorie> result = kategorieService.findById(1L);

        System.out.println("Gefunden: " + result.isPresent() + ", Name: " + result.get().getName());
        assertTrue(result.isPresent());
        assertEquals("Elektronik", result.get().getName());
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: findById gibt leeres Optional zurück wenn keine Kategorie mit dieser ID existiert.
     */
    @Test
    void testFindById_NichtGefunden() {
        System.out.println("=== Test: Kategorie nach ID suchen - nicht gefunden ===");

        when(kategorieRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Kategorie> result = kategorieService.findById(99L);

        System.out.println("Gefunden: " + result.isPresent() + " (erwartet: false)");
        assertFalse(result.isPresent());
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: save speichert eine Kategorie und gibt sie mit generierter ID zurück.
     * Erwartet ID 3 und Name "Sport".
     */
    @Test
    void testSave_SpeichertKategorie() {
        System.out.println("=== Test: Kategorie speichern ===");

        Kategorie neuKategorie = new Kategorie("Sport", "Sportartikel");
        Kategorie savedKategorie = new Kategorie(3L, "Sport", "Sportartikel");
        when(kategorieRepository.save(any(Kategorie.class))).thenReturn(savedKategorie);

        Kategorie result = kategorieService.save(neuKategorie);

        System.out.println("Gespeichert mit ID: " + result.getId() + ", Name: " + result.getName());
        assertEquals(3L, result.getId());
        assertEquals("Sport", result.getName());
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: existsByName gibt true zurück wenn eine Kategorie mit diesem Namen existiert.
     */
    @Test
    void testExistsByName_Vorhanden() {
        System.out.println("=== Test: Kategorie nach Name prüfen - vorhanden ===");

        Kategorie k = new Kategorie(1L, "Elektronik", "Test");
        when(kategorieRepository.findByName("Elektronik")).thenReturn(Optional.of(k));

        boolean exists = kategorieService.existsByName("Elektronik");

        System.out.println("Existiert: " + exists + " (erwartet: true)");
        assertTrue(exists);
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: deleteById delegiert den Aufruf an das Repository.
     * Prüft ob deleteById(1L) genau einmal aufgerufen wurde.
     */
    @Test
    void testDeleteById_RuftRepositoryAuf() {
        System.out.println("=== Test: Kategorie löschen ===");

        doNothing().when(kategorieRepository).deleteById(1L);

        kategorieService.deleteById(1L);

        verify(kategorieRepository, times(1)).deleteById(1L);
        System.out.println("deleteById(1L) wurde aufgerufen");
        System.out.println("Test bestanden: true");
    }
}
