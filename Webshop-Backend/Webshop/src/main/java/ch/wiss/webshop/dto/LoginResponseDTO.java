package ch.wiss.webshop.dto;

/**
 * DTO für Login-Antworten mit JWT-Token und Benutzerinformationen.
 * Enthält alle Daten die das Frontend nach einem erfolgreichen Login benötigt:
 * token, tokenType (Bearer), userId, username, email, role und expiresIn
 * (Sekunden).
 */
public class LoginResponseDTO {

    /**
     * Das JWT-Token – wird im Authorization-Header als "Bearer <token>"
     * mitgesendet.
     */
    private String token;

    /** Token-Typ nach OAuth2-Standard – immer "Bearer". */
    private String tokenType = "Bearer";

    /** Datenbank-ID des eingeloggten Benutzers. */
    private Long userId;

    /** Anzeigename des Benutzers. */
    private String username;

    /** E-Mail-Adresse des Benutzers. */
    private String email;

    /** Rolle des Benutzers (ADMIN oder KUNDE). */
    private String role;

    /** Gültigkeitsdauer des Tokens in Sekunden. */
    private long expiresIn;

    /** Standard-Konstruktor für Deserialisierung. */
    public LoginResponseDTO() {
    }

    /**
     * Vollständiger Konstruktor.
     *
     * @param token     Das JWT-Token
     * @param userId    Benutzer-ID
     * @param username  Anzeigename
     * @param email     E-Mail
     * @param role      Rolle (ADMIN oder KUNDE)
     * @param expiresIn Gültigkeitsdauer in Sekunden
     */
    public LoginResponseDTO(String token, Long userId, String username, String email,
            String role, long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    /** Getter-Methode */
    public String getToken() {
        return token;
    }

    /** Setter-Methode */
    public void setToken(String token) {
        this.token = token;
    }

    /** Getter-Methode */
    public String getTokenType() {
        return tokenType;
    }

    /** Setter-Methode */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /** Getter-Methode */
    public Long getUserId() {
        return userId;
    }

    /** Setter-Methode */
    public void setUserId(Long userId) {
        this.userId = userId;
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
    public long getExpiresIn() {
        return expiresIn;
    }

    /** Setter-Methode */
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
