import axios from "axios";

// Zentrale axios-Instanz für alle API-Requests.
// baseURL vereinfacht alle Fetch-Aufrufe – nur noch relative Pfade nötig.
const apiClient = axios.create({
    baseURL: "http://localhost:8081/api",
    timeout: 10000,
    headers: {
        "Content-Type": "application/json"
    }
});

// Request Interceptor: Token wird aus localStorage gelesen und bei jedem Request
// mitgeschickt, damit das Backend den User über den JwtAuthenticationFilter
// identifizieren kann.
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("authToken");
        if (token) {
            config.headers["Authorization"] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response Interceptor: Behandelt globale HTTP-Fehler zentral,
// damit jede Komponente nicht selbst auf 401/403 reagieren muss.
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Token abgelaufen oder ungültig – Session bereinigen und zur Login-Seite weiterleiten.
            // Dies verhindert, dass der User mit einem ungültigen Token weiterarbeitet.
            localStorage.removeItem("authToken");
            localStorage.removeItem("userData");
            window.location.href = "/login";
        } else if (error.response?.status === 403) {
            // User ist eingeloggt, hat aber keine Berechtigung für diese Ressource.
            console.log("Keine Berechtigung für diese Ressource");
        }
        return Promise.reject(error);
    }
);

export default apiClient;
