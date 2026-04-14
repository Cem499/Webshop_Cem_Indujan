import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../services/api-client";
import { useAuth } from "../context/AuthContext";
import AuthRequiredModal from "./AuthRequiredModal";
import stuhlImg from "../assets/Stuhl.jpg";
import esstischImg from "../assets/Esstisch.jpg";

// Keyword → Bild Mapping: Produktname wird gegen diese Keywords geprüft (lowercase, Teilübereinstimmung).
// Neue Produkte mit Bild: Keyword hier eintragen und Import oben hinzufügen.
const produktBilder = {
    stuhl: stuhlImg,
    tisch: esstischImg,
};

// Sucht das passende Bild anhand eines Keywords im Produktnamen.
// Gibt null zurück wenn kein Keyword übereinstimmt → Fallback-Emoji wird angezeigt.
function getProduktBild(name) {
    if (!name) return null;
    const key = name.toLowerCase();
    for (const [keyword, img] of Object.entries(produktBilder)) {
        if (key.includes(keyword)) return img;
    }
    return null;
}

export default function ProdukteList() {
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuth();

    const [produkte, setProdukte] = useState([]);
    const [kategorien, setKategorien] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [kategorieFilter, setKategorieFilter] = useState('ALLE');
    const [mengen, setMengen] = useState({});
    const [showAuthModal, setShowAuthModal] = useState(false);

    const isAdmin = user?.role === "ADMIN";

    useEffect(() => {
        Promise.all([
            apiClient.get("/produkte"),
            apiClient.get("/kategorien")
        ]).then(([produkteRes, kategorienRes]) => {
            setProdukte(produkteRes.data);
            setKategorien(kategorienRes.data);
        }).catch(err => {
            console.error('Fehler beim Laden:', err);
            setError('Fehler beim Laden. Läuft das Backend auf Port 8081?');
        }).finally(() => setLoading(false));
    }, []);

    const deleteProdukt = async (id) => {
        if (!window.confirm("Produkt wirklich löschen?")) return;
        try {
            await apiClient.delete(`/produkte/${id}`);
            setProdukte(prev => prev.filter(p => p.id !== id));
            showMessage('Produkt erfolgreich gelöscht');
        } catch (err) {
            if (err.response?.status === 409) {
                setError(err.response?.data || "Produkt wird noch in Bestellungen verwendet");
            } else {
                setError(`Fehler beim Löschen: ${err.message}`);
            }
        }
    };

    function getMenge(produktId) {
        return mengen[produktId] || 1;
    }

    function setMenge(produktId, value, maxBestand) {
        const val = Math.max(1, Math.min(parseInt(value) || 1, maxBestand));
        setMengen(prev => ({ ...prev, [produktId]: val }));
    }

    function addToCart(produkt) {
        if (!isAuthenticated) {
            setShowAuthModal(true);
            return;
        }
        if (produkt.bestand === 0) return;

        // Warenkorb ist pro User getrennt – cart_<userId> verhindert Vermischung bei mehreren Usern
        const cartKey = `cart_${user.id}`;
        const menge = getMenge(produkt.id);
        const cart = JSON.parse(localStorage.getItem(cartKey) || "[]");
        const existing = cart.find(item => item.id === produkt.id);
        if (existing) {
            // Bestehende Position: Menge erhöhen, aber nicht über den Bestand hinaus
            existing.menge = Math.min(existing.menge + menge, produkt.bestand);
        } else {
            cart.push({ ...produkt, menge });
        }
        localStorage.setItem(cartKey, JSON.stringify(cart));
        showMessage(`${produkt.name} (${menge}×) zum Warenkorb hinzugefügt!`);
        // Manuell gefeuert damit useCartCount() in Navigation.jsx sofort reagiert,
        // da localStorage.setItem keinen storage-Event im selben Tab auslöst.
        window.dispatchEvent(new Event("storage"));
    }

    const showMessage = (msg) => {
        setMessage(msg);
        setError(null);
        setTimeout(() => setMessage(''), 3000);
    };

    // Kombinierter Filter: Suchtext (Name, Beschreibung, Kategorie) UND Kategorie-Dropdown müssen beide passen
    const filteredProdukte = produkte.filter(prod => {
        const q = searchQuery.toLowerCase();
        const matchesSearch = (
            prod.name?.toLowerCase().includes(q) ||
            prod.beschreibung?.toLowerCase().includes(q) ||
            prod.kategorie?.name?.toLowerCase().includes(q)
        );
        // Kategorie-ID als String vergleichen, da select-value immer String ist
        const matchesKategorie = kategorieFilter === 'ALLE' || String(prod.kategorie?.id) === kategorieFilter;
        return matchesSearch && matchesKategorie;
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
            {showAuthModal && (
                <AuthRequiredModal
                    onClose={() => setShowAuthModal(false)}
                    message="Um Produkte in den Warenkorb zu legen und zu bestellen, musst du angemeldet sein."
                />
            )}

            <div className="page-header">
                <h1>Produkte</h1>
                {isAdmin && (
                    <button className="btn btn-primary" onClick={() => navigate("/new-produkt")}>
                        + Neues Produkt
                    </button>
                )}
            </div>

            {/* Such- und Filterleiste */}
            <div className="produkte-filter">
                <input
                    type="text"
                    className="form-control"
                    placeholder="Suchen nach Name, Beschreibung..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    style={{ flex: 1 }}
                />
                <select
                    className="form-control"
                    value={kategorieFilter}
                    onChange={(e) => setKategorieFilter(e.target.value)}
                    style={{ width: 'auto', minWidth: '160px' }}
                >
                    <option value="ALLE">Alle Kategorien</option>
                    {kategorien.map(k => (
                        <option key={k.id} value={String(k.id)}>{k.name}</option>
                    ))}
                </select>
                {(searchQuery || kategorieFilter !== 'ALLE') && (
                    <button className="btn btn-secondary btn-sm" onClick={() => { setSearchQuery(''); setKategorieFilter('ALLE'); }}>
                        Filter zurücksetzen
                    </button>
                )}
            </div>

            {(searchQuery || kategorieFilter !== 'ALLE') && (
                <p style={{ marginBottom: '1rem', color: '#7f8c8d', fontSize: '0.875rem' }}>
                    {filteredProdukte.length} von {produkte.length} Produkte gefunden
                </p>
            )}

            {!isAuthenticated && (
                <div className="alert" style={{
                    background: '#e8f4f8', border: '1px solid #3498db',
                    color: '#2980b9', marginBottom: '1rem'
                }}>
                    Melde dich an, um Produkte in den Warenkorb zu legen.
                </div>
            )}

            {message && <div className="alert alert-success">{message}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            {filteredProdukte.length === 0 ? (
                <div className="card empty-state">
                    <h2>{searchQuery || kategorieFilter !== 'ALLE' ? 'Keine Produkte gefunden' : 'Keine Produkte vorhanden'}</h2>
                    {(searchQuery || kategorieFilter !== 'ALLE') && <p>Versuche einen anderen Suchbegriff oder Filter.</p>}
                </div>
            ) : (
                <div className="produkte-grid">
                    {filteredProdukte.map(prod => {
                        const bild = getProduktBild(prod.name);
                        return (
                            <div key={prod.id} className={`produkt-card${prod.bestand === 0 ? ' produkt-card--ausverkauft' : ''}`}>
                                <div className="produkt-card__image">
                                    {bild ? (
                                        <img
                                            src={bild}
                                            alt={prod.name}
                                            style={{ width: '100%', height: '100%', objectFit: 'contain', padding: '0.5rem' }}
                                        />
                                    ) : (
                                        <span className="produkt-card__emoji">🛍️</span>
                                    )}
                                    {prod.bestand === 0 && (
                                        <span className="produkt-card__badge produkt-card__badge--out">Ausverkauft</span>
                                    )}
                                    {prod.bestand > 0 && prod.bestand <= 5 && (
                                        <span className="produkt-card__badge produkt-card__badge--low">Nur noch {prod.bestand}!</span>
                                    )}
                                </div>

                                <div className="produkt-card__body">
                                    {prod.kategorie && (
                                        <span className="produkt-card__kategorie">{prod.kategorie.name}</span>
                                    )}
                                    <h3 className="produkt-card__name">{prod.name}</h3>
                                    {prod.beschreibung && (
                                        <p className="produkt-card__beschreibung">{prod.beschreibung}</p>
                                    )}
                                </div>

                                <div className="produkt-card__footer">
                                    <div className="produkt-card__preis">
                                        CHF {parseFloat(prod.preis).toFixed(2)}
                                    </div>

                                    {prod.bestand > 0 ? (
                                        <>
                                            <div className="produkt-card__menge">
                                                <button
                                                    className="menge-btn"
                                                    onClick={() => setMenge(prod.id, getMenge(prod.id) - 1, prod.bestand)}
                                                    disabled={getMenge(prod.id) <= 1}
                                                >−</button>
                                                <input
                                                    type="number"
                                                    className="menge-input"
                                                    value={getMenge(prod.id)}
                                                    onChange={(e) => setMenge(prod.id, e.target.value, prod.bestand)}
                                                    min="1"
                                                    max={prod.bestand}
                                                />
                                                <button
                                                    className="menge-btn"
                                                    onClick={() => setMenge(prod.id, getMenge(prod.id) + 1, prod.bestand)}
                                                    disabled={getMenge(prod.id) >= prod.bestand}
                                                >+</button>
                                            </div>
                                            <button
                                                className="btn btn-success"
                                                style={{ width: '100%' }}
                                                onClick={() => addToCart(prod)}
                                            >
                                                In den Warenkorb
                                            </button>
                                        </>
                                    ) : (
                                        <button className="btn btn-secondary" style={{ width: '100%' }} disabled>
                                            Nicht verfügbar
                                        </button>
                                    )}

                                    {isAdmin && (
                                        <div className="action-buttons" style={{ marginTop: '0.5rem' }}>
                                            <button className="btn btn-primary btn-sm" onClick={() => navigate(`/edit-produkt/${prod.id}`)}>
                                                Bearbeiten
                                            </button>
                                            <button className="btn btn-danger btn-sm" onClick={() => deleteProdukt(prod.id)}>
                                                Löschen
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
