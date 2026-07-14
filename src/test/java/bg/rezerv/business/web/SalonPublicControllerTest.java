package bg.rezerv.business.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bg.rezerv.business.service.SalonQueryService;
import bg.rezerv.business.web.dto.CityResponse;
import bg.rezerv.business.web.dto.PageResponse;
import bg.rezerv.business.web.dto.SalonCardResponse;
import bg.rezerv.business.web.error.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SalonPublicController.class)
class SalonPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SalonQueryService salonQueryService;

    @Test
    void searchReturnsPageOfCards() throws Exception {
        SalonCardResponse card = new SalonCardResponse(
                10L, "Barber Bros", new CityResponse(1L, "София", "sofia"),
                "ул. Шишман 12", new BigDecimal("4.90"), 211,
                "http://p/main.jpg", new BigDecimal("25.00"));
        when(salonQueryService.search(eq(1L), eq(2L), eq(null), eq(0), eq(20)))
                .thenReturn(new PageResponse<>(List.of(card), 0, 20, 1, 1));

        mockMvc.perform(get("/business/public/salons?cityId=1&categoryId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Barber Bros"))
                .andExpect(jsonPath("$.content[0].city.slug").value("sofia"))
                .andExpect(jsonPath("$.content[0].priceFrom").value(25.00));
    }

    @Test
    void getSalonReturns404WithUnifiedErrorFormat() throws Exception {
        when(salonQueryService.getSalon(99L))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "SALON_NOT_FOUND", "Салонът не е намерен"));

        mockMvc.perform(get("/business/public/salons/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("SALON_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Салонът не е намерен"));
    }
}
