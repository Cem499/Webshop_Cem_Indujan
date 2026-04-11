package ch.wiss.webshop.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO für Login-Anfragen.
 *
 * <p>
 * Das Feld {@code usernameOrEmail} akzeptiert sowohl den Anzeigenamen als auch
 * die
 * E-Mail-Adresse. Der {@link ch.wiss.webshop.service.AppUserDetailsService}
 * sucht
 * zuerst nach E-Mail, dann nach Username.
 * </p>
 */
public class LoginRequestDTO {

    @NotBlank(message = "Benutzername oder E-Mail darf nicht leer sein")
    private String usernameOrEmail;

    @NotBlank(message = "Passwort darf nicht leer sein")
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    /**
     * Alias für getUsernameOrEmail() – Kompatibilität mit bestehendem
     * AppUserService.
     */
    public String getEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
