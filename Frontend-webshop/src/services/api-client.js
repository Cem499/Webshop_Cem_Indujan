import axios from "axios";

// alle requests laufen über diesen einen client, so steht die URL nur einmal im code
const apiClient = axios.create({
    baseURL: "http://localhost:8081/api",
    timeout: 10000,
    headers: {
        "Content-Type": "application/json"
    }
});

// läuft vor jedem request: token aus dem browser lesen und als header mitsenden
// ohne diesen header erkennt das backend den user nicht
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

// läuft nach jeder antwort: 401 und 403 werden hier zentral behandelt
// so muss das nicht in jeder einzelnen komponente wiederholt werden
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // token abgelaufen oder ungültig, browser aufräumen und zur loginseite
            localStorage.removeItem("authToken");
            localStorage.removeItem("userData");
            window.location.href = "/login";
        } else if (error.response?.status === 403) {
            // user ist eingeloggt aber hat keine rechte für diese ressource
            console.log("Keine Berechtigung für diese Ressource");
        }
        return Promise.reject(error);
    }
);

export default apiClient;
