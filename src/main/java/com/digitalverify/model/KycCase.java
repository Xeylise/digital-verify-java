package com.digitalverify.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad central del dominio que modela un Caso de Verificacion KYC en Digital Verify.
 * Representa el expediente completo del cliente para auditoria y cumplimiento regulatorio.
 * Demuestra principios de Programacion Orientada a Objetos (POO):
 * Encapsulamiento, sobrecarga de constructores, sobrecarga de metodos y relacion 1 a N con KycDocument.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class KycCase {

    /**
     * Identificador unico del caso en la base de datos.
     */
    private Long id;

    /**
     * Codigo de referencia publico del expediente (ej: DV-1024).
     */
    private String caseNumber;

    /**
     * Identificador foraneo del usuario jugador al que pertenece el caso.
     */
    private Long userId;

    /**
     * Numero de cuenta del jugador dentro de la plataforma de apuestas.
     */
    private String accountNumber;

    /**
     * Nombre completo del jugador titular de la cuenta.
     */
    private String playerName;

    /**
     * Codigo ISO del pais de origen o residencia del jugador (ej: CO, ES, BR).
     */
    private String country;

    /**
     * Estado operativo de la cuenta del jugador (Active, Suspended, Closed).
     */
    private String accountStatus;

    /**
     * Estado del caso de verificacion (pending, in_review, approved, rejected, info_requested).
     */
    private String status;

    /**
     * Nivel de prioridad asignado al caso (low, medium, high, critical).
     */
    private String priority = "medium";

    /**
     * Puntuacion de riesgo calculada en escala de 0 a 100.
     */
    private Integer riskScore;

    /**
     * Motivo o disparador de la verificacion (proactive_kyc, fraud_review, withdrawal_threshold, high_deposit).
     */
    private String reason;

    /**
     * Nombre del operador asignado para revisar y dictaminar el caso.
     */
    private String assignedTo;

    /**
     * Observaciones, justificaciones o notas de la decision del operador.
     */
    private String decisionNotes;

    /**
     * Marca de tiempo de cuando el caso fue asignado a un analista.
     */
    private LocalDateTime assignedAt;

    /**
     * Marca de tiempo de cuando el caso paso a estado activo de revision.
     */
    private LocalDateTime inReviewAt;

    /**
     * Marca de tiempo de cuando el caso fue cerrado con decision definitiva.
     */
    private LocalDateTime resolvedAt;

    /**
     * Marca de tiempo de cuando el cliente envio sus documentos para verificacion.
     */
    private LocalDateTime submittedAt;

    /**
     * Fecha y hora de la ultima actualizacion del expediente.
     */
    private LocalDateTime updatedAt;

    /**
     * Relacion 1 a N: Coleccion de documentos fisicos o digitales adjuntos al caso.
     */
    private List<KycDocument> documents = new ArrayList<>();

    /**
     * Constructor por defecto para serializacion y persistencia JDBC.
     */
    public KycCase() {}

    /**
     * Sobrecarga de constructor 1: Permite la creacion rapida de un nuevo caso desde el portal del jugador.
     *
     * @param caseNumber Codigo de referencia generado (DV-XXXX).
     * @param userId     Identificador del jugador.
     * @param playerName Nombre del jugador.
     * @param reason     Motivo de la verificacion.
     */
    public KycCase(String caseNumber, Long userId, String playerName, String reason) {
        this.caseNumber = caseNumber;
        this.userId = userId;
        this.playerName = playerName;
        this.reason = reason;
        this.status = "pending";
        this.riskScore = 50;
        this.assignedTo = "Unassigned";
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sobrecarga de constructor 2: Inicializacion estandar desde consultas de repositorio.
     *
     * @param id            Identificador unico.
     * @param caseNumber    Numero de caso.
     * @param userId        ID de usuario.
     * @param accountNumber Numero de cuenta.
     * @param playerName    Nombre del jugador.
     * @param country       Pais de residencia.
     * @param accountStatus Estado de cuenta.
     * @param status        Estado del caso.
     * @param priority      Prioridad.
     * @param riskScore     Puntaje de riesgo.
     * @param reason        Motivo de revision.
     * @param assignedTo    Analista asignado.
     * @param decisionNotes Notas del dictamen.
     * @param submittedAt   Fecha de envio.
     * @param updatedAt     Fecha de actualizacion.
     */
    public KycCase(Long id, String caseNumber, Long userId, String accountNumber,
                   String playerName, String country, String accountStatus,
                   String status, String priority, Integer riskScore, String reason,
                   String assignedTo, String decisionNotes,
                   LocalDateTime submittedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.playerName = playerName;
        this.country = country;
        this.accountStatus = accountStatus;
        this.status = status;
        this.priority = priority != null ? priority : "medium";
        this.riskScore = riskScore;
        this.reason = reason;
        this.assignedTo = assignedTo;
        this.decisionNotes = decisionNotes;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Sobrecarga de constructor 3: Inicializacion completa con todas las marcas de tiempo historicas.
     *
     * @param id            Identificador unico.
     * @param caseNumber    Numero de caso.
     * @param userId        ID de usuario.
     * @param accountNumber Numero de cuenta.
     * @param playerName    Nombre del jugador.
     * @param country       Pais.
     * @param accountStatus Estado de cuenta.
     * @param status        Estado del caso.
     * @param riskScore     Puntaje de riesgo.
     * @param reason        Motivo de revision.
     * @param assignedTo    Operador asignado.
     * @param decisionNotes Notas.
     * @param assignedAt    Fecha de asignacion.
     * @param inReviewAt    Fecha de inicio de revision.
     * @param resolvedAt    Fecha de resolucion.
     * @param submittedAt   Fecha de envio.
     * @param updatedAt     Fecha de actualizacion.
     */
    public KycCase(Long id, String caseNumber, Long userId, String accountNumber,
                   String playerName, String country, String accountStatus,
                   String status, Integer riskScore, String reason,
                   String assignedTo, String decisionNotes,
                   LocalDateTime assignedAt, LocalDateTime inReviewAt, LocalDateTime resolvedAt,
                   LocalDateTime submittedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.playerName = playerName;
        this.country = country;
        this.accountStatus = accountStatus;
        this.status = status;
        this.riskScore = riskScore;
        this.reason = reason;
        this.assignedTo = assignedTo;
        this.decisionNotes = decisionNotes;
        this.assignedAt = assignedAt;
        this.inReviewAt = inReviewAt;
        this.resolvedAt = resolvedAt;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Agrega un documento a la coleccion del caso demostrando la relacion 1 a N.
     *
     * @param document Documento a vincular al expediente.
     */
    public void addDocument(KycDocument document) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(document);
    }

    /**
     * Sobrecarga de metodo 1: Modifica unicamente el estado del caso y actualiza la marca de tiempo.
     *
     * @param status Nuevo estado a aplicar.
     */
    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sobrecarga de metodo 2: Modifica el estado del caso adjuntando observaciones de decision.
     *
     * @param status        Nuevo estado a aplicar.
     * @param decisionNotes Comentarios o justificaciones.
     */
    public void updateStatus(String status, String decisionNotes) {
        this.status = status;
        this.decisionNotes = decisionNotes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sobrecarga de metodo 3: Modifica estado, reasigna analista y agrega notas.
     *
     * @param status        Nuevo estado.
     * @param assignedTo    Nuevo operador responsable.
     * @param decisionNotes Notas del cambio.
     */
    public void updateStatus(String status, String assignedTo, String decisionNotes) {
        this.status = status;
        this.assignedTo = assignedTo;
        this.decisionNotes = decisionNotes;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters que garantizan el encapsulamiento
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getInReviewAt() { return inReviewAt; }
    public void setInReviewAt(LocalDateTime inReviewAt) { this.inReviewAt = inReviewAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<KycDocument> getDocuments() { return documents; }
    public void setDocuments(List<KycDocument> documents) { this.documents = documents; }
}
