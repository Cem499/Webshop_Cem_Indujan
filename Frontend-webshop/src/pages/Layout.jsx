import { Outlet } from "react-router-dom";
import Navigation from "./Navigation";

export default function Layout() {
    return (
        <div className="app">
            <nav className="navbar">
                <div className="nav-container">
                    <h1 className="nav-logo">
                        <img className="nav-logo-img" src="src/assets/Logo.png" alt="Logo" />

                        <a href="/">Cem Sin Webshop</a>
                    </h1>
                    <Navigation />
                </div>
            </nav>

            <main className="main-content">
                <Outlet />
            </main>

            <footer className="footer">
                <p>© 2026 Webshop – React Frontend</p>
            </footer>
        </div>
    );
}