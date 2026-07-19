package bg.rezerv.business.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bg.rezerv.business.domain.CompanyStatus;
import bg.rezerv.business.service.CompanyOnboardingService;
import bg.rezerv.business.web.dto.CompanyResponse;
import bg.rezerv.business.web.error.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CompanyController.class, SalonManagementController.class})
class CompanyOnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyOnboardingService onboardingService;

    @Test
    void registerCompanyRequiresAuthHeader() throws Exception {
        mockMvc.perform(post("/business/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eik":"204815936","name":"Test","legalName":"Test EOOD","email":"t@test.bg","phone":"+359888"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void registerCompanyReturnsCreatedCompany() throws Exception {
        when(onboardingService.registerCompany(any(), any())).thenReturn(new CompanyResponse(
                10L, "204815936", "Test", "Test EOOD", "t@test.bg", "+359888", 7L,
                CompanyStatus.PENDING_APPROVAL, Instant.parse("2026-07-15T00:00:00Z")));

        mockMvc.perform(post("/business/companies")
                        .header(ContextHeaders.USER_ID, "7")
                        .header(ContextHeaders.USER_ROLES, "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eik":"204815936","name":"Test","legalName":"Test EOOD","email":"t@test.bg","phone":"+359888"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void createSalonReturns403WhenNotOwner() throws Exception {
        when(onboardingService.createSalon(any(), eq(1L), any()))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "NOT_COMPANY_OWNER", "Нямате права"));

        mockMvc.perform(post("/business/companies/1/salons")
                        .header(ContextHeaders.USER_ID, "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Salon","cityId":22,"address":"ул. Тест 1","email":"salon@example.bg","phone":"+359888888888",
                                 "workingHours":[{"dayOfWeek":1,"openTime":"09:00","closeTime":"18:00"}]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_COMPANY_OWNER"));
    }
}
