import { useState, useEffect } from "react";
import apiClient from "../services/api-client";

// Admin-Ansicht: zeigt alle Bestellungen aller Kunden.
// Nur für ADMIN zugänglich – ProtectedRoute und @PreAuthorize im Backend schützen diesen Bereich.
export default function BestellungenList() {
    const [bestellungen, setBestellungen] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALLE');

    useEffect(() => {
        loadBestellungen();
    }, []);

    const loadBestellungen = async () => {
        try {
            setLoading(true);
            // apiClient sendet JWT automatisch – Backend prüft ADMIN-Rolle
            const response = await apiClient.get("/bestellungen");
            setBestellungen(response.data);
            setError(null);
        } catch (err) {
            console.error('Fehler beim Laden:', err);
            setError('Fehler beim Laden der Bestellungen.');
        } finally {
            setLoading(false);
        }
    };

    const updateStatus = async (id, newStatus) => {
        try {
            const bestellung = bestellungen.find(b => b.id === id);
            await apiClient.put(`/bestellungen/${id}`, { ...bestellung, status: newStatus });
            showMessage(`Status auf ${newStatus} gesetzt`);
            loadBestellungen();
        } catch (err) {
            setError('Fehler beim Aktualisieren: ' + err.message);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Bestellung wirklich löschen?')) return;
        try {
            await apiClient.delete(`/bestellungen/${id}`);
            showMessage('Bestellung gelöscht');
            loadBestellungen();
        } catch (err) {
            if (err.response?.status === 409) {
                setError(err.response?.data || 'Bestellung kann nicht gelöscht werden');
            } else {
                setError('Fehler beim Löschen: ' + err.message);
            }
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const formatDate = (dateString) => new Date(dateString).toLocaleString('de-CH');

    const statusBadgeStyle = (status) => ({
        padding: '0.25rem 0.5rem',
        borderRadius: '4px',
        fontSize: '0.8rem',
        background: status === 'OFFEN' ? '#fff3cd' : status === 'BEZAHLT' ? '#d4edda' : '#f8d7da',
        color: status === 'OFFEN' ? '#856404' : status === 'BEZAHLT' ? '#155724' : '#721c24'
    });

    const filteredBestellungen = statusFilter === 'ALLE'
        ? bestellungen
        : bestellungen.filter(b => b.status === statusFilter);

    if (loading) {
        return (
            <div className="loading">
                <div className="spinner"></div>
                <p>Lade Bestellungen...</p>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <h1>Alle Bestellungen (Admin)</h1>
                <select
                    className="form-control"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    style={{ width: 'auto' }}
                >
                    <option value="ALLE">Alle Status</option>
                    <option value="OFFEN">Offen</option>
                    <option value="BEZAHLT">Bezahlt</option>
                    <option value="STORNIERT">Storniert</option>
                </select>
            </div>

            {message && <div className="alert alert-success">{message}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            {filteredBestellungen.length === 0 ? (
                <div className="card empty-state">
                    <h2>Keine Bestellungen gefunden</h2>
                </div>
            ) : (
                <table className="table">
                    <thead>
                        <tr>
                            <th>Nr.</th>
                            <th>Kunde</th>
                            <th>Erstellt am</th>
                            <th>Betrag</th>
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
                                <td>CHF {parseFloat(b.gesamtbetrag).toFixed(2)}</td>
                                <td><span style={statusBadgeStyle(b.status)}>{b.status}</span></td>
                                <td>
                                    <div className="action-buttons">
                                        {b.status === 'OFFEN' && (
                                            <button className="btn btn-success btn-sm"
                                                onClick={() => updateStatus(b.id, 'BEZAHLT')}>
                                                Bezahlt
                                            </button>
                                        )}
                                        {b.status !== 'STORNIERT' && (
                                            <button className="btn btn-secondary btn-sm"
                                                onClick={() => updateStatus(b.id, 'STORNIERT')}>
                                                Stornieren
                                            </button>
                                        )}
                                        <button className="btn btn-danger btn-sm"
                                            onClick={() => handleDelete(b.id)}>
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
