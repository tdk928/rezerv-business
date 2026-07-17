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
        when(eikValidator.isValid("204815936")).thenReturn(true);
        when(companyRepository.existsByEik("204815936")).thenReturn(false);
        when(companyRepository.save(any())).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        var response = service.registerCompany(owner, new CreateCompanyRequest(
                "204815936", "Тест ООД", "Test EOOD", "office@test.bg", "+359888111222"));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(CompanyStatus.PENDING_APPROVAL);
        assertThat(response.ownerUserId()).isEqualTo(42L);
        verify(casClient).assignCompany(42L, 100L);
    }

    @Test
    void registerCompanyAllowsSecondCompanyForSameOwner() {
        when(eikValidator.isValid("131529327")).thenReturn(true);
        when(companyRepository.existsByEik("131529327")).thenReturn(false);
        when(companyRepository.save(any())).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(200L);
            return c;
        });

        var response = service.registerCompany(owner, new CreateCompanyRequest(
                "131529327", "Втора ООД", "Second EOOD", "second@test.bg", "+359888333444"));

        assertThat(response.id()).isEqualTo(200L);
        verify(casClient).assignCompany(42L, 200L);
    }

    @Test
    void createSalonRequiresCompanyOwnership() {
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSalon(owner, 1L, new CreateSalonRequest(
                "Salon", null, 1L, "addr", null, null, "salon@example.bg", "+359888888888")))
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

    @Test
    void listMyCompaniesIncludesSalons() {
        Company company = Company.builder()
                .id(10L)
                .eik("131529327")
                .name("Фирма")
                .legalName("Firma")
                .ownerUserId(42L)
                .status(CompanyStatus.PENDING_APPROVAL)
                .build();
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        var salon = bg.rezerv.business.domain.Salon.builder()
                .id(3L)
                .companyId(10L)
                .name("Обект 1")
                .city(city)
                .address("ул. 1")
                .email("a@b.bg")
                .phone("+359")
                .status(bg.rezerv.business.domain.SalonStatus.ACTIVE)
                .build();
        when(companyRepository.findByOwnerUserIdOrderByCreatedAtAsc(42L)).thenReturn(List.of(company));
        when(salonRepository.findByCompanyIdInOrderByNameAsc(List.of(10L))).thenReturn(List.of(salon));

        var result = service.listMyCompanies(owner);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().salons()).hasSize(1);
        assertThat(result.getFirst().salons().getFirst().name()).isEqualTo("Обект 1");
    }

    @Test
    void listAllCompaniesForAdminRequiresPlatformAdmin() {
        assertThatThrownBy(() -> service.listAllCompaniesForAdmin(owner))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("PLATFORM_ADMIN");
    }

    @Test
    void createSalonIsInactiveWhenCompanyPendingApproval() {
        Company company = Company.builder()
                .id(1L)
                .ownerUserId(42L)
                .status(CompanyStatus.PENDING_APPROVAL)
                .build();
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L)).thenReturn(Optional.of(company));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(salonRepository.save(any())).thenAnswer(inv -> {
            bg.rezerv.business.domain.Salon s = inv.getArgument(0);
            s.setId(9L);
            return s;
        });

        var response = service.createSalon(owner, 1L, new CreateSalonRequest(
                "Обект", null, 1L, "addr", null, null, "salon@example.bg", "+359888888888"));

        assertThat(response.status()).isEqualTo(bg.rezerv.business.domain.SalonStatus.INACTIVE);
    }

    @Test
    void createSalonIsActiveWhenCompanyApproved() {
        Company company = Company.builder()
                .id(1L)
                .ownerUserId(42L)
                .status(CompanyStatus.APPROVED)
                .build();
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L)).thenReturn(Optional.of(company));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(salonRepository.save(any())).thenAnswer(inv -> {
            bg.rezerv.business.domain.Salon s = inv.getArgument(0);
            s.setId(9L);
            return s;
        });

        var response = service.createSalon(owner, 1L, new CreateSalonRequest(
                "Обект", null, 1L, "addr", null, null, "salon@example.bg", "+359888888888"));

        assertThat(response.status()).isEqualTo(bg.rezerv.business.domain.SalonStatus.ACTIVE);
    }

    @Test
    void resolveSalonStatusForCompany_pendingIsInactive() {
        Company pending = Company.builder().status(CompanyStatus.PENDING_APPROVAL).build();
        Company approved = Company.builder().status(CompanyStatus.APPROVED).build();
        assertThat(CompanyOnboardingService.resolveSalonStatusForCompany(pending))
                .isEqualTo(bg.rezerv.business.domain.SalonStatus.INACTIVE);
        assertThat(CompanyOnboardingService.resolveSalonStatusForCompany(approved))
                .isEqualTo(bg.rezerv.business.domain.SalonStatus.ACTIVE);
    }

    @Test
    void listAllCompaniesForAdminEnrichesOwnersFromCas() {
        RequestContext admin = new RequestContext(1L, List.of("PLATFORM_ADMIN"), null);
        Company company = Company.builder()
                .id(7L)
                .eik("100000001")
                .name("Админ фирма")
                .legalName("Admin")
                .email("a@b.bg")
                .phone("+359")
                .ownerUserId(42L)
                .status(CompanyStatus.APPROVED)
                .build();
        when(companyRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(company));
        when(casClient.lookupUsers(List.of(42L))).thenReturn(List.of(
                new CasClient.UserSummary(42L, "ivan@example.bg", "Иван", "Иванов")));

        var result = service.listAllCompaniesForAdmin(admin);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().owner().email()).isEqualTo("ivan@example.bg");
        assertThat(result.getFirst().name()).isEqualTo("Админ фирма");
    }

    @Test
    void approveCompanySetsApprovedAndActivatesInactiveSalons() {
        RequestContext admin = new RequestContext(1L, List.of("PLATFORM_ADMIN"), null);
        Company company = Company.builder()
                .id(7L)
                .eik("100000001")
                .name("Чакаща")
                .legalName("Pending")
                .email("a@b.bg")
                .phone("+359")
                .ownerUserId(42L)
                .status(CompanyStatus.PENDING_APPROVAL)
                .build();
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        var inactiveSalon = bg.rezerv.business.domain.Salon.builder()
                .id(3L)
                .companyId(7L)
                .name("Обект")
                .city(city)
                .address("а")
                .email("s@b.bg")
                .phone("+359")
                .status(bg.rezerv.business.domain.SalonStatus.INACTIVE)
                .build();
        when(companyRepository.findById(7L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(salonRepository.findByCompanyId(7L)).thenReturn(List.of(inactiveSalon));
        when(salonRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(casClient.lookupUsers(List.of(42L))).thenReturn(List.of(
                new CasClient.UserSummary(42L, "ivan@example.bg", "Иван", "Иванов")));

        var result = service.approveCompany(admin, 7L);

        assertThat(result.status()).isEqualTo(CompanyStatus.APPROVED);
        assertThat(inactiveSalon.getStatus()).isEqualTo(bg.rezerv.business.domain.SalonStatus.ACTIVE);
    }

    @Test
    void approveCompanyRequiresPlatformAdmin() {
        assertThatThrownBy(() -> service.approveCompany(owner, 7L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("PLATFORM_ADMIN");
    }
}

