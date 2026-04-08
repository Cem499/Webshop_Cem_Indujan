import apiClient from "./api-client";

// Führt Login durch und speichert Token sowie User-Daten im localStorage,
// damit die Session nach einem Seiten-Reload erhalten bleibt.
const login = async (usernameOrEmail, password) => {
    try {
        const response = await apiClient.post("/auth/login", { usernameOrEmail, password });
        const data = response.data;

        // Token und User-Daten persistent speichern – wird vom api-client bei
        // jedem folgenden Request automatisch als Bearer-Token mitgeschickt.
        localStorage.setItem("authToken", data.token);
        localStorage.setItem("userData", JSON.stringify({
            id: data.userId,
            username: data.username,
            email: data.email,
            role: data.role
        }));

        return data;
    } catch (error) {
        // Fehlermeldung vom Backend bevorzugen, damit dem User konkrete Infos
        // angezeigt werden (z.B. "Falsches Passwort" statt generisches "Login fehlgeschlagen").
        throw new Error(error.response?.data?.message || "Login fehlgeschlagen");
    }
};

// Registriert einen neuen User. Token wird hier NICHT gespeichert –
// der User muss sich danach explizit einloggen.
const register = async (userData) => {
    try {
        const response = await apiClient.post("/auth/register", userData);
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data?.message || "Registrierung fehlgeschlagen");
    }
};

// Bereinigt alle Auth-Daten aus dem localStorage.
// Nach dem Logout schickt der api-client keinen Authorization-Header mehr.
const logout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userData");
};

// Prüft ob ein Token im localStorage vorhanden ist.
// Hinweis: Validiert NICHT ob der Token noch gültig ist – das prüft der 401-Interceptor.
const isAuthenticated = () => {
    return !!localStorage.getItem("authToken");
};

const getToken = () => {
    return localStorage.getItem("authToken");
};

// Gibt geparste User-Daten zurück oder null wenn kein User eingeloggt ist.
const getUserData = () => {
    try {
        const data = localStorage.getItem("userData");
        return data ? JSON.parse(data) : null;
    } catch {
        // Beschädigte Daten im localStorage sauber behandeln
        return null;
    }
};

const authService = { login, register, logout, isAuthenticated, getToken, getUserData };

export default authService;
