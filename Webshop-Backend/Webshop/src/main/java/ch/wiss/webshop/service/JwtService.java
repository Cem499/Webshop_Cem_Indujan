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
 * Service zur Erstellung, Validierung und Auswertung von JWT Tokens.
 * Tokens sind standardmässig 24 Stunden gültig.
 * Der Secret-Key wird Base64-kodiert aus application.properties geladen.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Erstellt ein JWT Token für den angegebenen Benutzer.
     *
     * @param userDetails Der Benutzer für den das Token erstellt wird
     * @return Signiertes JWT Token als String
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Erstellt ein JWT Token mit zusätzlichen Claims.
     *
     * @param extraClaims Zusätzliche Key-Value-Paare im Token
     * @param userDetails Der Benutzer für den Subject-Claim
     * @return Signiertes JWT Token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // Subject = E-Mail
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Erstellt ein JWT Token anhand von Username und Rolle.
     *
     * @param username Der Username bzw. die E-Mail
     * @param role     Die Benutzerrolle
     * @return Signiertes JWT Token
     */
    public String generateToken(String username, Role role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role.name());
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Liest die E-Mail (Subject) aus dem Token.
     *
     * @param token Das JWT Token
     * @return E-Mail des Benutzers
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Liest die Rolle aus dem Token wenn vorhanden.
     *
     * @param token Das JWT Token
     * @return Rollenname oder null wenn nicht im Token enthalten
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Liest einen beliebigen Claim aus dem Token.
     *
     * @param <T>            Rückgabetyp des Claims
     * @param token          Das JWT Token
     * @param claimsResolver Lambda zur Extraktion des gewünschten Claims
     * @return Der extrahierte Claim-Wert
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Prüft ob ein Token für den angegebenen Benutzer gültig und nicht abgelaufen
     * ist.
     *
     * @param token       Das JWT Token
     * @param userDetails Der zu prüfende Benutzer
     * @return true wenn gültig, false sonst
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Getter-Methode
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * Prüft ob das Token abgelaufen ist.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Liest den Ablaufzeitpunkt aus dem Token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parst das Token und gibt alle Claims zurück.
     *
     * @throws io.jsonwebtoken.JwtException wenn das Token ungültig oder manipuliert
     *                                      ist
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Erzeugt den HMAC-SHA256 Schlüssel aus dem Base64-kodierten Secret.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
