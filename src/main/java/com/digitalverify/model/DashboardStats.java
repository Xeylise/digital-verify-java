package com.digitalverify.model;

/**
 * Modelo de datos para las metricas y tarjetas KPI del Verification Dashboard en el Backoffice.
 * Agrupa contadores por estado, alertas de fraude y porcentaje de cumplimiento de SLA.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class DashboardStats {

    /**
     * Total absoluto de casos de verificacion registrados.
     */
    private long totalCases;

    /**
     * Casos en estado pendiente de inicio o revision basica.
     */
    private long pendingCases;

    /**
     * Casos actualmente en proceso de revision por analistas.
     */
    private long inReviewCases;

    /**
     * Casos resueltos satisfactoriamente con aprobacion.
     */
    private long approvedCases;

    /**
     * Casos rechazados o declinados por incumplimiento documental.
     */
    private long rejectedCases;

    /**
     * Casos clasificados bajo revision de fraude o riesgo critico.
     */
    private long fraudReviewCases;

    /**
     * Casos asignados especificamente al operador en sesion.
     */
    private long myAssignedCases;

    /**
     * Porcentaje de cumplimiento del Acuerdo de Nivel de Servicio (SLA).
     */
    private double slaCompliancePercentage;

    /**
     * Constructor por defecto.
     */
    public DashboardStats() {}

    /**
     * Constructor con todos los indicadores agregados del panel de control.
     *
     * @param totalCases              Total de casos.
     * @param pendingCases            Casos pendientes.
     * @param inReviewCases           Casos en revision.
     * @param approvedCases           Casos aprobados.
     * @param rejectedCases           Casos rechazados.
     * @param fraudReviewCases        Casos por fraude.
     * @param myAssignedCases         Casos asignados al operador.
     * @param slaCompliancePercentage Porcentaje de cumplimiento SLA.
     */
    public DashboardStats(long totalCases, long pendingCases, long inReviewCases,
                          long approvedCases, long rejectedCases, long fraudReviewCases,
                          long myAssignedCases, double slaCompliancePercentage) {
        this.totalCases = totalCases;
        this.pendingCases = pendingCases;
        this.inReviewCases = inReviewCases;
        this.approvedCases = approvedCases;
        this.rejectedCases = rejectedCases;
        this.fraudReviewCases = fraudReviewCases;
        this.myAssignedCases = myAssignedCases;
        this.slaCompliancePercentage = slaCompliancePercentage;
    }

    public long getTotalCases() { return totalCases; }
    public void setTotalCases(long totalCases) { this.totalCases = totalCases; }

    public long getPendingCases() { return pendingCases; }
    public void setPendingCases(long pendingCases) { this.pendingCases = pendingCases; }

    public long getInReviewCases() { return inReviewCases; }
    public void setInReviewCases(long inReviewCases) { this.inReviewCases = inReviewCases; }

    public long getApprovedCases() { return approvedCases; }
    public void setApprovedCases(long approvedCases) { this.approvedCases = approvedCases; }

    public long getRejectedCases() { return rejectedCases; }
    public void setRejectedCases(long rejectedCases) { this.rejectedCases = rejectedCases; }

    public long getFraudReviewCases() { return fraudReviewCases; }
    public void setFraudReviewCases(long fraudReviewCases) { this.fraudReviewCases = fraudReviewCases; }

    public long getMyAssignedCases() { return myAssignedCases; }
    public void setMyAssignedCases(long myAssignedCases) { this.myAssignedCases = myAssignedCases; }

    public double getSlaCompliancePercentage() { return slaCompliancePercentage; }
    public void setSlaCompliancePercentage(double slaCompliancePercentage) { this.slaCompliancePercentage = slaCompliancePercentage; }
}
