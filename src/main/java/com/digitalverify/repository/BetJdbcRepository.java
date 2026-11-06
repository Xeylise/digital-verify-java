package com.digitalverify.repository;

import com.digitalverify.model.Bet;
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
 * Repositorio de acceso a datos para apuestas deportivas (bets).
 * Permite gestionar el historial de apuestas realizadas por los jugadores,
 * sus montos, cuotas y estados en la plataforma.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Repository
public class BetJdbcRepository {

    /**
     * Plantilla JDBC de Spring para interactuar con la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor con inyeccion de dependencias para el repositorio de apuestas.
     *
     * @param jdbcTemplate Plantilla JDBC configurada.
     */
    public BetJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Mapeador de filas SQL a instancias del modelo Bet.
     */
    private final RowMapper<Bet> betRowMapper = (rs, rowNum) -> new Bet(
            rs.getLong("id"),
            rs.getString("bet_reference"),
            rs.getLong("user_id"),
            rs.getString("event_name"),
            rs.getString("selection"),
            rs.getBigDecimal("odds"),
            rs.getBigDecimal("stake"),
            rs.getBigDecimal("payout"),
            rs.getString("status"),
            rs.getTimestamp("placed_at") != null ? rs.getTimestamp("placed_at").toLocalDateTime() : null
    );

    /**
     * Consulta todas las apuestas realizadas por un usuario especifico.
     *
     * @param userId Identificador del usuario jugador.
     * @return Lista de apuestas ordenadas cronologicamente descendente.
     */
    public List<Bet> findByUserId(Long userId) {
        // Filtra apuestas por identificador de usuario
        String sql = "SELECT * FROM bets WHERE user_id = ? ORDER BY placed_at DESC";
        return jdbcTemplate.query(sql, betRowMapper, userId);
    }

    /**
     * Consulta las apuestas de un usuario filtradas por su estado (ej. WON, PENDING, LOST).
     *
     * @param userId Identificador del usuario.
     * @param status Estado de la apuesta a filtrar.
     * @return Lista de apuestas coincidentes.
     */
    public List<Bet> findByUserIdAndStatus(Long userId, String status) {
        // Filtra por usuario y estado comparando en minusculas
        String sql = "SELECT * FROM bets WHERE user_id = ? AND LOWER(status) = LOWER(?) ORDER BY placed_at DESC";
        return jdbcTemplate.query(sql, betRowMapper, userId, status);
    }

    /**
     * Almacena una nueva apuesta en la base de datos y retorna su identificador asignado.
     *
     * @param bet Objeto Bet con la informacion de la apuesta.
     * @return Identificador unico generado o null si ocurrio un error.
     */
    public Long save(Bet bet) {
        // Sentencia SQL para insertar registro de apuesta
        String sql = "INSERT INTO bets (bet_reference, user_id, event_name, selection, odds, stake, payout, status, placed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Inserta el registro capturando la clave primaria generada
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, bet.getBetReference());
            ps.setLong(2, bet.getUserId());
            ps.setString(3, bet.getEventName());
            ps.setString(4, bet.getSelection());
            ps.setBigDecimal(5, bet.getOdds());
            ps.setBigDecimal(6, bet.getStake());
            ps.setBigDecimal(7, bet.getPayout());
            ps.setString(8, bet.getStatus());
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            return ps;
        }, keyHolder);

        // Extrae el ID generado y actualiza el objeto en memoria
        Long generatedId = extractId(keyHolder);
        if (generatedId != null) {
            bet.setId(generatedId);
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
