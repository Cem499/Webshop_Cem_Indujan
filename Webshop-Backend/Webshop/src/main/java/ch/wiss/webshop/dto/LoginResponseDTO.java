package ch.wiss.webshop.dto;

/**
 * DTO für Login-Antworten mit JWT-Token und Benutzerinformationen.
 *
 * <p>
 * Dieses DTO enthält alle Informationen, die das Frontend nach einem
 * erfolgreichen Login benötigt:
 * </p>
 * <ul>
 * <li>{@code token} – Das JWT-Token für nachfolgende Requests (im
 * Authorization-Header)</li>
 * <li>{@code tokenType} – Immer "Bearer" (OAuth2-Standard)</li>
 * <li>{@code userId} – ID des Benutzers für Frontend-Logik</li>
 * <li>{@code username} – Anzeigename des Benutzers</li>
 * <li>{@code email} – E-Mail des Benutzers</li>
 * <li>{@code role} – Rolle (ADMIN oder KUNDE) für Zugriffssteuerung im
 * Frontend</li>
 * <li>{@code expiresIn} – Gültigkeitsdauer in Sekunden (Standard: 86400 =
 * 24h)</li>
 * </ul>
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

    // Getter / Setter

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
