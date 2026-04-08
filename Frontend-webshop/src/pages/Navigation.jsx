import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navigation() {
    // Auth-State aus Context lesen – kein prop-drilling nötig
    const { isAuthenticated, user, logout } = useAuth();

    return (
        <ul className="nav-menu">
            <li>
                <Link to="/" className="nav-link">Home</Link>
            </li>
            <li>
                <Link to="/produkte" className="nav-link">Produkte</Link>
            </li>

            {isAuthenticated ? (
                // Eingeloggte User sehen Warenkorb, Bestellungen und Logout
                <>
                    <li>
                        <Link to="/warenkorb" className="nav-link">Warenkorb</Link>
                    </li>
                    <li>
                        <Link to="/bestellungen" className="nav-link">Meine Bestellungen</Link>
                    </li>

                    {/* Admin-Bereich nur für Benutzer mit ADMIN-Rolle sichtbar.
                        Das Backend prüft die Rolle nochmals via @PreAuthorize. */}
                    {user?.role === "ADMIN" && (
                        <>
                            <li>
                                <Link to="/kategorien" className="nav-link">Kategorien</Link>
                            </li>
                            <li>
                                <Link to="/admin/produkte" className="nav-link">Produkt erstellen</Link>
                            </li>
                            <li>
                                <Link to="/admin/kategorien" className="nav-link">Kategorie erstellen</Link>
                            </li>
                            <li>
                                <Link to="/admin/bestellungen" className="nav-link">Alle Bestellungen</Link>
                            </li>
                        </>
                    )}

                    <li style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                        <span className="nav-link" style={{ color: "#95a5a6", cursor: "default" }}>
                            Hallo, {user?.username}
                        </span>
                        {/* logout() bereinigt localStorage und leitet zur Startseite weiter */}
                        <button
                            onClick={logout}
                            className="btn btn-secondary btn-sm"
                            style={{ padding: "0.25rem 0.75rem" }}
                        >
                            Abmelden
                        </button>
                    </li>
                </>
            ) : (
                // Nicht eingeloggte User sehen Login und Register
                <>
                    <li>
                        <Link to="/login" className="nav-link">Anmelden</Link>
                    </li>
                    <li>
                        <Link to="/register" className="nav-link">Registrieren</Link>
                    </li>
                </>
            )}
        </ul>
    );
}
