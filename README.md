## Webshop – Modul 223

Ein Fullstack-Webshop entwickelt im Rahmen der Leistungsbeurteilung im Modul 223 (WISS, Applikationsentwicklung EFZ).

**Autoren:** Cem & Indujan
**Repository:** [github.com/Cem499/Webshop_Cem_Indujan](https://github.com/Cem499/Webshop_Cem_Indujan)

## Über das Projekt

Der Webshop besteht aus einem Spring Boot Backend und einem React Frontend mit MySQL als Datenbank. Kunden können sich registrieren, einloggen, Produkte nach Kategorien durchsuchen und Bestellungen aufgeben. Administratoren können zusätzlich Produkte und Kategorien verwalten sowie alle Bestellungen einsehen. Die Authentifizierung erfolgt stateless über JWT Tokens.

## Tech-Stack

**Backend**
- Java 17
- Spring Boot 3.3.2
- Spring Security 6.x
- JJWT 0.12.3
- Spring Data JPA / Hibernate
- MySQL 8.x
- Maven

**Frontend**
- React 19.2
- Vite 7.x
- React Router 7.x
- Axios 1.14
- Context API
- Jest + React Testing Library

## Projektstruktur


Front-Backend-Webshop/
├── Webshop-Backend/
│   └── Webshop/           # Spring Boot Projekt
│       ├── src/main/java/ch/wiss/webshop/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── model/
│       │   ├── dto/
│       │   ├── config/
│       │   └── security/
│       └── src/test/java/
└── Frontend-webshop/      # React Projekt
    └── src/
        ├── pages/
        ├── components/
        ├── services/
        └── context/


## Voraussetzungen

- Java 17 JDK
- Maven 3.8 oder neuer
- MySQL 8.x
- Node.js 20 oder neuer (inkl. npm)

## Setup

### Datenbank

MySQL starten und folgende Datenbank + Benutzer anlegen:


CREATE DATABASE LB_shop_modul_295;
CREATE USER 'manager'@'localhost' IDENTIFIED BY 'managerpw';
GRANT ALL PRIVILEGES ON LB_shop_modul_295.* TO 'manager'@'localhost';
FLUSH PRIVILEGES;


Hibernate erstellt das Schema beim Backend-Start automatisch (`ddl-auto=update`).

### Backend starten


cd Webshop-Backend/Webshop
mvn clean install
mvn spring-boot:run


Backend läuft auf `http://localhost:8081`.
Swagger UI: `http://localhost:8081/swagger-ui.html`

### Frontend starten


cd Frontend-webshop
npm install
npm run dev


Frontend läuft auf `http://localhost:5173`.

## Tests ausführen

**Backend**

cd Webshop-Backend/Webshop
mvn test


**Frontend**

cd Frontend-webshop
npm test


## Rollen

| Rolle  | Berechtigungen                                                  |
|--------|-----------------------------------------------------------------|
| KUNDE  | Produkte ansehen, bestellen, eigene Bestellungen einsehen      |
| ADMIN  | Alles + Produkte und Kategorien verwalten, alle Bestellungen   |

## REST-API (Auszug)

| Methode | Pfad                                  | Rolle          | Zweck                                |
|---------|---------------------------------------|----------------|--------------------------------------|
| POST    | /api/auth/register                    | Public         | Neuen Kunden registrieren            |
| POST    | /api/auth/login                       | Public         | Login, gibt JWT Token zurück         |
| GET     | /api/produkte                         | Public         | Alle Produkte auflisten              |
| POST    | /api/produkte                         | ADMIN          | Produkt anlegen                      |
| GET     | /api/bestellungen                     | Authenticated  | Eigene Bestellungen (Admin: alle)    |
| POST    | /api/bestellungen                     | Authenticated  | Neue Bestellung aufgeben             |
| POST    | /api/bestellungen/{id}/bezahlen       | Authenticated  | Bestellung als bezahlt markieren     |
| POST    | /api/bestellungen/{id}/stornieren     | Authenticated  | Bestellung stornieren                |

Vollständige Übersicht in der Swagger UI.

## Sicherheit

- Passwörter werden mit BCrypt gehasht (Salt + bewusst langsam)
- JWT signiert mit HMAC-SHA256, 24h Gültigkeit
- Rollenbasierte Autorisierung über `@PreAuthorize`
- Ownership-Check gegen IDOR im BestellungController
- Bean Validation gegen ungültige Eingaben
- Stateless Auth, daher kein CSRF-Risiko

## Dokumentation

Die vollständige Projektdokumentation liegt im Repository unter `Projektdokumentation_Webshop_Cem_Indujan.pdf`. Sie enthält Architekturdiagramme, ER-Diagramm, JWT-Flow, Testprotokolle und das Sicherheitskonzept.

## Lizenz

Dieses Projekt entstand im Rahmen einer Leistungsbeurteilung an der WISS und wird nicht produktiv betrieben.
