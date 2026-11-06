package com.digitalverify.controller;

import com.digitalverify.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession employeeSession;
    private MockHttpSession clientSession;

    @BeforeEach
    void setUp() {
        employeeSession = new MockHttpSession();
        User employee = new User(8L, "emp90210@verify.com", "pass", "Paola Castro", "EMPLOYEE",
                "ES", "+34 600 000 002", "EMP002", "APPROVED", "/img/employee_avatar.png", true, null);
        employeeSession.setAttribute("currentEmployee", employee);

        clientSession = new MockHttpSession();
        User client = new User(1L, "carlos.mendoza@email.com", "pass", "Carlos Mendoza", "PLAYER",
                "CO", "+57 300 123 4567", "80948", "PENDING", "/img/user_avatar.jpg", false, null);
        clientSession.setAttribute("currentUser", client);
    }

    @Test
    @DisplayName("GET / should return 200 and index template")
    void testClientIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/index"));
    }

    @Test
    @DisplayName("POST /login with valid credentials should redirect to home")
    void testValidLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "carlos.mendoza@email.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home.html"));
    }

    @Test
    @DisplayName("POST /login with invalid email format should fail validation and return index")
    void testInvalidEmailFormatLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "not-an-email")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/index"))
                .andExpect(model().attributeExists("loginError"));
    }

    @Test
    @DisplayName("POST /login with wrong password should fail authentication")
    void testWrongPasswordLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "carlos.mendoza@email.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/index"))
                .andExpect(model().attributeExists("loginError"));
    }

    @Test
    @DisplayName("POST /register with valid data should create user and redirect")
    void testValidRegister() throws Exception {
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@email.com";
        mockMvc.perform(post("/register")
                        .param("fullName", "Test User")
                        .param("email", uniqueEmail)
                        .param("password", "secret123")
                        .param("phone", "+57 300 000 0000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home.html"));
    }

    @Test
    @DisplayName("POST /login_employee with employee credentials should redirect to dashboard")
    void testEmployeeLogin() throws Exception {
        mockMvc.perform(post("/login_employee")
                        .param("email", "emp90210@verify.com")
                        .param("password", "operator123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard.html"));
    }

    @Test
    @DisplayName("GET /dashboard should return 200 and dashboard template")
    void testEmployeeDashboard() throws Exception {
        mockMvc.perform(get("/dashboard").session(employeeSession))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("cases"));
    }

    @Test
    @DisplayName("GET /queue should return 200 and queue template")
    void testEmployeeQueue() throws Exception {
        mockMvc.perform(get("/queue").session(employeeSession))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/queue"))
                .andExpect(model().attributeExists("cases"));
    }

    @Test
    @DisplayName("POST /employee/case/1/decision should update case in DB")
    void testCaseDecisionPost() throws Exception {
        mockMvc.perform(post("/employee/case/1/decision").session(employeeSession)
                        .param("status", "approved")
                        .param("decisionNotes", "Approved via automated test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/case.html?caseId=1"));
    }

    @Test
    @DisplayName("POST /kyc/upload-id with valid file should save document and redirect")
    void testUploadIdPost() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "documentFile",
                "test_id.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        mockMvc.perform(multipart("/kyc/upload-id").file(file).session(clientSession)
                        .param("documentType", "NATIONAL_ID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/proof_of_address.html"));
    }

    @Test
    @DisplayName("POST /kyc/upload-id with invalid file extension should reject")
    void testUploadIdInvalidExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "documentFile",
                "malicious.exe",
                "application/octet-stream",
                "bad content".getBytes()
        );

        mockMvc.perform(multipart("/kyc/upload-id").file(file).session(clientSession)
                        .param("documentType", "NATIONAL_ID"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/upload_id.html"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("GET /api/cases should return JSON array of cases")
    void testApiCases() throws Exception {
        mockMvc.perform(get("/api/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/stats/dashboard should return dashboard metrics")
    void testApiDashboardStats() throws Exception {
        mockMvc.perform(get("/api/stats/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCases").isNumber());
    }
}
