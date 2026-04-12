import { Outlet } from "react-router-dom";
import Navigation from "./Navigation";
import logoImg from "../assets/Logo.png";

export default function Layout() {
    return (
        <div className="app">
            <nav className="navbar">
                <div className="nav-container">
                    <h1 className="nav-logo">
                        <img className="nav-logo-img" src={logoImg} alt="Logo" />

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