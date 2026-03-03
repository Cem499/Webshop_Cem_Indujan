import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";

export default function ProdukteForm() {
    const navigate = useNavigate();
    const { id } = useParams();
    const isEdit = !!id;

    const [kategorien, setKategorien] = useState([]);
    const [formData, setFormData] = useState({
        name: "",
        beschreibung: "",
        preis: "",
        bestand: "",
        kategorie: { id: "" }
    });

    useEffect(() => {
        fetch("http://localhost:8081/api/kategorien")
            .then(response => response.ok && response.json() || Promise.reject(response))
            .then(data => setKategorien(data))
            .catch(error => console.error(error));
    }, []);

    useEffect(() => {
        if (isEdit) {
            fetch(`http://localhost:8081/api/produkte/${id}`)
                .then(response => response.ok && response.json() || Promise.reject(response))
                .then(data => setFormData({
                    name: data.name,
                    beschreibung: data.beschreibung || "",
                    preis: data.preis,
                    bestand: data.bestand,
                    kategorie: { id: data.kategorie?.id || "" }
                }))
                .catch(error => console.error(error));
        }
    }, [id, isEdit]);

    function handleSubmit(event) {
        event.preventDefault();

        const produktData = {
            name: formData.name,
            beschreibung: formData.beschreibung,
            preis: parseFloat(formData.preis),
            bestand: parseInt(formData.bestand),
            kategorie: { id: parseInt(formData.kategorie.id) }
        };

        const url = isEdit
            ? `http://localhost:8081/api/produkte/${id}`
            : "http://localhost:8081/api/produkte";

        const method = isEdit ? "PUT" : "POST";

        fetch(url, {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(produktData)
        })
            .then(response => response.ok && navigate("/produkte") || Promise.reject(response))
            .catch(error => console.error(error));
    }

    return (
        <div>
            <div className="page-header">
                <h1>{isEdit ? "Produkt bearbeiten" : "Neues Produkt"}</h1>
            </div>

            <div className="card">
                <div className="card-header">
                    {isEdit ? "Produkt aktualisieren" : "Produkt erstellen"}
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
                        />
                    </div>

                    <div className="form-group">
                        <label>Beschreibung</label>
                        <textarea
                            className="form-control"
                            value={formData.beschreibung}
                            onChange={(e) => setFormData({ ...formData, beschreibung: e.target.value })}
                            rows="3"
                        />
                    </div>

                    <div className="form-group">
                        <label>Preis (CHF) *</label>
                        <input
                            type="number"
                            step="0.01"
                            className="form-control"
                            value={formData.preis}
                            onChange={(e) => setFormData({ ...formData, preis: e.target.value })}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Bestand *</label>
                        <input
                            type="number"
                            className="form-control"
                            value={formData.bestand}
                            onChange={(e) => setFormData({ ...formData, bestand: e.target.value })}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Kategorie *</label>
                        <select
                            className="form-control"
                            value={formData.kategorie.id}
                            onChange={(e) => setFormData({ ...formData, kategorie: { id: e.target.value } })}
                            required
                        >
                            <option value="">Bitte wählen...</option>
                            {kategorien.map(kat => (
                                <option key={kat.id} value={kat.id}>{kat.name}</option>
                            ))}
                        </select>
                    </div>

                    <div className="form-actions">
                        <button type="button" className="btn btn-secondary" onClick={() => navigate("/produkte")}>
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