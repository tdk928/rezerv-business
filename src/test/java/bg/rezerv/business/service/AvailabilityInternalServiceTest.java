package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import bg.rezerv.business.domain.City;
import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.StaffMember;
import bg.rezerv.business.domain.WorkingHours;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.SalonClosureRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.repository.StaffMemberRepository;
import bg.rezerv.business.repository.StaffServiceLinkRepository;
import bg.rezerv.business.repository.TimeOffRepository;
import bg.rezerv.business.repository.WorkingHoursRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvailabilityInternalServiceTest {

    @Mock private SalonRepository salonRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private WorkingHoursRepository workingHoursRepository;
    @Mock private SalonClosureRepository salonClosureRepository;
    @Mock private StaffMemberRepository staffMemberRepository;
    @Mock private StaffServiceLinkRepository staffServiceLinkRepository;
    @Mock private TimeOffRepository timeOffRepository;
    @Mock private SalonServiceItemRepository salonServiceItemRepository;

    @InjectMocks
    private AvailabilityInternalService service;

    @Test
    void availabilityContextReturnsSalonHoursAndStaff() {
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        Salon salon = Salon.builder().id(5L).companyId(1L).city(city).name("S").address("a").build();
        when(salonRepository.findWithCityById(5L)).thenReturn(Optional.of(salon));
        when(workingHoursRepository.findBySalonIdAndStaffIdIsNullOrderByDayOfWeekAsc(5L))
                .thenReturn(List.of(WorkingHours.builder()
                        .dayOfWeek((short) 1)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .build()));
        when(salonClosureRepository.findBySalonIdAndClosedOnBetweenOrderByClosedOnAsc(
                5L, LocalDate.of(2026, 7, 19), LocalDate.of(2026, 8, 19)))
                .thenReturn(List.of());
        when(staffMemberRepository.findBySalonIdAndActiveTrueOrderByDisplayNameAsc(5L))
                .thenReturn(List.of(StaffMember.builder().id(10L).salonId(5L).userId(9L)
                        .displayName("Мария").active(true).build()));
        when(staffServiceLinkRepository.findByStaffIdIn(List.of(10L))).thenReturn(List.of());
        when(workingHoursRepository.findBySalonIdAndStaffIdIsNotNullOrderByStaffIdAscDayOfWeekAsc(5L))
                .thenReturn(List.of());
        when(timeOffRepository.findOverlapping(org.mockito.ArgumentMatchers.eq(List.of(10L)),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
        when(salonServiceItemRepository.findBySalonIdAndActiveTrueOrderByNameAsc(5L)).thenReturn(List.of());

        var ctx = service.availabilityContext(5L, LocalDate.of(2026, 7, 19), LocalDate.of(2026, 8, 19));

        assertThat(ctx.salonId()).isEqualTo(5L);
        assertThat(ctx.salonWorkingHours()).hasSize(1);
        assertThat(ctx.staff()).hasSize(1);
        assertThat(ctx.staff().getFirst().displayName()).isEqualTo("Мария");
    }

    @Test
    void actorDetectsOwnerAndStaff() {
        City city = City.builder().id(1L).name("София").slug("sofia").build();
        when(salonRepository.findWithCityById(5L)).thenReturn(Optional.of(
                Salon.builder().id(5L).companyId(1L).city(city).name("S").address("a").build()));
        when(companyRepository.findByIdAndOwnerUserId(1L, 42L))
                .thenReturn(Optional.of(Company.builder().id(1L).ownerUserId(42L).build()));
        when(staffMemberRepository.findByUserIdAndSalonIdAndActiveTrue(42L, 5L))
                .thenReturn(Optional.of(StaffMember.builder().id(10L).salonId(5L).userId(42L)
                        .displayName("OwnerStaff").active(true).build()));

        var actor = service.actor(5L, 42L);

        assertThat(actor.owner()).isTrue();
        assertThat(actor.staffMemberId()).isEqualTo(10L);
    }
}
