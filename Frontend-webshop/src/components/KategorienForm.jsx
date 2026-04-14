import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from "../services/api-client";

// Formular für Create und Edit von Kategorien.
// /new-kategorie = Create, /edit-kategorie/:id = Edit (Daten werden beim Mount geladen).
export default function KategorienForm() {
    const navigate = useNavigate();
    const { id } = useParams();
    // id aus URL-Parameter: gesetzt = Edit-Modus, nicht gesetzt = Create-Modus
    const isEdit = !!id;

    const [formData, setFormData] = useState({
        name: "",
        beschreibung: ""
    });

    useEffect(() => {
        if (isEdit) {
            // apiClient sendet JWT automatisch mit – kein manuelles Header-Setzen nötig
            apiClient.get(`/kategorien/${id}`)
                .then(response => setFormData({
                    name: response.data.name,
                    beschreibung: response.data.beschreibung || ""
                }))
                .catch(error => console.error(error));
        }
    }, [id, isEdit]);

    function handleSubmit(event) {
        event.preventDefault();

        const request = isEdit
            ? apiClient.put(`/kategorien/${id}`, formData)
            : apiClient.post("/kategorien", formData);

        request
            .then(() => navigate("/kategorien"))
            .catch(error => console.error(error));
    }

    return (
        <div>
            <div className="page-header">
                <h1>{isEdit ? "Kategorie bearbeiten" : "Neue Kategorie"}</h1>
            </div>

            <div className="card">
                <div className="card-header">
                    {isEdit ? "Kategorie aktualisieren" : "Kategorie erstellen"}
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Name *</label>
                        <input
                            type="text"
                            className="form-control"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            required
                            placeholder="z.B. Elektronik"
                        />
                    </div>

                    <div className="form-group">
                        <label>Beschreibung</label>
                        <textarea
                            className="form-control"
                            value={formData.beschreibung}
                            onChange={(e) => setFormData({ ...formData, beschreibung: e.target.value })}
                            rows="3"
                            placeholder="Optionale Beschreibung"
                        />
                    </div>

                    <div className="form-actions">
                        <button type="button" className="btn btn-secondary" onClick={() => navigate("/kategorien")}>
                            Abbrechen
                        </button>
                        <button type="submit" className="btn btn-primary">
                            {isEdit ? "Aktualisieren" : "Speichern"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
