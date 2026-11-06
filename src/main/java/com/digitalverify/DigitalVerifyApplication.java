package com.digitalverify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de inicio para la aplicacion Digital Verify.
 * Inicializa el contexto de Spring Boot, el contenedor embebido Tomcat
 * y la configuracion de base de datos H2 persistente.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@SpringBootApplication
public class DigitalVerifyApplication {

    /**
     * Punto de entrada principal de la aplicacion Spring Boot.
     *
     * @param args Argumentos pasados por linea de comandos al iniciar la aplicacion.
     */
    public static void main(String[] args) {
        // Inicializa el contexto y levanta el servidor embebido
        SpringApplication.run(DigitalVerifyApplication.class, args);
    }
}
