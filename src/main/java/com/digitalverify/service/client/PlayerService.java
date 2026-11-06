package com.digitalverify.service.client;

import com.digitalverify.model.Bet;
import com.digitalverify.model.User;
import com.digitalverify.repository.BetJdbcRepository;
import com.digitalverify.repository.UserJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de logica de negocio para operaciones del portal de clientes y jugadores.
 * Permite consultar y actualizar el perfil del jugador, asi como obtener su historial
 * de apuestas deportivas registradas en la plataforma.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Service
public class PlayerService {

    /**
     * Repositorio de acceso a datos para usuarios.
     */
    private final UserJdbcRepository userRepo;

    /**
     * Repositorio de acceso a datos para apuestas deportivas.
     */
    private final BetJdbcRepository betRepo;

    /**
     * Constructor con inyeccion de dependencias para el servicio de jugadores.
     *
     * @param userRepo Repositorio de usuarios.
     * @param betRepo Repositorio de apuestas deportivas.
     */
    public PlayerService(UserJdbcRepository userRepo, BetJdbcRepository betRepo) {
        this.userRepo = userRepo;
        this.betRepo = betRepo;
    }

    /**
     * Obtiene el perfil completo del jugador a partir de su identificador unico.
     *
     * @param userId Identificador unico del usuario jugador.
     * @return Optional conteniendo la informacion del usuario si existe, o vacio si no se encuentra.
     */
    public Optional<User> getPlayerProfile(Long userId) {
        // Consulta el usuario en base de datos por su ID primario
        return userRepo.findById(userId);
    }

    /**
     * Actualiza los datos de contacto y pais de residencia del perfil del jugador.
     *
     * @param userId Identificador del usuario a modificar.
     * @param fullName Nombre completo del jugador.
     * @param phone Numero telefonico de contacto.
     * @param country Codigo de pais de residencia (ej. CO, PE, MX).
     * @return true si la actualizacion en base de datos fue exitosa; false en caso contrario.
     */
    public boolean updateProfile(Long userId, String fullName, String phone, String country) {
        // Ejecuta actualizacion en el repositorio y valida que afecte al menos un registro
        return userRepo.updateProfile(userId, fullName, phone, country) > 0;
    }

    /**
     * Recupera el listado historico de apuestas realizadas por el jugador.
     *
     * @param userId Identificador unico del jugador.
     * @return Lista de apuestas ordenadas cronologicamente descendente.
     */
    public List<Bet> getPlayerBets(Long userId) {
        // Obtiene todas las apuestas asociadas al usuario
        return betRepo.findByUserId(userId);
    }

    /**
     * Recupera el historial de apuestas de un jugador filtradas por su estado actual (ej. WON, PENDING, LOST).
     *
     * @param userId Identificador unico del jugador.
     * @param status Estado de la apuesta a filtrar.
     * @return Lista de apuestas coincidentes con el criterio solicitado.
     */
    public List<Bet> getPlayerBetsByStatus(Long userId, String status) {
        // Consulta apuestas filtradas por estado en el repositorio
        return betRepo.findByUserIdAndStatus(userId, status);
    }
}
