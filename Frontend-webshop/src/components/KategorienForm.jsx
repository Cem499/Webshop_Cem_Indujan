import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";

export default function KategorienForm() {
    const navigate = useNavigate();
    const { id } = useParams();
    const isEdit = !!id;

    const [formData, setFormData] = useState({
        name: "",
        beschreibung: ""
    });

    useEffect(() => {
        if (isEdit) {
            fetch(`http://localhost:8081/api/kategorien/${id}`)
                .then(response => response.ok && response.json() || Promise.reject(response))
                .then(data => setFormData({
                    name: data.name,
                    beschreibung: data.beschreibung || ""
                }))
                .catch(error => console.error(error));
        }
    }, [id, isEdit]);

    function handleSubmit(event) {
        event.preventDefault();

        const url = isEdit
            ? `http://localhost:8081/api/kategorien/${id}`
            : "http://localhost:8081/api/kategorien";

        const method = isEdit ? "PUT" : "POST";

        fetch(url, {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(formData)
        })
            .then(response => response.ok && navigate("/kategorien") || Promise.reject(response))
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