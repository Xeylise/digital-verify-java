package com.digitalverify.repository;

import com.digitalverify.model.Notification;
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
 * Repositorio de acceso a datos para notificaciones de usuario (notifications).
 * Permite registrar alertas sobre cambios en el estado de revision KYC,
 * consultar alertas pendientes y actualizar su estado de lectura.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class NotificationJdbcRepository {

    /**
     * Plantilla JDBC de Spring para interactuar con la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias para el repositorio de notificaciones.
     *
     * @param jdbcTemplate Plantilla JDBC configurada.
     */
    public NotificationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a objetos Notification.
     */
    private final RowMapper<Notification> notifRowMapper = (rs, rowNum) -> new Notification(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("title"),
            rs.getString("message"),
            rs.getString("category"),
            rs.getBoolean("is_read"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
    );

    /**
     * Obtiene el listado completo de notificaciones de un usuario ordenadas por fecha.
     *
     * @param userId Identificador del usuario.
     * @return Lista de notificaciones del usuario.
     */
    public List<Notification> findByUserId(Long userId) {
        // Consulta notificaciones asociadas al usuario
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, notifRowMapper, userId);
    }

    /**
     * Obtiene unicamente las notificaciones no leidas de un usuario.
     *
     * @param userId Identificador del usuario.
     * @return Lista de notificaciones sin leer.
     */
    public List<Notification> findUnreadByUserId(Long userId) {
        // Filtra notificaciones donde is_read sea false
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, notifRowMapper, userId);
    }

    /**
     * Cuenta la cantidad de notificaciones no leidas de un usuario.
     *
     * @param userId Identificador del usuario.
     * @return Numero de notificaciones no leidas.
     */
    public long countUnreadByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0L;
    }

    /**
     * Marca una notificacion individual como leida.
     *
     * @param notificationId Identificador de la notificacion.
     * @return Numero de registros actualizados.
     */
    public int markAsRead(Long notificationId) {
        // Actualiza la bandera is_read a true
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        return jdbcTemplate.update(sql, notificationId);
    }

    /**
     * Marca todas las notificaciones pendientes de un usuario como leidas.
     *
     * @param userId Identificador del usuario.
     * @return Total de registros modificados.
     */
    public int markAllAsRead(Long userId) {
        // Actualiza masivamente a true todas las notificaciones del usuario
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ?";
        return jdbcTemplate.update(sql, userId);
    }

    /**
     * Elimina una notificacion especifica de la base de datos.
     *
     * @param notificationId Identificador de la notificacion a eliminar.
     * @return Numero de registros eliminados.
     */
    public int deleteById(Long notificationId) {
        // Ejecuta eliminacion por identificador primario
        String sql = "DELETE FROM notifications WHERE id = ?";
        return jdbcTemplate.update(sql, notificationId);
    }

    /**
     * Inserta una nueva notificacion y actualiza el objeto con el ID autogenerado.
     *
     * @param notif Objeto Notification con el mensaje y destinatario.
     * @return Identificador unico generado o null si ocurrio un error.
     */
    public Long save(Notification notif) {
        // Sentencia SQL de insercion
        String sql = "INSERT INTO notifications (user_id, title, message, category, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Inserta la notificacion capturando la clave primaria generada
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setLong(1, notif.getUserId());
            ps.setString(2, notif.getTitle());
            ps.setString(3, notif.getMessage());
            ps.setString(4, notif.getCategory() != null ? notif.getCategory() : "General");
            ps.setBoolean(5, notif.getIsRead() != null && notif.getIsRead());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Extrae el identificador autogenerado y lo asigna al objeto
        Long generatedId = extractId(keyHolder);
        if (generatedId != null) {
            notif.setId(generatedId);
            return generatedId;
        }
        return null;
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
