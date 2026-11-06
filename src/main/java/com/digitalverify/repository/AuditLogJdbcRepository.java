package com.digitalverify.repository;

import com.digitalverify.model.AuditLogEntry;
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
 * Repositorio de acceso a datos para los registros de auditoria (audit_logs).
 * Permite registrar y consultar las acciones realizadas por los operadores
 * sobre los casos KYC evaluados en el sistema.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class AuditLogJdbcRepository {

    /**
     * Plantilla de Spring para ejecucion de consultas JDBC directas.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor para inyeccion de dependencias del repositorio de auditoria.
     *
     * @param jdbcTemplate Plantilla JDBC de Spring configurada para el DataSource.
     */
    public AuditLogJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos AuditLogEntry.
     */
    private final RowMapper<AuditLogEntry> logRowMapper = (rs, rowNum) -> new AuditLogEntry(
            rs.getLong("id"),
            rs.getString("operator_name"),
            rs.getString("case_number"),
            rs.getString("action"),
            rs.getString("details"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
    );

    /**
     * Obtiene el listado completo de registros de auditoria ordenados descendentemente por fecha.
     *
     * @return Lista de entradas de auditoria encontradas.
     */
    public List<AuditLogEntry> findAll() {
        // Ejecuta consulta para obtener todas las acciones de auditoria
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, logRowMapper);
    }

    /**
     * Busca los registros de auditoria asociados a un numero de caso especifico.
     *
     * @param caseNumber Codigo de referencia del caso KYC (ej. DV-1019).
     * @return Lista de entradas de auditoria correspondientes al caso.
     */
    public List<AuditLogEntry> findByCaseNumber(String caseNumber) {
        // Consulta insensible a mayusculas/minusculas para el numero de caso
        String sql = "SELECT * FROM audit_logs WHERE LOWER(case_number) = LOWER(?) ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, logRowMapper, caseNumber);
    }

    /**
     * Inserta un nuevo registro de auditoria en la base de datos y asigna el identificador generado.
     *
     * @param entry Objeto AuditLogEntry con los datos de la accion realizada.
     * @return Identificador unico generado para la entrada de auditoria, o null si falla.
     */
    public Long save(AuditLogEntry entry) {
        // Sentencia SQL de insercion en tabla audit_logs
        String sql = "INSERT INTO audit_logs (operator_name, case_number, action, details, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Ejecuta la actualizacion capturando la llave primaria autogenerada
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, entry.getOperatorName());
            ps.setString(2, entry.getCaseNumber());
            ps.setString(3, entry.getAction());
            ps.setString(4, entry.getDetails());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Extrae el identificador autogenerado y lo asigna al objeto
        Long generatedId = extractId(keyHolder);
        if (generatedId != null) {
            entry.setId(generatedId);
            return generatedId;
        }
        return null;
    }

    /**
     * Metodo auxiliar para extraer de forma segura el identificador generado por H2 o bases relacionales.
     *
     * @param keyHolder Contenedor de llaves autogeneradas.
     * @return Identificador numerico o null si no se genero.
     */
    private Long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKeyList().isEmpty()) {
            return null;
        }
        Map<String, Object> keys = keyHolder.getKeyList().get(0);
        Object idVal = keys.get("ID") != null ? keys.get("ID") : keys.get("id");
        if (idVal instanceof Number n) {
            return n.longValue();
        }
        return null;
    }
}
