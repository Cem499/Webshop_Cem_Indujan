import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from "../services/api-client";

// Formular für Create und Edit von Bestellungen (Admin-Nutzung).
// /edit-bestellung/:id = Edit (Daten werden beim Mount geladen).
export default function BestellungenForm() {
    const navigate = useNavigate();
    const { id } = useParams();

    // id aus URL-Parameter: gesetzt = Edit-Modus, nicht gesetzt = Create-Modus
    const isEditMode = !!id;

    const [formData, setFormData] = useState({
        kundenName: "",
        kundenEmail: "",
        lieferStrasse: "",
        lieferPlz: "",
        lieferStadt: "",
        lieferLand: "Schweiz",
        status: "OFFEN"
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!isEditMode) return;

        setLoading(true);
        // apiClient sendet JWT automatisch – Backend prüft ob User Zugriff auf diese Bestellung hat
        apiClient.get(`/bestellungen/${id}`)
            .then(response => setFormData(response.data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [id, isEditMode]);

    function handleChange(e) {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    }

    function handleSubmit(e) {
        e.preventDefault();

        const request = isEditMode
            ? apiClient.put(`/bestellungen/${id}`, formData)
            : apiClient.post("/bestellungen", formData);

        request
            .then(() => navigate("/bestellungen"))
            .catch(err => setError(err.message));
    }

    function handleDelete() {
        if (!window.confirm("Wirklich löschen?")) return;

        apiClient.delete(`/bestellungen/${id}`)
            .then(() => navigate("/bestellungen"))
            .catch(err => setError(err.message));
    }

    if (loading) return <p>Lade...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div>
            <h1>{isEditMode ? "Bestellung bearbeiten" : "Neue Bestellung"}</h1>

            <form onSubmit={handleSubmit}>
                <input type="text" name="kundenName" placeholder="Kundenname"
                    value={formData.kundenName} onChange={handleChange} required />
                <input type="email" name="kundenEmail" placeholder="Email"
                    value={formData.kundenEmail} onChange={handleChange} required />
                <input type="text" name="lieferStrasse" placeholder="Strasse"
                    value={formData.lieferStrasse} onChange={handleChange} required />
                <input type="text" name="lieferPlz" placeholder="PLZ"
                    value={formData.lieferPlz} onChange={handleChange} required />
                <input type="text" name="lieferStadt" placeholder="Stadt"
                    value={formData.lieferStadt} onChange={handleChange} required />
                <input type="text" name="lieferLand" placeholder="Land"
                    value={formData.lieferLand} onChange={handleChange} required />
                <select name="status" value={formData.status} onChange={handleChange}>
                    <option value="OFFEN">Offen</option>
                    <option value="BEZAHLT">Bezahlt</option>
                    <option value="STORNIERT">Storniert</option>
                </select>

                <div style={{ marginTop: "20px" }}>
                    <button type="submit">{isEditMode ? "Aktualisieren" : "Erstellen"}</button>
                    <button type="button" onClick={() => navigate("/bestellungen")} style={{ marginLeft: "10px" }}>
                        Abbrechen
                    </button>
                    {isEditMode && (
                        <button type="button" onClick={handleDelete}
                            style={{ marginLeft: "10px", backgroundColor: "red", color: "white" }}>
                            Löschen
                        </button>
                    )}
                </div>
            </form>
        </div>
    );
}
