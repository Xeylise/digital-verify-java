package com.digitalverify.service.employee;

import com.digitalverify.model.ActivityLogEntry;
import com.digitalverify.model.AuditLogEntry;
import com.digitalverify.repository.ActivityLogJdbcRepository;
import com.digitalverify.repository.AuditLogJdbcRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de gestion y registro de auditoria y trazabilidad operativa.
 * Proporciona metodos para registrar y consultar eventos tanto en la bitacora
 * general de auditoria (audit_logs) como en la tabla de actividad formal del SENA (activity_log).
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Service
public class AuditLogService {

    /**
     * Repositorio de acceso a datos para registros de auditoria operativa.
     */
    private final AuditLogJdbcRepository auditRepo;

    /**
     * Repositorio de acceso a datos para eventos de trazabilidad de actividad.
     */
    private final ActivityLogJdbcRepository activityLogRepo;

    /**
     * Constructor con inyeccion de dependencias para el servicio de auditoria.
     *
     * @param auditRepo Repositorio de logs de auditoria.
     * @param activityLogRepo Repositorio de registros de actividad.
     */
    public AuditLogService(AuditLogJdbcRepository auditRepo, ActivityLogJdbcRepository activityLogRepo) {
        this.auditRepo = auditRepo;
        this.activityLogRepo = activityLogRepo;
    }

    /**
     * Obtiene el listado completo de registros de auditoria ordenados descendentemente.
     *
     * @return Lista de entradas de auditoria.
     */
    public List<AuditLogEntry> getAllLogs() {
        return auditRepo.findAll();
    }

    /**
     * Obtiene la totalidad de eventos de actividad registrados en el sistema.
     *
     * @return Lista completa de registros de actividad.
     */
    public List<ActivityLogEntry> getAllActivityLogs() {
        return activityLogRepo.findAll();
    }

    /**
     * Consulta los registros de auditoria asociados a un caso especifico por su codigo de caso.
     *
     * @param caseNumber Codigo de referencia del caso (ej. DV-1019).
     * @return Lista de entradas de auditoria coincidentes.
     */
    public List<AuditLogEntry> getLogsByCase(String caseNumber) {
        return auditRepo.findByCaseNumber(caseNumber);
    }

    /**
     * Consulta los eventos de actividad asociados al identificador numerico de un caso.
     *
     * @param caseId Identificador unico del caso KYC.
     * @return Lista de eventos de actividad correspondientes.
     */
    public List<ActivityLogEntry> getActivityLogsByCaseId(Long caseId) {
        return activityLogRepo.findByCaseId(caseId);
    }

    /**
     * Registra una accion de auditoria efectuada por un operador sobre un caso KYC.
     *
     * @param operatorName Nombre del operador responsable de la accion.
     * @param caseNumber Numero o codigo del caso intervenido.
     * @param action Tipo o etiqueta de la accion realizada.
     * @param details Descripcion detallada o justificacion tecnica.
     * @return Identificador unico generado para el registro de auditoria.
     */
    public Long recordAction(String operatorName, String caseNumber, String action, String details) {
        // Instancia y asigna atributos de la entrada de auditoria
        AuditLogEntry entry = new AuditLogEntry();
        entry.setOperatorName(operatorName);
        entry.setCaseNumber(caseNumber);
        entry.setAction(action);
        entry.setDetails(details);
        return auditRepo.save(entry);
    }

    /**
     * Registra un evento de trazabilidad formal en la bitacora de actividad del sistema.
     *
     * @param internalUserId Identificador del usuario interno u operador.
     * @param eventType Tipo de evento ocurrido (ej. Decision, Asignacion, Carga).
     * @param verificationId Identificador del caso o verificacion intervenida.
     * @param details Detalles adicionales del evento.
     * @return Identificador unico generado para la entrada de actividad.
     */
    public Long recordActivity(Long internalUserId, String eventType, Long verificationId, String details) {
        // Crea y configura el registro de actividad
        ActivityLogEntry entry = new ActivityLogEntry();
        entry.setFecha(LocalDateTime.now());
        entry.setUsuarioInternoId(internalUserId != null ? internalUserId : 7L);
        entry.setTipoEvento(eventType);
        entry.setIdVerification(verificationId);
        entry.setDetalles(details);
        return activityLogRepo.save(entry);
    }
}
