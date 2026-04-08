import { render, screen } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";

// useAuth wird gemockt damit wir Auth-Zustände direkt steuern können,
// ohne einen echten AuthProvider mit localStorage-Abhängigkeit zu brauchen.
jest.mock("../context/AuthContext", () => ({
    useAuth: jest.fn()
}));

import { useAuth } from "../context/AuthContext";

// Hilfskomponente zum Rendern mit React Router (ProtectedRoute verwendet Navigate)
function renderWithRouter(ui, { initialPath = "/" } = {}) {
    return render(
        <MemoryRouter initialEntries={[initialPath]}>
            <Routes>
                <Route path="/" element={ui} />
                <Route path="/login" element={<div>Login-Seite</div>} />
                <Route path="/forbidden" element={<div>Forbidden-Seite</div>} />
                <Route path="/protected" element={<div>Geschützter Inhalt</div>} />
            </Routes>
        </MemoryRouter>
    );
}

beforeEach(() => {
    jest.clearAllMocks();
});

describe("ProtectedRoute", () => {

    test("protectedRoute_redirectsToLogin_wennNichtEingeloggt", () => {
        // User nicht eingeloggt – soll zur Login-Seite weitergeleitet werden
        useAuth.mockReturnValue({
            isAuthenticated: false,
            isLoading: false,
            user: null
        });

        render(
            <MemoryRouter initialEntries={["/"]}>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute>
                            <div>Geschützter Inhalt</div>
                        </ProtectedRoute>
                    } />
                    <Route path="/login" element={<div>Login-Seite</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByText("Login-Seite")).toBeInTheDocument();
        expect(screen.queryByText("Geschützter Inhalt")).not.toBeInTheDocument();
    });

    test("protectedRoute_redirectsToForbidden_beiFalscherRolle", () => {
        // KUNDE versucht auf ADMIN-Seite zuzugreifen – soll zur Forbidden-Seite
        useAuth.mockReturnValue({
            isAuthenticated: true,
            isLoading: false,
            user: { username: "kunde", role: "KUNDE" }
        });

        render(
            <MemoryRouter initialEntries={["/"]}>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <div>Admin-Bereich</div>
                        </ProtectedRoute>
                    } />
                    <Route path="/forbidden" element={<div>Forbidden-Seite</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByText("Forbidden-Seite")).toBeInTheDocument();
        expect(screen.queryByText("Admin-Bereich")).not.toBeInTheDocument();
    });

    test("protectedRoute_rendertChildren_beiKorrekterRolle", () => {
        // ADMIN greift auf ADMIN-Route zu – Inhalt soll angezeigt werden
        useAuth.mockReturnValue({
            isAuthenticated: true,
            isLoading: false,
            user: { username: "admin", role: "ADMIN" }
        });

        render(
            <MemoryRouter initialEntries={["/"]}>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <div>Admin-Bereich</div>
                        </ProtectedRoute>
                    } />
                    <Route path="/forbidden" element={<div>Forbidden-Seite</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(screen.getByText("Admin-Bereich")).toBeInTheDocument();
        expect(screen.queryByText("Forbidden-Seite")).not.toBeInTheDocument();
    });

});
