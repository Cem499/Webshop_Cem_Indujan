import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../services/api-client";
import { useAuth } from "../context/AuthContext";

export default function Warenkorb() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [cart, setCart] = useState([]);
    const [showCheckout, setShowCheckout] = useState(false);
    const [formData, setFormData] = useState({
        kundenName: "",
        kundenEmail: "",
        lieferStrasse: "",
        lieferPlz: "",
        lieferStadt: "",
        lieferLand: "Schweiz"
    });

    // Pro-User-Key verhindert Datenvermischung wenn mehrere Accounts denselben Browser nutzen
    const cartKey = user ? `cart_${user.id}` : null;

    useEffect(() => {
        if (!cartKey) return;
        const data = JSON.parse(localStorage.getItem(cartKey) || "[]");
        setCart(data);
        // Benutzerdaten vorausfüllen wenn eingeloggt
        if (user) {
            setFormData(prev => ({
                ...prev,
                kundenName: user.username || prev.kundenName,
                kundenEmail: user.email || prev.kundenEmail
            }));
        }
    }, [user, cartKey]);

    function updateMenge(id, menge) {
        if (menge <= 0) return removeItem(id);

        const updated = cart.map(item =>
            item.id === id ? { ...item, menge: Math.min(menge, item.bestand) } : item
        );
        localStorage.setItem(cartKey, JSON.stringify(updated));
        setCart(updated);
    }

    function removeItem(id) {
        const updated = cart.filter(item => item.id !== id);
        localStorage.setItem(cartKey, JSON.stringify(updated));
        setCart(updated);
    }

    function clearCart() {
        if (window.confirm("Warenkorb leeren?")) {
            localStorage.removeItem(cartKey);
            setCart([]);
        }
    }

    function getTotal() {
        return cart.reduce((sum, item) => sum + (item.preis * item.menge), 0);
    }

    async function handleCheckout(event) {
        event.preventDefault();

        // Gesamtbetrag wird vom Backend nach dem Erstellen der Positionen berechnet – hier mit 0 initialisieren
        const bestellungData = { ...formData, status: "OFFEN", gesamtbetrag: 0 };

        try {
            // Schritt 1: Bestellung erstellen – apiClient sendet JWT, Backend setzt den Owner auf den eingeloggten User
            const response = await apiClient.post("/bestellungen", bestellungData);
            const bestellung = response.data;

            // Schritt 2: Alle Positionen parallel anlegen – Backend reduziert den Bestand pro Position
            const positionRequests = cart.map(item =>
                apiClient.post("/bestellpositionen", {
                    bestellung: { id: bestellung.id },
                    produkt: { id: item.id },
                    menge: item.menge,
                    einzelpreis: item.preis
                })
            );
            await Promise.all(positionRequests);

            localStorage.removeItem(cartKey);
            // Storage-Event manuell auslösen damit die Warenkorb-Badge in Navigation sofort 0 zeigt
            window.dispatchEvent(new Event("storage"));
            // Erfolgstext per location.state übergeben – wird in Bestellungen.jsx ausgelesen
            navigate("/bestellungen", { state: { successMessage: "Bestellung erfolgreich aufgegeben!" } });
        } catch (error) {
            console.error("Fehler bei Bestellung:", error);
            alert("Fehler beim Aufgeben der Bestellung. Bitte versuche es erneut.");
        }
    }

    if (showCheckout) {
        return (
            <div>
                <div className="page-header">
                    <h1>Kasse</h1>
                    <button className="btn btn-secondary" onClick={() => setShowCheckout(false)}>
                        ← Zurück
                    </button>
                </div>

                <div className="card">
                    <h3>Lieferadresse</h3>
                    <form onSubmit={handleCheckout}>
                        <div className="form-group">
                            <label>Name *</label>
                            <input type="text" className="form-control" value={formData.kundenName}
                                onChange={(e) => setFormData({ ...formData, kundenName: e.target.value })} required />
                        </div>
                        <div className="form-group">
                            <label>Email *</label>
                            <input type="email" className="form-control" value={formData.kundenEmail}
                                onChange={(e) => setFormData({ ...formData, kundenEmail: e.target.value })} required />
                        </div>
                        <div className="form-group">
                            <label>Strasse *</label>
                            <input type="text" className="form-control" value={formData.lieferStrasse}
                                onChange={(e) => setFormData({ ...formData, lieferStrasse: e.target.value })} required />
                        </div>
                        <div className="form-group">
                            <label>PLZ *</label>
                            <input type="text" className="form-control" value={formData.lieferPlz}
                                onChange={(e) => setFormData({ ...formData, lieferPlz: e.target.value })} required />
                        </div>
                        <div className="form-group">
                            <label>Stadt *</label>
                            <input type="text" className="form-control" value={formData.lieferStadt}
                                onChange={(e) => setFormData({ ...formData, lieferStadt: e.target.value })} required />
                        </div>
                        <div className="form-group">
                            <label>Land *</label>
                            <input type="text" className="form-control" value={formData.lieferLand}
                                onChange={(e) => setFormData({ ...formData, lieferLand: e.target.value })} />
                        </div>

                        <div className="card" style={{ background: '#f8f9fa', marginTop: '1rem' }}>
                            <h4>Bestellübersicht</h4>
                            <p><strong>Gesamtbetrag: CHF {getTotal().toFixed(2)}</strong></p>
                        </div>

                        <div className="form-actions">
                            <button type="button" className="btn btn-secondary" onClick={() => setShowCheckout(false)}>
                                Abbrechen
                            </button>
                            <button type="submit" className="btn btn-success">
                                Jetzt bestellen
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <h1>Warenkorb</h1>
                {cart.length > 0 && (
                    <button className="btn btn-danger" onClick={clearCart}>
                        Warenkorb leeren
                    </button>
                )}
            </div>

            {cart.length === 0 ? (
                <div className="card empty-state">
                    <h2>Warenkorb ist leer</h2>
                    <p>Füge Produkte hinzu um zu bestellen</p>
                </div>
            ) : (
                <>
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Produkt</th>
                                <th>Preis</th>
                                <th>Menge</th>
                                <th>Zwischensumme</th>
                                <th>Aktionen</th>
                            </tr>
                        </thead>
                        <tbody>
                            {cart.map(item => (
                                <tr key={item.id}>
                                    <td><strong>{item.name}</strong></td>
                                    <td>CHF {item.preis.toFixed(2)}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                                            <button className="btn btn-sm" onClick={() => updateMenge(item.id, item.menge - 1)}>-</button>
                                            <input
                                                type="number"
                                                value={item.menge}
                                                onChange={(e) => updateMenge(item.id, parseInt(e.target.value) || 1)}
                                                style={{ width: '60px', textAlign: 'center', padding: '0.25rem' }}
                                                min="1"
                                                max={item.bestand}
                                            />
                                            <button className="btn btn-sm" onClick={() => updateMenge(item.id, item.menge + 1)}
                                                disabled={item.menge >= item.bestand}>+</button>
                                        </div>
                                        <small style={{ color: '#7f8c8d' }}>Max: {item.bestand}</small>
                                    </td>
                                    <td><strong>CHF {(item.preis * item.menge).toFixed(2)}</strong></td>
                                    <td>
                                        <button className="btn btn-danger btn-sm" onClick={() => removeItem(item.id)}>
                                            Entfernen
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>

                    <div className="card" style={{ textAlign: 'right' }}>
                        <h2>Gesamtbetrag: CHF {getTotal().toFixed(2)}</h2>
                        <button className="btn btn-success" onClick={() => setShowCheckout(true)} style={{ marginTop: '1rem' }}>
                            Zur Kasse →
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}
