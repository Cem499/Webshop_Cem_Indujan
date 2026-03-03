import { Link } from "react-router-dom";

export default function Navigation() {
    return (
        <ul className="nav-menu">
            <li>
                <Link to="/" className="nav-link">Home</Link>
            </li>
            <li>
                <Link to="/kategorien" className="nav-link">Kategorien</Link>
            </li>
            <li>
                <Link to="/produkte" className="nav-link">Produkte</Link>
            </li>
            <li>
                <Link to="/warenkorb" className="nav-link">Warenkorb</Link>
            </li>
            <li>
                <Link to="/bestellungen" className="nav-link">Bestellungen</Link>
            </li>
        </ul>
    );
}