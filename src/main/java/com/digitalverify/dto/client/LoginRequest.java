package com.digitalverify.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Objeto de Transferencia de Datos (DTO) para la autenticacion de usuarios.
 * Encapsula y valida las credenciales requeridas en el inicio de sesion.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class LoginRequest {

    /**
     * Correo electronico registrado del usuario.
     */
    @NotBlank(message = "El correo electrónico es requerido.")
    @Email(message = "Ingresa un correo electrónico válido.")
    private String email;

    /**
     * Contraseña de acceso en texto plano para verificacion contra el hash.
     */
    @NotBlank(message = "La contraseña es requerida.")
    private String password;

    /**
     * Constructor por defecto para serializacion y deserializacion.
     */
    public LoginRequest() {}

    /**
     * Constructor con parametros para inicializacion rapida de credenciales.
     *
     * @param email    Correo electronico del usuario.
     * @param password Contraseña de acceso.
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Obtiene el correo electronico.
     *
     * @return Correo electronico del usuario.
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
}
