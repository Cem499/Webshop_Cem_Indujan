import { Link } from "react-router-dom";

// Wird angezeigt wenn ein nicht-eingeloggter User eine Aktion versucht,
// die eine Anmeldung erfordert (z.B. Produkt in den Warenkorb legen).
export default function AuthRequiredModal({ onClose, message }) {
    return (
        // Dunkler Hintergrund-Overlay
        <div
            onClick={onClose}
            style={{
                position: "fixed", inset: 0,
                background: "rgba(0,0,0,0.5)",
                display: "flex", alignItems: "center", justifyContent: "center",
                zIndex: 1000
            }}
        >
            {/* Klick auf Modal selbst soll es nicht schliessen */}
            <div
                onClick={(e) => e.stopPropagation()}
                className="card"
                style={{ maxWidth: "400px", width: "90%", margin: 0, textAlign: "center" }}
            >
                <div style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>🔒</div>
                <h2 style={{ marginBottom: "0.5rem" }}>Anmeldung erforderlich</h2>
                <p style={{ color: "#7f8c8d", marginBottom: "1.5rem" }}>
                    {message || "Um diese Funktion zu nutzen, musst du angemeldet sein."}
                </p>

                <div style={{ display: "flex", gap: "1rem", justifyContent: "center", flexWrap: "wrap" }}>
                    <Link to="/login" className="btn btn-primary" onClick={onClose}>
                        Anmelden
                    </Link>
                    <Link to="/register" className="btn btn-secondary" onClick={onClose}>
                        Registrieren
                    </Link>
                </div>

                <button
                    onClick={onClose}
                    style={{
                        marginTop: "1rem", background: "none", border: "none",
                        color: "#95a5a6", cursor: "pointer", fontSize: "0.875rem"
                    }}
                >
                    Abbrechen
                </button>
            </div>
        </div>
    );
}
