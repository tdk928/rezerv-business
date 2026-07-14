package bg.rezerv.business.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bg.rezerv.business.service.NomenclatureService;
import bg.rezerv.business.web.dto.CityResponse;
import bg.rezerv.business.web.dto.ServiceCategoryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NomenclatureController.class)
class NomenclatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NomenclatureService nomenclatureService;

    @Test
    void getCitiesReturnsJsonList() throws Exception {
        when(nomenclatureService.getCities())
                .thenReturn(List.of(new CityResponse(1L, "София", "sofia")));

        mockMvc.perform(get("/business/public/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("София"))
                .andExpect(jsonPath("$[0].slug").value("sofia"));
    }

    @Test
    void getCategoriesReturnsJsonList() throws Exception {
        when(nomenclatureService.getServiceCategories())
                .thenReturn(List.of(new ServiceCategoryResponse(1L, "Масаж", "masazh", "massage")));

        mockMvc.perform(get("/business/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Масаж"))
                .andExpect(jsonPath("$[0].icon").value("massage"));
    }
}
