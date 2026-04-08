import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navigation() {
    const { isAuthenticated, user, logout } = useAuth();
    const isAdmin = user?.role === "ADMIN";

    return (
        <ul className="nav-menu">
            <li><Link to="/" className="nav-link">Home</Link></li>
            <li><Link to="/produkte" className="nav-link">Produkte</Link></li>

            {isAuthenticated ? (
                <>
                    <li><Link to="/warenkorb" className="nav-link">Warenkorb</Link></li>
                    <li><Link to="/bestellungen" className="nav-link">Bestellungen</Link></li>

                    {/* Admin-Bereich mit visueller Trennlinie */}
                    {isAdmin && (
                        <>
                            <li className="nav-divider" aria-hidden="true" />
                            <li><Link to="/kategorien" className="nav-link">Kategorien</Link></li>
                            <li><Link to="/admin/produkte" className="nav-link">+ Produkt</Link></li>
                            <li><Link to="/admin/kategorien" className="nav-link">+ Kategorie</Link></li>
                            <li><Link to="/admin/bestellungen" className="nav-link">Alle Bestellungen</Link></li>
                        </>
                    )}

                    <li className="nav-divider" aria-hidden="true" />
                    <li style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                        <span className="nav-link" style={{ color: "#bdc3c7", cursor: "default", padding: "0.4rem 0.4rem" }}>
                            {user?.username}
                        </span>
                        <button onClick={logout} className="btn btn-secondary btn-sm"
                            style={{ padding: "0.3rem 0.6rem", fontSize: "0.85rem" }}>
                            Abmelden
                        </button>
                    </li>
                </>
            ) : (
                <>
                    <li><Link to="/login" className="nav-link">Anmelden</Link></li>
                    <li><Link to="/register" className="nav-link">Registrieren</Link></li>
                </>
            )}
        </ul>
    );
}
