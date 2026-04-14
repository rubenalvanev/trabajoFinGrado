package com.tfg.trabajoFinGrado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController // 1. Añadimos esto
public class TrabajoFinGradoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrabajoFinGradoApplication.class, args);
	}

	@GetMapping("/") // 2. Y añadimos este método
	public String hola() {
		return "¡Hola mundo!";
	}
}