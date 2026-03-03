import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "./App";
import Home from "./pages/Home";
import Navigation from "./pages/Navigation";

// Test 1: Home zeigt Begrüssungstext
test("Home zeigt Begrüssungstext", () => {
    render(<Home />);
    expect(screen.getByText(/Willkommen im Cem Sin Webshop!/i)).toBeInTheDocument();
});

// Test 2: Navigation zeigt alle Links
test("Navigation zeigt alle Links", () => {
    render(
        <MemoryRouter>
            <Navigation />
        </MemoryRouter>
    );
    expect(screen.getByText("Home")).toBeInTheDocument();
    expect(screen.getByText("Kategorien")).toBeInTheDocument();
    expect(screen.getByText("Produkte")).toBeInTheDocument();
});

// Test 3: App zeigt das Logo im Layout
test("App zeigt das Logo im Layout", () => {
    render(
        <MemoryRouter>
            <App />
        </MemoryRouter>
    );
    expect(screen.getByAltText("Logo")).toBeInTheDocument();
    expect(screen.getAllByText(/Cem Sin Webshop/i).length).toBeGreaterThan(0);
});

// Test 4: Home zeigt Features
test("Home zeigt Features", () => {
    render(<Home />);
    expect(screen.getByText("Grosse Auswahl")).toBeInTheDocument();
    expect(screen.getByText("Schnelle Lieferung")).toBeInTheDocument();
    expect(screen.getByText("Einfach Bestellen")).toBeInTheDocument();
});

// Test 5: Navigation enthält Warenkorb-Link
test("Navigation enthält Warenkorb-Link", () => {
    render(
        <MemoryRouter>
            <Navigation />
        </MemoryRouter>
    );
    expect(screen.getByText("Warenkorb")).toBeInTheDocument();
});