package com.digitalverify.service.common;

import com.digitalverify.model.User;
import com.digitalverify.repository.UserJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de autenticacion y gestion de credenciales para usuarios y operadores.
 * Controla el inicio de sesion, registro de nuevos jugadores, actualizacion de contraseñas
 * y configuraciones de preferencias personales de interfaz.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Service
public class AuthService {

    /**
     * Repositorio de acceso a datos para usuarios.
     */
    private final UserJdbcRepository userRepo;

    /**
     * Constructor para inyeccion de dependencias del servicio de autenticacion.
     *
     * @param userRepo Repositorio de usuarios.
     */
    public AuthService(UserJdbcRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Valida las credenciales de acceso de un usuario segun su correo electronico y contraseña.
     *
     * @param email Direccion de correo electronico suministrada.
     * @param password Contraseña ingresada por el usuario.
     * @return Optional con el usuario autenticado si las credenciales son validas, o vacio si son incorrectas.
     */
    public Optional<User> authenticate(String email, String password) {
        // Busca usuario por correo electronico normalizado
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Compara la contraseña almacenada con la ingresada
            if (user.getPasswordHash().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Registra un nuevo usuario con rol de jugador (PLAYER) en el sistema.
     *
     * @param fullName Nombre completo del jugador.
     * @param email Correo electronico unico.
     * @param password Contraseña elegida.
     * @param phone Numero telefonico de contacto.
     * @return Optional con la entidad User creada y su ID asignado, o vacio si el correo ya se encuentra registrado.
     */
    public Optional<User> register(String fullName, String email, String password, String phone) {
        // Verifica si el correo ya existe previamente en base de datos
        if (userRepo.findByEmail(email).isPresent()) {
            return Optional.empty();
        }

        // Construye entidad con valores iniciales de registro seguro
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPasswordHash(password);
        newUser.setPhone(phone);
        newUser.setRole("PLAYER");
        newUser.setCountry("CO");
        newUser.setKycStatus("PENDING");
        newUser.setAvatarUrl("/img/user_avatar.jpg");
        newUser.setTwoFactorEnabled(false);

        // Persiste en repositorio y asigna identificador autogenerado
        Long id = userRepo.save(newUser);
        newUser.setId(id);
        return Optional.of(newUser);
    }

    /**
     * Obtiene un usuario a partir de su identificador unico primario.
     *
     * @param id Identificador unico del usuario.
     * @return Optional con el usuario si existe, o vacio si no se encuentra.
     */
    public Optional<User> findById(Long id) {
        // Consulta en repositorio por ID primario
        return userRepo.findById(id);
    }

    /**
     * Actualiza las configuraciones de idioma, zona horaria y alertas de cola de un usuario.
     *
     * @param userId Identificador del usuario.
     * @param language Codigo de idioma preferido (ej. es, en).
     * @param timezone Zona horaria seleccionada (ej. utc-5).
     * @param queueAlertsEnabled Indicador de notificaciones para nuevas tareas en cola.
     * @return true si la actualizacion fue exitosa; false en caso contrario.
     */
    public boolean updateSettings(Long userId, String language, String timezone, Boolean queueAlertsEnabled) {
        // Persiste las nuevas preferencias en el repositorio
        return userRepo.updateSettings(userId, language, timezone, queueAlertsEnabled) > 0;
    }

    /**
     * Actualiza la contraseña de acceso de un usuario.
     *
     * @param userId Identificador unico del usuario.
     * @param newPassword Nueva clave de acceso.
     * @return true si la clave se actualizo correctamente; false en caso contrario.
     */
    public boolean updatePassword(Long userId, String newPassword) {
        // Modifica el hash de la contraseña en base de datos
        return userRepo.updatePassword(userId, newPassword) > 0;
    }
}
