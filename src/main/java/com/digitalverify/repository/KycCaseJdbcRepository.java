package com.digitalverify.repository;

import com.digitalverify.model.KycCase;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para los casos de verificacion de identidad (kyc_cases).
 * Provee operaciones CRUD, consultas filtradas con paginacion/busqueda y metodos
 * estadisticos para la gestion de casos en el backoffice de analistas y oficiales de cumplimiento.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class KycCaseJdbcRepository {

    /**
     * Plantilla JDBC de Spring para interactuar con la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias para el repositorio de casos KYC.
     *
     * @param jdbcTemplate Plantilla JDBC configurada.
     */
    public KycCaseJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos KycCase.
     */
    private final RowMapper<KycCase> caseRowMapper = (rs, rowNum) -> new KycCase(
            rs.getLong("id"),
            rs.getString("case_number"),
            rs.getLong("user_id"),
            rs.getString("account_number"),
            rs.getString("player_name"),
            rs.getString("country"),
            rs.getString("account_status"),
            rs.getString("status"),
            rs.getInt("risk_score"),
            rs.getString("reason"),
            rs.getString("assigned_to"),
            rs.getString("decision_notes"),
            rs.getTimestamp("assigned_at") != null ? rs.getTimestamp("assigned_at").toLocalDateTime() : null,
            rs.getTimestamp("in_review_at") != null ? rs.getTimestamp("in_review_at").toLocalDateTime() : null,
            rs.getTimestamp("resolved_at") != null ? rs.getTimestamp("resolved_at").toLocalDateTime() : null,
            rs.getTimestamp("submitted_at") != null ? rs.getTimestamp("submitted_at").toLocalDateTime() : null,
            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
    );

    /**
     * Busca un caso KYC por su clave primaria ID.
     *
     * @param id Identificador unico del caso.
     * @return Optional con el caso KYC encontrado o vacio si no existe.
     */
    public Optional<KycCase> findById(Long id) {
        // Ejecuta consulta por llave primaria
        String sql = "SELECT * FROM kyc_cases WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, caseRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca un caso KYC por su codigo unico de referencia de caso (ej. DV-1019).
     *
     * @param caseNumber Codigo de referencia del caso.
     * @return Optional con el caso o vacio si no coincide.
     */
    public Optional<KycCase> findByCaseNumber(String caseNumber) {
        // Realiza consulta insensible a mayusculas/minusculas
        String sql = "SELECT * FROM kyc_cases WHERE LOWER(case_number) = LOWER(?)";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, caseRowMapper, caseNumber));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene el caso KYC mas reciente sometido por un usuario jugador especifico.
     *
     * @param userId Identificador del usuario.
     * @return Optional con el caso mas reciente del usuario.
     */
    public Optional<KycCase> findLatestByUserId(Long userId) {
        // Ordena descendentemente por fecha de envio y toma el primer registro
        String sql = "SELECT * FROM kyc_cases WHERE user_id = ? ORDER BY submitted_at DESC LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, caseRowMapper, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Retorna la totalidad de casos KYC en orden cronologico de envio.
     *
     * @return Lista completa de casos registrados.
     */
    public List<KycCase> findAll() {
        // Consulta todos los casos ordenados por fecha de radicacion
        return jdbcTemplate.query("SELECT * FROM kyc_cases ORDER BY submitted_at DESC", caseRowMapper);
    }

    /**
     * Busca casos KYC aplicando filtros combinados opcionales de estado, texto de busqueda, analista asignado y motivo.
     *
     * @param status Estado del caso (pending, in_review, approved, rejected, o null/all para ignorar).
     * @param search Texto para buscar por coincidencia en player_name, case_number o account_number.
     * @param assignedTo Nombre del operador asignado o null/all para ignorar.
     * @param reason Motivo de activacion del KYC o null/all para ignorar.
     * @return Lista de casos que cumplen con todos los criterios de busqueda especificados.
     */
    public List<KycCase> findFiltered(String status, String search, String assignedTo, String reason) {
        StringBuilder sql = new StringBuilder("SELECT * FROM kyc_cases WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // Filtro condicional por estado
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) {
            sql.append("AND status = ? ");
            params.add(status.toLowerCase());
        }

        // Filtro condicional por texto de busqueda omnicanal (nombre, caso o cuenta)
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(player_name) LIKE ? OR LOWER(case_number) LIKE ? OR LOWER(account_number) LIKE ?) ");
            String wildcard = "%" + search.toLowerCase().trim() + "%";
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        // Filtro condicional por operador asignado
        if (assignedTo != null && !assignedTo.isBlank() && !assignedTo.equalsIgnoreCase("all")) {
            sql.append("AND LOWER(assigned_to) LIKE ? ");
            params.add("%" + assignedTo.toLowerCase().trim() + "%");
        }

        // Filtro condicional por motivo de activacion
        if (reason != null && !reason.isBlank() && !reason.equalsIgnoreCase("all")) {
            sql.append("AND LOWER(reason) LIKE ? ");
            params.add("%" + reason.toLowerCase().trim() + "%");
        }

        sql.append("ORDER BY submitted_at DESC");
        return jdbcTemplate.query(sql.toString(), caseRowMapper, params.toArray());
    }

    /**
     * Inserta un nuevo caso KYC en la base de datos y retorna el identificador autogenerado.
     *
     * @param kycCase Objeto KycCase con los datos iniciales del caso.
     * @return Identificador unico generado o null si fallo la insercion.
     */
    public Long save(KycCase kycCase) {
        // Sentencia de insercion completa
        String sql = "INSERT INTO kyc_cases (case_number, user_id, account_number, player_name, country, account_status, status, risk_score, reason, assigned_to, decision_notes, assigned_at, in_review_at, resolved_at, submitted_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Ejecuta insercion con captura de llave
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, kycCase.getCaseNumber());
            ps.setLong(2, kycCase.getUserId());
            ps.setString(3, kycCase.getAccountNumber() != null ? kycCase.getAccountNumber() : "80948");
            ps.setString(4, kycCase.getPlayerName());
            ps.setString(5, kycCase.getCountry() != null ? kycCase.getCountry() : "CO");
            ps.setString(6, kycCase.getAccountStatus() != null ? kycCase.getAccountStatus() : "Active");
            ps.setString(7, kycCase.getStatus() != null ? kycCase.getStatus() : "pending");
            ps.setInt(8, kycCase.getRiskScore() != null ? kycCase.getRiskScore() : 50);
            ps.setString(9, kycCase.getReason());
            ps.setString(10, kycCase.getAssignedTo() != null ? kycCase.getAssignedTo() : "Unassigned");
            ps.setString(11, kycCase.getDecisionNotes());
            ps.setTimestamp(12, kycCase.getAssignedAt() != null ? Timestamp.valueOf(kycCase.getAssignedAt()) : null);
            ps.setTimestamp(13, kycCase.getInReviewAt() != null ? Timestamp.valueOf(kycCase.getInReviewAt()) : null);
            ps.setTimestamp(14, kycCase.getResolvedAt() != null ? Timestamp.valueOf(kycCase.getResolvedAt()) : null);
            ps.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(16, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Asigna identificador generado a la entidad
        Long generatedId = extractId(keyHolder);
        if (generatedId != null) {
            kycCase.setId(generatedId);
            return generatedId;
        }
        return null;
    }

    /**
     * Actualiza el estado resolutivo y las notas de decision de un caso evaluado.
     *
     * @param caseId Identificador unico del caso.
     * @param status Nuevo estado (approved o rejected).
     * @param assignedTo Operador que emitio el veredicto.
     * @param decisionNotes Comentarios y justificacion de la decision.
     * @return Cantidad de registros modificados.
     */
    public int updateStatusAndDecision(Long caseId, String status, String assignedTo, String decisionNotes) {
        // Actualiza decision resolutiva con marcas de tiempo actuales
        String sql = "UPDATE kyc_cases SET status = ?, assigned_to = ?, decision_notes = ?, resolved_at = ?, updated_at = ? WHERE id = ?";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return jdbcTemplate.update(sql, status, assignedTo, decisionNotes, now, now, caseId);
    }

    /**
     * Asigna un operador de cumplimiento a un caso y cambia su estado a 'in_review'.
     *
     * @param caseId Identificador del caso.
     * @param operatorName Nombre del analista u operador asignado.
     * @return Cantidad de filas afectadas.
     */
    public int assignOperator(Long caseId, String operatorName) {
        // Marca el caso en revision y fija el analista asignado
        String sql = "UPDATE kyc_cases SET assigned_to = ?, status = 'in_review', assigned_at = ?, in_review_at = ?, updated_at = ? WHERE id = ?";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return jdbcTemplate.update(sql, operatorName, now, now, now, caseId);
    }

    /**
     * Cuenta la cantidad de casos que tienen un estado determinado.
     *
     * @param status Estado a consultar.
     * @return Total de casos en dicho estado.
     */
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM kyc_cases WHERE status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status);
        return count != null ? count : 0L;
    }

    /**
     * Cuenta la cantidad de casos asociados a un motivo de verificacion especifico.
     *
     * @param reason Motivo a contabilizar (ej. withdrawal_threshold).
     * @return Total de casos con dicho motivo.
     */
    public long countByReason(String reason) {
        String sql = "SELECT COUNT(*) FROM kyc_cases WHERE reason = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, reason);
        return count != null ? count : 0L;
    }

    /**
     * Cuenta la cantidad de casos asignados a un operador determinado.
     *
     * @param operatorName Nombre del operador.
     * @return Total de casos bajo la responsabilidad del operador.
     */
    public long countByAssignedTo(String operatorName) {
        String sql = "SELECT COUNT(*) FROM kyc_cases WHERE LOWER(assigned_to) LIKE ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, "%" + operatorName.toLowerCase() + "%");
        return count != null ? count : 0L;
    }

    /**
     * Cuenta el total global de casos KYC registrados en el sistema.
     *
     * @return Numero total de casos.
     */
    public long countTotal() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kyc_cases", Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Obtiene los casos resueltos o radicados en un mes particular para generacion de reportes.
     *
     * @param yearMonth Periodo en formato 'yyyy-MM'.
     * @return Lista de casos correspondientes al mes.
     */
    public List<KycCase> findCasesByMonth(String yearMonth) {
        // Consulta casos agrupados por mes de resolucion o envio
        String sql = "SELECT * FROM kyc_cases WHERE FORMATDATETIME(COALESCE(resolved_at, submitted_at), 'yyyy-MM') = ? ORDER BY submitted_at DESC";
        return jdbcTemplate.query(sql, caseRowMapper, yearMonth);
    }

    /**
     * Cuenta la cantidad de casos revisados (aprobados o rechazados) en un mes particular.
     *
     * @param yearMonth Periodo en formato 'yyyy-MM'.
     * @return Cantidad de casos revisados en el periodo.
     */
    public long countReviewedInMonth(String yearMonth) {
        String sql = "SELECT COUNT(*) FROM kyc_cases WHERE status IN ('approved', 'rejected') AND FORMATDATETIME(COALESCE(resolved_at, submitted_at), 'yyyy-MM') = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, yearMonth);
        return count != null ? count : 0L;
    }

    /**
     * Extrae de forma segura el identificador autogenerado desde el KeyHolder.
     *
     * @param keyHolder Contenedor de llaves generadas.
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
