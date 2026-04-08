import { render, screen, act, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider, useAuth } from "./AuthContext";

// auth-service wird gemockt damit Tests nicht vom Backend abhängen.
// Die Mock-Implementierung simuliert das Verhalten des echten Services
// inklusive localStorage-Schreiben, damit wir den vollen Auth-Flow testen können.
jest.mock("../services/auth-service", () => ({
    __esModule: true,
    default: {
        login: jest.fn(),
        logout: jest.fn(),
        getToken: jest.fn(),
        getUserData: jest.fn(),
    }
}));

import authService from "../services/auth-service";

// Hilfskomponente: macht Auth-State für Tests sichtbar
function AuthStateDisplay() {
    const { isAuthenticated, user } = useAuth();
    return (
        <div>
            <span data-testid="is-authenticated">{isAuthenticated ? "true" : "false"}</span>
            {user && <span data-testid="username">{user.username}</span>}
        </div>
    );
}

// Hilfsfunktion: rendert eine Komponente im AuthProvider-Kontext mit Router
function renderWithAuth(ui, { token = null, userData = null } = {}) {
    authService.getToken.mockReturnValue(token);
    authService.getUserData.mockReturnValue(userData);

    return render(
        <MemoryRouter>
            <AuthProvider>
                {ui}
            </AuthProvider>
        </MemoryRouter>
    );
}

beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
});

describe("AuthContext", () => {

    test("login_speichertTokenInLocalStorage", async () => {
        const mockToken = "test-jwt-token-123";
        const mockResponse = {
            token: mockToken,
            userId: 1,
            username: "testuser",
            email: "test@test.com",
            role: "KUNDE"
        };

        // Mock simuliert echtes Login-Verhalten inkl. localStorage-Schreiben,
        // weil authService.login im echten Code diese Aufgabe übernimmt.
        authService.login.mockImplementation(async () => {
            localStorage.setItem("authToken", mockToken);
            localStorage.setItem("userData", JSON.stringify({
                id: 1, username: "testuser", email: "test@test.com", role: "KUNDE"
            }));
            return mockResponse;
        });

        let loginFn;
        function LoginCapture() {
            const auth = useAuth();
            loginFn = auth.login;
            return null;
        }

        renderWithAuth(<LoginCapture />);

        await act(async () => {
            await loginFn("testuser", "password");
        });

        expect(authService.login).toHaveBeenCalledWith("testuser", "password");
        expect(localStorage.getItem("authToken")).toBe(mockToken);
        expect(localStorage.getItem("userData")).not.toBeNull();
    });

    test("logout_loeschtTokenAusLocalStorage", () => {
        // Eingeloggten Zustand vorbereiten
        localStorage.setItem("authToken", "some-valid-token");
        localStorage.setItem("userData", JSON.stringify({ username: "testuser", role: "KUNDE" }));

        // Mock simuliert echtes Logout-Verhalten inkl. localStorage-Bereinigung
        authService.logout.mockImplementation(() => {
            localStorage.removeItem("authToken");
            localStorage.removeItem("userData");
        });

        let logoutFn;
        function LogoutCapture() {
            const auth = useAuth();
            logoutFn = auth.logout;
            return null;
        }

        renderWithAuth(<LogoutCapture />, {
            token: "some-valid-token",
            userData: { username: "testuser", role: "KUNDE" }
        });

        act(() => {
            logoutFn();
        });

        expect(authService.logout).toHaveBeenCalled();
        expect(localStorage.getItem("authToken")).toBeNull();
        expect(localStorage.getItem("userData")).toBeNull();
    });

    test("checkAuth_setztIsAuthenticatedTrue_wennTokenVorhanden", async () => {
        // Beim Mount liest checkAuth() den Token aus dem localStorage.
        // Wenn ein Token vorhanden ist, soll isAuthenticated auf true gesetzt werden.
        const userData = { id: 1, username: "testuser", email: "test@test.com", role: "KUNDE" };

        renderWithAuth(<AuthStateDisplay />, {
            token: "valid-token-abc",
            userData
        });

        await waitFor(() => {
            expect(screen.getByTestId("is-authenticated").textContent).toBe("true");
        });

        expect(screen.getByTestId("username").textContent).toBe("testuser");
    });

});
