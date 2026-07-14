package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import bg.rezerv.business.domain.City;
import bg.rezerv.business.domain.ServiceCategory;
import bg.rezerv.business.repository.CityRepository;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.web.dto.CityResponse;
import bg.rezerv.business.web.dto.ServiceCategoryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NomenclatureServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;

    @InjectMocks
    private NomenclatureService nomenclatureService;

    @Test
    void getCitiesMapsEntitiesToResponses() {
        when(cityRepository.findAllByOrderByNameAsc()).thenReturn(List.of(
                City.builder().id(1L).name("Бургас").slug("burgas").build(),
                City.builder().id(2L).name("София").slug("sofia").build()));

        List<CityResponse> cities = nomenclatureService.getCities();

        assertThat(cities).containsExactly(
                new CityResponse(1L, "Бургас", "burgas"),
                new CityResponse(2L, "София", "sofia"));
    }

    @Test
    void getServiceCategoriesMapsEntitiesToResponses() {
        when(serviceCategoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(
                ServiceCategory.builder().id(1L).name("Масаж").slug("masazh").icon("massage").build()));

        List<ServiceCategoryResponse> categories = nomenclatureService.getServiceCategories();

        assertThat(categories).containsExactly(
                new ServiceCategoryResponse(1L, "Масаж", "masazh", "massage"));
    }

    @Test
    void getCitiesReturnsEmptyListWhenNoData() {
        when(cityRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        assertThat(nomenclatureService.getCities()).isEmpty();
    }
}
