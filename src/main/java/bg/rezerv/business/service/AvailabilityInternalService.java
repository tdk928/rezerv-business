package bg.rezerv.business.service;

import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonClosure;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.domain.StaffMember;
import bg.rezerv.business.domain.StaffServiceLink;
import bg.rezerv.business.domain.TimeOff;
import bg.rezerv.business.domain.WorkingHours;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.SalonClosureRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.repository.StaffMemberRepository;
import bg.rezerv.business.repository.StaffServiceLinkRepository;
import bg.rezerv.business.repository.TimeOffRepository;
import bg.rezerv.business.repository.WorkingHoursRepository;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse.ClosureDay;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse.DayHours;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse.ServiceInfo;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse.StaffAvailability;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse.TimeOffInterval;
import bg.rezerv.business.web.dto.internal.SalonActorResponse;
import bg.rezerv.business.web.error.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvailabilityInternalService {

    public static final ZoneId ZONE = ZoneId.of("Europe/Sofia");

    private final SalonRepository salonRepository;
    private final CompanyRepository companyRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final SalonClosureRepository salonClosureRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final StaffServiceLinkRepository staffServiceLinkRepository;
    private final TimeOffRepository timeOffRepository;
    private final SalonServiceItemRepository salonServiceItemRepository;

    public AvailabilityInternalService(SalonRepository salonRepository,
                                       CompanyRepository companyRepository,
                                       WorkingHoursRepository workingHoursRepository,
                                       SalonClosureRepository salonClosureRepository,
                                       StaffMemberRepository staffMemberRepository,
                                       StaffServiceLinkRepository staffServiceLinkRepository,
                                       TimeOffRepository timeOffRepository,
                                       SalonServiceItemRepository salonServiceItemRepository) {
        this.salonRepository = salonRepository;
        this.companyRepository = companyRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.salonClosureRepository = salonClosureRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.staffServiceLinkRepository = staffServiceLinkRepository;
        this.timeOffRepository = timeOffRepository;
        this.salonServiceItemRepository = salonServiceItemRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityContextResponse availabilityContext(Long salonId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE",
                    "Невалиден период from/to");
        }
        Salon salon = salonRepository.findWithCityById(salonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SALON_NOT_FOUND", "Салонът не е намерен"));

        List<DayHours> salonHours = workingHoursRepository
                .findBySalonIdAndStaffIdIsNullOrderByDayOfWeekAsc(salonId).stream()
                .map(this::toDayHours)
                .toList();

        List<ClosureDay> closures = salonClosureRepository
                .findBySalonIdAndClosedOnBetweenOrderByClosedOnAsc(salonId, from, to).stream()
                .map(c -> new ClosureDay(c.getClosedOn(), c.getReason()))
                .toList();

        List<StaffMember> staff = staffMemberRepository.findBySalonIdAndActiveTrueOrderByDisplayNameAsc(salonId);
        List<Long> staffIds = staff.stream().map(StaffMember::getId).toList();

        Map<Long, List<Long>> servicesByStaff = staffIds.isEmpty()
                ? Map.of()
                : staffServiceLinkRepository.findByStaffIdIn(staffIds).stream()
                        .collect(Collectors.groupingBy(
                                StaffServiceLink::getStaffId,
                                Collectors.mapping(StaffServiceLink::getServiceId, Collectors.toList())));

        Map<Long, List<DayHours>> hoursByStaff = new LinkedHashMap<>();
        for (WorkingHours wh : workingHoursRepository
                .findBySalonIdAndStaffIdIsNotNullOrderByStaffIdAscDayOfWeekAsc(salonId)) {
            hoursByStaff
                    .computeIfAbsent(wh.getStaffId(), id -> new ArrayList<>())
                    .add(toDayHours(wh));
        }

        Instant fromInstant = from.atStartOfDay(ZONE).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZONE).toInstant();
        Map<Long, List<TimeOffInterval>> timeOffByStaff = new LinkedHashMap<>();
        if (!staffIds.isEmpty()) {
            for (TimeOff t : timeOffRepository.findOverlapping(staffIds, fromInstant, toInstant)) {
                timeOffByStaff
                        .computeIfAbsent(t.getStaffId(), id -> new ArrayList<>())
                        .add(new TimeOffInterval(t.getStartsAt(), t.getEndsAt()));
            }
        }

        List<StaffAvailability> staffAvailability = staff.stream()
                .map(s -> new StaffAvailability(
                        s.getId(),
                        s.getDisplayName(),
                        servicesByStaff.getOrDefault(s.getId(), List.of()),
                        hoursByStaff.getOrDefault(s.getId(), List.of()),
                        timeOffByStaff.getOrDefault(s.getId(), List.of())))
                .toList();

        List<ServiceInfo> services = salonServiceItemRepository
                .findBySalonIdAndActiveTrueOrderByNameAsc(salonId).stream()
                .map(sv -> new ServiceInfo(
                        sv.getId(),
                        sv.getName(),
                        sv.getDurationMin(),
                        sv.getPrice(),
                        Boolean.TRUE.equals(sv.getActive())))
                .toList();

        return new AvailabilityContextResponse(
                salon.getId(),
                ZONE.getId(),
                salonHours,
                closures,
                staffAvailability,
                services);
    }

    @Transactional(readOnly = true)
    public SalonActorResponse actor(Long salonId, Long userId) {
        Salon salon = salonRepository.findWithCityById(salonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SALON_NOT_FOUND", "Салонът не е намерен"));
        boolean owner = companyRepository.findByIdAndOwnerUserId(salon.getCompanyId(), userId).isPresent();
        Long staffId = staffMemberRepository.findByUserIdAndSalonIdAndActiveTrue(userId, salonId)
                .map(StaffMember::getId)
                .orElse(null);
        return new SalonActorResponse(salonId, userId, owner, staffId);
    }

    private DayHours toDayHours(WorkingHours wh) {
        return new DayHours(wh.getDayOfWeek(), wh.getStartTime(), wh.getEndTime());
    }
}
