package com.digitalverify.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un documento de identidad o comprobante cargado en el sistema KYC.
 * Mantiene la metadata de archivos fisicos (nombre, ruta local, tamaño, tipo MIME y estado).
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class KycDocument {

    /**
     * Identificador unico del documento.
     */
    private Long id;

    /**
     * Identificador foraneo del caso de verificacion (KycCase) al que pertenece el documento.
     */
    private Long caseId;

    /**
     * Tipo de documento segun catalogo (NATIONAL_ID, PASSPORT, UTILITY_BILL, DRIVERS_LICENSE).
     */
    private String documentType;

    /**
     * Nombre original del archivo proporcionado por el usuario.
     */
    private String fileName;

    /**
     * Ruta de almacenamiento en el sistema de archivos del servidor.
     */
    private String filePath;

    /**
     * Tamaño del archivo en bytes.
     */
    private Long fileSize;

    /**
     * Tipo MIME del archivo (image/jpeg, image/png, application/pdf).
     */
    private String mimeType;

    /**
     * Estado de validacion del documento (PENDING, VERIFIED, REJECTED).
     */
    private String status;

    /**
     * Fecha y hora en la que el documento fue cargado al servidor.
     */
    private LocalDateTime uploadedAt;

    /**
     * Constructor por defecto para mapeadores de base de datos.
     */
    public KycDocument() {}

    /**
     * Constructor completo con atributos de persistencia.
     *
     * @param id           Identificador del documento.
     * @param caseId       ID del caso asociado.
     * @param documentType Tipo de documento.
     * @param fileName     Nombre de archivo original.
     * @param filePath     Ruta en disco.
     * @param fileSize     Tamaño en bytes.
     * @param mimeType     Tipo de contenido MIME.
     * @param status       Estado de verificacion.
     * @param uploadedAt   Fecha de carga.
     */
    public KycDocument(Long id, Long caseId, String documentType, String fileName,
                       String filePath, Long fileSize, String mimeType, String status,
                       LocalDateTime uploadedAt) {
        this.id = id;
        this.caseId = caseId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
