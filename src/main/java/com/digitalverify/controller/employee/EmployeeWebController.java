package com.digitalverify.controller.employee;

import com.digitalverify.dto.client.LoginRequest;
import com.digitalverify.dto.employee.CaseDecisionRequest;
import com.digitalverify.model.AuditLogEntry;
import com.digitalverify.model.DashboardStats;
import com.digitalverify.model.KycCase;
import com.digitalverify.model.KycDocument;
import com.digitalverify.model.User;
import com.digitalverify.service.common.AuthService;
import com.digitalverify.service.common.NotificationService;
import com.digitalverify.service.employee.AuditLogService;
import com.digitalverify.service.employee.KycCaseService;
import com.digitalverify.service.employee.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador web para el portal de administradores, analistas y oficiales de cumplimiento (Backoffice).
 * Gestiona el panel de control (Dashboard), cola de verificacion (Queue), listado de todos los casos,
 * modal y vista de detalle de casos con dictamenes, bitacora de auditoria, reportes mensuales y exportacion CSV.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Controller
public class EmployeeWebController {

    /** Servicio de autenticacion. */
    private final AuthService authService;

    /** Servicio de casos KYC. */
    private final KycCaseService caseService;

    /** Servicio de auditoria. */
    private final AuditLogService auditService;

    /** Servicio de reportes estadisticos. */
    private final ReportService reportService;

    /** Servicio de notificaciones. */
    private final NotificationService notificationService;

    /**
     * Constructor con inyeccion de dependencias para el controlador de empleados.
     *
     * @param authService Servicio de autenticacion.
     * @param caseService Servicio de casos KYC.
     * @param auditService Servicio de auditoria.
     * @param reportService Servicio de reportes.
     * @param notificationService Servicio de notificaciones.
     */
    public EmployeeWebController(AuthService authService,
                                 KycCaseService caseService,
                                 AuditLogService auditService,
                                 ReportService reportService,
                                 NotificationService notificationService) {
        this.authService = authService;
        this.caseService = caseService;
        this.auditService = auditService;
        this.reportService = reportService;
        this.notificationService = notificationService;
    }

    /**
     * Obtiene el empleado u operador autenticado en la sesion actual de backoffice.
     *
     * @param session Sesion HTTP.
     * @return Entidad User del empleado autenticado o null si no existe.
     */
    private User getCurrentEmployee(HttpSession session) {
        return (session != null) ? (User) session.getAttribute("currentEmployee") : null;
    }

    /**
     * Agrega el contador de notificaciones no leidas al modelo de vista.
     *
     * @param session Sesion HTTP.
     * @return Conteo de alertas sin leer.
     */
    @ModelAttribute("unreadNotifCount")
    public long unreadNotifCount(HttpSession session) {
        User employee = getCurrentEmployee(session);
        return employee != null ? notificationService.getUnreadCount(employee.getId()) : 0L;
    }

