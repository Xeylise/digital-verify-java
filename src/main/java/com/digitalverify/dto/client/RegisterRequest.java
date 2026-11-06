package com.digitalverify.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Objeto de Transferencia de Datos (DTO) para el registro de nuevos jugadores.
 * Aplica reglas de validacion para nombre completo, correo, contraseña y telefono.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class RegisterRequest {

    /**
     * Nombre y apellidos completos del jugador.
     */
    @NotBlank(message = "El nombre completo es requerido.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    private String fullName;

    /**
     * Correo electronico que servira como identificador de inicio de sesion.
     */
    @NotBlank(message = "El correo electrónico es requerido.")
    @Email(message = "Ingresa un correo electrónico válido.")
    private String email;

    /**
     * Contraseña elegida por el usuario con longitud minima de seguridad.
     */
    @NotBlank(message = "La contraseña es requerida.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    /**
     * Numero de contacto telefonico opcional.
     */
    private String phone;

    /**
     * Constructor por defecto para binding en controladores de Spring MVC.
     */
    public RegisterRequest() {}

    /**
     * Constructor con todos los campos para pruebas e inicializacion manual.
     *
     * @param fullName Nombre completo del jugador.
     * @param email    Correo electronico.
     * @param password Contraseña de acceso.
     * @param phone    Numero telefonico de contacto.
     */
    public RegisterRequest(String fullName, String email, String password, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    /**
     * Obtiene el nombre completo.
     *
     * @return Nombre del jugador.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Establece el nombre completo.
     *
     * @param fullName Nombre completo a asignar.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Obtiene el correo electronico.
     *
     * @return Correo electronico.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electronico.
     *
     * @param email Correo electronico a asignar.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña.
     *
     * @return Contraseña ingresada.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña.
     *
     * @param password Contraseña a asignar.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el numero telefonico.
     *
     * @return Numero de telefono.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Establece el numero telefonico.
     *
     * @param phone Numero de telefono a asignar.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
