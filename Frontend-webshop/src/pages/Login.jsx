import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const [usernameOrEmail, setUsernameOrEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        // Leere Felder abfangen bevor der Request gesendet wird
        if (!usernameOrEmail.trim() || !password.trim()) {
            setError("Bitte alle Felder ausfüllen");
            return;
        }

        setIsSubmitting(true);
        try {
            await login(usernameOrEmail, password);
            // Nach erfolgreichem Login zur Startseite navigieren
            navigate("/");
        } catch (err) {
            // Fehlermeldung vom Backend anzeigen (z.B. "Falsches Passwort")
            setError(err.message || "Login fehlgeschlagen");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div style={{ maxWidth: "400px", margin: "4rem auto", padding: "0 1rem" }}>
            <div className="card">
                <div className="card-header">
                    <h1 style={{ margin: 0, fontSize: "1.5rem" }}>Anmelden</h1>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Benutzername oder E-Mail</label>
                        <input
                            type="text"
                            className="form-control"
                            value={usernameOrEmail}
                            onChange={(e) => setUsernameOrEmail(e.target.value)}
                            placeholder="Benutzername oder E-Mail"
                            autoComplete="username"
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="form-group">
                        <label>Passwort</label>
                        <input
                            type="password"
                            className="form-control"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Passwort"
                            autoComplete="current-password"
                            disabled={isSubmitting}
                        />
                    </div>

                    {error && (
                        <div className="alert alert-error" style={{ marginBottom: "1rem" }}>
                            {error}
                        </div>
                    )}

                    <div className="form-actions">
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={isSubmitting}
                            style={{ width: "100%" }}
                        >
                            {isSubmitting ? "Anmelden..." : "Anmelden"}
                        </button>
                    </div>
                </form>

                <div style={{ textAlign: "center", marginTop: "1rem", paddingBottom: "0.5rem" }}>
                    <p style={{ color: "#7f8c8d" }}>
                        Noch kein Konto?{" "}
                        <Link to="/register" style={{ color: "#3498db" }}>
                            Jetzt registrieren
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
