package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import bg.rezerv.business.TestcontainersConfiguration;
import bg.rezerv.business.client.CasClient;
import bg.rezerv.business.domain.CompanyStatus;
import bg.rezerv.business.repository.CityRepository;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.web.RequestContext;
import bg.rezerv.business.web.dto.CreateCompanyRequest;
import bg.rezerv.business.web.dto.CreateSalonPhotoRequest;
import bg.rezerv.business.web.dto.CreateSalonRequest;
import bg.rezerv.business.web.dto.CreateSalonServiceRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CompanyOnboardingIntegrationTest {

    private static final long NEW_OWNER_ID = 9_999L;

    @Autowired
    private CompanyOnboardingService onboardingService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @MockitoBean
    private CasClient casClient;

    @Test
    void fullOnboardingFlowCreatesCompanySalonServiceAndPhoto() {
        RequestContext ctx = new RequestContext(NEW_OWNER_ID, List.of("CLIENT"), null);

        var company = onboardingService.registerCompany(ctx, new CreateCompanyRequest(
                "175074752", "Нова фирма", "Nova firma EOOD", "nova@firma.bg", "+359888000111"));
        assertThat(company.status()).isEqualTo(CompanyStatus.PENDING_APPROVAL);
        assertThat(companyRepository.findByOwnerUserIdOrderByCreatedAtAsc(NEW_OWNER_ID)).hasSize(1);

        RequestContext admin = new RequestContext(1L, List.of("PLATFORM_ADMIN"), null);
        onboardingService.approveCompany(admin, company.id());

        Long sofiaId = cityRepository.findAll().stream()
                .filter(c -> c.getSlug().equals("sofia"))
                .findFirst().orElseThrow().getId();

        var salon = onboardingService.createSalon(ctx, company.id(), new CreateSalonRequest(
                "Моят салон", "Описание", sofiaId, "ул. Тест 1", 42.69, 23.32,
                "salon@example.bg", "+359888888888",
                java.util.List.of(
                        new bg.rezerv.business.web.dto.WorkingHoursDayRequest(
                                1, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(18, 0)),
                        new bg.rezerv.business.web.dto.WorkingHoursDayRequest(
                                2, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(18, 0)))));
        assertThat(salon.companyId()).isEqualTo(company.id());
        assertThat(salon.status()).isEqualTo(bg.rezerv.business.domain.SalonStatus.ACTIVE);
        assertThat(salon.workingHours()).hasSize(2);

        Long categoryId = serviceCategoryRepository.findAllByOrderByNameAsc().getFirst().getId();
        var service = onboardingService.addService(ctx, salon.id(), new CreateSalonServiceRequest(
                categoryId, "Тест услуга", 45, new BigDecimal("40.00")));
        assertThat(service.salonId()).isEqualTo(salon.id());

        var photo = onboardingService.addPhoto(ctx, salon.id(), new CreateSalonPhotoRequest(
                "https://example.com/salon.jpg", 0));
        assertThat(photo.url()).contains("example.com");
    }
}
