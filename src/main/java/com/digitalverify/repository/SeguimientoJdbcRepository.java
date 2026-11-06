package com.digitalverify.repository;

import com.digitalverify.model.SeguimientoEntry;
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
 * Repositorio de acceso a datos para las entradas del modulo de seguimiento KYC.
 * Registra formalmente las decisiones tomadas por los operadores internos sobre
 * los casos y el estado de la cuenta del jugador (ej. Reactivate, Keep Suspended).
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class SeguimientoJdbcRepository {

    /**
     * Plantilla JDBC de Spring para interactuar con la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias para el repositorio de seguimiento.
     *
     * @param jdbcTemplate Plantilla JDBC configurada.
     */
    public SeguimientoJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos SeguimientoEntry, incluyendo datos del operador y numero de caso.
     */
    private final RowMapper<SeguimientoEntry> rowMapper = (rs, rowNum) -> {
        SeguimientoEntry entry = new SeguimientoEntry(
                rs.getLong("id"),
                rs.getLong("case_id"),
                rs.getLong("internal_user_id"),
                rs.getString("verification_decision"),
                rs.getString("account_status_decision"),
                rs.getString("comments"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
        );
        try {
            entry.setOperatorName(rs.getString("operator_name"));
            entry.setCaseNumber(rs.getString("case_number"));
        } catch (Exception ignored) {}
        return entry;
    };

    /**
     * Obtiene el historial de decisiones de seguimiento asociadas a un caso KYC especifico.
     *
     * @param caseId Identificador unico del caso KYC.
     * @return Lista de entradas de seguimiento ordenadas descendentemente por fecha.
     */
    public List<SeguimientoEntry> findByCaseId(Long caseId) {
        // Realiza JOIN con tablas users y kyc_cases para enriquecer nombres y referencias
        String sql = "SELECT s.*, u.full_name as operator_name, c.case_number as case_number " +
                "FROM seguimiento s " +
                "JOIN users u ON s.internal_user_id = u.id " +
                "JOIN kyc_cases c ON s.case_id = c.id " +
                "WHERE s.case_id = ? ORDER BY s.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, caseId);
    }

    /**
     * Inserta un nuevo registro de decision en la tabla seguimiento y asigna el ID generado.
     *
     * @param entry Objeto SeguimientoEntry con los detalles del dictamen emitido.
     * @return Identificador unico generado o null si ocurrio un error.
     */
    public Long save(SeguimientoEntry entry) {
        // Sentencia SQL de insercion en la tabla seguimiento
        String sql = "INSERT INTO seguimiento (case_id, internal_user_id, verification_decision, account_status_decision, comments, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Inserta la resolucion capturando la llave primaria autogenerada
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setLong(1, entry.getCaseId());
            ps.setLong(2, entry.getInternalUserId());
            ps.setString(3, entry.getVerificationDecision() != null ? entry.getVerificationDecision() : "Approve Verification");
            ps.setString(4, entry.getAccountStatusDecision() != null ? entry.getAccountStatusDecision() : "Reactivate Account");
            ps.setString(5, entry.getComments());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Extrae el identificador autogenerado y lo asigna al objeto
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
