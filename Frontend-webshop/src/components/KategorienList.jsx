import { useState, useEffect } from "react";
import apiClient from "../services/api-client";

export default function KategorienList() {
    const [kategorien, setKategorien] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState({ name: '', beschreibung: '' });
    const [errors, setErrors] = useState({});
    const [message, setMessage] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    useEffect(() => {
        loadKategorien();
    }, []);

    const loadKategorien = async () => {
        try {
            setLoading(true);
            // apiClient sendet JWT automatisch mit – kein manuelles Header-Setzen nötig
            const response = await apiClient.get("/kategorien");
            setKategorien(response.data);
            setError(null);
        } catch (err) {
            console.error('Backend Fehler:', err);
            setError('Fehler beim Laden. Läuft das Backend?');
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = () => {
        setEditingId(null);
        setFormData({ name: '', beschreibung: '' });
        setErrors({});
        setShowForm(true);
    };

    const handleEdit = (kategorie) => {
        setEditingId(kategorie.id);
        setFormData({ name: kategorie.name, beschreibung: kategorie.beschreibung || '' });
        setErrors({});
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Wirklich löschen?')) {
            try {
                await apiClient.delete(`/kategorien/${id}`);
                showMessage('Kategorie gelöscht');
                loadKategorien();
            } catch (err) {
                if (err.response?.status === 409) {
                    setError(err.response?.data || 'Kategorie wird noch von Produkten verwendet');
                } else {
                    console.error('Fehler:', err);
                    setError('Fehler beim Löschen: ' + err.message);
                }
            }
        }
    };

    const validate = () => {
        const newErrors = {};
        if (!formData.name.trim()) newErrors.name = 'Name erforderlich';
        if (formData.name.length < 2) newErrors.name = 'Mindestens 2 Zeichen';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validate()) return;
        try {
            if (editingId) {
                await apiClient.put(`/kategorien/${editingId}`, formData);
            } else {
                await apiClient.post("/kategorien", formData);
            }
            showMessage(editingId ? 'Kategorie aktualisiert' : 'Kategorie erstellt');
            setShowForm(false);
            setFormData({ name: '', beschreibung: '' });
            loadKategorien();
        } catch (err) {
            console.error('Fehler:', err);
            setError('Fehler beim Speichern');
        }
    };

    const showMessage = (msg) => {
        setMessage(msg);
        setTimeout(() => setMessage(''), 3000);
    };

    const filteredKategorien = kategorien.filter(k => {
        const q = searchQuery.toLowerCase();
        return (
            k.name?.toLowerCase().includes(q) ||
            k.beschreibung?.toLowerCase().includes(q)
        );
    });

    if (loading) {
        return (
            <div className="loading">
                <div className="spinner"></div>
                <p>Lade Daten vom Backend...</p>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <h1>Kategorien</h1>
                {!showForm && (
                    <button className="btn btn-primary" onClick={handleCreate}>
                        Neue Kategorie
                    </button>
                )}
            </div>

            {!showForm && (
                <div style={{ marginBottom: '1.5rem' }}>
                    <div style={{ position: 'relative' }}>
                        <h3 style={{ color: '#2c3e50' }}>Suchen</h3>

                        <input
                            type="text"
                            className="form-control"
                            placeholder="Kategorien suchen nach Name oder Beschreibung..."
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
                            {filteredKategorien.length} von {kategorien.length} Kategorien gefunden
                        </p>
                    )}
                </div>
            )}

            {message && <div className="alert alert-success">{message}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            {showForm ? (
                <div className="card">
                    <div className="card-header">
                        {editingId ? 'Kategorie bearbeiten' : 'Neue Kategorie'}
                    </div>
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label>Name *</label>
                            <input
                                type="text"
                                className="form-control"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                placeholder="z.B. Elektronik"
                            />
                            {errors.name && <div className="error">{errors.name}</div>}
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
                            <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                                Abbrechen
                            </button>
                            <button type="submit" className="btn btn-primary">
                                Speichern
                            </button>
                        </div>
                    </form>
                </div>
            ) : filteredKategorien.length === 0 ? (
                <div className="card empty-state">
                    <h2>{searchQuery ? 'Keine Kategorien gefunden' : 'Noch keine Kategorien'}</h2>
                    <p>{searchQuery ? 'Versuche einen anderen Suchbegriff.' : 'Erstelle deine erste Kategorie!'}</p>
                </div>
            ) : (
                <table className="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Beschreibung</th>
                            <th>Aktionen</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredKategorien.map(k => (
                            <tr key={k.id}>
                                <td>{k.id}</td>
                                <td>{k.name}</td>
                                <td>{k.beschreibung || '-'}</td>
                                <td>
                                    <div className="action-buttons">
                                        <button className="btn btn-primary btn-sm" onClick={() => handleEdit(k)}>
                                            Bearbeiten
                                        </button>
                                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(k.id)}>
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
