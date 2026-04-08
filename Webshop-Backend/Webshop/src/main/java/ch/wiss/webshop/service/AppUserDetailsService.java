package ch.wiss.webshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.repository.AppUserRepository;

/**
 * Spring Security UserDetailsService – lädt Benutzerdaten für die Authentifizierung.
 *
 * <p>Spring Security ruft {@link #loadUserByUsername(String)} während der Authentifizierung auf.
 * In diesem System wird als "Username" die E-Mail-Adresse verwendet, da sie systemweit
 * eindeutig ist. Der Anzeigename (username-Feld) kann doppelt vorkommen.</p>
 *
 * <p>Unterstützt Login sowohl per E-Mail als auch per Anzeigename:
 * zuerst wird per E-Mail gesucht, dann per Username (Fallback).</p>
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * Lädt einen Benutzer anhand E-Mail oder Anzeigename.
     *
     * <p>Wird vom {@link org.springframework.security.authentication.AuthenticationManager}
     * beim Login aufgerufen, um den Benutzer für die Passwortprüfung zu laden.</p>
     *
     * @param emailOrUsername E-Mail-Adresse oder Anzeigename
     * @return {@link UserDetails} des gefundenen Benutzers
     * @throws UsernameNotFoundException wenn kein Benutzer gefunden wurde
     */
    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        // E-Mail hat Vorrang (ist der primäre Login-Identifier im System)
        return appUserRepository.findByEmail(emailOrUsername)
                .or(() -> appUserRepository.findByUsername(emailOrUsername))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kein Benutzer mit E-Mail oder Benutzername '" + emailOrUsername + "' gefunden"));
    }
}
