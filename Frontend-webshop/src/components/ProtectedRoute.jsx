import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// Frontend-Routing-Schutz: verhindert Navigation zu geschützten Seiten
// für nicht autorisierte User. WICHTIG: Das reicht allein nicht aus –
// das Backend MUSS die Endpoints ebenfalls via @PreAuthorize schützen,
// da ein User den Frontend-Schutz durch direkte API-Aufrufe umgehen könnte.
export default function ProtectedRoute({ children, requiredRole, allowedRoles }) {
    const { isAuthenticated, isLoading, user } = useAuth();

    // Warten bis checkAuth() aus dem localStorage geladen hat,
    // sonst würde ein eingeloggter User sofort zur Login-Seite weitergeleitet.
    if (isLoading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", padding: "2rem" }}>
                <div className="spinner"></div>
            </div>
        );
    }

    // Nicht eingeloggt → zur Login-Seite
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // Exakte Rolle gefordert (z.B. nur ADMIN) → Forbidden
    if (requiredRole && user?.role !== requiredRole) {
        return <Navigate to="/forbidden" replace />;
    }

    // Liste erlaubter Rollen – User muss in der Liste sein
    if (allowedRoles && !allowedRoles.includes(user?.role)) {
        // ADMIN der auf /bestellungen geht → zu /admin/bestellungen umleiten
        if (user?.role === "ADMIN") {
            return <Navigate to="/admin/bestellungen" replace />;
        }
        return <Navigate to="/forbidden" replace />;
    }

    return children;
}
