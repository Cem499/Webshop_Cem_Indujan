import { Link } from "react-router-dom";

export default function Forbidden() {
    return (
        <div style={{ textAlign: "center", padding: "4rem 1rem" }}>
            <div className="card" style={{ maxWidth: "500px", margin: "0 auto" }}>
                <h1 style={{ fontSize: "4rem", margin: "1rem 0", color: "#e74c3c" }}>403</h1>
                <h2>Kein Zugriff</h2>
                <p style={{ color: "#7f8c8d", marginBottom: "2rem" }}>
                    Du hast keine Berechtigung für diese Seite.
                </p>
                <Link to="/" className="btn btn-primary">
                    Zurück zur Startseite
                </Link>
            </div>
        </div>
    );
}
