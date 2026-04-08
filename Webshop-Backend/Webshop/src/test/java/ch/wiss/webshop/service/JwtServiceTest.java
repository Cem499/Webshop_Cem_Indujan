package ch.wiss.webshop.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Role;

/**
 * Unit/Integration-Tests für den {@link JwtService}.
 *
 * <p>Prüft die korrekte Erstellung, Validierung und Analyse von JWT-Tokens.
 * Verwendet H2 In-Memory Datenbank, damit keine echte MySQL-Verbindung benötigt wird.</p>
 */
@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
    "app.jwt.expiration=86400000",
    "spring.datasource.url=jdbc:h2:mem:jwtservicetest",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser("MaxMuster", "max@test.ch", "hashedPw", Role.KUNDE);
        testUser.setId(1L);
    }

    // =========================================================================
    // Ursprüngliche Tests (behalten)
    // =========================================================================

    @Test
    void testGenerateToken_NichtNull() {
        System.out.println("=== Test: Token generieren ===");

        String token = jwtService.generateToken(testUser);

        System.out.println("Token generiert: " + (token != null && !token.isEmpty()));
        assertNotNull(token);
        assertFalse(token.isEmpty());
        System.out.println("Test bestanden: true");
    }

    @Test
    void testExtractUsername_GibtEmailZurueck() {
        System.out.println("=== Test: Username aus Token extrahieren ===");

        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        System.out.println("Extrahierter Username: " + username);
        System.out.println("Erwarteter Username: max@test.ch");

        assertEquals("max@test.ch", username);
        System.out.println("Test bestanden: true");
    }

    @Test
    void testIsTokenValid_GueltigesToken_ReturnsTrue() {
        System.out.println("=== Test: Gültiges Token validieren ===");

        String token = jwtService.generateToken(testUser);
        boolean valid = jwtService.isTokenValid(token, testUser);

        System.out.println("Token gültig: " + valid);
        assertTrue(valid);
        System.out.println("Test bestanden: true");
    }

    @Test
    void testIsTokenValid_FalscherBenutzer_ReturnsFalse() {
        System.out.println("=== Test: Token für anderen Benutzer ungültig ===");

        String token = jwtService.generateToken(testUser);

        AppUser andererUser = new AppUser("Hans", "hans@test.ch", "pw", Role.KUNDE);
        andererUser.setId(2L);

        boolean valid = jwtService.isTokenValid(token, andererUser);

        System.out.println("Token gültig für anderen User: " + valid + " (erwartet: false)");
        assertFalse(valid);
        System.out.println("Test bestanden: true");
    }

    // =========================================================================
    // Pflicht-Tests mit spezifischen Methodennamen (Bewertungspunkt)
    // =========================================================================

    /**
     * Test: Token generieren und Username korrekt extrahieren.
     *
     * <p>Prüft, dass der Subject-Claim im Token die E-Mail des Benutzers enthält,
     * da {@link AppUser#getUsername()} die E-Mail zurückgibt.</p>
     */
    @Test
    void generateToken_extractUsername_returnsCorrectUsername() {
        System.out.println("=== Pflicht-Test: generateToken → extractUsername ===");

        String token = jwtService.generateToken(testUser);
        String extractedUsername = jwtService.extractUsername(token);

        System.out.println("Generiertes Token: " + token.substring(0, 20) + "...");
        System.out.println("Extrahierter Username: " + extractedUsername);
        System.out.println("Erwarteter Username: max@test.ch (E-Mail als Spring-Security-Principal)");

        assertEquals("max@test.ch", extractedUsername,
                "Der Subject-Claim soll die E-Mail des Benutzers enthalten");
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: Gültiges Token wird als gültig erkannt.
     *
     * <p>Ein frisch generiertes Token für einen Benutzer soll bei der Validierung
     * mit demselben Benutzer als gültig erkannt werden.</p>
     */
    @Test
    void validateToken_mitGueltigemToken_returnsTrue() {
        System.out.println("=== Pflicht-Test: validateToken mit gültigem Token ===");

        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);

        System.out.println("Token gültig: " + isValid + " (erwartet: true)");
        assertTrue(isValid, "Ein frisch generiertes Token muss gültig sein");
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: Token mit falschem Benutzernamen wird als ungültig erkannt.
     *
     * <p>Ein Token, das für Benutzer A generiert wurde, soll bei der Validierung
     * mit Benutzer B (anderer Username) als ungültig zurückgegeben werden.</p>
     */
    @Test
    void validateToken_mitFalschemUsername_returnsFalse() {
        System.out.println("=== Pflicht-Test: validateToken mit falschem Username ===");

        // Token für testUser generieren
        String token = jwtService.generateToken(testUser);

        // Validierung mit einem anderen User (anderer Username/E-Mail)
        AppUser andererUser = new AppUser("AnderesPerson", "andere@test.ch", "pw", Role.ADMIN);
        andererUser.setId(99L);

        boolean isValid = jwtService.isTokenValid(token, andererUser);

        System.out.println("Token gültig für anderen User: " + isValid + " (erwartet: false)");
        assertFalse(isValid,
                "Ein Token soll für einen anderen Benutzer als ungültig gelten");
        System.out.println("Test bestanden: true");
    }
}
