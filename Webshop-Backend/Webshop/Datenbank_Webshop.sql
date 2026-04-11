CREATE DATABASE IF NOT EXISTS LB_shop_modul_295;
USE LB_shop_modul_295;
CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);
CREATE TABLE IF NOT EXISTS kategorien (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    beschreibung VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS produkte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    beschreibung TEXT,
    preis DECIMAL(10, 2) NOT NULL,
    bestand INT NOT NULL,
    kategorie_id BIGINT NOT NULL,
    FOREIGN KEY (kategorie_id) REFERENCES kategorien(id)
);
CREATE TABLE IF NOT EXISTS bestellungen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    gesamtbetrag DECIMAL(12, 2) NOT NULL DEFAULT 0,
    erstellt_am DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    kunde_name VARCHAR(150) NOT NULL,
    kunde_email VARCHAR(150),
    liefer_strasse VARCHAR(150),
    liefer_plz VARCHAR(20),
    liefer_stadt VARCHAR(100),
    liefer_land VARCHAR(100),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES app_users(id)
);
CREATE TABLE IF NOT EXISTS bestellpositionen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bestellung_id BIGINT NOT NULL,
    produkt_id BIGINT NOT NULL,
    menge INT NOT NULL,
    einzelpreis DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (bestellung_id) REFERENCES bestellungen(id),
    FOREIGN KEY (produkt_id) REFERENCES produkte(id)
);
DROP USER IF EXISTS 'manager' @'%';
CREATE USER 'manager' @'%' IDENTIFIED BY 'managerpw';
GRANT ALL PRIVILEGES ON LB_shop_modul_295.* TO 'manager' @'%';
FLUSH PRIVILEGES;
Select *
from produkte;