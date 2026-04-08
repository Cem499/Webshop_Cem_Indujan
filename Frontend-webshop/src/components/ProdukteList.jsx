import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../services/api-client";

export default function ProdukteList() {
    const navigate = useNavigate();
    const [produkte, setProdukte] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    useEffect(() => {
        loadProdukte();
    }, []);

    const loadProdukte = async () => {
        try {
            setLoading(true);
            // apiClient sendet JWT automatisch mit – bei 401 wird zur Login-Seite weitergeleitet
            const response = await apiClient.get("/produkte");
            setProdukte(response.data);
            setError(null);
        } catch (err) {
            console.error('Fehler beim Laden:', err);
            setError('Fehler beim Laden. Läuft das Backend auf Port 8081?');
        } finally {
            setLoading(false);
        }
    };

    const deleteProdukt = async (id) => {
        if (!window.confirm("Produkt wirklich löschen?")) return;
        try {
            const response = await apiClient.delete(`/produkte/${id}`);
            if (response.status === 200 || response.status === 204) {
                showMessage('Produkt erfolgreich gelöscht');
                loadProdukte();
            }
        } catch (err) {
            if (err.response?.status === 409) {
                setError(err.response?.data || "Produkt wird noch in Bestellungen verwendet");
            } else {
                console.error('Fehler beim Löschen:', err);
                setError(`Fehler beim Löschen: ${err.message}`);
            }
        }
    };

    function addToCart(produkt) {
        if (produkt.bestand === 0) return;
        const cart = JSON.parse(localStorage.getItem("cart") || "[]");
        const existing = cart.find(item => item.id === produkt.id);
        if (existing) {
            existing.menge += 1;
        } else {
            cart.push({ ...produkt, menge: 1 });
        }
        localStorage.setItem("cart", JSON.stringify(cart));
        showMessage(` ${produkt.name} zum Warenkorb hinzugefügt!`);
        window.dispatchEvent(new Event("storage"));
    }

    const showMessage = (msg) => {
        setMessage(msg);
        setError(null);
        setTimeout(() => setMessage(''), 3000);
    };

    const filteredProdukte = produkte.filter(prod => {
        const q = searchQuery.toLowerCase();
        return (
            prod.name?.toLowerCase().includes(q) ||
            prod.beschreibung?.toLowerCase().includes(q) ||
            prod.kategorie?.name?.toLowerCase().includes(q)
        );
    });

    if (loading) {
        return (
            <div className="loading">
                <div className="spinner"></div>
                <p>Lade Produkte...</p>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <h1>Produkte</h1>
                <button className="btn btn-primary" onClick={() => navigate("/new-produkt")}>
                    Neues Produkt
                </button>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
                <div style={{ position: 'relative' }}>
                    <h3 style={{ color: '#2c3e50' }}>Suchen</h3>

                    <input
                        type="text"
                        className="form-control"
                        placeholder="Produkte suchen nach Name, Beschreibung oder Kategorie..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ paddingLeft: '2.5rem', paddingRight: searchQuery ? '2.5rem' : '0.75rem' }}
                    />
                    {searchQuery && (
                        <button onClick={() => setSearchQuery('')} style={{
                            position: 'absolute', right: '0.75rem', top: '50%',
                            transform: 'translateY(-50%)', background: 'none',
                            border: 'none', cursor: 'pointer', color: '#95a5a6', fontSize: '1rem', padding: 0
                        }}>✕</button>
                    )}
                </div>
                {searchQuery && (
                    <p style={{ marginTop: '0.5rem', color: '#7f8c8d', fontSize: '0.875rem' }}>
                        {filteredProdukte.length} von {produkte.length} Produkte gefunden
                    </p>
                )}
            </div>

            {message && <div className="alert alert-success">{message}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            {filteredProdukte.length === 0 ? (
                <div className="card empty-state">
                    <h2>{searchQuery ? 'Keine Produkte gefunden' : 'Keine Produkte vorhanden'}</h2>
                    {searchQuery && <p>Versuche einen anderen Suchbegriff.</p>}
                </div>
            ) : (
                <table className="table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Preis</th>
                            <th>Bestand</th>
                            <th>Kategorie</th>
                            <th>Aktionen</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredProdukte.map(prod => (
                            <tr key={prod.id}>
                                <td>
                                    <strong>{prod.name}</strong>
                                    {prod.beschreibung && <><br /><small>{prod.beschreibung}</small></>}
                                </td>
                                <td>CHF {parseFloat(prod.preis).toFixed(2)}</td>
                                <td style={{ color: prod.bestand > 0 ? 'green' : 'red' }}>{prod.bestand}</td>
                                <td>{prod.kategorie?.name || '-'}</td>
                                <td>
                                    <div className="action-buttons">
                                        <button className="btn btn-success btn-sm" onClick={() => addToCart(prod)} disabled={prod.bestand === 0}>
                                            In den Warenkorb
                                        </button>
                                        <button className="btn btn-primary btn-sm" onClick={() => navigate(`/edit-produkt/${prod.id}`)}>
                                            Bearbeiten
                                        </button>
                                        <button className="btn btn-danger btn-sm" onClick={() => deleteProdukt(prod.id)}>
                                            Löschen
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
