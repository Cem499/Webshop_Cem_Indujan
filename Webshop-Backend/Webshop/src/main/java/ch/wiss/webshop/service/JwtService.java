package ch.wiss.webshop.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Service zur Erstellung, Validierung und Analyse von JWT-Tokens.
 *
 * <h2>Was ist JWT?</h2>
 * <p>Ein JSON Web Token (JWT) ist ein kompaktes, URL-sicheres Format zur Übertragung
 * von Informationen als JSON-Objekt. Es besteht aus drei Teilen:<br>
 * {@code Header.Payload.Signature}</p>
 * <ul>
 *   <li><strong>Header:</strong> Algorithmus (hier: HS256) und Token-Typ</li>
 *   <li><strong>Payload:</strong> Claims (Ansprüche) – z. B. Benutzername, Rolle, Ablaufzeit</li>
 *   <li><strong>Signature:</strong> HMAC-SHA256-Signatur mit dem Secret-Key zur Integritätsprüfung</li>
 * </ul>
 *
 * <h2>Ablauf im Webshop</h2>
 * <ol>
 *   <li>Benutzer sendet E-Mail + Passwort an {@code POST /api/auth/login}</li>
 *   <li>{@link AppUserService} authentifiziert via Spring Security</li>
 *   <li>Dieser Service generiert ein Token mit E-Mail als Subject und Rolle als Claim</li>
 *   <li>Bei jedem weiteren Request prüft der {@code JwtAuthenticationFilter} das Token</li>
 * </ol>
 *
 * <h2>Sicherheit</h2>
 * <p>Der Secret-Key muss mindestens 256 Bit (32 Zeichen) lang sein und darf nie
 * öffentlich bekannt werden. Er wird Base64-kodiert in {@code application.properties}
 * unter {@code app.jwt.secret} gespeichert.</p>
 *
 * @see ch.wiss.webshop.security.JwtAuthenticationFilter
 * @see ch.wiss.webshop.service.AppUserService
 */
@Service
public class JwtService {

    /**
     * Geheimer Schlüssel für die HMAC-SHA256-Signatur.
     * Wird aus {@code application.properties} (app.jwt.secret) geladen.
     * Muss Base64-kodiert und mindestens 256 Bit lang sein.
     */
    @Value("${app.jwt.secret}")
    private String secretKey;

    /**
     * Gültigkeitsdauer des Tokens in Millisekunden.
     * Standard: 86400000 ms = 24 Stunden.
     * Wird aus {@code application.properties} (app.jwt.expiration) geladen.
     */
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // =========================================================================
    // Token-Generierung
    // =========================================================================

    /**
     * Generiert ein JWT-Token für den angegebenen Benutzer (UserDetails).
     *
     * <p>Das Token enthält die E-Mail als {@code subject}-Claim (via
     * {@code UserDetails.getUsername()}, das in {@link ch.wiss.webshop.model.AppUser}
     * die E-Mail zurückgibt).</p>
     *
     * @param userDetails Der Benutzer, für den das Token erstellt wird
     * @return Signiertes JWT-Token als Base64-kodierter String
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generiert ein JWT-Token mit zusätzlichen Claims.
     *
     * <p>Extra-Claims ermöglichen es, beliebige Informationen (z. B. Rolle, Benutzer-ID)
     * im Token-Payload zu hinterlegen, ohne die Datenbank erneut abfragen zu müssen.</p>
     *
     * @param extraClaims Zusätzliche Key-Value-Paare im Token-Payload
     * @param userDetails Der Benutzer für den Subject-Claim
     * @return Signiertes JWT-Token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)                                               // Zusätzliche Claims (z. B. role)
                .subject(userDetails.getUsername())                                // Subject = E-Mail des Benutzers
                .issuedAt(new Date(System.currentTimeMillis()))                    // Ausstellungszeitpunkt
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))  // Ablaufzeitpunkt
                .signWith(getSignInKey())                                           // HMAC-SHA256 Signatur
                .compact();                                                        // Zusammenbauen → String
    }

    /**
     * Generiert ein JWT-Token anhand von Benutzername und Rolle (ohne UserDetails-Objekt).
     *
     * <p>Diese Variante eignet sich, wenn nur Benutzername und Rolle bekannt sind,
     * ohne ein vollständiges {@link UserDetails}-Objekt zu laden. Die Rolle wird
     * als {@code "role"}-Claim gespeichert.</p>
     *
     * @param username Der Benutzername / die E-Mail (wird als Subject gespeichert)
     * @param role     Die Benutzerrolle (wird als {@code "role"}-Claim gespeichert)
     * @return Signiertes JWT-Token
     */
    public String generateToken(String username, Role role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role.name()); // Rolle im Token-Payload speichern
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    // =========================================================================
    // Claims-Extraktion
    // =========================================================================

