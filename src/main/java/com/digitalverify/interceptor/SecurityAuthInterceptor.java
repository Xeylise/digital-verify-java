package com.digitalverify.interceptor;

import com.digitalverify.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Interceptor de seguridad para control y verificacion de sesiones activas.
 * Protege las rutas privadas de clientes (jugadores) y empleados (operadores de cumplimiento)
 * evitando accesos no autorizados mediante redirecciones al login correspondiente.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Component
public class SecurityAuthInterceptor implements HandlerInterceptor {

    /**
     * Conjunto inmutable de rutas exclusivas del rol Empleado (Backoffice).
     */
    private static final Set<String> EMPLOYEE_PATHS = Set.of(
            "/dashboard", "/dashboard.html",
            "/queue", "/queue.html",
            "/all-cases", "/all-cases.html", "/all_cases.html",
            "/case", "/case.html",
            "/activity_log", "/activity_log.html",
            "/reports", "/reports.html",
            "/settings_employee", "/settings_employee.html"
    );

    /**
     * Conjunto inmutable de rutas protegidas del rol Cliente (Jugador).
     */
    private static final Set<String> CLIENT_PATHS = Set.of(
            "/home", "/home.html",
            "/profile", "/profile.html",
            "/settings", "/settings.html",
            "/my_bets", "/my_bets.html",
            "/notifications", "/notifications.html",
            "/start_kyc", "/start_kyc.html",
            "/upload_id", "/upload_id.html",
            "/proof_of_address", "/proof_of_address.html",
            "/kyc_status_review", "/kyc_status_review.html",
            "/main_gaming", "/main_gaming.html"
    );

    /**
     * Intercepta las solicitudes HTTP entrantes antes de que lleguen al controlador correspondiente.
     * Evalua si la URI solicitada requiere sesion activa de empleado o cliente.
     *
     * @param request  Peticion HTTP entrante con sesion y encabezados.
     * @param response Respuesta HTTP para redirigir si no cumple permisos.
     * @param handler  Manejador de ejecucion de Spring MVC.
     * @return true si la peticion tiene autorizacion para continuar; false si se cancela y redirige.
     * @throws Exception En caso de error de entrada/salida o redireccion.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 1. Verificacion de acceso a rutas exclusivas de Empleado (Backoffice)
        boolean isEmployeePath = EMPLOYEE_PATHS.contains(uri)
                || (uri.startsWith("/employee/") && !uri.equals("/employee/login") && !uri.equals("/employee/logout"))
                || uri.startsWith("/case/");

        if (isEmployeePath) {
            HttpSession session = request.getSession(false);
            User employee = (session != null) ? (User) session.getAttribute("currentEmployee") : null;
            
            // Si no hay sesion activa o el rol no es EMPLOYEE, redirige al login de empleado
            if (employee == null || !"EMPLOYEE".equalsIgnoreCase(employee.getRole())) {
                response.sendRedirect("/login_employee.html?error=session_required");
                return false;
            }
            return true;
        }

        // 2. Verificacion de acceso a rutas protegidas de Cliente (Jugador)
        boolean isClientPath = CLIENT_PATHS.contains(uri)
                || uri.startsWith("/kyc/");

        if (isClientPath) {
            HttpSession session = request.getSession(false);
            User client = (session != null) ? (User) session.getAttribute("currentUser") : null;
            
            // Si el cliente no ha iniciado sesion, redirige al formulario de acceso principal
            if (client == null) {
                response.sendRedirect("/?error=session_required");
                return false;
            }
            return true;
        }

        // Rutas publicas permitidas sin restriccion de sesion
        return true;
    }
}
