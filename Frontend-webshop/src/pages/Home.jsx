export default function Home() {
    return (
        <div>
            <div className="card" style={{
                background: 'linear-gradient(140deg, #ffffff 10%, #2c3e50 50%)',
                color: 'white',
                textAlign: 'center',
                padding: '3rem',
                marginBottom: '2rem'
            }}>
                <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>
                    Willkommen im Cem Sin Webshop!
                </h1>
                <p style={{ fontSize: '1.2rem' }}>
                    Dein Online-Shop für alles was du brauchst
                </p>
            </div>

            <div className="features">
                <div className="feature-box">
                    <h3>Grosse Auswahl</h3>
                    <p>Produkte aus verschiedenen Kategorien</p>
                </div>
                <div className="feature-box">
                    <h3>Schnelle Lieferung</h3>
                    <p>Deine Bestellung kommt sicher an</p>
                </div>
                <div className="feature-box">
                    <h3>Einfach Bestellen</h3>
                    <p>Sicher und unkompliziert</p>
                </div>
            </div>

            <div className="card">
                <h2 style={{ textAlign: 'center', marginBottom: '1rem' }}>
                    So funktioniert's
                </h2>
                <ol
                    style={{
                        padding: '0 1rem',
                        maxWidth: '600px',
                        fontSize: '1.1rem',
                        lineHeight: '2',
                        margin: '0 auto',
                        paddingLeft: '180px'
                    }}
                >
                    <li>Kategorien durchstöbern</li>
                    <li>Produkte zum Warenkorb hinzufügen</li>
                    <li>Bestellung mit Lieferadresse aufgeben</li>
                    <li>Fertig!</li>
                </ol>
            </div>
        </div>
    );
}