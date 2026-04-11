package ch.wiss.webshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ch.wiss.webshop.repository.AppUserRepository;

/**
 * Lädt Benutzerdaten für Spring Security.
 * Unterstützt Login per E-Mail oder Anzeigename.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * Lädt einen Benutzer anhand E-Mail oder Anzeigename.
     *
     * @param emailOrUsername E-Mail-Adresse oder Anzeigename
     * @return UserDetails des gefundenen Benutzers
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