    /**
     * Muestra la pantalla de inicio de sesion para analistas y oficiales de cumplimiento.
     *
     * @param model Modelo de la vista.
     * @return Plantilla employee/login_employee.
     */
    @GetMapping({"/login_employee", "/login_employee.html", "/employee/login"})
    public String loginEmployee(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest("", ""));
        }
        return "employee/login_employee";
    }

    /**
     * Cierra la sesion del empleado en el backoffice.
     *
     * @param session Sesion HTTP a invalidar.
     * @return Redireccion al login de empleados.
     */
    @GetMapping({"/employee/logout", "/logout_employee"})
    public String employeeLogout(HttpSession session) {
        if (session != null) {
            session.removeAttribute("currentEmployee");
            session.invalidate();
        }
        return "redirect:/login_employee.html?logged_out=true";
    }

    /**
     * Procesa la autenticacion del personal de cumplimiento con validacion de rol.
     *
     * @param loginReq DTO con credenciales.
     * @param bindingResult Errores de validacion.
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Redireccion al dashboard si es valido o recarga del login con mensaje de error.
     */
    @PostMapping({"/login_employee", "/employee/login"})
    public String handleEmployeeLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginReq,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "employee/login_employee";
        }

        // Valida credenciales y rol exclusivo EMPLOYEE
        Optional<User> userOpt = authService.authenticate(loginReq.getEmail(), loginReq.getPassword());
        if (userOpt.isPresent() && "EMPLOYEE".equalsIgnoreCase(userOpt.get().getRole())) {
            session.setAttribute("currentEmployee", userOpt.get());
            return "redirect:/dashboard.html";
        }

        model.addAttribute("error", "Credenciales de validador inválidas.");
        return "employee/login_employee";
    }

    /**
     * Renderiza el panel principal de control (Dashboard) con KPIs, graficas y tabla de casos recientes.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/dashboard.
     */
    @GetMapping({"/dashboard", "/dashboard.html", "/employee/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);

        String employeeName = employee != null ? employee.getFullName() : "Paola Castro";
        DashboardStats stats = caseService.getDashboardStats(employeeName);
        model.addAttribute("stats", stats);

        List<KycCase> recentCases = caseService.getAllCases();
        model.addAttribute("cases", recentCases);
        if (employee != null) {
            model.addAttribute("unreadNotifCount", notificationService.getUnreadCount(employee.getId()));
        }

        return "employee/dashboard";
    }

    /**
     * Renderiza la cola de trabajo de verificacion (Queue) con filtros interactivos.
     *
     * @param status Filtro de estado.
     * @param search Texto de busqueda.
     * @param reason Motivo de alerta.
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/queue.
     */
    @GetMapping({"/queue", "/queue.html", "/employee/queue"})
    public String queue(@RequestParam(required = false) String status,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String reason,
                        HttpSession session,
                        Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);

        List<KycCase> queueCases = caseService.getFilteredCases(status, search, null, reason);
        model.addAttribute("cases", queueCases);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentReason", reason);

        if (employee != null) {
            model.addAttribute("unreadNotifCount", notificationService.getUnreadCount(employee.getId()));
        }

        return "employee/queue";
    }

    /**
     * Renderiza la vista de todos los casos del sistema (All Cases) con filtros avanzados.
     *
     * @param status Filtro de estado.
     * @param search Texto de busqueda.
     * @param assignedTo Analista asignado.
     * @param reason Motivo.
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/all-cases.
     */
    @GetMapping({"/all-cases", "/all-cases.html", "/all_cases.html", "/employee/all-cases"})
    public String allCases(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String assignedTo,
                           @RequestParam(required = false) String reason,
                           HttpSession session,
                           Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);

        List<KycCase> cases = caseService.getFilteredCases(status, search, assignedTo, reason);
        model.addAttribute("cases", cases);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentAssignedTo", assignedTo);
        model.addAttribute("currentReason", reason);
        return "employee/all-cases";
    }

    /**
     * Muestra la vista dedicada de detalle de un caso KYC.
     *
     * @param id Identificador en path variable.
     * @param caseId Identificador como query param opcional.
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/case.
     */
    @GetMapping({"/case", "/case.html", "/employee/case/{id}"})
    public String caseDetail(@PathVariable(required = false) Long id,
                             @RequestParam(required = false) Long caseId,
                             HttpSession session,
                             Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);

        Long targetId = id != null ? id : (caseId != null ? caseId : 1L);
        Optional<KycCase> kycCaseOpt = caseService.getCaseById(targetId);
        if (kycCaseOpt.isPresent()) {
            KycCase kycCase = kycCaseOpt.get();
            model.addAttribute("kycCase", kycCase);
            List<KycDocument> documents = caseService.getDocumentsForCase(kycCase.getId());
            model.addAttribute("documents", documents);
            model.addAttribute("seguimiento", caseService.getSeguimientoForCase(kycCase.getId()));
            List<AuditLogEntry> logs = auditService.getLogsByCase(kycCase.getCaseNumber());
            model.addAttribute("logs", logs);
        }

        return "employee/case";
    }

    /**
     * Procesa la decision de un caso desde un formulario web tradicional con soporte para redirect attributes.
     *
     * @param id ID de caso en ruta.
     * @param caseId ID de caso como parametro.
     * @param decisionReq DTO con las decisiones tomadas.
     * @param bindingResult Validaciones de entrada.
     * @param session Sesion HTTP.
     * @param redirectAttributes Mensajes flash.
     * @return Redireccion al detalle del caso.
     */
    @PostMapping({"/employee/case/{id}/decision", "/case/decision"})
    public String makeDecision(@PathVariable(required = false) Long id,
                               @RequestParam(required = false) Long caseId,
                               @Valid @ModelAttribute CaseDecisionRequest decisionReq,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long targetId = id != null ? id : caseId;
        if (targetId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de caso no especificado.");
            return "redirect:/queue.html";
        }

        User employee = getCurrentEmployee(session);
        String operatorName = employee != null ? employee.getFullName() : "Paola Castro";
        Long internalUserId = employee != null ? employee.getId() : 7L;

        boolean updated = caseService.updateCaseDecisionDetailed(
                targetId,
                decisionReq.getStatus(),
                decisionReq.getVerificationDecision(),
                decisionReq.getAccountStatusDecision(),
                operatorName,
                internalUserId,
                decisionReq.getDecisionNotes()
        );
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Caso actualizado exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se encontró el caso especificado.");
        }

        return "redirect:/case.html?caseId=" + targetId;
    }

    /**
     * Procesa la asignacion de un analista a un caso.
     *
     * @param id ID de caso.
     * @param caseId ID de caso query param.
     * @param operatorName Nombre del analista.
     * @param session Sesion HTTP.
     * @param redirectAttributes Atributos flash.
     * @return Redireccion a la cola.
     */
    @PostMapping({"/employee/case/{id}/assign", "/case/assign"})
    public String assignCase(@PathVariable(required = false) Long id,
                             @RequestParam(required = false) Long caseId,
                             @RequestParam(required = false) String operatorName,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long targetId = id != null ? id : caseId;
        if (targetId == null) {
            return "redirect:/queue.html";
        }

        User employee = getCurrentEmployee(session);
        if (operatorName == null || operatorName.isBlank()) {
            operatorName = employee != null ? employee.getFullName() : "Paola Castro";
        }
        Long userId = employee != null ? employee.getId() : 7L;

        caseService.assignCaseWithUser(targetId, operatorName, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Caso asignado correctamente a " + operatorName);
        return "redirect:/queue.html";
    }

    /**
     * Muestra la bitacora de auditoria y trazabilidad formal de eventos de la plataforma.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/activity_log.
     */
    @GetMapping({"/activity_log", "/activity_log.html", "/employee/activity-log"})
    public String activityLog(HttpSession session, Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);

        List<com.digitalverify.model.ActivityLogEntry> logs = auditService.getAllActivityLogs();
        model.addAttribute("logs", logs);
        return "employee/activity_log";
    }

    /**
     * Muestra la pantalla de reportes gerenciales de productividad mensual.
     *
     * @param originMonth Mes de analisis.
     * @param compMonth Mes de comparacion.
     * @param goal Meta operativa.
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/reports.
     */
    @GetMapping({"/reports", "/reports.html", "/employee/reports"})
    public String reports(@RequestParam(required = false) String originMonth,
                          @RequestParam(required = false) String compMonth,
                          @RequestParam(required = false) Integer goal,
                          HttpSession session,
                          Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);

        List<ReportService.MonthOption> availableMonths = reportService.getLastSixMonths();
        model.addAttribute("availableMonths", availableMonths);

        if (originMonth == null || originMonth.isBlank()) {
            originMonth = !availableMonths.isEmpty() ? availableMonths.get(0).getValue() : "2026-09";
        }
        if (compMonth == null || compMonth.isBlank()) {
            compMonth = availableMonths.size() > 1 ? availableMonths.get(1).getValue() : "2026-08";
        }
        if (goal == null) {
            goal = 600;
        }

        Map<String, Object> productivityReport = reportService.getMonthlyProductivityReport(originMonth, compMonth, goal);
        model.addAttribute("report", productivityReport);
        model.addAttribute("selectedOriginMonth", originMonth);
        model.addAttribute("selectedCompMonth", compMonth);
        model.addAttribute("selectedGoal", goal);

        model.addAttribute("reportsSummary", reportService.getReportsSummary());
        return "employee/reports";
    }

    /**
     * Exporta el desglose del reporte de productividad mensual a formato CSV descargable.
     *
     * @param originMonth Mes de analisis.
     * @param compMonth Mes de comparacion.
     * @param goal Meta operativa.
     * @return Bytes del archivo CSV con cabecera de descarga adjunta.
     */
    @GetMapping({"/employee/reports/export.csv", "/reports/export.csv"})
    public org.springframework.http.ResponseEntity<byte[]> exportReportsCsv(@RequestParam(required = false) String originMonth,
                                                                            @RequestParam(required = false) String compMonth,
                                                                            @RequestParam(required = false) Integer goal) {
        Map<String, Object> report = reportService.getMonthlyProductivityReport(originMonth, compMonth, goal);
        @SuppressWarnings("unchecked")
        List<KycCase> cases = (List<KycCase>) report.get("cases");

        StringBuilder csv = new StringBuilder();
        csv.append("CASE ID,ACCOUNT NUMBER,VERIFICATION REASON,ACCOUNT STATUS,STATUS\n");
        if (cases != null) {
            for (KycCase c : cases) {
                csv.append(escapeCsv(c.getCaseNumber())).append(",");
                csv.append(escapeCsv(c.getAccountNumber())).append(",");
                csv.append(escapeCsv(c.getReason())).append(",");
                csv.append(escapeCsv(c.getAccountStatus())).append(",");
                csv.append(escapeCsv(c.getStatus())).append("\n");
            }
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_productividad_" + originMonth + ".csv\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    /**
     * Escapa valores para escritura segura en archivos CSV.
     *
     * @param val Cadena original.
     * @return Cadena escapada.
     */
    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    /**
     * Muestra la pagina de preferencias personales del empleado en el backoffice.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla employee/settings_employee.
     */
    @GetMapping({"/settings_employee", "/settings_employee.html", "/employee/settings"})
    public String settingsEmployee(HttpSession session, Model model) {
        User employee = getCurrentEmployee(session);
        model.addAttribute("employee", employee);
        return "employee/settings_employee";
    }

    /**
     * Procesa la actualizacion de preferencias de idioma, zona horaria y alertas de cola del empleado.
     *
     * @param language Idioma seleccionado.
     * @param timezone Zona horaria seleccionada.
     * @param queueAlertsEnabled Indicador de alertas habilitadas.
     * @param session Sesion HTTP.
     * @param redirectAttributes Mensajes flash.
     * @return Redireccion a la configuracion con confirmacion.
     */
    @PostMapping({"/employee/settings/update", "/settings_employee/update"})
    public String updateSettings(@RequestParam String language,
                                 @RequestParam String timezone,
                                 @RequestParam(required = false) Boolean queueAlertsEnabled,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User employee = getCurrentEmployee(session);
        if (employee != null) {
            authService.updateSettings(employee.getId(), language, timezone, queueAlertsEnabled != null && queueAlertsEnabled);
            employee.setLanguage(language);
            employee.setTimezone(timezone);
            employee.setQueueAlertsEnabled(queueAlertsEnabled != null && queueAlertsEnabled);
            session.setAttribute("currentEmployee", employee);
            auditService.recordActivity(employee.getId(), "Settings Updated", null, "Updated language to " + language + ", timezone to " + timezone);
            redirectAttributes.addFlashAttribute("successMessage", "Preferencias guardadas exitosamente.");
        }
        return "redirect:/settings_employee.html";
    }
}
