package com.digitalverify.employee;

import com.digitalverify.model.KycCase;
import com.digitalverify.model.User;
import com.digitalverify.repository.KycCaseJdbcRepository;
import com.digitalverify.repository.UserJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeQueueAndCasesWorkflowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KycCaseJdbcRepository caseRepo;

    @Autowired
    private UserJdbcRepository userRepo;

    private MockHttpSession employeeSession;

    @BeforeEach
    void setUp() {
        employeeSession = new MockHttpSession();
        User employee = new User(8L, "emp90210@verify.com", "pass", "Paola Castro", "EMPLOYEE",
                "ES", "+34 600 000 002", "EMP002", "APPROVED", "/img/employee_avatar.png", true, null);
        employeeSession.setAttribute("currentEmployee", employee);
    }

    @Test
    @DisplayName("Cola KYC: Debe cargar casos de la base de datos para el operador")
    void testQueueDisplaysCasesFromDatabase() throws Exception {
        mockMvc.perform(get("/queue.html").session(employeeSession))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/queue"))
                .andExpect(model().attributeExists("cases"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    @DisplayName("Filtro de Cola: Filtrar por estado pending debe retornar únicamente casos pendientes")
    void testQueueFilterByStatusPending() throws Exception {
        mockMvc.perform(get("/queue.html").session(employeeSession)
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/queue"))
                .andExpect(model().attribute("currentStatus", "pending"))
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<KycCase> cases = (List<KycCase>) result.getModelAndView().getModel().get("cases");
                    assertNotNull(cases);
                    assertTrue(cases.stream().allMatch(c -> "pending".equalsIgnoreCase(c.getStatus())));
                });
    }

    @Test
    @DisplayName("Filtro de Cola: Filtrar por razón de verificación debe funcionar")
    void testQueueFilterByPriorityCritical() throws Exception {
        mockMvc.perform(get("/queue.html").session(employeeSession)
                        .param("reason", "fraud_review"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/queue"))
                .andExpect(model().attribute("currentReason", "fraud_review"));
    }

    @Test
    @DisplayName("Detalle de Caso: Carga información del caso, documentos adjuntos e historial de auditoría")
    void testCaseDetailLoadsFullInformation() throws Exception {
        mockMvc.perform(get("/case.html").session(employeeSession)
                        .param("caseId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/case"))
                .andExpect(model().attributeExists("kycCase"))
                .andExpect(model().attributeExists("documents"))
                .andExpect(model().attributeExists("logs"));
    }

    @Test
    @DisplayName("Decisión Operativa: Aprobar un caso actualiza el estado en BD y el estado KYC del usuario")
    void testApproveCaseWorkflow() throws Exception {
        mockMvc.perform(post("/employee/case/1/decision").session(employeeSession)
                        .param("status", "approved")
                        .param("decisionNotes", "Aprobación verificada por suite de pruebas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/case.html?caseId=1"))
                .andExpect(flash().attributeExists("successMessage"));

        Optional<KycCase> updated = caseRepo.findById(1L);
        assertTrue(updated.isPresent());
        assertEquals("approved", updated.get().getStatus());

        Optional<User> userOpt = userRepo.findById(updated.get().getUserId());
        assertTrue(userOpt.isPresent());
        assertEquals("APPROVED", userOpt.get().getKycStatus());
    }

    @Test
    @DisplayName("Decisión Operativa: Rechazar un caso actualiza el estado en BD")
    void testRejectCaseWorkflow() throws Exception {
        mockMvc.perform(post("/employee/case/2/decision").session(employeeSession)
                        .param("status", "rejected")
                        .param("decisionNotes", "Documentos ilegibles. Se solicita reenvío."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/case.html?caseId=2"))
                .andExpect(flash().attributeExists("successMessage"));

        Optional<KycCase> updated = caseRepo.findById(2L);
        assertTrue(updated.isPresent());
        assertEquals("rejected", updated.get().getStatus());
    }

    @Test
    @DisplayName("Asignación de Operador: Asignar un caso persiste el operador en base de datos")
    void testAssignOperatorWorkflow() throws Exception {
        mockMvc.perform(post("/employee/case/5/assign").session(employeeSession)
                        .param("operatorName", "Paola Castro"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/queue.html"))
                .andExpect(flash().attributeExists("successMessage"));

        Optional<KycCase> updated = caseRepo.findById(5L);
        assertTrue(updated.isPresent());
        assertEquals("Paola Castro", updated.get().getAssignedTo());
    }
}
