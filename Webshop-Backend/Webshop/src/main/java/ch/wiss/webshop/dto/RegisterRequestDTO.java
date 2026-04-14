package ch.wiss.webshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO für Registrierungsanfragen.
 * Enthält Anzeigename, E-Mail und Passwort des neuen Benutzers.
 */
public class RegisterRequestDTO {

    @NotBlank(message = "Benutzername darf nicht leer sein")
    @Size(min = 3, max = 50, message = "Benutzername muss zwischen 3 und 50 Zeichen lang sein")
    private String username;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
    private String password;

    /** Standard-Konstruktor für Deserialisierung. */
    public RegisterRequestDTO() {
    }

    /**
     * Konstruktor für direkte Erstellung.
     *
     * @param username Anzeigename
     * @param email    E-Mail-Adresse
     * @param password Klartext-Passwort
     */
    public RegisterRequestDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
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
    public String getPassword() {
        return password;
    }

    /** Setter-Methode */
    public void setPassword(String password) {
        this.password = password;
    }
}
