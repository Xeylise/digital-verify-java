package com.digitalverify.controller.client;

import com.digitalverify.dto.client.LoginRequest;
import com.digitalverify.dto.client.RegisterRequest;
import com.digitalverify.model.Bet;
import com.digitalverify.model.KycCase;
import com.digitalverify.model.KycDocument;
import com.digitalverify.model.Notification;
import com.digitalverify.model.User;
import com.digitalverify.service.client.PlayerService;
import com.digitalverify.service.common.AuthService;
import com.digitalverify.service.common.FileUploadValidationService;
import com.digitalverify.service.common.NotificationService;
import com.digitalverify.service.employee.KycCaseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

/**
 * Controlador web para el portal del cliente y jugador.
 * Gestiona las vistas y flujos de autenticacion, registro, panel principal de juego,
 * apuestas deportivas, notificaciones y el asistente de verificacion KYC paso a paso.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Controller
public class ClientWebController {

    /** Servicio de autenticacion. */
    private final AuthService authService;

    /** Servicio de operaciones del jugador. */
    private final PlayerService playerService;

    /** Servicio de casos KYC. */
    private final KycCaseService caseService;

    /** Servicio de notificaciones. */
    private final NotificationService notificationService;

    /** Servicio de validacion tecnica de archivos cargados. */
    private final FileUploadValidationService fileValidationService;

    /** Directorio fisico de subida de archivos. */
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Constructor con inyeccion de dependencias para el controlador de clientes.
     *
     * @param authService Servicio de autenticacion.
     * @param playerService Servicio de jugadores.
     * @param caseService Servicio de casos KYC.
     * @param notificationService Servicio de notificaciones.
     * @param fileValidationService Servicio de validacion de archivos.
     */
    public ClientWebController(AuthService authService,
                               PlayerService playerService,
                               KycCaseService caseService,
                               NotificationService notificationService,
                               FileUploadValidationService fileValidationService) {
        this.authService = authService;
        this.playerService = playerService;
        this.caseService = caseService;
        this.notificationService = notificationService;
        this.fileValidationService = fileValidationService;
    }

    /**
     * Obtiene el usuario jugador autenticado en la sesion actual.
     *
     * @param session Sesion HTTP.
     * @return Entidad User del jugador logueado o null si no esta autenticado.
     */
    private User getCurrentPlayer(HttpSession session) {
        return (session != null) ? (User) session.getAttribute("currentUser") : null;
    }

    /**
     * Agrega el contador de notificaciones no leidas a todos los modelos de vista del cliente.
     *
     * @param session Sesion HTTP.
     * @return Cantidad de notificaciones pendientes de lectura.
     */
    @ModelAttribute("unreadNotifCount")
    public long unreadNotifCount(HttpSession session) {
        User player = getCurrentPlayer(session);
        return player != null ? notificationService.getUnreadCount(player.getId()) : 0L;
    }

    /**
     * Renderiza la pagina de inicio y bienvenida / inicio de sesion para clientes.
     *
     * @param session Sesion HTTP.
     * @param model Modelo para la vista Thymeleaf.
     * @return Nombre de la plantilla client/index.
     */
    @GetMapping({"/", "/index.html", "/login", "/client/login"})
    public String index(HttpSession session, Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest("", ""));
        }
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "client/index";
    }

    /**
     * Cierra la sesion del jugador y redirige al portal de inicio.
     *
     * @param session Sesion HTTP a invalidar.
     * @return Redireccion con parametro de confirmacion.
     */
    @GetMapping({"/logout", "/client/logout"})
    public String logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute("currentUser");
            session.invalidate();
        }
        return "redirect:/?logged_out=true";
    }

    /**
     * Procesa la solicitud de inicio de sesion del usuario o empleado.
     *
     * @param loginReq Formulario de login validado.
     * @param bindingResult Errores de validacion del formulario.
     * @param session Sesion HTTP para almacenar credenciales.
     * @param model Modelo de la vista.
     * @return Redireccion segun el rol del usuario o retorno a la vista en caso de fallo.
     */
    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginReq,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("loginError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("registerRequest", new RegisterRequest());
            return "client/index";
        }

        // Valida credenciales contra la base de datos
        Optional<User> userOpt = authService.authenticate(loginReq.getEmail(), loginReq.getPassword());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("currentUser", user);
            // Si el rol es empleado, redirige al backoffice
            if ("EMPLOYEE".equalsIgnoreCase(user.getRole())) {
                session.setAttribute("currentEmployee", user);
                return "redirect:/dashboard.html";
            }
            return "redirect:/home.html";
        }

        model.addAttribute("loginError", "Credenciales incorrectas. Verifica tu correo y contraseña.");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "client/index";
    }

    /**
     * Muestra el formulario de registro para nuevos jugadores.
     *
     * @param model Modelo de la vista.
     * @return Plantilla client/register.
     */
    @GetMapping({"/register", "/register.html", "/client/register"})
    public String register(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "client/register";
    }

    /**
     * Procesa la creacion de cuenta de un nuevo jugador.
     *
     * @param registerReq DTO con datos de registro.
     * @param bindingResult Validaciones de entrada.
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Redireccion a home o recarga con mensaje de error.
     */
    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest registerReq,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "client/register";
        }

        // Registra al nuevo jugador
        Optional<User> userOpt = authService.register(
                registerReq.getFullName(),
                registerReq.getEmail(),
                registerReq.getPassword(),
                registerReq.getPhone()
        );

        if (userOpt.isPresent()) {
            session.setAttribute("currentUser", userOpt.get());
            return "redirect:/home.html";
        }

        model.addAttribute("error", "El correo electrónico ya se encuentra registrado.");
        return "client/register";
    }

    /**
     * Muestra la pagina principal del jugador con su estado KYC y alertas.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/home.
     */
    @GetMapping({"/home", "/home.html", "/client/home"})
    public String home(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        if (player != null) {
            Optional<KycCase> kycCase = caseService.getLatestCaseForUser(player.getId());
            model.addAttribute("kycCase", kycCase.orElse(null));
            model.addAttribute("unreadNotifCount", notificationService.getUnreadCount(player.getId()));
        }
        return "client/home";
    }

    /**
     * Muestra el formulario de perfil del jugador.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/profile.
     */
    @GetMapping({"/profile", "/profile.html", "/client/profile"})
    public String profile(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        return "client/profile";
    }

    /**
     * Procesa la actualizacion del perfil del jugador.
     *
     * @param fullName Nombre completo actualizado.
     * @param phone Telefono actualizado.
     * @param country Pais actualizado.
     * @param session Sesion HTTP.
     * @param redirectAttributes Atributos flash para redireccion.
     * @return Redireccion al perfil con mensaje de exito o error.
     */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String country,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User player = getCurrentPlayer(session);
        if (player != null) {
            if (fullName == null || fullName.trim().length() < 2) {
                redirectAttributes.addFlashAttribute("error", "El nombre debe tener al menos 2 caracteres.");
                return "redirect:/profile.html";
            }
            playerService.updateProfile(player.getId(), fullName.trim(), phone, country);
            player.setFullName(fullName.trim());
            player.setPhone(phone);
            player.setCountry(country);
            session.setAttribute("currentUser", player);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        }
        return "redirect:/profile.html";
    }

    /**
     * Muestra la pantalla de configuraciones del cliente.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/settings.
     */
    @GetMapping({"/settings", "/settings.html", "/client/settings"})
    public String settings(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        return "client/settings";
    }

    /**
     * Muestra el listado de apuestas deportivas del jugador.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/my_bets.
     */
    @GetMapping({"/my_bets", "/my-bets", "/my_bets.html", "/client/my-bets"})
    public String myBets(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        if (player != null) {
            List<Bet> bets = playerService.getPlayerBets(player.getId());
            model.addAttribute("bets", bets);
        }
        return "client/my_bets";
    }

    /**
     * Muestra la bandeja de notificaciones recibidas por el jugador.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/notifications.
     */
    @GetMapping({"/notifications", "/notifications.html", "/client/notifications"})
    public String notifications(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        if (player != null) {
            List<Notification> notifs = notificationService.getUserNotifications(player.getId());
            model.addAttribute("notifications", notifs);
        }
        return "client/notifications";
    }

    /**
     * Muestra la pantalla inicial informativa del proceso KYC.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/start_kyc.
     */
    @GetMapping({"/start_kyc", "/start_kyc.html", "/kyc/start", "/client/kyc/start"})
    public String startKyc(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        return "client/start_kyc";
    }

    /**
     * Muestra la pantalla del paso 1 de KYC: Carga de Documento de Identidad.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/upload_id.
     */
    @GetMapping({"/upload_id", "/upload_id.html", "/kyc/upload-id", "/client/kyc/upload-id"})
    public String uploadId(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        return "client/upload_id";
    }

    /**
     * Procesa la carga y almacenamiento fisico del documento de identidad.
     *
     * @param documentFile Archivo multipart del documento.
     * @param documentType Tipo de identificacion (NATIONAL_ID, PASSPORT, etc.).
     * @param session Sesion HTTP.
     * @param redirectAttributes Mensajes flash de retorno.
     * @return Redireccion al siguiente paso o permanencia en caso de error.
     */
    @PostMapping("/kyc/upload-id")
    public String handleUploadId(@RequestParam(value = "documentFile", required = false) MultipartFile documentFile,
                                 @RequestParam(value = "documentType", defaultValue = "NATIONAL_ID") String documentType,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User player = getCurrentPlayer(session);
        if (player == null) {
            return "redirect:/login";
        }

        // Valida restricciones del archivo subido
        String validationError = fileValidationService.validateFile(documentFile);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            return "redirect:/upload_id.html";
        }

        try {
            // Guarda el archivo de manera fisica en la carpeta configurada
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String savedFileName = "user_" + player.getId() + "_" + System.currentTimeMillis() + "_" + documentFile.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir).resolve(savedFileName);
            Files.copy(documentFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Obtiene o crea el caso KYC del usuario y registra el documento
            KycCase kycCase = caseService.findOrCreateCaseForUser(player.getId(), player.getFullName(), player.getCountry(), player.getAccountNumber());
            caseService.addDocument(kycCase.getId(), documentType, documentFile.getOriginalFilename(), targetPath.toString(), documentFile.getSize(), documentFile.getContentType());

            redirectAttributes.addFlashAttribute("successMessage", "Documento de identidad cargado exitosamente.");
            return "redirect:/proof_of_address.html";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el archivo: " + e.getMessage());
            return "redirect:/upload_id.html";
        }
    }

    /**
     * Muestra la pantalla del paso 2 de KYC: Carga de Comprobante de Domicilio.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/proof_of_address.
     */
    @GetMapping({"/proof_of_address", "/proof_of_address.html", "/kyc/proof-of-address", "/client/kyc/proof-of-address"})
    public String proofOfAddress(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        return "client/proof_of_address";
    }

    /**
     * Procesa la carga del comprobante de domicilio y pasa el caso a estado de revision.
     *
     * @param addressFile Archivo del comprobante de domicilio.
     * @param addressDocType Tipo de comprobante.
     * @param session Sesion HTTP.
     * @param redirectAttributes Mensajes flash.
     * @return Redireccion a la vista de estado de revision.
     */
    @PostMapping("/kyc/proof-of-address")
    public String handleProofOfAddress(@RequestParam(value = "addressFile", required = false) MultipartFile addressFile,
                                       @RequestParam(value = "addressDocType", defaultValue = "UTILITY_BILL") String addressDocType,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        User player = getCurrentPlayer(session);
        if (player == null) {
            return "redirect:/login";
        }

        // Valida archivo
        String validationError = fileValidationService.validateFile(addressFile);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            return "redirect:/proof_of_address.html";
        }

        try {
            // Guarda localmente el comprobante
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String savedFileName = "address_" + player.getId() + "_" + System.currentTimeMillis() + "_" + addressFile.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir).resolve(savedFileName);
            Files.copy(addressFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Vincula documento al caso y marca el caso en revision
            KycCase kycCase = caseService.findOrCreateCaseForUser(player.getId(), player.getFullName(), player.getCountry(), player.getAccountNumber());
            caseService.addDocument(kycCase.getId(), addressDocType, addressFile.getOriginalFilename(), targetPath.toString(), addressFile.getSize(), addressFile.getContentType());

            caseService.updateCaseDecision(kycCase.getId(), "in_review", "System AI", "Todos los documentos requeridos fueron cargados.");

            // Notifica al personal de cumplimiento del backoffice
            notificationService.notifyEmployees(
                    "Nuevo Caso KYC en Cola",
                    "El jugador " + player.getFullName() + " (" + player.getAccountNumber() + ") ha completado la carga de documentos para el caso " + kycCase.getCaseNumber() + ".",
                    "KYC_QUEUE"
            );

            redirectAttributes.addFlashAttribute("successMessage", "Comprobante de domicilio recibido con éxito. Tu cuenta está en revisión.");
            return "redirect:/kyc_status_review.html";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el archivo: " + e.getMessage());
            return "redirect:/proof_of_address.html";
        }
    }

    /**
     * Muestra el estado actual y real de verificacion del jugador en tiempo real.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/kyc_status_review.
     */
    @GetMapping({"/kyc_status_review", "/kyc_status_review.html", "/kyc/status", "/client/kyc/status"})
    public String kycStatusReview(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        if (player != null) {
            Optional<KycCase> kycCase = caseService.getLatestCaseForUser(player.getId());
            if (kycCase.isPresent()) {
                model.addAttribute("kycCase", kycCase.get());
                List<KycDocument> docs = caseService.getDocumentsForCase(kycCase.get().getId());
                model.addAttribute("documents", docs);
            }
        }
        return "client/kyc_status_review";
    }

    /**
     * Muestra el lobby principal de juegos interactivos y apuestas deportivas.
     *
     * @param session Sesion HTTP.
     * @param model Modelo de la vista.
     * @return Plantilla client/main_gaming.
     */
    @GetMapping({"/main_gaming", "/main_gaming.html", "/client/main_gaming"})
    public String mainGaming(HttpSession session, Model model) {
        User player = getCurrentPlayer(session);
        model.addAttribute("user", player);
        return "client/main_gaming";
    }
}
