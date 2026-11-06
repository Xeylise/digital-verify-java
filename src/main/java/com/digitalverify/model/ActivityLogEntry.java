package com.digitalverify.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un registro de auditoria operativa en la tabla 'activity_logs'.
 * Cumple con la estructura requerida por el SENA:
 * fecha (timestamp), usuario interno (id fk), tipo de evento (VARCHAR), id verification (id fk), detalles (VARCHAR).
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class ActivityLogEntry {

    /**
     * Identificador unico auto-incremental del registro de actividad.
     */
    private Long id;

    /**
     * Marca de tiempo exacta en la que ocurrio el evento operativo.
     */
    private LocalDateTime fecha;

    /**
     * Identificador foraneo del usuario interno (operador/empleado) responsable de la accion.
     */
    private Long usuarioInternoId;

    /**
     * Categoria o tipo de evento (ej: Case Assigned, Status Changed, Decision: approved).
     */
    private String tipoEvento;

    /**
     * Identificador foraneo del caso de verificacion (KycCase) afectado.
     */
    private Long idVerification;

    /**
     * Descripcion textual detallada de la accion y cambios realizados.
     */
    private String detalles;

    /**
     * Nombre del operador responsable (campo enriquecido para la interfaz de usuario).
     */
    private String operatorName;

    /**
     * Codigo de referencia del caso (ej: DV-1024) enriquecido para la vista.
     */
    private String caseNumber;

    /**
     * Constructor por defecto para instanciacion por frameworks y RowMappers.
     */
    public ActivityLogEntry() {}

    /**
     * Constructor completo con los atributos base mapeados desde la base de datos.
     *
     * @param id               Identificador unico.
     * @param fecha            Fecha y hora del evento.
     * @param usuarioInternoId Identificador del empleado operador.
     * @param tipoEvento       Tipo de evento registrado.
     * @param idVerification   Identificador del caso KYC.
     * @param detalles         Detalles y observaciones de la accion.
     */
    public ActivityLogEntry(Long id, LocalDateTime fecha, Long usuarioInternoId, String tipoEvento, Long idVerification, String detalles) {
        this.id = id;
        this.fecha = fecha;
        this.usuarioInternoId = usuarioInternoId;
        this.tipoEvento = tipoEvento;
        this.idVerification = idVerification;
        this.detalles = detalles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Long getUsuarioInternoId() { return usuarioInternoId; }
    public void setUsuarioInternoId(Long usuarioInternoId) { this.usuarioInternoId = usuarioInternoId; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public Long getIdVerification() { return idVerification; }
    public void setIdVerification(Long idVerification) { this.idVerification = idVerification; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    /**
     * Metodo de compatibilidad con plantillas que referencian createdAt.
     *
     * @return Marca de tiempo del evento.
     */
    public LocalDateTime getCreatedAt() { return fecha; }

    /**
     * Metodo de compatibilidad con plantillas que referencian action.
     *
     * @return Tipo de evento.
     */
    public String getAction() { return tipoEvento; }

    /**
     * Metodo de compatibilidad con plantillas que referencian details.
     *
     * @return Descripcion detallada.
     */
    public String getDetails() { return detalles; }
}
