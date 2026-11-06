package com.digitalverify.e2e;

import com.digitalverify.model.KycCase;
import com.digitalverify.model.KycDocument;
import com.digitalverify.model.Notification;
import com.digitalverify.model.User;
import com.digitalverify.repository.DocumentJdbcRepository;
import com.digitalverify.repository.KycCaseJdbcRepository;
import com.digitalverify.repository.NotificationJdbcRepository;
import com.digitalverify.repository.UserJdbcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class KycClientEmployeeConnectionE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJdbcRepository userRepo;

    @Autowired
    private KycCaseJdbcRepository caseRepo;

    @Autowired
    private DocumentJdbcRepository documentRepo;

    @Autowired
    private NotificationJdbcRepository notifRepo;

    @Test
    @DisplayName("Flujo Conectado 360: Cliente sube KYC -> Empleado recibe alerta y caso -> Empleado aprueba -> Cliente recibe notificación y estado aprobado")
    void testEndToEndClientToEmployeeKycFlow() throws Exception {
        // 1. Crear nuevo cliente
        String email = "cliente_e2e_" + System.currentTimeMillis() + "@email.com";
        User clientUser = new User();
        clientUser.setEmail(email);
        clientUser.setPasswordHash("pass123");
        clientUser.setFullName("Jugador E2E");
        clientUser.setRole("PLAYER");
        clientUser.setCountry("CO");
        clientUser.setPhone("+57 311 000 1122");
        clientUser.setAccountNumber("99001");
        clientUser.setKycStatus("PENDING");
        Long clientId = userRepo.save(clientUser);
        assertNotNull(clientId);

        MockHttpSession clientSession = new MockHttpSession();
        clientUser.setId(clientId);
        clientSession.setAttribute("currentUser", clientUser);

        // 2. Cliente sube ID (Paso 1)
        MockMultipartFile idFile = new MockMultipartFile(
                "documentFile",
                "cedula_test.jpg",
                "image/jpeg",
                "cedula scan bytes".getBytes()
        );
        mockMvc.perform(multipart("/kyc/upload-id")
                        .file(idFile)
                        .param("documentType", "NATIONAL_ID")
                        .session(clientSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/proof_of_address.html"));

        // 3. Cliente sube Comprobante de Domicilio (Paso 2)
        MockMultipartFile addressFile = new MockMultipartFile(
                "addressFile",
                "recibo_servicios.pdf",
                "application/pdf",
                "%PDF-1.4 sample address content".getBytes()
        );
        mockMvc.perform(multipart("/kyc/proof-of-address")
                        .file(addressFile)
                        .param("addressDocType", "UTILITY_BILL")
                        .session(clientSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/kyc_status_review.html"));

        // 4. Verificar que se creó el caso en BD con estado in_review
        Optional<KycCase> latestCaseOpt = caseRepo.findLatestByUserId(clientId);
        assertTrue(latestCaseOpt.isPresent(), "El caso de KYC debe crearse en base de datos");
        KycCase kycCase = latestCaseOpt.get();
        assertEquals("in_review", kycCase.getStatus());

        // 5. Verificar que los empleados recibieron la notificación de la cola
        List<Notification> employeeNotifs = notifRepo.findByUserId(7L); // Paola Castro
        boolean hasQueueNotif = employeeNotifs.stream()
                .anyMatch(n -> "KYC_QUEUE".equals(n.getCategory()) && n.getMessage().contains(kycCase.getCaseNumber()));
        assertTrue(hasQueueNotif, "El empleado debe recibir notificación de nuevo caso en cola");

        // 5.b Verificar que los documentos se pueden abrir y visualizar con "Open Document" (/api/documents/{id}/view)
        List<KycDocument> documents = documentRepo.findByCaseId(kycCase.getId());
        assertFalse(documents.isEmpty(), "Deben existir documentos asociados al caso");
        for (KycDocument doc : documents) {
            mockMvc.perform(get("/api/documents/" + doc.getId() + "/view"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Content-Disposition"));
        }

        // 6. Empleado inicia sesión y toma la decisión
        MockHttpSession employeeSession = new MockHttpSession();
        User employee = userRepo.findById(7L).orElseThrow();
        employeeSession.setAttribute("currentEmployee", employee);

        // Empleado aprueba el caso vía API modal
        String decisionJson = "{" +
                "\"status\": \"approved\"," +
                "\"verificationDecision\": \"Approve Verification\"," +
                "\"accountStatusDecision\": \"Reactivate Account\"," +
                "\"operatorName\": \"Paola Castro\"," +
                "\"decisionNotes\": \"Documentos 100% legibles y verificados en listas.\"" +
                "}";

        mockMvc.perform(post("/api/cases/" + kycCase.getId() + "/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decisionJson)
                        .session(employeeSession))
                .andExpect(status().isOk());

        // 7. Verificar que el caso en BD quedó en approved
        KycCase updatedCase = caseRepo.findById(kycCase.getId()).orElseThrow();
        assertEquals("approved", updatedCase.getStatus());

        // 8. Verificar que el usuario cliente quedó en APPROVED
        User updatedClient = userRepo.findById(clientId).orElseThrow();
        assertEquals("APPROVED", updatedClient.getKycStatus());

        // 9. Verificar que el cliente recibió la notificación de aprobación
        List<Notification> clientNotifs = notifRepo.findByUserId(clientId);
        boolean hasApprovedNotif = clientNotifs.stream()
                .anyMatch(n -> "KYC".equals(n.getCategory()) && n.getTitle().contains("Aprobada"));
        assertTrue(hasApprovedNotif, "El cliente debe recibir la notificación de verificación aprobada");

        // 10. Cliente consulta su status review y su home
        mockMvc.perform(get("/kyc_status_review.html").session(clientSession))
                .andExpect(status().isOk())
                .andExpect(view().name("client/kyc_status_review"))
                .andExpect(model().attributeExists("kycCase"));

        mockMvc.perform(get("/home.html").session(clientSession))
                .andExpect(status().isOk())
                .andExpect(view().name("client/home"))
                .andExpect(model().attributeExists("kycCase"));
    }
}
