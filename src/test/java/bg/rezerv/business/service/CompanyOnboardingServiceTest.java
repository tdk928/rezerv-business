package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bg.rezerv.business.client.CasClient;
import bg.rezerv.business.domain.City;
import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.CompanyStatus;
import bg.rezerv.business.domain.ServiceCategory;
import bg.rezerv.business.repository.CityRepository;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.SalonPhotoRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.web.RequestContext;
import bg.rezerv.business.web.dto.CreateCompanyRequest;
import bg.rezerv.business.web.dto.CreateSalonRequest;
import bg.rezerv.business.web.dto.CreateSalonServiceRequest;
import bg.rezerv.business.web.error.ApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyOnboardingServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SalonRepository salonRepository;
    @Mock
    private SalonServiceItemRepository salonServiceItemRepository;
    @Mock
    private SalonPhotoRepository salonPhotoRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;
    @Mock
    private EikValidator eikValidator;

    @Mock
    private CasClient casClient;

    @InjectMocks
    private CompanyOnboardingService service;

    private final RequestContext owner = new RequestContext(42L, List.of("CLIENT"), null);

    @Test
    void registerCompanyPersistsPendingApprovalCompany() {
        when(companyRepository.existsByOwnerUserId(42L)).thenReturn(false);
        when(eikValidator.isValid("204815936")).thenReturn(true);
        when(companyRepository.existsByEik("204815936")).thenReturn(false);
        when(companyRepository.save(any())).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        var response = service.registerCompany(owner, new CreateCompanyRequest(
                "204815936", "Тест ООД", "Test EOOD"));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(CompanyStatus.PENDING_APPROVAL);
        assertThat(response.ownerUserId()).isEqualTo(42L);
        verify(casClient).assignCompany(42L, 100L);
    }

    @Test
    void registerCompanyRejectsDuplicateOwner() {
        when(companyRepository.existsByOwnerUserId(42L)).thenReturn(true);

        assertThatThrownBy(() -> service.registerCompany(owner, new CreateCompanyRequest(
                "204815936", "A", "A")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("фирма");
        verify(companyRepository, never()).save(any());
    }

    @Test
    void createSalonRequiresCompanyOwnership() {
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSalon(owner, 1L, new CreateSalonRequest(
                "Salon", null, 1L, "addr", null, null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("права");
    }

    @Test
    void addServiceRequiresExistingCategory() {
        Company company = Company.builder().id(1L).ownerUserId(42L).build();
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L)).thenReturn(Optional.of(company));
        when(salonRepository.findWithCityById(5L)).thenReturn(Optional.of(
                bg.rezerv.business.domain.Salon.builder().id(5L).companyId(1L).city(city).name("S").address("a").build()));
        when(serviceCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addService(owner, 5L, new CreateSalonServiceRequest(
                99L, "Масаж", 60, BigDecimal.TEN)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Категорията");
    }
}
