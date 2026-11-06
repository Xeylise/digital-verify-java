package com.digitalverify.employee;

import com.digitalverify.model.AuditLogEntry;
import com.digitalverify.model.User;
import com.digitalverify.repository.AuditLogJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeReportsAndActivityLogTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogJdbcRepository auditRepo;

    private MockHttpSession employeeSession;

    @BeforeEach
    void setUp() {
        employeeSession = new MockHttpSession();
        User employee = new User(8L, "emp90210@verify.com", "pass", "Paola Castro", "EMPLOYEE",
                "ES", "+34 600 000 002", "EMP002", "APPROVED", "/img/employee_avatar.png", true, null);
        employeeSession.setAttribute("currentEmployee", employee);
    }

    @Test
    @DisplayName("Registro de Actividad: Carga el historial de auditoría desde la base de datos")
    void testActivityLogDisplaysEntriesFromDatabase() throws Exception {
        // Asegurar que al menos existe un log
        AuditLogEntry entry = new AuditLogEntry();
        entry.setOperatorName("Paola Castro");
        entry.setCaseNumber("DV-1024");
        entry.setAction("Inspección de Documento");
        entry.setDetails("Cédula frontal aprobada");
        auditRepo.save(entry);

        mockMvc.perform(get("/activity_log.html").session(employeeSession))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/activity_log"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(result -> {
                    @SuppressWarnings("unchecked")
                    List<AuditLogEntry> logs = (List<AuditLogEntry>) result.getModelAndView().getModel().get("logs");
                    assertNotNull(logs);
                    assertFalse(logs.isEmpty());
                });
    }

    @Test
    @DisplayName("Reportes: Muestra métricas calculadas y listado dinámico de casos")
    void testReportsRendersSummaryAndDynamicCases() throws Exception {
        mockMvc.perform(get("/reports.html").session(employeeSession))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/reports"))
                .andExpect(model().attributeExists("reportsSummary"))
                .andExpect(model().attributeExists("report"));
    }

    @Test
    @DisplayName("Configuración Empleado: Muestra perfil del operador conectado")
    void testSettingsEmployeeDisplaysProfileData() throws Exception {
        mockMvc.perform(get("/settings_employee.html").session(employeeSession))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/settings_employee"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attribute("employee", org.hamcrest.Matchers.hasProperty("email", org.hamcrest.Matchers.is("emp90210@verify.com"))));
    }
}
