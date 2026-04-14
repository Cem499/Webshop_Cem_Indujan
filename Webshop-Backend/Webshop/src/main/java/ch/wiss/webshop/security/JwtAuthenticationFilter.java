package ch.wiss.webshop.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import ch.wiss.webshop.service.AppUserDetailsService;
import ch.wiss.webshop.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Prüft bei jedem Request den JWT aus dem Authorization-Header.
 * Wenn ein gültiges Token gefunden wird, setzt der Filter den SecurityContext.
 * Spring Security entscheidet danach ob der Zugriff erlaubt ist.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AppUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Kein Bearer-Token -> Request ohne Authentifizierung weiterreichen
        // Öffentliche Endpoints (z.B. /api/auth/login) funktionieren dann als anonymous
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring("Bearer ".length());
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Ungültiges oder abgelaufenes Token -> Request ohne Auth weiterreichen
            filterChain.doFilter(request, response);
            return;
        }

        // Nur authentifizieren wenn noch kein Auth im SecurityContext vorhanden
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Gültiges Token -> Authentication im SecurityContext setzen
                // Dadurch erkennt Spring Security den User als eingeloggt
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
