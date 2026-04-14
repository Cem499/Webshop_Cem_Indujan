package ch.wiss.webshop.dto;

/**
 * DTO für Registrierungsantworten und den /me-Endpoint.
 * Enthält Benutzerinformationen ohne sensitive Daten (kein Passwort-Hash).
 */
public class RegisterResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String message;

    /** Standard-Konstruktor für Deserialisierung. */
    public RegisterResponseDTO() {
    }

    /**
     * Vollständiger Konstruktor.
     *
     * @param id       Benutzer-ID
     * @param username Anzeigename
     * @param email    E-Mail-Adresse
     * @param role     Rolle (ADMIN oder KUNDE)
     * @param message  Bestätigungstext
     */
    public RegisterResponseDTO(Long id, String username, String email, String role, String message) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    /** Getter-Methode */
    public Long getId() {
        return id;
    }

    /** Setter-Methode */
    public void setId(Long id) {
        this.id = id;
    }

    /** Getter-Methode */
    public String getUsername() {
        return username;
    }

    /** Setter-Methode */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Getter-Methode */
    public String getEmail() {
        return email;
    }

    /** Setter-Methode */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Getter-Methode */
    public String getRole() {
        return role;
    }

    /** Setter-Methode */
    public void setRole(String role) {
        this.role = role;
    }

    /** Getter-Methode */
    public String getMessage() {
        return message;
    }

    /** Setter-Methode */
    public void setMessage(String message) {
        this.message = message;
    }
}
