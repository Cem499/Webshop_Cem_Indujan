package ch.wiss.webshop.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO für Login-Anfragen.
 * Das Feld usernameOrEmail akzeptiert sowohl den Anzeigenamen als auch die E-Mail.
 * Der AppUserDetailsService sucht zuerst nach E-Mail, dann nach Username.
 */
public class LoginRequestDTO {

    @NotBlank(message = "Benutzername oder E-Mail darf nicht leer sein")
    private String usernameOrEmail;

    @NotBlank(message = "Passwort darf nicht leer sein")
    private String password;

    /** Standard-Konstruktor für Deserialisierung. */
    public LoginRequestDTO() {
    }

    /**
     * Konstruktor für direkte Erstellung.
     *
     * @param usernameOrEmail E-Mail oder Anzeigename
     * @param password        Klartext-Passwort
     */
    public LoginRequestDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    /** Getter-Methode */
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    /** Setter-Methode */
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    // Alias für getUsernameOrEmail() – Kompatibilität mit bestehendem AppUserService
    /** Getter-Methode */
    public String getEmail() {
        return usernameOrEmail;
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
