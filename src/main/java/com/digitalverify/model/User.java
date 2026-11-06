package com.digitalverify.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa a un Usuario en Digital Verify (tanto Jugador como Empleado).
 * Contiene informacion de identidad, credenciales cifradas, estado de cumplimiento KYC,
 * preferencias regionales y relacion 1 a N con el historial de apuestas deportivas.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class User {

    /**
     * Identificador unico del usuario en la base de datos.
     */
    private Long id;

    /**
     * Correo electronico que actua como identificador unico de inicio de sesion.
     */
    private String email;

    /**
     * Contraseña cifrada en formato hash para almacenamiento seguro.
     */
    private String passwordHash;

    /**
     * Nombre completo y apellidos del usuario.
     */
    private String fullName;

    /**
     * Rol asignado en el sistema (PLAYER o EMPLOYEE).
     */
    private String role;

    /**
     * Codigo ISO del pais de residencia (ej: CO, ES, BR).
     */
    private String country;

    /**
     * Numero telefonico de contacto.
     */
    private String phone;

    /**
     * Numero de cuenta asociado para transacciones y apuestas.
     */
    private String accountNumber;

    /**
     * Estado de verificacion de identidad del usuario (PENDING, IN_REVIEW, APPROVED, REJECTED).
     */
    private String kycStatus;

    /**
     * Ruta relativa a la imagen de perfil o avatar del usuario.
     */
    private String avatarUrl;

    /**
     * Indicador de activacion de autenticacion de dos factores (2FA).
     */
    private Boolean twoFactorEnabled;

    /**
     * Preferencia de idioma del usuario (es, en, pt).
     */
    private String language = "en";

    /**
     * Zona horaria preferida por el usuario (ej: utc-5, utc+1).
     */
    private String timezone = "utc-5";

    /**
     * Indicador para recibir alertas visuales o sonoras en la cola de verificacion.
     */
    private Boolean queueAlertsEnabled = true;

    /**
     * Fecha y hora en la que el usuario fue registrado en la plataforma.
     */
    private LocalDateTime createdAt;

    /**
     * Relacion 1 a N: Coleccion de apuestas realizadas por el usuario.
     */
    private List<Bet> bets = new ArrayList<>();

    /**
     * Constructor por defecto para instanciacion por JDBC y controladores.
     */
    public User() {}

    /**
     * Sobrecarga de constructor 1: Inicializacion basica de un usuario.
     *
     * @param email    Correo electronico.
     * @param fullName Nombre completo.
     * @param role     Rol en el sistema.
     */
    public User(String email, String fullName, String role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.kycStatus = "PENDING";
        this.twoFactorEnabled = false;
        this.language = "en";
        this.timezone = "utc-5";
        this.queueAlertsEnabled = true;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Sobrecarga de constructor 2: Inicializacion completa estandar.
     *
     * @param id               Identificador unico.
     * @param email            Correo electronico.
     * @param passwordHash     Hash de contraseña.
     * @param fullName         Nombre completo.
     * @param role             Rol.
     * @param country          Pais.
     * @param phone            Telefono.
     * @param accountNumber    Numero de cuenta.
     * @param kycStatus        Estado KYC.
     * @param avatarUrl        Avatar.
     * @param twoFactorEnabled Estado 2FA.
     * @param createdAt        Fecha de registro.
     */
    public User(Long id, String email, String passwordHash, String fullName, String role,
                String country, String phone, String accountNumber, String kycStatus,
                String avatarUrl, Boolean twoFactorEnabled, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.country = country;
        this.phone = phone;
        this.accountNumber = accountNumber;
        this.kycStatus = kycStatus;
        this.avatarUrl = avatarUrl;
        this.twoFactorEnabled = twoFactorEnabled;
        this.language = "en";
        this.timezone = "utc-5";
        this.queueAlertsEnabled = true;
        this.createdAt = createdAt;
    }

    /**
     * Sobrecarga de constructor 3: Inicializacion completa con preferencias avanzadas de usuario.
     *
     * @param id                 Identificador unico.
     * @param email              Correo electronico.
     * @param passwordHash       Hash de contraseña.
     * @param fullName           Nombre completo.
     * @param role               Rol.
     * @param country            Pais.
     * @param phone              Telefono.
     * @param accountNumber      Numero de cuenta.
     * @param kycStatus          Estado KYC.
     * @param avatarUrl          Avatar.
     * @param twoFactorEnabled   Estado 2FA.
     * @param language           Idioma preferido.
     * @param timezone           Zona horaria.
     * @param queueAlertsEnabled Alertas en cola.
     * @param createdAt          Fecha de registro.
     */
    public User(Long id, String email, String passwordHash, String fullName, String role,
                String country, String phone, String accountNumber, String kycStatus,
                String avatarUrl, Boolean twoFactorEnabled, String language, String timezone,
                Boolean queueAlertsEnabled, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.country = country;
        this.phone = phone;
        this.accountNumber = accountNumber;
        this.kycStatus = kycStatus;
        this.avatarUrl = avatarUrl;
        this.twoFactorEnabled = twoFactorEnabled;
        this.language = language != null ? language : "en";
        this.timezone = timezone != null ? timezone : "utc-5";
        this.queueAlertsEnabled = queueAlertsEnabled != null ? queueAlertsEnabled : true;
        this.createdAt = createdAt;
    }

    /**
     * Asocia una nueva apuesta al historial del usuario demostrando la relacion 1 a N.
     *
     * @param bet Apuesta a registrar.
     */
    public void addBet(Bet bet) {
        if (this.bets == null) {
            this.bets = new ArrayList<>();
        }
        this.bets.add(bet);
    }

    /**
     * Sobrecarga de metodo: Actualiza unicamente el numero de telefono.
     *
     * @param phone Nuevo telefono.
     */
    public void updateContact(String phone) {
        this.phone = phone;
    }

    /**
     * Sobrecarga de metodo: Actualiza telefono y pais de residencia.
     *
     * @param phone   Nuevo telefono.
     * @param country Nuevo pais.
     */
    public void updateContact(String phone, String country) {
        this.phone = phone;
        this.country = country;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Boolean getQueueAlertsEnabled() { return queueAlertsEnabled; }
    public void setQueueAlertsEnabled(Boolean queueAlertsEnabled) { this.queueAlertsEnabled = queueAlertsEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Bet> getBets() { return bets; }
    public void setBets(List<Bet> bets) { this.bets = bets; }
}
