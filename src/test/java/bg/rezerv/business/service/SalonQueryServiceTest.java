package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import bg.rezerv.business.domain.City;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonPhoto;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.domain.ServiceCategory;
import bg.rezerv.business.repository.SalonPhotoRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.web.dto.PageResponse;
import bg.rezerv.business.web.dto.SalonCardResponse;
import bg.rezerv.business.web.dto.SalonDetailResponse;
import bg.rezerv.business.web.error.ApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SalonQueryServiceTest {

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private SalonPhotoRepository salonPhotoRepository;

    @Mock
    private SalonServiceItemRepository salonServiceItemRepository;

    @InjectMocks
    private SalonQueryService salonQueryService;

    private final City sofia = City.builder().id(1L).name("София").slug("sofia").build();

    @Test
    void searchBuildsCardsWithMainPhotoAndMinPrice() {
        Salon salon = Salon.builder()
                .id(10L).companyId(1L).name("Studio Glamour").city(sofia)
                .address("бул. Витоша 45").ratingAvg(new BigDecimal("4.80")).ratingCount(124)
                .build();
        when(salonRepository.search(eq(1L), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(salon), PageRequest.of(0, 20), 1));
        when(salonPhotoRepository.findBySalonIdInOrderByPositionAsc(anyCollection())).thenReturn(List.of(
                SalonPhoto.builder().salonId(10L).url("http://p/main.jpg").position(0).build(),
                SalonPhoto.builder().salonId(10L).url("http://p/second.jpg").position(1).build()));
        when(salonServiceItemRepository.findMinPrices(anyCollection()))
                .thenReturn(List.of(minPrice(10L, new BigDecimal("35.00"))));

        PageResponse<SalonCardResponse> result = salonQueryService.search(1L, null, "  ", 0, 20);

        assertThat(result.totalElements()).isEqualTo(1);
        SalonCardResponse card = result.content().getFirst();
        assertThat(card.name()).isEqualTo("Studio Glamour");
        assertThat(card.city().slug()).isEqualTo("sofia");
        assertThat(card.photoUrl()).isEqualTo("http://p/main.jpg");
        assertThat(card.priceFrom()).isEqualByComparingTo("35.00");
    }

    @Test
    void getSalonGroupsServicesByCategory() {
        ServiceCategory frizyor = ServiceCategory.builder()
                .id(1L).name("Фризьор").slug("frizyor").icon("scissors").build();
        ServiceCategory grim = ServiceCategory.builder()
                .id(2L).name("Грим").slug("grim").icon("brush").build();
        Salon salon = Salon.builder()
                .id(10L).companyId(1L).name("Studio Glamour").city(sofia)
                .address("бул. Витоша 45").ratingAvg(new BigDecimal("4.80")).ratingCount(124)
                .build();
        when(salonRepository.findWithCityById(10L)).thenReturn(Optional.of(salon));
        when(salonPhotoRepository.findBySalonIdOrderByPositionAsc(10L)).thenReturn(List.of());
        when(salonServiceItemRepository.findBySalonIdAndActiveTrueOrderByNameAsc(10L)).thenReturn(List.of(
                service(1L, frizyor, "Дамско подстригване", 60, "55.00"),
                service(2L, grim, "Официален грим", 60, "90.00"),
                service(3L, frizyor, "Боядисване", 120, "130.00")));

        SalonDetailResponse detail = salonQueryService.getSalon(10L);

        assertThat(detail.serviceGroups()).hasSize(2);
        SalonDetailResponse.ServiceGroup grimGroup = detail.serviceGroups().getFirst();
        assertThat(grimGroup.categorySlug()).isEqualTo("grim");
        SalonDetailResponse.ServiceGroup frizyorGroup = detail.serviceGroups().getLast();
        assertThat(frizyorGroup.services()).hasSize(2);
    }

    @Test
    void getSalonThrowsNotFoundForMissingId() {
        when(salonRepository.findWithCityById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> salonQueryService.getSalon(99L))
                .isInstanceOf(ApiException.class)
                .hasMessage("Салонът не е намерен");
    }

    private static SalonServiceItem service(Long id, ServiceCategory category, String name,
                                            int durationMin, String price) {
        return SalonServiceItem.builder()
                .id(id).salonId(10L).category(category).name(name)
                .durationMin(durationMin).price(new BigDecimal(price))
                .build();
    }

    private static SalonServiceItemRepository.MinPricePerSalon minPrice(Long salonId, BigDecimal price) {
        return new SalonServiceItemRepository.MinPricePerSalon() {
            @Override
            public Long getSalonId() {
                return salonId;
            }

            @Override
            public BigDecimal getMinPrice() {
                return price;
            }
        };
    }
}
