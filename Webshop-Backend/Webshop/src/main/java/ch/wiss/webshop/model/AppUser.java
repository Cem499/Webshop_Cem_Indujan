package ch.wiss.webshop.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Repräsentiert einen Benutzer im Webshop-System.
 *
 * <p>Diese Klasse implementiert das {@link UserDetails}-Interface von Spring Security,
 * wodurch sie direkt als Authentifizierungsobjekt verwendet werden kann. Spring Security
 * ruft {@code getUsername()} auf, um den eindeutigen Identifikator des Benutzers zu
 * ermitteln – in diesem System ist das die <strong>E-Mail-Adresse</strong>, da sie
 * systemweit eindeutig ist und als Login-Credential dient.</p>
 *
 * <p><strong>JWT-Relevanz:</strong> Der Wert von {@code getUsername()} (= E-Mail) wird
 * als {@code subject}-Claim im JWT-Token gespeichert. Beim nächsten Request extrahiert
 * der {@code JwtAuthenticationFilter} die E-Mail aus dem Token und lädt den Benutzer
 * über den {@code AppUserDetailsService} nach.</p>
 *
 * <p>Tabelle: {@code app_users}</p>
 *
 * @see ch.wiss.webshop.service.JwtService
 * @see ch.wiss.webshop.security.JwtAuthenticationFilter
 * @see ch.wiss.webshop.service.AppUserDetailsService
 */
@Entity
@Table(name = "app_users")
public class AppUser implements UserDetails {

    /**
     * Primärschlüssel – wird automatisch von der Datenbank generiert.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Anzeigename des Benutzers (z. B. "MaxMuster").
     * Muss eindeutig sein – kein doppelter Username erlaubt.
     * Wird NICHT als Spring-Security-Principal verwendet (das ist die E-Mail).
     */
    @NotBlank(message = "Benutzername darf nicht leer sein")
    @Size(min = 2, max = 100, message = "Benutzername muss zwischen 2 und 100 Zeichen lang sein")
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * E-Mail-Adresse – dient als Login-Credential und als Spring-Security-Principal.
     * Muss eindeutig sein.
     */
    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * BCrypt-gehashtes Passwort. Wird nie im Klartext gespeichert.
     * {@code @JsonIgnore} verhindert, dass der Hash in JSON-Antworten erscheint.
     */
    @NotBlank(message = "Passwort darf nicht leer sein")
    @Column(nullable = false)
    private String password;

    /**
     * Rolle des Benutzers im System (ADMIN oder KUNDE).
     * Wird als String in der Datenbank gespeichert (EnumType.STRING).
     * Wird auch als {@code role}-Claim im JWT hinterlegt.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Standard-Konstruktor – wird von JPA benötigt.
     */
    public AppUser() {}

    /**
     * Konstruktor für die Erstellung neuer Benutzer.
     *
     * @param username Anzeigename (muss eindeutig sein)
     * @param email    E-Mail-Adresse (muss eindeutig sein, wird als Login verwendet)
     * @param password BCrypt-gehashtes Passwort
     * @param role     Rolle (ADMIN oder KUNDE)
     */
    public AppUser(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // =========================================================================
    // Spring Security UserDetails Interface
    // =========================================================================

    /**
     * Gibt die Berechtigungen des Benutzers zurück.
     *
     * <p>Das Format {@code "ROLE_" + role.name()} ist die Spring-Security-Konvention.
     * {@code hasRole('ADMIN')} im {@code @PreAuthorize} sucht automatisch nach
     * {@code "ROLE_ADMIN"} in dieser Liste.</p>
     *
     * @return Liste mit einer {@link SimpleGrantedAuthority} (z. B. {@code ROLE_ADMIN})
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Gibt die E-Mail als Spring-Security-Principal zurück.
     *
     * <p><strong>Wichtig:</strong> Spring Security verwendet diesen Wert als eindeutigen
     * Benutzernamen. Er wird als {@code subject} im JWT gespeichert und beim Filter
     * zur Benutzer-Identifikation genutzt. Der Anzeigename ist unter
     * {@link #getDisplayName()} abrufbar.</p>
     *
     * <p>{@code @JsonIgnore} verhindert Doppelseralisierung mit {@link #getEmail()}.</p>
     *
     * @return E-Mail-Adresse des Benutzers
     */
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    /**
     * Gibt das BCrypt-gehashte Passwort zurück.
     * {@code @JsonIgnore} verhindert, dass der Hash in API-Antworten erscheint.
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /** Konto nicht abgelaufen – immer true (keine Ablauflogik implementiert). */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /** Konto nicht gesperrt – immer true (keine Sperr-Logik implementiert). */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /** Credentials nicht abgelaufen – immer true. */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /** Konto aktiv – immer true. */
    @Override
    public boolean isEnabled() { return true; }

    // =========================================================================
    // Getter / Setter
    // =========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /**
     * Gibt den Anzeigenamen zurück (nicht die E-Mail).
     * Diese Methode ist für die Anzeige in der UI gedacht.
     */
    public String getDisplayName() { return username; }

    /**
     * Setzt den Anzeigenamen.
     */
    public void setUsername(String username) { this.username = username; }

    /** Gibt die E-Mail zurück (auch Spring-Security-Principal). */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** Setzt das (bereits BCrypt-gehashte) Passwort. */
    public void setPassword(String password) { this.password = password; }

    /** Gibt die Rolle zurück (ADMIN oder KUNDE). */
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return "AppUser [id=" + id + ", username=" + username + ", email=" + email + ", role=" + role + "]";
    }
}
