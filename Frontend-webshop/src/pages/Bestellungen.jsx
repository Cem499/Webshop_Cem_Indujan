import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import apiClient from "../services/api-client";
import { formatDate, statusBadgeStyle } from "../utils/formatters";

export default function Bestellungen() {
    const location = useLocation();
    const [bestellungen, setBestellungen] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [positionen, setPositionen] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    // successMessage wird von Warenkorb.jsx per location.state übergeben nach erfolgreicher Bestellung
    const [message, setMessage] = useState(location.state?.successMessage || '');
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALLE');

    useEffect(() => {
        loadBestellungen();
    }, []);

    const loadBestellungen = async () => {
        try {
            setLoading(true);
            // /bestellungen/meine gibt immer nur eigene Bestellungen zurück (ADMIN + KUNDE)
            const response = await apiClient.get("/bestellungen/meine");
            setBestellungen(response.data);
            setError(null);
        } catch (err) {
            console.error('Fehler beim Laden:', err);
            setError('Fehler beim Laden. Läuft das Backend auf Port 8081?');
        } finally {
            setLoading(false);
        }
    };

    const loadPositionen = async (bestellungId) => {
        try {
            const response = await apiClient.get(`/bestellpositionen/bestellung/${bestellungId}`);
            setPositionen(response.data);
        } catch (err) {
            console.error('Fehler beim Laden der Positionen:', err);
            setError('Fehler beim Laden der Positionen');
        }
    };

    const handleSelectBestellung = (bestellung) => {
        setSelectedId(bestellung.id);
        loadPositionen(bestellung.id);
    };

    const handleStornieren = async (id) => {
        if (window.confirm('Bestellung wirklich stornieren?')) {
            try {
                await apiClient.patch(`/bestellungen/${id}/status?status=STORNIERT`);
                showMessage('Bestellung erfolgreich storniert');
                setSelectedId(null);
                loadBestellungen();
            } catch (err) {
                setError(`Fehler beim Stornieren: ${err.message}`);
            }
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const filteredBestellungen = bestellungen.filter(b => {
        const q = searchQuery.toLowerCase();
        const matchesSearch = (
            b.kundenName?.toLowerCase().includes(q) ||
            b.kundenEmail?.toLowerCase().includes(q) ||
            b.lieferStadt?.toLowerCase().includes(q) ||
            String(b.id).includes(q)
        );
        const matchesStatus = statusFilter === 'ALLE' || b.status === statusFilter;
        return matchesSearch && matchesStatus;
    });

    const selected = bestellungen.find(b => b.id === selectedId);

    if (loading) {
        return (
            <div className="loading">
                <div className="spinner"></div>
                <p>Lade Daten vom Backend...</p>
            </div>
        );
    }

    if (selected) {
        return (
            <div>
                <div className="page-header">
                    <h1>Bestellung #{selected.id}</h1>
                    <button className="btn btn-secondary" onClick={() => setSelectedId(null)}>← Zurück</button>
                </div>
                {message && <div className="alert alert-success">{message}</div>}
                {error && <div className="alert alert-error">{error}</div>}
                <div className="card">
                    <h3>Kundendaten</h3>
                    <p><strong>Name:</strong> {selected.kundenName}</p>
                    <p><strong>Email:</strong> {selected.kundenEmail}</p>
                    <p><strong>Erstellt am:</strong> {formatDate(selected.erstelltAm)}</p>
                    <p><strong>Status:</strong> <span style={statusBadgeStyle(selected.status)}>{selected.status}</span></p>
                </div>
                <div className="card">
                    <h3>Lieferadresse</h3>
                    <p>{selected.lieferStrasse}</p>
                    <p>{selected.lieferPlz} {selected.lieferStadt}</p>
                    <p>{selected.lieferLand}</p>
                </div>
                <div className="card">
                    <h3>Bestellpositionen</h3>
                    {positionen.length === 0 ? (
                        <p>Keine Positionen gefunden</p>
                    ) : (
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Produkt</th>
                                    <th>Einzelpreis</th>
                                    <th>Menge</th>
                                    <th>Zwischensumme</th>
                                </tr>
                            </thead>
                            <tbody>
                                {positionen.map((pos) => (
                                    <tr key={pos.id}>
                                        <td>{pos.produkt?.name || 'Unbekannt'}</td>
                                        <td>CHF {parseFloat(pos.einzelpreis).toFixed(2)}</td>
                                        <td>{pos.menge}</td>
                                        <td>CHF {(parseFloat(pos.einzelpreis) * pos.menge).toFixed(2)}</td>
                                    </tr>
                                ))}
                                <tr style={{ fontWeight: 'bold', background: '#f8f9fa' }}>
                                    <td colSpan="3">Gesamtbetrag</td>
                                    <td>CHF {parseFloat(selected.gesamtbetrag).toFixed(2)}</td>
                                </tr>
                            </tbody>
                        </table>
                    )}
                </div>
                <div className="action-buttons">
                    {selected.status === 'OFFEN' && (
                        <button className="btn btn-danger" onClick={() => handleStornieren(selected.id)}>
                            Bestellung stornieren
                        </button>
                    )}
                </div>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <h1>Meine Bestellungen</h1>
            </div>

            <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-start' }}>
                <div style={{ flex: '1', minWidth: '250px' }}>
                    <h3 style={{ color: '#2c3e50' }}>Suchen</h3>
                    <div style={{ position: 'relative' }}>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Bestellungen suchen nach Name, Email oder Nr...."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            style={{ paddingRight: searchQuery ? '2.5rem' : '0.75rem' }}
                        />
                        {searchQuery && (
                            <button onClick={() => setSearchQuery('')} style={{
                                position: 'absolute', right: '0.75rem', top: '50%',
                                transform: 'translateY(-50%)', background: 'none',
                                border: 'none', cursor: 'pointer', color: '#95a5a6', fontSize: '1rem', padding: 0
                            }}>✕</button>
                        )}
                    </div>
                </div>
                <select
                    className="form-control"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    style={{ width: 'auto', minWidth: '150px', marginTop: '1.8rem' }}
                >
                    <option value="ALLE">Alle Status</option>
                    <option value="OFFEN">Offen</option>
                    <option value="BEZAHLT">Bezahlt</option>
                    <option value="STORNIERT">Storniert</option>
                </select>
            </div>
            {(searchQuery || statusFilter !== 'ALLE') && (
                <p style={{ marginBottom: '1rem', color: '#7f8c8d', fontSize: '0.875rem' }}>
                    {filteredBestellungen.length} von {bestellungen.length} Bestellungen gefunden
                </p>
            )}

            {message && <div className="alert alert-success">{message}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            {filteredBestellungen.length === 0 ? (
                <div className="card empty-state">
                    <h2>{searchQuery || statusFilter !== 'ALLE' ? 'Keine Bestellungen gefunden' : 'Noch keine Bestellungen'}</h2>
                    <p>{searchQuery || statusFilter !== 'ALLE' ? 'Versuche einen anderen Suchbegriff oder Filter.' : 'Lege Produkte in den Warenkorb und bestelle!'}</p>
                </div>
            ) : (
                <table className="table">
                    <thead>
                        <tr>
                            <th>Nr.</th>
                            <th>Kunde</th>
                            <th>Erstellt am</th>
                            <th>Gesamtbetrag</th>
                            <th>Status</th>
                            <th>Aktionen</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredBestellungen.map(b => (
                            <tr key={b.id}>
                                <td>{b.id}</td>
                                <td>
                                    <strong>{b.kundenName}</strong><br />
                                    <small>{b.kundenEmail}</small>
                                </td>
                                <td>{formatDate(b.erstelltAm)}</td>
                                <td><strong>CHF {parseFloat(b.gesamtbetrag).toFixed(2)}</strong></td>
                                <td><span style={statusBadgeStyle(b.status)}>{b.status}</span></td>
                                <td>
                                    <div className="action-buttons">
                                        <button className="btn btn-primary btn-sm" onClick={() => handleSelectBestellung(b)}>Details</button>
                                        {b.status === 'OFFEN' && (
                                            <button className="btn btn-danger btn-sm" onClick={() => handleStornieren(b.id)}>
                                                Stornieren
                                            </button>
                                        )}
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
