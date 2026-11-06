package com.digitalverify.repository;

import com.digitalverify.model.KycDocument;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para documentos KYC (kyc_documents).
 * Gestiona el almacenamiento, consulta y actualizacion de estado de los archivos
 * cargados por los usuarios para verificacion de identidad o domicilio.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class DocumentJdbcRepository {

    /**
     * Plantilla JDBC de Spring para interactuar con la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias del repositorio de documentos.
     *
     * @param jdbcTemplate Plantilla JDBC configurada.
     */
    public DocumentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos KycDocument.
     */
    private final RowMapper<KycDocument> docRowMapper = (rs, rowNum) -> new KycDocument(
            rs.getLong("id"),
            rs.getLong("case_id"),
            rs.getString("document_type"),
            rs.getString("file_name"),
            rs.getString("file_path"),
            rs.getLong("file_size"),
            rs.getString("mime_type"),
            rs.getString("status"),
            rs.getTimestamp("uploaded_at") != null ? rs.getTimestamp("uploaded_at").toLocalDateTime() : null
    );

    /**
     * Obtiene todos los documentos vinculados a un caso KYC especifico.
     *
     * @param caseId Identificador unico del caso KYC.
     * @return Lista de documentos asociados ordenados por fecha de subida.
     */
    public List<KycDocument> findByCaseId(Long caseId) {
        // Consulta documentos por clave foranea del caso
        String sql = "SELECT * FROM kyc_documents WHERE case_id = ? ORDER BY uploaded_at ASC";
        return jdbcTemplate.query(sql, docRowMapper, caseId);
    }

    /**
     * Busca un documento KYC por su identificador unico primario.
     *
     * @param id Identificador unico del documento.
     * @return Contenedor Optional con el documento si existe, o vacio si no se encuentra.
     */
    public Optional<KycDocument> findById(Long id) {
        // Consulta segura por identificador unico
        String sql = "SELECT * FROM kyc_documents WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, docRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Inserta un nuevo registro de documento KYC y asigna el ID generado.
     *
     * @param doc Objeto KycDocument con los metadatos y ruta del archivo almacenado.
     * @return Identificador unico generado o null si fallo la insercion.
     */
    public Long save(KycDocument doc) {
        // Sentencia SQL para insertar documento KYC
        String sql = "INSERT INTO kyc_documents (case_id, document_type, file_name, file_path, file_size, mime_type, status, uploaded_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Ejecuta actualizacion extrayendo llave autogenerada
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setLong(1, doc.getCaseId());
            ps.setString(2, doc.getDocumentType());
            ps.setString(3, doc.getFileName());
            ps.setString(4, doc.getFilePath());
            ps.setLong(5, doc.getFileSize() != null ? doc.getFileSize() : 0L);
            ps.setString(6, doc.getMimeType());
            ps.setString(7, doc.getStatus() != null ? doc.getStatus() : "PENDING");
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Asigna la llave generada a la entidad
        Long generatedId = extractId(keyHolder);
        if (generatedId != null) {
            doc.setId(generatedId);
            return generatedId;
        }
        return null;
    }

    /**
     * Actualiza el estado de revision de un documento (ej. APPROVED, REJECTED, PENDING).
     *
     * @param docId Identificador unico del documento.
     * @param status Nuevo estado a asignar.
     * @return Numero de registros afectados en la base de datos.
     */
    public int updateStatus(Long docId, String status) {
        // Actualiza el campo status del documento
        String sql = "UPDATE kyc_documents SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, docId);
    }

    /**
     * Extrae de forma segura el identificador numerico autogenerado desde el KeyHolder.
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
