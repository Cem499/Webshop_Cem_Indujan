import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";

// Custom Hook: liest den Warenkorb des Users aus localStorage und hält den Zähler aktuell.
// Der storage-Event wird von ProdukteList und Warenkorb manuell gefeuert wenn sich der Cart ändert,
// damit die Badge ohne Seiten-Reload aktualisiert wird.
function useCartCount(userId) {
    const [count, setCount] = useState(0);

    useEffect(() => {
        const key = userId ? `cart_${userId}` : null;
        function update() {
            if (!key) { setCount(0); return; }
            const cart = JSON.parse(localStorage.getItem(key) || "[]");
            setCount(cart.reduce((sum, item) => sum + item.menge, 0));
        }
        update();
        window.addEventListener("storage", update);
        return () => window.removeEventListener("storage", update);
    }, [userId]);

    return count;
}

export default function Navigation() {
    const { isAuthenticated, user, logout } = useAuth();
    const isAdmin = user?.role === "ADMIN";
    const cartCount = useCartCount(user?.id);

    return (
        <ul className="nav-menu">
            <li><Link to="/" className="nav-link">Home</Link></li>
            <li><Link to="/produkte" className="nav-link">Produkte</Link></li>

            {isAuthenticated ? (
                <>
                    {/* Warenkorb für alle eingeloggten User */}
                    <li>
                        <Link to="/warenkorb" className="nav-link">
                            Warenkorb
                            {cartCount > 0 && (
                                <span className="cart-badge">{cartCount}</span>
                            )}
                        </Link>
                    </li>

                    {/* Eigene Bestellungen nur für KUNDE */}
                    {!isAdmin && (
                        <li><Link to="/bestellungen" className="nav-link">Bestellungen</Link></li>
                    )}

                    {/* Admin-Bereich */}
                    {isAdmin && (
                        <>
                            <li className="nav-divider" aria-hidden="true" />
                            <li><Link to="/admin/meine-bestellungen" className="nav-link">Meine Bestellungen</Link></li>
                            <li><Link to="/kategorien" className="nav-link">Kategorien</Link></li>
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
