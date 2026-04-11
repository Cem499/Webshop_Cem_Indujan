package ch.wiss.webshop;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

/**
 * Hauptklasse der Webshop-Applikation. Startet die Spring Boot Anwendung und
 * stellt einen einfachen Test-Endpoint bereit.
 */
@SpringBootApplication
@RestController
public class WebshopApplication {

	/**
	 * Hauptmethode zum Starten der Applikation.
	 *
	 * @param args Kommandozeilen-Argumente
	 */
	public static void main(String[] args) {
		SpringApplication.run(WebshopApplication.class, args);
	}

	/**
	 * Test-Endpoint um zu prüfen ob die Applikation läuft.
	 *
	 * @return Willkommensnachricht
	 */
	@GetMapping("/")
	public String index() {
		return "Webshop mit REST API - Modul 295 LB - Cem Sin";
	}

	@Autowired
	DataSource dataSource;

	@PostConstruct
	public void printDbInfo() throws Exception {
		System.out.println("CONNECTED TO: " +
				dataSource.getConnection().getMetaData().getURL());
	}

}