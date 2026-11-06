package com.digitalverify.model;

import java.time.LocalDateTime;

/**
 * Entidad de auditoria para el registro de eventos historicos y trazabilidad de acciones operativas.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class AuditLogEntry {

    /**
     * Identificador unico del registro de auditoria.
     */
    private Long id;

    /**
     * Nombre del operador responsable de la accion.
     */
    private String operatorName;

    /**
     * Numero del caso asociado (ej: DV-1024).
     */
    private String caseNumber;

    /**
     * Accion ejecutada sobre el caso.
     */
    private String action;

    /**
     * Informacion detallada o justificacion del cambio.
     */
    private String details;

    /**
     * Fecha y hora en que se genero el registro.
     */
    private LocalDateTime createdAt;

    /**
     * Constructor por defecto.
     */
    public AuditLogEntry() {}

    /**
     * Constructor completo para instanciar registros de auditoria.
     *
     * @param id           Identificador unico.
     * @param operatorName Nombre del operador.
     * @param caseNumber   Codigo de caso.
     * @param action       Accion realizada.
     * @param details      Detalles del evento.
     * @param createdAt    Marca de tiempo.
     */
    public AuditLogEntry(Long id, String operatorName, String caseNumber, String action, String details, LocalDateTime createdAt) {
        this.id = id;
        this.operatorName = operatorName;
        this.caseNumber = caseNumber;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
