package com.digitalverify.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio correspondiente a la tabla 'seguimiento' de la base de datos.
 * Registra formalmente el dictamen dual (Verification Decision y Account Status Decision)
 * emitido por un operador interno conforme a los estandares del SENA.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class SeguimientoEntry {

    /**
     * Identificador unico del registro de seguimiento.
     */
    private Long id;

    /**
     * Identificador foraneo del caso de verificacion (KycCase) evaluado.
     */
    private Long caseId;

    /**
     * Identificador foraneo del usuario interno (operador) que tomo la decision.
     */
    private Long internalUserId;

    /**
     * Decision sobre la verificacion documental (ej: Approve Verification, Reject Verification).
     */
    private String verificationDecision;

    /**
     * Decision sobre el estado operativo de la cuenta (ej: Reactivate Account, Permanently Close Account).
     */
    private String accountStatusDecision;

    /**
     * Comentarios, hallazgos o justificacion del dictamen.
     */
    private String comments;

    /**
     * Fecha y hora exacta de registro del seguimiento.
     */
    private LocalDateTime createdAt;

    /**
     * Nombre del operador responsable (campo enriquecido para la vista).
     */
    private String operatorName;

    /**
     * Codigo de caso (ej: DV-1024) enriquecido para la vista.
     */
    private String caseNumber;

    /**
     * Constructor por defecto.
     */
    public SeguimientoEntry() {}

    /**
     * Constructor completo con atributos de persistencia.
     *
     * @param id                     Identificador unico.
     * @param caseId                 ID del caso.
     * @param internalUserId         ID del empleado evaluador.
     * @param verificationDecision   Dictamen sobre documentos.
     * @param accountStatusDecision  Dictamen sobre cuenta.
     * @param comments               Comentarios de auditoria.
     * @param createdAt              Marca de tiempo.
     */
    public SeguimientoEntry(Long id, Long caseId, Long internalUserId, String verificationDecision,
                            String accountStatusDecision, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.caseId = caseId;
        this.internalUserId = internalUserId;
        this.verificationDecision = verificationDecision;
        this.accountStatusDecision = accountStatusDecision;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getInternalUserId() { return internalUserId; }
    public void setInternalUserId(Long internalUserId) { this.internalUserId = internalUserId; }

    public String getVerificationDecision() { return verificationDecision; }
    public void setVerificationDecision(String verificationDecision) { this.verificationDecision = verificationDecision; }

    public String getAccountStatusDecision() { return accountStatusDecision; }
    public void setAccountStatusDecision(String accountStatusDecision) { this.accountStatusDecision = accountStatusDecision; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
}
