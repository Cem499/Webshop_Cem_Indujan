package ch.wiss.webshop.model;

/**
 * Benutzerrollen im Webshop-System.
 *
 * <p>Wird als {@code EnumType.STRING} in der Datenbank gespeichert,
 * damit der Wert auch nach einem Enum-Refactoring lesbar bleibt.</p>
 *
 * <p>Spring Security erwartet das Präfix {@code ROLE_} – dies wird in
 * {@link AppUser#getAuthorities()} hinzugefügt, sodass {@code @PreAuthorize("hasRole('ADMIN')")}
 * korrekt funktioniert.</p>
 */
public enum Role {
    ADMIN,
    KUNDE
}
