import { Link } from "react-router-dom";

export default function Home() {
    return (
        <div>
            <div className="home-hero">
                <div className="home-hero__content">
                    <h1 className="home-hero__title">
                        Alles was du<br />
                        <span className="teal">brauchst</span>, online
                    </h1>
                    <p className="home-hero__sub">
                        Entdecke unser breites Sortiment — einfach bestellen,
                        schnell geliefert, sicher bezahlt.
                    </p>
                    <div className="home-hero__actions">
                        <Link to="/produkte" className="btn-hero-primary">
                            Produkte entdecken →
                        </Link>
                        <Link to="/register" className="btn-hero-secondary">
                            Konto erstellen
                        </Link>
                    </div>
                </div>
            </div>

            <div className="home-stats">
                <div className="home-stat">
                    <div className="home-stat__number">100%</div>
                    <div className="home-stat__label">Schweizer Qualität</div>
                </div>
                <div className="home-stat">
                    <div className="home-stat__number">24h</div>
                    <div className="home-stat__label">Schnelle Lieferung</div>
                </div>
                <div className="home-stat">
                    <div className="home-stat__number">SSL</div>
                    <div className="home-stat__label">Sicher & verschlüsselt</div>
                </div>
            </div>

            <div className="features">
                <div className="feature-box">
                    <div className="feature-box__icon">◈</div>
                    <h3>Grosse Auswahl</h3>
                    <p>Produkte aus verschiedenen Kategorien — für jeden Bedarf das Richtige.</p>
                </div>
                <div className="feature-box">
                    <div className="feature-box__icon">⟳</div>
                    <h3>Schnelle Lieferung</h3>
                    <p>Deine Bestellung kommt sicher und pünktlich an.</p>
                </div>
                <div className="feature-box">
                    <div className="feature-box__icon">◎</div>
                    <h3>Einfach Bestellen</h3>
                    <p>Warenkorb füllen, Adresse eingeben, fertig.</p>
                </div>
            </div>

            <div className="home-steps">
                <h2>So funktioniert's</h2>
                <ol className="home-steps__grid">
                    <li>Produkte durchstöbern</li>
                    <li>Zum Warenkorb hinzufügen</li>
                    <li>Lieferadresse eingeben</li>
                    <li>Bestellung aufgeben</li>
                </ol>
            </div>
        </div>
    );
}
