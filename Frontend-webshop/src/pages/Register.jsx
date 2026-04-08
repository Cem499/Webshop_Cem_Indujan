import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import authService from "../services/auth-service";

export default function Register() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({ username: "", email: "", password: "" });
    const [errors, setErrors] = useState({});
    const [serverError, setServerError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Client-seitige Validierung verhindert unnötige Backend-Requests
    // und gibt dem User sofortiges Feedback ohne Netzwerkverzögerung.
    const validate = () => {
        const newErrors = {};

        if (formData.username.trim().length < 3) {
            newErrors.username = "Benutzername muss mindestens 3 Zeichen lang sein";
        }
        if (formData.username.trim().length > 50) {
            newErrors.username = "Benutzername darf maximal 50 Zeichen lang sein";
        }

        // Einfache E-Mail-Prüfung: @ und Punkt nach dem @ müssen vorhanden sein
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
            newErrors.email = "Ungültige E-Mail-Adresse";
        }

        if (formData.password.length < 6) {
            newErrors.password = "Passwort muss mindestens 6 Zeichen lang sein";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setServerError("");

        if (!validate()) return;

        setIsSubmitting(true);
        try {
            await authService.register(formData);
            // Nach erfolgreicher Registrierung zur Login-Seite weiterleiten –
            // User muss sich separat einloggen um Token zu erhalten.
            navigate("/login");
        } catch (err) {
            setServerError(err.message || "Registrierung fehlgeschlagen");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleChange = (field) => (e) => {
        setFormData({ ...formData, [field]: e.target.value });
        // Feld-Fehler beim Tippen zurücksetzen für bessere UX
        if (errors[field]) {
            setErrors({ ...errors, [field]: "" });
        }
    };

    return (
        <div style={{ maxWidth: "400px", margin: "4rem auto", padding: "0 1rem" }}>
            <div className="card">
                <div className="card-header">
                    <h1 style={{ margin: 0, fontSize: "1.5rem" }}>Konto erstellen</h1>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Benutzername *</label>
                        <input
                            type="text"
                            className="form-control"
                            value={formData.username}
                            onChange={handleChange("username")}
                            placeholder="3–50 Zeichen"
                            disabled={isSubmitting}
                        />
                        {errors.username && <div className="error">{errors.username}</div>}
                    </div>

                    <div className="form-group">
                        <label>E-Mail *</label>
                        <input
                            type="email"
                            className="form-control"
                            value={formData.email}
                            onChange={handleChange("email")}
                            placeholder="name@example.com"
                            disabled={isSubmitting}
                        />
                        {errors.email && <div className="error">{errors.email}</div>}
                    </div>

                    <div className="form-group">
                        <label>Passwort *</label>
                        <input
                            type="password"
                            className="form-control"
                            value={formData.password}
                            onChange={handleChange("password")}
                            placeholder="Mindestens 6 Zeichen"
                            disabled={isSubmitting}
                        />
                        {errors.password && <div className="error">{errors.password}</div>}
                    </div>

                    {serverError && (
                        <div className="alert alert-error" style={{ marginBottom: "1rem" }}>
                            {serverError}
                        </div>
                    )}

                    <div className="form-actions">
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={isSubmitting}
                            style={{ width: "100%" }}
                        >
                            {isSubmitting ? "Registrieren..." : "Registrieren"}
                        </button>
                    </div>
                </form>

                <div style={{ textAlign: "center", marginTop: "1rem", paddingBottom: "0.5rem" }}>
                    <p style={{ color: "#7f8c8d" }}>
                        Bereits ein Konto?{" "}
                        <Link to="/login" style={{ color: "#3498db" }}>
                            Jetzt anmelden
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
