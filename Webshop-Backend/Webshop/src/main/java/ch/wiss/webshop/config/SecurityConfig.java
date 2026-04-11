package ch.wiss.webshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ch.wiss.webshop.security.JwtAuthenticationFilter;
import ch.wiss.webshop.service.AppUserDetailsService;

/**
 * Spring Security Konfiguration für JWT Authentifizierung.
 * Stateless: keine Sessions, jeder Request braucht einen gültigen JWT.
 * EnableMethodSecurity aktiviert @PreAuthorize auf den Controller-Methoden.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private AppUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Legt fest welche Endpoints öffentlich zugänglich sind und welche ein JWT erfordern.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deaktivieren – nicht benötigt bei stateless JWT-Auth (kein Session-Cookie)
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Auth-Endpoints sind immer öffentlich zugänglich
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                // Swagger UI öffentlich zugänglich
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // Produkte und Kategorien lesen ist öffentlich
                .requestMatchers(HttpMethod.GET, "/api/produkte/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/kategorien/**").permitAll()

                // Alle anderen Requests erfordern Authentifizierung
                .anyRequest().authenticated()
            )

            // Stateless: Keine HTTP-Session – jeder Request ist unabhängig
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authenticationProvider(authenticationProvider())

            // JWT-Filter vor dem Standard-Login-Filter einhängen
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt-Encoder für Passwort-Hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Verbindet UserDetailsService mit dem PasswordEncoder für die Passwortprüfung.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Wird im AppUserService für den Login verwendet.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
