package ch.wiss.webshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import ch.wiss.webshop.dto.LoginRequestDTO;
import ch.wiss.webshop.dto.LoginResponseDTO;
import ch.wiss.webshop.dto.RegisterRequestDTO;
import ch.wiss.webshop.dto.RegisterResponseDTO;
import ch.wiss.webshop.model.AppUser;
import ch.wiss.webshop.model.Role;
import ch.wiss.webshop.repository.AppUserRepository;

/**
 * Unit-Tests für AppUserService.
 * Verwendet Mockito um Repository, PasswordEncoder, JwtService und AuthenticationManager zu isolieren.
 */
@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser testUser;

    /** Initialisiert einen Test-User vor jedem Test. */
    @BeforeEach
    void setUp() {
        testUser = new AppUser("MaxMuster", "max@test.ch", "hashedPw", Role.KUNDE);
        testUser.setId(1L);
    }

    // Registrierung Tests

    /**
     * Test: Registrierung mit gültigen Daten → RegisterResponseDTO mit korrekter E-Mail, Rolle KUNDE und Bestätigung.
     */
    @Test
    void testRegister_Erfolgreich() {
        System.out.println("=== Test: Registrierung erfolgreich ===");

        RegisterRequestDTO request = new RegisterRequestDTO("MaxMuster", "max@test.ch", "passwort123");

        when(appUserRepository.existsByEmail("max@test.ch")).thenReturn(false);
        when(passwordEncoder.encode("passwort123")).thenReturn("hashedPw");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(testUser);

        RegisterResponseDTO response = appUserService.register(request);

        System.out.println("Response: " + response.getMessage() + " | Rolle: " + response.getRole());

        assertNotNull(response);
        assertEquals("max@test.ch", response.getEmail());
        assertEquals("KUNDE", response.getRole());
        assertEquals("Registrierung erfolgreich", response.getMessage());

        verify(appUserRepository).save(any(AppUser.class));
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: Registrierung mit bereits vergebener E-Mail → IllegalArgumentException mit E-Mail im Text.
     */
    @Test
    void testRegister_EmailBereitsVergeben_WirftException() {
        System.out.println("=== Test: Registrierung - E-Mail bereits vergeben ===");

        RegisterRequestDTO request = new RegisterRequestDTO("MaxMuster", "max@test.ch", "passwort123");
        when(appUserRepository.existsByEmail("max@test.ch")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appUserService.register(request));

        System.out.println("Exception: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("max@test.ch"));
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: Neue Benutzer bekommen immer die Rolle KUNDE, unabhängig von den Eingabedaten.
     */
    @Test
    void testRegister_RolleIstImmerKUNDE() {
        System.out.println("=== Test: Neue Benutzer bekommen immer Rolle KUNDE ===");

        RegisterRequestDTO request = new RegisterRequestDTO("Admin", "admin@test.ch", "passwort123");
        when(appUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPw");

        AppUser savedUser = new AppUser("Admin", "admin@test.ch", "hashedPw", Role.KUNDE);
        savedUser.setId(2L);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

        RegisterResponseDTO response = appUserService.register(request);

        assertEquals("KUNDE", response.getRole());
        System.out.println("Rolle: " + response.getRole() + " (erwartet: KUNDE)");
        System.out.println("Test bestanden: true");
    }

    // --- Login Tests ---

    /**
     * Test: Login mit korrekten Daten → LoginResponseDTO mit JWT-Token, E-Mail und Rolle KUNDE.
     */
    @Test
    void testLogin_Erfolgreich() {
        System.out.println("=== Test: Login erfolgreich ===");

        LoginRequestDTO request = new LoginRequestDTO("max@test.ch", "passwort123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(appUserRepository.findByEmail("max@test.ch")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("mock.jwt.token");

        LoginResponseDTO response = appUserService.login(request);

        System.out.println("Token erhalten: " + (response.getToken() != null));
        System.out.println("Email: " + response.getEmail());

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("max@test.ch", response.getEmail());
        assertEquals("KUNDE", response.getRole());
        System.out.println("Test bestanden: true");
    }

    /**
     * Test: Login mit falschem Passwort → BadCredentialsException vom AuthenticationManager.
     */
    @Test
    void testLogin_FalschesPasswort_WirftException() {
        System.out.println("=== Test: Login mit falschem Passwort ===");

        LoginRequestDTO request = new LoginRequestDTO("max@test.ch", "falschesPasswort");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> appUserService.login(request));

        System.out.println("Exception korrekt geworfen");
        System.out.println("Test bestanden: true");
    }
}
