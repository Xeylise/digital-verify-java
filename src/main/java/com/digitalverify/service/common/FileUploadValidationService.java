package com.digitalverify.service.common;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio de validacion tecnica y de seguridad para archivos cargados en el sistema (documentos KYC).
 * Verifica que los documentos cumplan con las restricciones de peso maximo permitido (10 MB),
 * extensiones permitidas (.jpg, .jpeg, .png, .pdf) y tipos MIME autorizados.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Service
public class FileUploadValidationService {

    /**
     * Extensiones de archivo permitidas para la carga de documentos de soporte KYC.
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".pdf");

    /**
     * Tipos MIME admitidos por las politicas de seguridad del sistema.
     */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "application/pdf"
    );

    /**
     * Tamaño maximo en bytes permitido por documento (10 Megabytes).
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Valida integralmente el archivo enviado por el usuario contra politicas de formato, tamaño y MIME.
     *
     * @param file Archivo multipart subido desde el formulario web.
     * @return Mensaje de error descriptivo en caso de fallo, o null si el archivo es completamente valido.
     */
    public String validateFile(MultipartFile file) {
        // 1. Validacion de presencia y contenido no vacio
        if (file == null || file.isEmpty()) {
            return "Por favor selecciona un archivo para subir.";
        }

        // 2. Validacion de tamaño maximo permitido (10 MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            return "El archivo excede el tamaño máximo permitido de 10 MB.";
        }

        // 3. Validacion de nombre de archivo y extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "Nombre de archivo inválido.";
        }

        String lowerName = originalFilename.toLowerCase();
        boolean validExt = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!validExt) {
            return "Formato de archivo no permitido. Solo se aceptan imágenes JPG, PNG o documentos PDF.";
        }

        // 4. Validacion del tipo MIME declarado en los encabezados HTTP
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return "Tipo MIME no permitido (" + contentType + "). Solo JPG, PNG o PDF.";
        }

        // Archivo valido sin discrepancias
        return null;
    }
}
