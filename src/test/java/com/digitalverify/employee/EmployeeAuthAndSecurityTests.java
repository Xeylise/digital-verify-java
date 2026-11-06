package com.digitalverify.employee;

import com.digitalverify.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeAuthAndSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Seguridad: Intento de acceso a /dashboard sin sesión debe ser bloqueado y redirigir a login")
    void testUnauthenticatedAccessToDashboardIsBlocked() throws Exception {
        mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Seguridad: Intento de acceso a /queue sin sesión debe ser bloqueado")
    void testUnauthenticatedAccessToQueueIsBlocked() throws Exception {
        mockMvc.perform(get("/queue.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Seguridad: Intento de acceso a /case sin sesión debe ser bloqueado")
    void testUnauthenticatedAccessToCaseDetailIsBlocked() throws Exception {
        mockMvc.perform(get("/case.html?caseId=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Seguridad: Intento de acceso a /all-cases sin sesión debe ser bloqueado")
    void testUnauthenticatedAccessToAllCasesIsBlocked() throws Exception {
        mockMvc.perform(get("/all-cases.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Seguridad: Intento de acceso a /reports sin sesión debe ser bloqueado")
    void testUnauthenticatedAccessToReportsIsBlocked() throws Exception {
        mockMvc.perform(get("/reports.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Seguridad: Intento de acceso a /activity_log sin sesión debe ser bloqueado")
    void testUnauthenticatedAccessToActivityLogIsBlocked() throws Exception {
        mockMvc.perform(get("/activity_log.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Login Empleado: Credenciales válidas autentican y redirigen al dashboard")
    void testValidEmployeeLoginEstablishesSession() throws Exception {
        mockMvc.perform(post("/login_employee")
                        .param("email", "emp90210@verify.com")
                        .param("password", "operator123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard.html"))
                .andExpect(request().sessionAttribute("currentEmployee", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Login Empleado: Contraseña errónea no autentica y muestra error en la vista")
    void testInvalidEmployeeLoginFails() throws Exception {
        mockMvc.perform(post("/login_employee")
                        .param("email", "emp90210@verify.com")
                        .param("password", "wrongpass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/login_employee"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("Seguridad: Rol Cliente no puede acceder a rutas de Empleado")
    void testClientRoleCannotAccessEmployeeDashboard() throws Exception {
        MockHttpSession clientSession = new MockHttpSession();
        User clientUser = new User("cliente@email.com", "Cliente Test", "PLAYER");
        clientSession.setAttribute("currentUser", clientUser);

        mockMvc.perform(get("/dashboard.html").session(clientSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?error=session_required"));
    }

    @Test
    @DisplayName("Logout Empleado: Invalida la sesión y redirige a login")
    void testEmployeeLogoutInvalidatesSession() throws Exception {
        MockHttpSession employeeSession = new MockHttpSession();
        User empUser = new User("emp90210@verify.com", "Paola Castro", "EMPLOYEE");
        employeeSession.setAttribute("currentEmployee", empUser);

        mockMvc.perform(get("/employee/logout").session(employeeSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login_employee.html?logged_out=true"));
    }
}
