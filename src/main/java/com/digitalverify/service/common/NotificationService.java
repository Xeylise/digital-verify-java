package com.digitalverify.service.common;

import com.digitalverify.model.Notification;
import com.digitalverify.model.User;
import com.digitalverify.repository.NotificationJdbcRepository;
import com.digitalverify.repository.UserJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para la administracion de notificaciones del sistema.
 * Provee la logica para despachar alertas a usuarios individuales,
 * emitir notificaciones grupales por rol (ej. operadores y oficiales de cumplimiento),
 * y gestionar el estado de lectura de las mismas.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Service
public class NotificationService {

    /**
     * Repositorio de acceso a datos para notificaciones.
     */
    private final NotificationJdbcRepository notifRepo;

    /**
     * Repositorio de acceso a datos para usuarios.
     */
    private final UserJdbcRepository userRepo;

    /**
     * Constructor con inyeccion de dependencias para el servicio de notificaciones.
     *
     * @param notifRepo Repositorio de notificaciones.
     * @param userRepo Repositorio de usuarios.
     */
    public NotificationService(NotificationJdbcRepository notifRepo, UserJdbcRepository userRepo) {
        this.notifRepo = notifRepo;
        this.userRepo = userRepo;
    }

    /**
     * Obtiene el listado completo de notificaciones de un usuario.
     *
     * @param userId Identificador unico del usuario.
     * @return Lista de notificaciones ordenadas por fecha.
     */
    public List<Notification> getUserNotifications(Long userId) {
        // Consulta notificaciones historicas del usuario
        return notifRepo.findByUserId(userId);
    }

    /**
     * Obtiene unicamente las notificaciones pendientes de lectura de un usuario.
     *
     * @param userId Identificador del usuario.
     * @return Lista de notificaciones no leidas.
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        // Filtra notificaciones no leidas
        return notifRepo.findUnreadByUserId(userId);
    }

    /**
     * Cuenta la cantidad de notificaciones no leidas de un usuario para mostrar en el badge de la interfaz.
     *
     * @param userId Identificador del usuario.
     * @return Numero total de notificaciones no leidas.
     */
    public long getUnreadCount(Long userId) {
        return notifRepo.countUnreadByUserId(userId);
    }

    /**
     * Marca una notificacion individual como leida.
     *
     * @param notificationId Identificador de la notificacion.
     * @return true si la notificacion se marco exitosamente; false en caso contrario.
     */
    public boolean markAsRead(Long notificationId) {
        return notifRepo.markAsRead(notificationId) > 0;
    }

    /**
     * Marca todas las notificaciones pendientes de un usuario como leidas.
     *
     * @param userId Identificador unico del usuario.
     * @return true si se actualizaron registros; false si no habia notificaciones pendientes.
     */
    public boolean markAllAsRead(Long userId) {
        return notifRepo.markAllAsRead(userId) > 0;
    }

    /**
     * Elimina permanentemente una notificacion especifica.
     *
     * @param notificationId Identificador unico de la notificacion.
     * @return true si fue eliminada; false en caso contrario.
     */
    public boolean dismissNotification(Long notificationId) {
        return notifRepo.deleteById(notificationId) > 0;
    }

    /**
     * Crea y envia una nueva notificacion a un usuario determinado.
     *
     * @param userId Identificador del usuario destinatario.
     * @param title Titulo del mensaje o alerta.
     * @param message Cuerpo explicativo de la notificacion.
     * @param category Categoria funcional (ej. KYC, General, Seguridad).
     * @return Identificador unico generado para la notificacion.
     */
    public Long sendNotification(Long userId, String title, String message, String category) {
        // Construye el objeto de notificacion
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setCategory(category);
        n.setIsRead(false);
        // Persiste en base de datos
        return notifRepo.save(n);
    }

    /**
     * Envia una notificacion de difusion a todos los usuarios que pertenezcan a un rol particular.
     *
     * @param role Nombre del rol destino (ej. EMPLOYEE, KYC_OFFICER).
     * @param title Titulo del mensaje.
     * @param message Contenido explicativo.
     * @param category Categoria funcional.
     */
    public void notifyRole(String role, String title, String message, String category) {
        // Busca usuarios pertenecientes al rol y despacha notificaciones individuales
        List<User> users = userRepo.findByRole(role);
        for (User u : users) {
            sendNotification(u.getId(), title, message, category);
        }
    }

    /**
     * Envia una alerta informativa a todos los operadores y empleados de cumplimiento del backoffice.
     *
     * @param title Titulo de la notificacion.
     * @param message Contenido de la alerta.
     * @param category Categoria de la notificacion.
     */
    public void notifyEmployees(String title, String message, String category) {
        notifyRole("EMPLOYEE", title, message, category);
    }
}
