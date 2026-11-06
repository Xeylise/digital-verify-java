package com.digitalverify.repository;

import com.digitalverify.model.ActivityLogEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Repositorio de acceso a datos para la tabla 'activity_logs' utilizando JdbcTemplate.
 * Implementa operaciones CRUD para registrar y consultar eventos de auditoria y trazabilidad.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class ActivityLogJdbcRepository {

    /**
     * Plantilla JDBC de Spring para ejecucion de consultas parametrizadas.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias del JdbcTemplate.
     *
     * @param jdbcTemplate Plantilla JDBC configurada con HikariCP.
     */
    public ActivityLogJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos ActivityLogEntry, incluyendo campos enriquecidos de joins.
     */
    private final RowMapper<ActivityLogEntry> rowMapper = (rs, rowNum) -> {
        ActivityLogEntry entry = new ActivityLogEntry(
                rs.getLong("id"),
                rs.getTimestamp("fecha") != null ? rs.getTimestamp("fecha").toLocalDateTime() : null,
                rs.getLong("usuario_interno_id"),
                rs.getString("tipo_evento"),
                rs.getObject("id_verification") != null ? rs.getLong("id_verification") : null,
                rs.getString("detalles")
        );
        try {
            entry.setOperatorName(rs.getString("operator_name"));
            entry.setCaseNumber(rs.getString("case_number"));
        } catch (Exception ignored) {}
        return entry;
    };

    /**
     * Recupera todos los registros de actividad ordenados cronologicamente de forma descendente.
     *
     * @return Lista de entradas de actividad con datos del operador y caso.
     */
    public List<ActivityLogEntry> findAll() {
        // Consulta SQL con joins hacia usuarios y casos KYC
        String sql = "SELECT a.*, u.full_name AS operator_name, c.case_number AS case_number " +
                "FROM activity_logs a " +
                "JOIN users u ON a.usuario_interno_id = u.id " +
                "LEFT JOIN kyc_cases c ON a.id_verification = c.id " +
                "ORDER BY a.fecha DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Busca los registros de actividad asociados a un caso especifico por su ID.
     *
     * @param caseId Identificador del caso de verificacion.
     * @return Lista de logs del caso.
     */
    public List<ActivityLogEntry> findByCaseId(Long caseId) {
        String sql = "SELECT a.*, u.full_name AS operator_name, c.case_number AS case_number " +
                "FROM activity_logs a " +
                "JOIN users u ON a.usuario_interno_id = u.id " +
                "LEFT JOIN kyc_cases c ON a.id_verification = c.id " +
                "WHERE a.id_verification = ? " +
                "ORDER BY a.fecha DESC";
        return jdbcTemplate.query(sql, rowMapper, caseId);
    }

    /**
     * Busca los registros de actividad filtrando por el numero o codigo de caso (ej: DV-1024).
     *
     * @param caseNumber Codigo publico del caso.
     * @return Lista de logs coincidentes.
     */
    public List<ActivityLogEntry> findByCaseNumber(String caseNumber) {
        String sql = "SELECT a.*, u.full_name AS operator_name, c.case_number AS case_number " +
                "FROM activity_logs a " +
                "JOIN users u ON a.usuario_interno_id = u.id " +
                "LEFT JOIN kyc_cases c ON a.id_verification = c.id " +
                "WHERE LOWER(c.case_number) = LOWER(?) " +
                "ORDER BY a.fecha DESC";
        return jdbcTemplate.query(sql, rowMapper, caseNumber);
    }

    /**
     * Inserta un nuevo registro de actividad en la base de datos y asigna la clave primaria generada.
     *
     * @param entry Entidad con los datos del evento a persistir.
     * @return Identificador autogenerado del registro guardado.
     */
    public Long save(ActivityLogEntry entry) {
        String sql = "INSERT INTO activity_logs (fecha, usuario_interno_id, tipo_evento, id_verification, detalles) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        // Ejecucion con PreparedStatement y extraccion de ID generado
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setTimestamp(1, entry.getFecha() != null ? Timestamp.valueOf(entry.getFecha()) : new Timestamp(System.currentTimeMillis()));
            ps.setLong(2, entry.getUsuarioInternoId() != null ? entry.getUsuarioInternoId() : 7L);
            ps.setString(3, entry.getTipoEvento());
            if (entry.getIdVerification() != null) {
                ps.setLong(4, entry.getIdVerification());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
            ps.setString(5, entry.getDetalles());
            return ps;
        }, keyHolder);

        // Validacion y extraccion de la clave generada
        if (keyHolder.getKeyList().isEmpty()) {
            return null;
        }
        Map<String, Object> keys = keyHolder.getKeyList().get(0);
        Object idVal = keys.get("ID") != null ? keys.get("ID") : keys.get("id");
        if (idVal instanceof Number n) {
            entry.setId(n.longValue());
            return n.longValue();
        }
        return null;
    }
}
