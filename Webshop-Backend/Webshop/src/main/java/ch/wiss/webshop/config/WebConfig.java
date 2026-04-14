package ch.wiss.webshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS-Konfiguration für das Webshop-Backend.
 * Erlaubt Anfragen vom React-Dev-Server (Port 3000 und 5173) auf alle /api/** Endpunkte.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registriert CORS-Regeln für alle API-Endpunkte.
     * Erlaubte Origins: localhost:3000 (Create React App) und localhost:5173 (Vite).
     *
     * @param registry Die CORS-Registry von Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}