import { createContext, useContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../services/auth-service";

// Context für den globalen Auth-State. Wird in useAuth() ausgelesen,
// damit Komponenten ohne prop-drilling auf User/Token zugreifen können.
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    // isLoading verhindert, dass Protected Routes sofort weiterleiten,
    // bevor checkAuth() aus dem localStorage geladen hat.
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    const navigate = useNavigate();

    // Beim App-Start einmalig den localStorage auslesen und State wiederherstellen.
    // Ohne checkAuth() würde ein Seiten-Reload den User ausloggen.
    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = () => {
        const savedToken = authService.getToken();
        const savedUser = authService.getUserData();

        if (savedToken && savedUser) {
            // Vorhandene Session wiederherstellen – Token-Gültigkeit
            // prüft das Backend beim nächsten API-Request via 401-Response.
            setToken(savedToken);
            setUser(savedUser);
            setIsAuthenticated(true);
        } else {
            setToken(null);
            setUser(null);
            setIsAuthenticated(false);
        }

        // isLoading deaktivieren damit ProtectedRoute korrekt rendern kann
        setIsLoading(false);
    };

    const login = async (usernameOrEmail, password) => {
        // authService übernimmt das localStorage-Schreiben,
        // hier nur den React-State synchronisieren.
        const data = await authService.login(usernameOrEmail, password);
        setToken(data.token);
        setUser({
            id: data.userId,
            username: data.username,
            email: data.email,
            role: data.role
        });
        setIsAuthenticated(true);
        return data;
    };

    const logout = () => {
        authService.logout();
        setToken(null);
        setUser(null);
        setIsAuthenticated(false);
        // Nach Logout zur Startseite weiterleiten
        navigate("/");
    };

    // Während checkAuth() läuft, nichts rendern – verhindert kurzes Aufblitzen
    // von Login-Seite obwohl der User noch eingeloggt ist.
    if (isLoading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh" }}>
                <div>
                    <div className="spinner"></div>
                    <p>Authentifizierung wird geprüft...</p>
                </div>
            </div>
        );
    }

    return (
        <AuthContext.Provider value={{ user, token, isAuthenticated, isLoading, login, logout, checkAuth }}>
            {children}
        </AuthContext.Provider>
    );
}

// Custom Hook mit Fehler wenn ausserhalb AuthProvider verwendet –
// verhindert stille Fehler bei falsch platzierten Komponenten.
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth muss innerhalb eines AuthProvider verwendet werden");
    }
    return context;
}

export default AuthContext;
