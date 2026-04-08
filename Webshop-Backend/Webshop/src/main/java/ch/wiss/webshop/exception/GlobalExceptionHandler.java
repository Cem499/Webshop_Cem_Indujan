package ch.wiss.webshop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Globale Fehlerbehandlung für alle Controller.
 *
 * <p>{@code @RestControllerAdvice} fängt Exceptions aus allen Controllern ab
 * und wandelt sie in einheitliche HTTP-Antworten um.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Behandelt Validierungsfehler aus {@code @Valid}-annotierten Request-Bodies.
     * Gibt HTTP 400 mit einer Map der Feldnamen und Fehlermeldungen zurück.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Behandelt Geschäftslogik-Fehler (z.B. Duplikat-Username bei Registrierung).
     * Gibt HTTP 400 mit der Fehlermeldung zurück.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Fallback für alle nicht explizit behandelten Exceptions.
     * Gibt HTTP 500 zurück ohne interne Details preiszugeben.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Ein interner Fehler ist aufgetreten");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
