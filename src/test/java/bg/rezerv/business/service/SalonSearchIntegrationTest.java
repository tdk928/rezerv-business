package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import bg.rezerv.business.TestcontainersConfiguration;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.web.dto.PageResponse;
import bg.rezerv.business.web.dto.SalonCardResponse;
import bg.rezerv.business.web.dto.SalonDetailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** Търсенето срещу реален PostgreSQL с Flyway миграциите + демо seed данните. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SalonSearchIntegrationTest {

    @Autowired
    private SalonQueryService salonQueryService;

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @Test
    void searchWithoutFiltersReturnsAllSeededSalonsOrderedByRating() {
        PageResponse<SalonCardResponse> result = salonQueryService.search(null, null, null, 0, 20);

        assertThat(result.totalElements()).isEqualTo(6);
        assertThat(result.content().getFirst().name()).isEqualTo("Barber Bros");
        assertThat(result.content()).allSatisfy(card -> {
            assertThat(card.photoUrl()).isNotBlank();
            assertThat(card.priceFrom()).isNotNull();
        });
    }

    @Test
    void searchFiltersByCategory() {
        Long barberCategoryId = serviceCategoryRepository.findAllByOrderByNameAsc().stream()
                .filter(c -> c.getSlug().equals("barber"))
                .findFirst().orElseThrow().getId();

        PageResponse<SalonCardResponse> result =
                salonQueryService.search(null, barberCategoryId, null, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("Barber Bros");
    }

    @Test
    void searchFiltersByNameQuery() {
        PageResponse<SalonCardResponse> result = salonQueryService.search(null, null, "glamour", 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().name()).isEqualTo("Studio Glamour");
    }

    @Test
    void salonDetailContainsPhotosAndGroupedServices() {
        Long salonId = salonQueryService.search(null, null, "Studio Glamour", 0, 1)
                .content().getFirst().id();

        SalonDetailResponse detail = salonQueryService.getSalon(salonId);

        assertThat(detail.photos()).hasSize(2);
        assertThat(detail.serviceGroups()).extracting(SalonDetailResponse.ServiceGroup::categorySlug)
                .containsExactlyInAnyOrder("frizyor", "grim");
    }
}