    /**
     * Extrahiert den Benutzernamen (Subject-Claim = E-Mail) aus dem Token.
     *
     * @param token Das JWT-Token (ohne "Bearer "-Prefix)
     * @return E-Mail des Benutzers
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrahiert die Rolle aus dem Token (falls als Claim gespeichert).
     *
     * <p>Funktioniert nur, wenn das Token mit {@link #generateToken(String, Role)}
     * erstellt wurde und den {@code "role"}-Claim enthält.</p>
     *
     * @param token Das JWT-Token
     * @return Rollenname (z. B. "ADMIN" oder "KUNDE"), oder {@code null} wenn nicht vorhanden
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Generische Methode zur Extraktion eines beliebigen Claims.
     *
     * <p>Nimmt einen {@link Function}-Lambda entgegen, der auf dem {@link Claims}-Objekt
     * ausgeführt wird. Dadurch können alle Claims typsicher abgerufen werden.</p>
     *
     * @param <T>            Rückgabetyp des Claims
     * @param token          Das JWT-Token
     * @param claimsResolver Lambda zur Extraktion des gewünschten Claims
     * @return Der extrahierte Claim-Wert
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // =========================================================================
    // Token-Validierung
    // =========================================================================

    /**
     * Prüft, ob ein Token für den angegebenen Benutzer gültig ist.
     *
     * <p>Ein Token ist gültig, wenn:</p>
     * <ol>
     *   <li>Der Subject-Claim (E-Mail) mit dem UserDetails-Username übereinstimmt</li>
     *   <li>Das Token noch nicht abgelaufen ist</li>
     * </ol>
     *
     * @param token       Das JWT-Token
     * @param userDetails Die UserDetails des zu prüfenden Benutzers
     * @return {@code true} wenn das Token gültig ist, {@code false} sonst
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Gibt die Gültigkeitsdauer in Millisekunden zurück.
     * Wird für die {@link ch.wiss.webshop.dto.LoginResponseDTO} (expiresIn-Feld) benötigt.
     *
     * @return Ablaufzeit in Millisekunden (Standard: 86400000 = 24h)
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }

    // =========================================================================
    // Private Hilfsmethoden
    // =========================================================================

    /**
     * Prüft, ob das Token abgelaufen ist.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrahiert den Ablaufzeitpunkt aus dem Token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parst das Token und gibt alle Claims zurück.
     *
     * <p>Hier wird die JJWT-Bibliothek verwendet:</p>
     * <ol>
     *   <li>{@code Jwts.parser()} erstellt einen Token-Parser</li>
     *   <li>{@code verifyWith(getSignInKey())} verifiziert die HMAC-Signatur</li>
     *   <li>{@code parseSignedClaims(token)} parst und validiert das Token</li>
     *   <li>{@code getPayload()} gibt die Claims (Payload) zurück</li>
     * </ol>
     *
     * @throws io.jsonwebtoken.JwtException wenn das Token ungültig oder manipuliert ist
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Erzeugt den HMAC-SHA256-Schlüssel aus dem Base64-kodierten Secret.
     *
     * <p>Der Secret-Key wird aus {@code application.properties} als Base64-String
     * gelesen, dekodiert und in einen {@link SecretKey} umgewandelt, der von JJWT
     * für die Signatur verwendet wird.</p>
     *
     * @return HMAC-SHA256 {@link SecretKey}
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
