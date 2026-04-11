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
 * Repräsentiert einen Benutzer im Webshop.
 * Implementiert UserDetails damit Spring Security den User direkt für die
 * Authentifizierung verwenden kann.
 * Als Spring Security Username gilt die E-Mail, da sie systemweit eindeutig
 * ist.
 * Tabelle: app_users
 */
@Entity
@Table(name = "app_users")
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Benutzername darf nicht leer sein")
    @Size(min = 2, max = 100, message = "Benutzername muss zwischen 2 und 100 Zeichen lang sein")
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Standard-Konstruktor für JPA.
     */
    public AppUser() {
    }

    /**
     * Konstruktor für die Erstellung neuer Benutzer.
     *
     * @param username Anzeigename
     * @param email    E-Mail-Adresse, wird als Login verwendet
     * @param password BCrypt-gehashtes Passwort
     * @param role     Rolle (ADMIN oder KUNDE)
     */
    public AppUser(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * Gibt die Berechtigungen zurück, z.B. ROLE_ADMIN oder ROLE_KUNDE.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Gibt die E-Mail als Spring Security Principal zurück.
     * Achtung: Das ist die E-Mail, nicht der Anzeigename.
     */
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    /**
     * Getter-Methode
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /** Konto nicht abgelaufen. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Konto nicht gesperrt. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Credentials nicht abgelaufen. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Konto aktiv. */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Getter-Methode
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter-Methode
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gibt den Anzeigenamen zurück, nicht die E-Mail.
     */
    public String getDisplayName() {
        return username;
    }

    /**
     * Setter-Methode
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter-Methode
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter-Methode
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Setter-Methode
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter-Methode
     */
    public Role getRole() {
        return role;
    }

    /**
     * Setter-Methode
     */
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AppUser [id=" + id + ", username=" + username + ", email=" + email + ", role=" + role + "]";
    }
}
