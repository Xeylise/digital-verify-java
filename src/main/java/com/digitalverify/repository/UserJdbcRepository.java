package com.digitalverify.repository;

import com.digitalverify.model.User;
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
 * Repositorio de acceso a datos para la entidad de usuarios (users).
 * Administra el ciclo de vida, autenticacion, roles (PLAYER, KYC_OFFICER, SENIOR_OPERATOR),
 * perfil y configuraciones de preferencias de los usuarios del sistema.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class UserJdbcRepository {

    /**
     * Plantilla JDBC de Spring para interactuar con la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias para el repositorio de usuarios.
     *
     * @param jdbcTemplate Plantilla JDBC configurada.
     */
    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos User con valores por defecto seguros para preferencias.
     */
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        String lang = "en";
        String tz = "utc-5";
        boolean qAlerts = true;
        try {
            lang = rs.getString("language");
            tz = rs.getString("timezone");
            qAlerts = rs.getBoolean("queue_alerts_enabled");
        } catch (Exception ignored) {}

        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("country"),
                rs.getString("phone"),
                rs.getString("account_number"),
                rs.getString("kyc_status"),
                rs.getString("avatar_url"),
                rs.getBoolean("two_factor_enabled"),
                lang != null ? lang : "en",
                tz != null ? tz : "utc-5",
                qAlerts,
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
        );
    };

    /**
     * Busca un usuario por su identificador unico primario.
     *
     * @param id Identificador unico del usuario.
     * @return Optional con el usuario si existe, o vacio si no se encuentra.
     */
    public Optional<User> findById(Long id) {
        // Consulta segura por ID
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Busca un usuario por su direccion de correo electronico unica.
     *
     * @param email Correo electronico registrado.
     * @return Optional con el usuario si coincide, o vacio si no existe.
     */
    public Optional<User> findByEmail(String email) {
        // Consulta insensible a mayusculas/minusculas
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email.trim());
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene el listado de todos los usuarios registrados en el sistema.
     *
     * @return Lista completa de usuarios.
     */
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id ASC", userRowMapper);
    }

    /**
     * Filtra los usuarios de acuerdo a su rol en el sistema (ej. PLAYER, KYC_OFFICER).
     *
     * @param role Nombre del rol a consultar.
     * @return Lista de usuarios con dicho rol.
     */
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE UPPER(role) = UPPER(?) ORDER BY id ASC";
        return jdbcTemplate.query(sql, userRowMapper, role.trim());
    }

    /**
     * Registra un nuevo usuario en la base de datos y asigna el identificador autogenerado.
     *
     * @param user Entidad User con los datos de registro.
     * @return Identificador unico generado o null si fallo la creacion.
     */
    public Long save(User user) {
        // Sentencia SQL de insercion para usuario
        String sql = "INSERT INTO users (email, password_hash, full_name, role, country, phone, account_number, kyc_status, avatar_url, two_factor_enabled, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Inserta el registro capturando la llave primaria autogenerada
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, user.getEmail().trim());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName().trim());
            ps.setString(4, user.getRole() != null ? user.getRole() : "PLAYER");
            ps.setString(5, user.getCountry() != null ? user.getCountry() : "CO");
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getAccountNumber() != null ? user.getAccountNumber() : String.valueOf(10000 + (int)(Math.random() * 90000)));
            ps.setString(8, user.getKycStatus() != null ? user.getKycStatus() : "PENDING");
            ps.setString(9, user.getAvatarUrl() != null ? user.getAvatarUrl() : "/img/user_avatar.jpg");
            ps.setBoolean(10, user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled());
            ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Extrae el identificador autogenerado y lo asigna al objeto
        Long generatedId = extractId(keyHolder);
        if (generatedId != null) {
            user.setId(generatedId);
            return generatedId;
        }
        return null;
    }

    /**
     * Actualiza el estado KYC del usuario (ej. PENDING, IN_REVIEW, APPROVED, REJECTED).
     *
     * @param userId Identificador del usuario.
     * @param kycStatus Nuevo estado KYC.
     * @return Cantidad de filas afectadas.
     */
    public int updateKycStatus(Long userId, String kycStatus) {
        String sql = "UPDATE users SET kyc_status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, kycStatus, userId);
    }

    /**
     * Actualiza los datos de perfil basico del usuario (nombre completo, telefono y pais).
     *
     * @param userId Identificador del usuario.
     * @param fullName Nombre completo del usuario.
     * @param phone Numero telefonico de contacto.
     * @param country Codigo de pais (ej. CO, PE, MX).
     * @return Cantidad de filas actualizadas.
     */
    public int updateProfile(Long userId, String fullName, String phone, String country) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, country = ? WHERE id = ?";
        return jdbcTemplate.update(sql, fullName, phone, country, userId);
    }

    /**
     * Actualiza las preferencias de interfaz y notificaciones del usuario.
     *
     * @param userId Identificador del usuario.
     * @param language Idioma preferido (ej. es, en).
     * @param timezone Zona horaria seleccionada.
     * @param queueAlertsEnabled Indicador de alertas habilitadas para cola.
     * @return Cantidad de filas actualizadas.
     */
    public int updateSettings(Long userId, String language, String timezone, Boolean queueAlertsEnabled) {
        String sql = "UPDATE users SET language = ?, timezone = ?, queue_alerts_enabled = ? WHERE id = ?";
        return jdbcTemplate.update(sql, language, timezone, queueAlertsEnabled, userId);
    }

    /**
     * Actualiza la clave de acceso codificada del usuario.
     *
     * @param userId Identificador del usuario.
     * @param newPassword Clave codificada o en texto segun la politica activa.
     * @return Cantidad de filas actualizadas.
     */
    public int updatePassword(Long userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newPassword, userId);
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
