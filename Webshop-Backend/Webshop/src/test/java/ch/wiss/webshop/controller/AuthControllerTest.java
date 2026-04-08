package ch.wiss.webshop.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integrations-Tests für den AuthController.
 *
 * <p>Verwendet H2 In-Memory Datenbank (via {@code @TestPropertySource}) und
 * MockMvc für HTTP-Requests ohne echten Netzwerk-Stack.</p>
 *
 * <p>{@code @DirtiesContext} stellt sicher, dass der Spring-Kontext nach dieser
 * Testklasse zurückgesetzt wird, damit die H2-Datenbank geleert wird und keine
 * Daten zwischen Testklassen interferieren.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:authcontrollertest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    // Hibernate-Dialect explizit auf H2 setzen – überschreibt den MySQLDialect aus application.properties
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
    "jwt.expiration=86400000",
    "logging.level.org.springframework.security=WARN",
    "logging.level.org.hibernate.SQL=WARN"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Hilfsmethoden
    // =========================================================================

    /** Erstellt einen JSON-String für Registrierungs-Requests. */
    private String registerJson(String username, String email, String password) {
        return String.format(
            "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
            username, email, password
        );
    }

    /** Erstellt einen JSON-String für Login-Requests. */
    private String loginJson(String email, String password) {
        return String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
    }

    // =========================================================================
    // Registrierungs-Tests
    // =========================================================================

    /**
     * Test: Registrierung mit gültigen Daten → HTTP 201 Created.
     *
     * <p>Erwartet, dass ein neuer Benutzer erfolgreich erstellt wird und
     * die Antwort die Benutzerinfos mit Rolle KUNDE enthält.</p>
     */
    @Test
    void register_mitGueltigenDaten_returns201() throws Exception {
        System.out.println("=== Test: Registrierung mit gültigen Daten ===");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("testuser", "testuser@test.ch", "passwort123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@test.ch"))
                .andExpect(jsonPath("$.role").value("KUNDE"))
                .andExpect(jsonPath("$.message").value("Registrierung erfolgreich"));

        System.out.println("Test bestanden: HTTP 201, Rolle KUNDE");
    }

    /**
     * Test: Registrierung mit doppeltem Username → HTTP 400 Bad Request.
     *
     * <p>Zuerst wird ein Benutzer registriert. Ein zweiter Registrierungsversuch
     * mit dem gleichen Username (aber anderer E-Mail) muss mit 400 abgewiesen werden.
     * Der {@link ch.wiss.webshop.exception.GlobalExceptionHandler} wandelt die
     * {@code IllegalArgumentException} des Services in HTTP 400 um.</p>
     */
    @Test
    void register_mitDoppeltemUsername_returns400() throws Exception {
        System.out.println("=== Test: Registrierung mit doppeltem Username ===");

        // Ersten Benutzer registrieren
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("duplikatuser", "erster@test.ch", "passwort123")))
                .andExpect(status().isCreated());

        System.out.println("Erster Benutzer registriert.");

        // Zweiten Benutzer mit gleichem Username (anderer E-Mail) versuchen
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("duplikatuser", "zweiter@test.ch", "passwort123")))
                .andExpect(status().isBadRequest());

        System.out.println("Test bestanden: HTTP 400 bei doppeltem Username");
    }

    // =========================================================================
    // Login-Tests
    // =========================================================================

    /**
     * Test: Login mit falschem Passwort → HTTP 401 Unauthorized.
     *
     * <p>Der DataInitializer erstellt beim Start den Benutzer "admin@webshop.ch".
     * Ein Login mit diesem Benutzer aber falschem Passwort muss 401 zurückgeben.</p>
     */
    @Test
    void login_mitFalschemPasswort_returns401() throws Exception {
        System.out.println("=== Test: Login mit falschem Passwort ===");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson("admin@webshop.ch", "falschesPasswort")))
                .andExpect(status().isUnauthorized());

        System.out.println("Test bestanden: HTTP 401 bei falschem Passwort");
    }

    /**
     * Test: Login mit korrekten Daten → HTTP 200 mit Token und Benutzerdata.
     *
     * <p>Der DataInitializer erstellt beim Start den Admin-Benutzer.
     * Login mit korrekten Credentials muss ein JWT-Token und Benutzerinfos zurückgeben.</p>
     */
    @Test
    void login_mitKorrektenDaten_returnsTokenUndUserData() throws Exception {
        System.out.println("=== Test: Login mit korrekten Daten ===");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson("admin@webshop.ch", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("admin@webshop.ch"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.expiresIn").isNumber());

        System.out.println("Test bestanden: HTTP 200 mit JWT-Token und Benutzerdata");
    }
}
