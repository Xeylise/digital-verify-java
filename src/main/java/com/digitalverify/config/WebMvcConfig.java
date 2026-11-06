package com.digitalverify.config;

import com.digitalverify.interceptor.SecurityAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion del ciclo de interceptores de Spring MVC.
 * Registra el interceptor de seguridad para validar sesiones y roles
 * tanto en las vistas del portal del cliente como en el Backoffice del empleado.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Interceptor que valida la autenticacion y rol del usuario en cada peticion HTTP.
     */
    private final SecurityAuthInterceptor securityAuthInterceptor;

    /**
     * Constructor con inyeccion de dependencias para el interceptor de seguridad.
     *
     * @param securityAuthInterceptor Instancia del interceptor de autenticacion.
     */
    public WebMvcConfig(SecurityAuthInterceptor securityAuthInterceptor) {
        this.securityAuthInterceptor = securityAuthInterceptor;
    }

    /**
     * Registra interceptores y define los patrones de rutas protegidas y excluidas.
     *
     * @param registry Registro de interceptores de Spring MVC.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aplica el interceptor de seguridad a todas las rutas, excluyendo recursos publicos y estaticos
        registry.addInterceptor(securityAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Vistas publicas de inicio, registro y login
                        "/", "/index.html", "/login", "/client/login",
                        "/register", "/register.html", "/client/register",
                        "/login_employee", "/login_employee.html", "/employee/login",
                        "/logout", "/employee/logout", "/logout_employee",
                        // Recursos estaticos web y archivos locales subidos
                        "/css/**", "/js/**", "/img/**", "/uploads/**", "/favicon.ico",
                        // Consola de administracion H2
                        "/h2-console/**",
                        // API REST
                        "/api/**"
                );
    }
}
