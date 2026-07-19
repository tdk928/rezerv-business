package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bg.rezerv.business.client.CasClient;
import bg.rezerv.business.domain.City;
import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.StaffMember;
import bg.rezerv.business.domain.TimeOffRequest;
import bg.rezerv.business.domain.TimeOffRequestStatus;
import bg.rezerv.business.domain.TimeOffSource;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.SalonClosureRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.repository.StaffMemberRepository;
import bg.rezerv.business.repository.StaffServiceLinkRepository;
import bg.rezerv.business.repository.TimeOffRepository;
import bg.rezerv.business.repository.TimeOffRequestRepository;
import bg.rezerv.business.repository.WorkingHoursRepository;
import bg.rezerv.business.web.RequestContext;
import bg.rezerv.business.web.dto.AddStaffRequest;
import bg.rezerv.business.web.dto.CreateStaffRequest;
import bg.rezerv.business.web.dto.CreateTimeOffRequest;
import bg.rezerv.business.web.error.ApiException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StaffManagementServiceTest {

    @Mock private SalonRepository salonRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private StaffMemberRepository staffMemberRepository;
    @Mock private StaffServiceLinkRepository staffServiceLinkRepository;
    @Mock private SalonServiceItemRepository salonServiceItemRepository;
    @Mock private WorkingHoursRepository workingHoursRepository;
    @Mock private SalonClosureRepository salonClosureRepository;
    @Mock private TimeOffRepository timeOffRepository;
    @Mock private TimeOffRequestRepository timeOffRequestRepository;
    @Mock private CasClient casClient;

    @InjectMocks
    private StaffManagementService service;

    private final RequestContext owner = new RequestContext(42L, List.of("BUSINESS_OWNER"), 1L);
    private final City sofia = City.builder().id(1L).name("София").slug("sofia").build();

    @Test
    void addStaffLooksUpCasAssignsRoleAndPersists() {
        Salon salon = Salon.builder().id(5L).companyId(1L).city(sofia).name("S").address("a").build();
        when(salonRepository.findWithCityById(5L)).thenReturn(Optional.of(salon));
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L))
                .thenReturn(Optional.of(Company.builder().id(1L).ownerUserId(42L).build()));
        when(casClient.findByEmail("maria@example.bg"))
                .thenReturn(new CasClient.UserSummary(9L, "maria@example.bg", "Мария", "Петрова"));
        when(staffMemberRepository.existsBySalonIdAndUserId(5L, 9L)).thenReturn(false);
        when(staffMemberRepository.save(any())).thenAnswer(inv -> {
            StaffMember s = inv.getArgument(0);
            s.setId(77L);
            return s;
        });
        when(staffServiceLinkRepository.findByStaffId(77L)).thenReturn(List.of());
        when(workingHoursRepository.findBySalonIdAndStaffIdOrderByDayOfWeekAsc(5L, 77L)).thenReturn(List.of());

        var response = service.addStaff(owner, 5L, new AddStaffRequest("maria@example.bg", null, "Фризьор"));

        assertThat(response.id()).isEqualTo(77L);
        assertThat(response.userId()).isEqualTo(9L);
        assertThat(response.displayName()).isEqualTo("Мария Петрова");
        verify(casClient).assignStaff(9L, 1L);
    }

    @Test
    void createStaffProvisionsCasUserAndPersists() {
        Salon salon = Salon.builder().id(5L).companyId(1L).city(sofia).name("S").address("a").build();
        when(salonRepository.findWithCityById(5L)).thenReturn(Optional.of(salon));
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L))
                .thenReturn(Optional.of(Company.builder().id(1L).ownerUserId(42L).build()));
        when(casClient.createStaffUser(
                eq("new@example.bg"), eq("parola123"), eq("Иван"), eq("Иванов"),
                eq("+359888123456"), eq(1L)))
                .thenReturn(new CasClient.UserSummary(11L, "new@example.bg", "Иван", "Иванов"));
        when(staffMemberRepository.existsBySalonIdAndUserId(5L, 11L)).thenReturn(false);
        when(staffMemberRepository.save(any())).thenAnswer(inv -> {
            StaffMember s = inv.getArgument(0);
            s.setId(88L);
            return s;
        });
        when(staffServiceLinkRepository.findByStaffId(88L)).thenReturn(List.of());
        when(workingHoursRepository.findBySalonIdAndStaffIdOrderByDayOfWeekAsc(5L, 88L)).thenReturn(List.of());

        var response = service.createStaff(owner, 5L, new CreateStaffRequest(
                "new@example.bg", "parola123", "Иван", "Иванов", "+359888123456", null, "Гримьор"));

        assertThat(response.id()).isEqualTo(88L);
        assertThat(response.displayName()).isEqualTo("Иван Иванов");
        assertThat(response.title()).isEqualTo("Гримьор");
    }

    @Test
    void createMyTimeOffRequestRequiresStaffRole() {
        RequestContext client = new RequestContext(9L, List.of("CLIENT"), null);
        assertThatThrownBy(() -> service.createMyTimeOffRequest(client, new CreateTimeOffRequest(
                Instant.parse("2026-08-01T09:00:00Z"),
                Instant.parse("2026-08-05T18:00:00Z"),
                "отпуск")))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).code())
                .isEqualTo("FORBIDDEN");
    }

    @Test
    void approveRequestCreatesTimeOff() {
        StaffMember staff = StaffMember.builder().id(77L).salonId(5L).userId(9L).displayName("M").active(true).build();
        Salon salon = Salon.builder().id(5L).companyId(1L).city(sofia).name("S").address("a").build();
        TimeOffRequest pending = TimeOffRequest.builder()
                .id(3L)
                .staffId(77L)
                .startsAt(Instant.parse("2026-08-01T09:00:00Z"))
                .endsAt(Instant.parse("2026-08-05T18:00:00Z"))
                .reason("отпуск")
                .status(TimeOffRequestStatus.PENDING)
                .build();
        when(timeOffRequestRepository.findByIdAndStatus(3L, TimeOffRequestStatus.PENDING))
                .thenReturn(Optional.of(pending));
        when(staffMemberRepository.findById(77L)).thenReturn(Optional.of(staff));
        when(salonRepository.findWithCityById(5L)).thenReturn(Optional.of(salon));
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L))
                .thenReturn(Optional.of(Company.builder().id(1L).ownerUserId(42L).build()));
        when(timeOffRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timeOffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = service.approveRequest(owner, 3L);

        assertThat(response.status()).isEqualTo(TimeOffRequestStatus.APPROVED);
        verify(timeOffRepository).save(org.mockito.ArgumentMatchers.argThat(t ->
                t.getSource() == TimeOffSource.REQUEST && t.getStaffId().equals(77L)));
    }
}
