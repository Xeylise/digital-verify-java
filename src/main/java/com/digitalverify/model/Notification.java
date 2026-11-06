package com.digitalverify.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio para el sistema de alertas y notificaciones internas.
 * Permite comunicar eventos relevantes a clientes (aprobacion/rechazo de KYC)
 * y a empleados del Backoffice (llegada de nuevos casos a la cola).
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class Notification {

    /**
     * Identificador unico de la notificacion.
     */
    private Long id;

    /**
     * Identificador del usuario destinatario (cliente o empleado).
     */
    private Long userId;

    /**
     * Titulo o encabezado breve de la alerta.
     */
    private String title;

    /**
     * Mensaje descriptivo con el detalle de la notificacion.
     */
    private String message;

    /**
     * Categoria de la notificacion (KYC, KYC_QUEUE, Security, General).
     */
    private String category;

    /**
     * Indicador booleano de lectura (true: leida, false: pendiente).
     */
    private Boolean isRead;

    /**
     * Fecha y hora en que se genero la notificacion.
     */
    private LocalDateTime createdAt;

    /**
     * Constructor por defecto para instanciacion por frameworks.
     */
    public Notification() {}

    /**
     * Constructor completo con todos los atributos de persistencia.
     *
     * @param id        Identificador unico.
     * @param userId    ID del usuario destinatario.
     * @param title     Titulo de la notificacion.
     * @param message   Mensaje o cuerpo de la alerta.
     * @param category  Categoria funcional.
     * @param isRead    Estado de lectura.
     * @param createdAt Fecha y hora de creacion.
     */
    public Notification(Long id, Long userId, String title, String message, String category, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.category = category;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
