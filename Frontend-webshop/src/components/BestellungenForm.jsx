import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";

export default function BestellungenForm() {
    const navigate = useNavigate();
    const { id } = useParams();

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

        fetch(`http://localhost:8081/api/bestellungen/${id}`)
            .then(res => {
                if (!res.ok) throw new Error("Fehler beim Laden");
                return res.json();
            })
            .then(data => setFormData(data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));

    }, [id, isEditMode]);


    function handleChange(e) {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    }


    function handleSubmit(e) {
        e.preventDefault();

        const method = isEditMode ? "PUT" : "POST";
        const url = isEditMode
            ? `http://localhost:8081/api/bestellungen/${id}`
            : `http://localhost:8081/api/bestellungen`;

        fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(formData)
        })
            .then(res => {
                if (!res.ok) throw new Error("Fehler beim Speichern");
                navigate("/bestellungen");
            })
            .catch(err => setError(err.message));
    }


    function handleDelete() {
        if (!window.confirm("Wirklich löschen?")) return;

        fetch(`http://localhost:8081/api/bestellungen/${id}`, {
            method: "DELETE"
        })
            .then(res => {
                if (!res.ok) throw new Error("Fehler beim Löschen");
                navigate("/bestellungen");
            })
            .catch(err => setError(err.message));
    }

    if (loading) return <p>Lade...</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <div>
            <h1>
                {isEditMode ? "Bestellung bearbeiten" : "Neue Bestellung"}
            </h1>

            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    name="kundenName"
                    placeholder="Kundenname"
                    value={formData.kundenName}
                    onChange={handleChange}
                    required
                />

                <input
                    type="email"
                    name="kundenEmail"
                    placeholder="Email"
                    value={formData.kundenEmail}
                    onChange={handleChange}
                    required
                />

                <input
                    type="text"
                    name="lieferStrasse"
                    placeholder="Strasse"
                    value={formData.lieferStrasse}
                    onChange={handleChange}
                    required
                />

                <input
                    type="text"
                    name="lieferPlz"
                    placeholder="PLZ"
                    value={formData.lieferPlz}
                    onChange={handleChange}
                    required
                />

                <input
                    type="text"
                    name="lieferStadt"
                    placeholder="Stadt"
                    value={formData.lieferStadt}
                    onChange={handleChange}
                    required
                />

                <input
                    type="text"
                    name="lieferLand"
                    placeholder="Land"
                    value={formData.lieferLand}
                    onChange={handleChange}
                    required
                />

                <select
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                >
                    <option value="OFFEN">Offen</option>
                    <option value="BEZAHLT">Bezahlt</option>
                    <option value="STORNIERT">Storniert</option>
                </select>

                <div style={{ marginTop: "20px" }}>
                    <button type="submit">
                        {isEditMode ? "Aktualisieren" : "Erstellen"}
                    </button>

                    <button
                        type="button"
                        onClick={() => navigate("/bestellungen")}
                        style={{ marginLeft: "10px" }}
                    >
                        Abbrechen
                    </button>

                    {isEditMode && (
                        <button
                            type="button"
                            onClick={handleDelete}
                            style={{
                                marginLeft: "10px",
                                backgroundColor: "red",
                                color: "white"
                            }}
                        >
                            Löschen
                        </button>
                    )}
                </div>
            </form>
        </div>
    );
}
