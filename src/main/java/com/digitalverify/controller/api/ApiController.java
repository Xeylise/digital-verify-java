package com.digitalverify.controller.api;

import com.digitalverify.dto.employee.CaseDecisionRequest;
import com.digitalverify.model.AuditLogEntry;
import com.digitalverify.model.DashboardStats;
import com.digitalverify.model.KycCase;
import com.digitalverify.model.KycDocument;
import com.digitalverify.model.Notification;
import com.digitalverify.model.User;
import com.digitalverify.service.common.FileUploadValidationService;
import com.digitalverify.service.common.NotificationService;
import com.digitalverify.service.employee.AuditLogService;
import com.digitalverify.service.employee.KycCaseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para el consumo asincrono y aserciones de la API de Digital Verify.
 * Expone endpoints JSON para la gestion de casos KYC, resolucion de dictamenes,
 * carga y visualizacion segura de documentos locales, consultas estadisticas y notificaciones.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    /** Servicio de casos KYC. */
    private final KycCaseService caseService;

    /** Servicio de notificaciones. */
    private final NotificationService notificationService;

    /** Servicio de auditoria y trazabilidad. */
    private final AuditLogService auditService;

    /** Servicio de validacion tecnica de archivos subidos. */
    private final FileUploadValidationService fileValidationService;

    /**
     * Directorio fisico local donde residen los documentos KYC cargados.
     */
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Constructor con inyeccion de dependencias para el controlador API.
     *
     * @param caseService Servicio de casos KYC.
     * @param notificationService Servicio de notificaciones.
     * @param auditService Servicio de auditoria.
     * @param fileValidationService Servicio de validacion de archivos.
     */
    public ApiController(KycCaseService caseService,
                         NotificationService notificationService,
                         AuditLogService auditService,
                         FileUploadValidationService fileValidationService) {
        this.caseService = caseService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.fileValidationService = fileValidationService;
    }

    /**
     * Obtiene el listado de casos KYC aplicando filtros opcionales de busqueda.
     *
     * @param status Filtro por estado (pending, in_review, approved, rejected).
     * @param search Texto de busqueda en nombre, caso o numero de cuenta.
     * @param assignedTo Nombre del operador asignado.
     * @param reason Motivo de activacion del caso KYC.
     * @return Respuesta HTTP con la lista de casos coincidentes.
     */
    @GetMapping("/cases")
    public ResponseEntity<List<KycCase>> getCases(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String search,
                                                  @RequestParam(required = false) String assignedTo,
                                                  @RequestParam(required = false) String reason) {
        // Ejecuta consulta filtrada a traves de la capa de servicio
        return ResponseEntity.ok(caseService.getFilteredCases(status, search, assignedTo, reason));
    }

    /**
     * Obtiene el detalle completo de un caso KYC, incluyendo sus documentos de soporte e historial de seguimiento.
     *
     * @param id Identificador unico del caso KYC.
     * @return Respuesta HTTP con el mapa de datos del caso o estado 404 si no existe.
     */
    @GetMapping("/cases/{id}")
    public ResponseEntity<?> getCaseById(@PathVariable Long id) {
        // 1. Busca el caso por ID
        Optional<KycCase> kycCase = caseService.getCaseById(id);
        if (kycCase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 2. Agrega documentos y decisiones asociadas
        Map<String, Object> response = new HashMap<>();
        response.put("case", kycCase.get());
        response.put("documents", caseService.getDocumentsForCase(id));
        response.put("seguimiento", caseService.getSeguimientoForCase(id));
        return ResponseEntity.ok(response);
    }

    /**
     * Registra la decision final tomada por un operador sobre un caso KYC.
     *
     * @param id Identificador del caso a resolver.
     * @param request DTO con los detalles del dictamen (estado, decision de verificacion, decision de cuenta y notas).
     * @param bindingResult Resultado de las validaciones del Bean Validation.
     * @param session Sesion HTTP activa para identificar al operador logueado.
     * @return Respuesta HTTP de confirmacion o error de validacion.
     */
    @PostMapping("/cases/{id}/decision")
    public ResponseEntity<?> makeDecision(@PathVariable Long id,
                                          @Valid @RequestBody CaseDecisionRequest request,
                                          BindingResult bindingResult,
                                          HttpSession session) {
        // 1. Validacion de errores en la peticion recibida
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("error", bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }

        // 2. Determina el operador responsable desde la sesion o el payload
        User employee = (User) session.getAttribute("currentEmployee");
        String operator = employee != null ? employee.getFullName() : (request.getOperatorName() != null ? request.getOperatorName() : "Paola Castro");
        Long internalUserId = employee != null ? employee.getId() : 7L;

        // 3. Aplica resolucion integral del caso
        boolean updated = caseService.updateCaseDecisionDetailed(
                id,
                request.getStatus(),
                request.getVerificationDecision(),
                request.getAccountStatusDecision(),
                operator,
                internalUserId,
                request.getDecisionNotes()
        );
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Decisión registrada exitosamente", "status", request.getStatus()));
    }

    /**
     * Asigna un operador de cumplimiento a un caso KYC en la cola.
     *
     * @param id Identificador del caso.
     * @param operatorName Nombre del operador o null para tomar el operador en sesion.
     * @param session Sesion HTTP activa.
     * @return Respuesta HTTP con el resultado de la asignacion.
     */
    @PostMapping("/cases/{id}/assign")
    public ResponseEntity<?> assignCase(@PathVariable Long id,
                                        @RequestParam(required = false) String operatorName,
                                        HttpSession session) {
        // Determina el operador a asignar
        if (operatorName == null || operatorName.isBlank()) {
            User employee = (User) session.getAttribute("currentEmployee");
            operatorName = employee != null ? employee.getFullName() : "Paola Castro";
        }
        boolean success = caseService.assignCase(id, operatorName);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Asignado a " + operatorName));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Caso no encontrado."));
    }

    /**
     * Retorna los indicadores consolidados para el tablero de control de backoffice.
     *
     * @param session Sesion HTTP activa.
     * @return Respuesta HTTP con el objeto DashboardStats.
     */
    @GetMapping("/stats/dashboard")
    public ResponseEntity<DashboardStats> getStats(HttpSession session) {
        User employee = (User) session.getAttribute("currentEmployee");
        String operatorName = employee != null ? employee.getFullName() : "Paola Castro";
        return ResponseEntity.ok(caseService.getDashboardStats(operatorName));
    }

    /**
     * Obtiene el listado de notificaciones para el usuario o empleado autenticado.
     *
     * @param session Sesion HTTP activa.
     * @return Lista de notificaciones del usuario.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(HttpSession session) {
        // Resuelve el usuario activo (jugador o empleado)
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            user = (User) session.getAttribute("currentEmployee");
        }
        Long userId = user != null ? user.getId() : 7L;
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    /**
     * Descarta o elimina una notificacion especifica.
     *
     * @param id Identificador de la notificacion.
     * @return Respuesta de confirmacion.
     */
    @PostMapping("/notifications/{id}/dismiss")
    public ResponseEntity<?> dismissNotification(@PathVariable Long id) {
        boolean dismissed = notificationService.dismissNotification(id);
        return ResponseEntity.ok(Map.of("success", dismissed));
    }

    /**
     * Marca todas las notificaciones del usuario autenticado como leidas.
     *
     * @param session Sesion HTTP activa.
     * @return Respuesta de confirmacion.
     */
    @PostMapping("/notifications/mark-all-read")
    public ResponseEntity<?> markAllRead(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            user = (User) session.getAttribute("currentEmployee");
        }
        Long userId = user != null ? user.getId() : 7L;
        boolean updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", updated));
    }

    /**
     * Obtiene la bitacora general de registros de auditoria operativa.
     *
     * @return Lista completa de registros de auditoria.
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogEntry>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    /**
     * Recibe y almacena de forma segura un archivo multipart cargado desde el cliente,
     * persistiendo sus metadatos en la tabla kyc_documents.
     *
     * @param file Archivo multipart subido.
     * @param caseId Identificador del caso KYC vinculado.
     * @param docType Tipo de documento de verificacion.
     * @return Respuesta HTTP con el ID y metadatos del documento creado.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "caseId", defaultValue = "1") Long caseId,
                                              @RequestParam(value = "documentType", defaultValue = "PASSPORT") String docType) {
        // 1. Valida el archivo segun politicas de tamaño y formato
        String validationError = fileValidationService.validateFile(file);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", validationError));
        }

        try {
            // 2. Crea directorio de almacenamiento local si no existe
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 3. Escribe archivo en disco con nombre unico temporal
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path dest = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            // 4. Registra documento en la base de datos
            Long docId = caseService.addDocument(caseId, docType, file.getOriginalFilename(),
                    dest.toAbsolutePath().toString(), file.getSize(), file.getContentType());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "documentId", docId,
                    "fileName", file.getOriginalFilename(),
                    "fileSize", file.getSize()
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Sirve un documento KYC para visualizacion directa (inline) en el navegador del operador.
     *
     * @param id Identificador unico del documento.
     * @return Recurso de archivo con encabezados inline para visualizacion.
     */
    @GetMapping("/documents/{id}/view")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id) {
        return getDocumentResource(id, false);
    }

    /**
     * Permite descargar un documento KYC directamente en disco (attachment).
     *
     * @param id Identificador unico del documento.
     * @return Recurso de archivo con encabezados de descarga.
     */
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        return getDocumentResource(id, true);
    }

    /**
     * Resuelve el recurso fisico en disco de un documento para streaming seguro HTTP.
     *
     * @param id Identificador unico del documento KYC.
     * @param asDownload true para forzar descarga; false para visualizacion en navegador.
     * @return ResponseEntity con el Resource del archivo y cabeceras MIME correspondientes.
     */
    private ResponseEntity<Resource> getDocumentResource(Long id, boolean asDownload) {
        // 1. Busca metadatos del documento
        Optional<KycDocument> docOpt = caseService.getDocumentById(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        KycDocument doc = docOpt.get();
        // 2. Localiza la ruta fisica del archivo en uploads
        Path resolvedPath = resolveDocumentPath(doc);

        // 3. Si no existe en disco, genera un documento de prueba seguro
        if (resolvedPath == null || !Files.exists(resolvedPath)) {
            resolvedPath = createPlaceholderIfMissing(doc);
        }

        if (resolvedPath == null || !Files.exists(resolvedPath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 4. Construye recurso UrlResource y encabezados de respuesta
            Resource resource = new UrlResource(resolvedPath.toUri());
            String mimeType = doc.getMimeType();
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = Files.probeContentType(resolvedPath);
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            String disposition = asDownload ? "attachment" : "inline";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + doc.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resuelve de forma robusta la ruta fisica de un archivo subido dentro del directorio de uploads.
     *
     * @param doc Entidad del documento KYC con ruta o nombre de archivo.
     * @return Path fisico resuelto o null si no se encontro.
     */
    private Path resolveDocumentPath(KycDocument doc) {
        if (doc == null) return null;

        // 1. Probar ruta directa
        if (doc.getFilePath() != null && !doc.getFilePath().isBlank()) {
            Path direct = Paths.get(doc.getFilePath());
            if (Files.exists(direct)) {
                return direct;
            }
            Path inUploadDir = Paths.get(uploadDir).resolve(direct.getFileName());
            if (Files.exists(inUploadDir)) {
                return inUploadDir;
            }
        }

        // 2. Probar por nombre de archivo
        if (doc.getFileName() != null && !doc.getFileName().isBlank()) {
            Path byName = Paths.get(uploadDir).resolve(doc.getFileName());
            if (Files.exists(byName)) {
                return byName;
            }

            try {
                Path dir = Paths.get(uploadDir);
                if (Files.exists(dir)) {
                    Optional<Path> match = Files.list(dir)
                            .filter(p -> p.getFileName().toString().endsWith(doc.getFileName()))
                            .findFirst();
                    if (match.isPresent()) {
                        return match.get();
                    }
                }
            } catch (IOException ignored) {}
        }

        return null;
    }

    /**
     * Crea un archivo representativo de demostracion si el archivo original no existe fisicamente en disco.
     *
     * @param doc Metadatos del documento.
     * @return Path del archivo generado.
     */
    private Path createPlaceholderIfMissing(KycDocument doc) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String name = (doc.getFileName() != null && !doc.getFileName().isBlank()) ? doc.getFileName() : "sample_doc_" + doc.getId() + ".pdf";
            Path target = Paths.get(uploadDir).resolve(name);
            if (!Files.exists(target)) {
                if (name.toLowerCase().endsWith(".pdf")) {
                    String samplePdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Count 1/Kids[3 0 R]>>endobj\n3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Resources<<>>>>endobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000052 00000 n \n0000000101 00000 n \ntrailer<</Size 4/Root 1 0 R>>\nstartxref\n180\n%%EOF\n";
                    Files.writeString(target, samplePdf);
                } else {
                    byte[] pngBytes = new byte[]{
                            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89,
                            0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41, 0x54,
                            0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
                            0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4,
                            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                            (byte) 0xAE, 0x42, 0x60, (byte) 0x82
                    };
                    Files.write(target, pngBytes);
                }
            }
            return target;
        } catch (Exception e) {
            return null;
        }
    }
}
