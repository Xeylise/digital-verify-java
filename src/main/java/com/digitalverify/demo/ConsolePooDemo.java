package com.digitalverify.demo;

import com.digitalverify.model.Bet;
import com.digitalverify.model.KycCase;
import com.digitalverify.model.KycDocument;
import com.digitalverify.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Clase demostrativa para la evidencia SENA:
 * GA7-220501096-AA3-EV01 - Codificación de Módulos del Software Stand-alone, Web y Móvil.
 * Programa de formación: Tecnólogo en Análisis y Desarrollo de Software (ADSO).
 *
 * Cumple exhaustivamente con:
 * 1. Instanciación de objetos usando constructores con sobrecarga.
 * 2. Uso de encapsulamiento con visibilidad privada y métodos de acceso getters/setters.
 * 3. Ejecución de métodos sobrecargados de lógica de negocio.
 * 4. Demostración de relaciones 1 a N mediante colecciones Java estandarizadas (List).
 * 5. Verificación de salida estructurada por consola.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class ConsolePooDemo {

    /**
     * Metodo principal de ejecucion demostrativa de principios de programacion orientada a objetos (POO).
     *
     * @param args Argumentos de linea de comandos (opcionales).
     */
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" SENA - GA7-220501096-AA3-EV01: DEMOSTRACIÓN POO & ASOCIACIONES 1 A N");
        System.out.println(" Proyecto: Digital Verify (Sistema de Verificación KYC y Juegos)");
        System.out.println(" Aprendiz: Paola Andrea Castro Sarabia - Ficha: 3336092");
        System.out.println("================================================================================\n");

        // 1. Instanciacion con constructor sobrecargado y encapsulamiento
        System.out.println(">>> 1. CREACIÓN DE USUARIO (Constructor sobrecargado & Encapsulamiento)");
        User jugador = new User("carlos.mendoza@email.com", "Carlos Mendoza", "CLIENT");
        jugador.setId(101L);
        jugador.setCountry("CO");
        jugador.setAccountNumber("ACC-48920");

        // Prueba de sobrecarga de metodo para actualizacion de datos de contacto
        jugador.updateContact("+57 312 456 7890", "Colombia");

        System.out.println("Usuario Creado:");
        System.out.println(" - ID: " + jugador.getId());
        System.out.println(" - Nombre: " + jugador.getFullName());
        System.out.println(" - Correo: " + jugador.getEmail());
        System.out.println(" - Rol: " + jugador.getRole());
        System.out.println(" - Teléfono: " + jugador.getPhone());
        System.out.println(" - País: " + jugador.getCountry());
        System.out.println(" - Estado KYC: " + jugador.getKycStatus());
        System.out.println();

        // 2. Asociacion 1 a N: Usuario -> Apuestas (List<Bet>)
        System.out.println(">>> 2. ASOCIACIÓN 1 A N: Un Usuario tiene N Apuestas (List<Bet>)");
        Bet apuesta1 = new Bet(1L, "BET-001", jugador.getId(), "Real Madrid vs Barcelona", "Real Madrid",
                new BigDecimal("2.10"), new BigDecimal("150.00"), new BigDecimal("315.00"), "WON", LocalDateTime.now());
        Bet apuesta2 = new Bet(2L, "BET-002", jugador.getId(), "Roulette European", "Rojo #17",
                new BigDecimal("35.00"), new BigDecimal("50.00"), BigDecimal.ZERO, "LOST", LocalDateTime.now());
        Bet apuesta3 = new Bet(3L, "BET-003", jugador.getId(), "Blackjack VIP Table 4", "Jugador Gana",
                new BigDecimal("1.50"), new BigDecimal("80.00"), null, "ACTIVE", LocalDateTime.now());

        // Vinculacion de apuestas a la coleccion del jugador
        jugador.addBet(apuesta1);
        jugador.addBet(apuesta2);
        jugador.addBet(apuesta3);

        System.out.println("Total de apuestas registradas para " + jugador.getFullName() + ": " + jugador.getBets().size());
        for (Bet b : jugador.getBets()) {
            System.out.println(String.format("   [Apuesta #%d Ref: %-7s] Evento: %-26s | Selección: %-13s | Monto: $%s | Estado: %s",
                    b.getId(), b.getBetReference(), b.getEventName(), b.getSelection(), b.getStake(), b.getStatus()));
        }
        System.out.println();

        // 3. Creacion de Caso KYC con constructor sobrecargado
        System.out.println(">>> 3. CREACIÓN DE CASO KYC (Constructor sobrecargado & Sobrecarga de Métodos)");
        KycCase casoKyc = new KycCase("KYC-2026-0099", jugador.getId(), jugador.getFullName(), "Alerta de retiro sospechoso > $2,000 USD");
        casoKyc.setId(501L);
        casoKyc.setAccountNumber(jugador.getAccountNumber());
        casoKyc.setCountry(jugador.getCountry());
        casoKyc.setPriority("HIGH");
        casoKyc.setRiskScore(78);

        System.out.println("Caso KYC Inicial:");
        System.out.println(" - Número de Caso: " + casoKyc.getCaseNumber());
        System.out.println(" - Jugador: " + casoKyc.getPlayerName());
        System.out.println(" - Motivo: " + casoKyc.getReason());
        System.out.println(" - Prioridad: " + casoKyc.getPriority() + " | Risk Score: " + casoKyc.getRiskScore());
        System.out.println(" - Estado Actual: " + casoKyc.getStatus() + " | Asignado a: " + casoKyc.getAssignedTo());
        System.out.println();

        // 4. Asociacion 1 a N: Caso KYC -> Documentos de soporte (List<KycDocument>)
        System.out.println(">>> 4. ASOCIACIÓN 1 A N: Un Caso KYC tiene N Documentos (List<KycDocument>)");
        KycDocument doc1 = new KycDocument(1001L, casoKyc.getId(), "NATIONAL_ID", "cedula_carlos_frontal.jpg",
                "/uploads/kyc/cedula_carlos_frontal.jpg", 1845200L, "image/jpeg", "APPROVED", LocalDateTime.now());
        KycDocument doc2 = new KycDocument(1002L, casoKyc.getId(), "PASSPORT", "pasaporte_carlos.pdf",
                "/uploads/kyc/pasaporte_carlos.pdf", 3421500L, "application/pdf", "APPROVED", LocalDateTime.now());
        KycDocument doc3 = new KycDocument(1003L, casoKyc.getId(), "UTILITY_BILL", "recibo_gas_domicilio.pdf",
                "/uploads/kyc/recibo_gas_domicilio.pdf", 950200L, "application/pdf", "PENDING", LocalDateTime.now());

        // Vinculacion de documentos al caso
        casoKyc.addDocument(doc1);
        casoKyc.addDocument(doc2);
        casoKyc.addDocument(doc3);

        System.out.println("Documentos adjuntos al caso " + casoKyc.getCaseNumber() + " (" + casoKyc.getDocuments().size() + " documentos):");
        for (KycDocument doc : casoKyc.getDocuments()) {
            System.out.println(String.format("   [Doc #%d] Tipo: %-13s | Archivo: %-26s | Tamaño: %d KB | Estado: %s",
                    doc.getId(), doc.getDocumentType(), doc.getFileName(), doc.getFileSize() / 1024, doc.getStatus()));
        }
        System.out.println();

        // 5. Demostracion de ejecucion de metodo sobrecargado de resolucion
        System.out.println(">>> 5. EJECUCIÓN DE MÉTODO SOBRECARGADO (updateStatus con operador y notas)");
        casoKyc.updateStatus("approved", "Paola Castro (Operador KYC)", "Documentos de identidad y domicilio cotejados exitosamente contra registro civil.");

        System.out.println("Caso KYC después de dictamen:");
        System.out.println(" - Estado: " + casoKyc.getStatus().toUpperCase());
        System.out.println(" - Validador Asignado: " + casoKyc.getAssignedTo());
        System.out.println(" - Notas de Decisión: " + casoKyc.getDecisionNotes());
        System.out.println(" - Fecha de Actualización: " + casoKyc.getUpdatedAt());

        System.out.println("\n================================================================================");
        System.out.println(" DEMOSTRACIÓN FINALIZADA CON ÉXITO - TODOS LOS PRINCIPIOS POO VERIFICADOS");
        System.out.println("================================================================================");
    }
}
