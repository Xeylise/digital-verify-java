package com.digitalverify.dto.employee;

/**
 * Objeto de Transferencia de Datos (DTO) para registrar la decision de un operador
 * sobre un caso KYC en el Backoffice. Soporta el modelo de decision dual requerido por
 * el SENA: decision sobre la verificacion y decision sobre el estado de la cuenta.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class CaseDecisionRequest {

    /**
     * Estado general resultante del caso (approved, rejected, in_review, info_requested).
     */
    private String status;

    /**
     * Decision especifica sobre los documentos de verificacion (ej: Approve Verification, Reject Verification).
     */
    private String verificationDecision;

    /**
     * Decision sobre el estado de la cuenta del jugador (ej: Reactivate Account, Permanently Close Account).
     */
    private String accountStatusDecision;

    /**
     * Justificacion tecnica o motivo del dictamen emitido por el operador.
     */
    private String decisionNotes;

    /**
     * Nombre del operador responsable de la evaluacion del caso.
     */
    private String operatorName;

    /**
     * Constructor por defecto.
     */
    public CaseDecisionRequest() {}

    /**
     * Constructor para decisiones rapidas con parametros basicos.
     *
     * @param status        Estado del caso.
     * @param decisionNotes Notas u observaciones de la decision.
     * @param operatorName  Nombre del operador responsable.
     */
    public CaseDecisionRequest(String status, String decisionNotes, String operatorName) {
        this.status = status;
        this.decisionNotes = decisionNotes;
        this.operatorName = operatorName;
    }

    /**
     * Obtiene el estado normalizado del caso infiriendolo de la decision si no fue asignado directamente.
     *
     * @return Estado normalizado en minusculas (approved, rejected, info_requested).
     */
    public String getStatus() { 
        if (status != null && !status.isBlank()) {
            return status;
        }
        if (verificationDecision != null) {
            String vdLower = verificationDecision.toLowerCase();
            if (vdLower.contains("approve")) return "approved";
            if (vdLower.contains("reject")) return "rejected";
            if (vdLower.contains("additional")) return "info_requested";
        }
        return "approved";
    }

    /**
     * Establece el estado general del caso.
     *
     * @param status Estado a asignar.
     */
    public void setStatus(String status) { 
        this.status = status; 
    }

    /**
     * Obtiene la decision de verificacion formal con respaldo por defecto.
     *
     * @return Texto de la decision sobre la verificacion de documentos.
     */
    public String getVerificationDecision() { 
        if (verificationDecision != null && !verificationDecision.isBlank()) {
            return verificationDecision;
        }
        if ("approved".equalsIgnoreCase(status)) return "Approve Verification";
        if ("rejected".equalsIgnoreCase(status)) return "Reject Verification (Needs Reupload)";
        return "Request Additional Documents";
    }

    /**
     * Establece la decision sobre los documentos de verificacion.
     *
     * @param verificationDecision Decision de verificacion.
     */
    public void setVerificationDecision(String verificationDecision) { 
        this.verificationDecision = verificationDecision; 
    }

    /**
     * Obtiene la decision sobre el estado operativo de la cuenta del jugador.
     *
     * @return Accion sobre la cuenta (Reactivate Account, Permanently Close Account, Not Applicable).
     */
    public String getAccountStatusDecision() { 
        if (accountStatusDecision != null && !accountStatusDecision.isBlank()) {
            return accountStatusDecision;
        }
        if ("approved".equalsIgnoreCase(getStatus())) return "Reactivate Account";
        if ("rejected".equalsIgnoreCase(getStatus())) return "Permanently Close Account";
        return "Not Applicable";
    }

    /**
     * Establece la decision sobre el estado de la cuenta.
     *
     * @param accountStatusDecision Accion a aplicar sobre la cuenta.
     */
    public void setAccountStatusDecision(String accountStatusDecision) { 
        this.accountStatusDecision = accountStatusDecision; 
    }

    /**
     * Obtiene las notas de decision ingresadas por el operador.
     *
     * @return Comentarios u observaciones del caso.
     */
    public String getDecisionNotes() { 
        return decisionNotes; 
    }

    /**
     * Establece las notas u observaciones de la decision.
     *
     * @param decisionNotes Comentarios a guardar.
     */
    public void setDecisionNotes(String decisionNotes) { 
        this.decisionNotes = decisionNotes; 
    }

    /**
     * Obtiene el nombre del operador evaluador.
     *
     * @return Nombre del operador.
     */
    public String getOperatorName() { 
        return operatorName; 
    }

    /**
     * Establece el nombre del operador evaluador.
     *
     * @param operatorName Nombre del operador.
     */
    public void setOperatorName(String operatorName) { 
        this.operatorName = operatorName; 
    }
}
