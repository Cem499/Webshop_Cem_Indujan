import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "./App";
import Home from "./pages/Home";

// App verwendet AuthProvider, der useNavigate() aufruft – MemoryRouter muss als äusserster Wrapper sein.
// Navigation verwendet useAuth() – wird durch AuthProvider bereitgestellt, der in App enthalten ist.

// Test 1: Home zeigt Begrüssungstext
test("Home zeigt Begrüssungstext", () => {
    render(<Home />);
    expect(screen.getByText(/Willkommen im Cem Sin Webshop!/i)).toBeInTheDocument();
});

// Test 2: App rendert ohne Absturz und zeigt Logo
test("App zeigt das Logo im Layout", () => {
    render(
        <MemoryRouter>
            <App />
        </MemoryRouter>
    );
    expect(screen.getByAltText("Logo")).toBeInTheDocument();
    expect(screen.getAllByText(/Cem Sin Webshop/i).length).toBeGreaterThan(0);
});

// Test 3: Home zeigt Features
test("Home zeigt Features", () => {
    render(<Home />);
    expect(screen.getByText("Grosse Auswahl")).toBeInTheDocument();
    expect(screen.getByText("Schnelle Lieferung")).toBeInTheDocument();
    expect(screen.getByText("Einfach Bestellen")).toBeInTheDocument();
});

// Test 4: Nicht eingeloggte Navigation zeigt Login und Register
test("Navigation zeigt Login und Register wenn nicht eingeloggt", () => {
    render(
        <MemoryRouter>
            <App />
        </MemoryRouter>
    );
    expect(screen.getByText("Anmelden")).toBeInTheDocument();
    expect(screen.getByText("Registrieren")).toBeInTheDocument();
});

// Test 5: Navigation zeigt immer Home und Produkte
test("Navigation zeigt Home und Produkte", () => {
    render(
        <MemoryRouter>
            <App />
        </MemoryRouter>
    );
    expect(screen.getByText("Home")).toBeInTheDocument();
    expect(screen.getByText("Produkte")).toBeInTheDocument();
});
