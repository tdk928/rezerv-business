package bg.rezerv.business.service;

import bg.rezerv.business.client.CasClient;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonClosure;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.domain.StaffMember;
import bg.rezerv.business.domain.StaffServiceLink;
import bg.rezerv.business.domain.TimeOff;
import bg.rezerv.business.domain.TimeOffRequest;
import bg.rezerv.business.domain.TimeOffRequestStatus;
import bg.rezerv.business.domain.TimeOffSource;
import bg.rezerv.business.domain.WorkingHours;
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
import bg.rezerv.business.web.dto.CreateSalonClosureRequest;
import bg.rezerv.business.web.dto.CreateTimeOffRequest;
import bg.rezerv.business.web.dto.ReplaceSalonWorkingHoursRequest;
import bg.rezerv.business.web.dto.ReplaceStaffServicesRequest;
import bg.rezerv.business.web.dto.SalonClosureResponse;
import bg.rezerv.business.web.dto.StaffMemberResponse;
import bg.rezerv.business.web.dto.TimeOffRequestResponse;
import bg.rezerv.business.web.dto.TimeOffResponse;
import bg.rezerv.business.web.dto.WorkingHoursResponse;
import bg.rezerv.business.web.error.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffManagementService {

    private static final String ROLE_STAFF = "STAFF";

    private final SalonRepository salonRepository;
    private final CompanyRepository companyRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final StaffServiceLinkRepository staffServiceLinkRepository;
    private final SalonServiceItemRepository salonServiceItemRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final SalonClosureRepository salonClosureRepository;
    private final TimeOffRepository timeOffRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final CasClient casClient;

    public StaffManagementService(SalonRepository salonRepository,
                                  CompanyRepository companyRepository,
                                  StaffMemberRepository staffMemberRepository,
                                  StaffServiceLinkRepository staffServiceLinkRepository,
                                  SalonServiceItemRepository salonServiceItemRepository,
                                  WorkingHoursRepository workingHoursRepository,
                                  SalonClosureRepository salonClosureRepository,
                                  TimeOffRepository timeOffRepository,
                                  TimeOffRequestRepository timeOffRequestRepository,
                                  CasClient casClient) {
        this.salonRepository = salonRepository;
        this.companyRepository = companyRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.staffServiceLinkRepository = staffServiceLinkRepository;
        this.salonServiceItemRepository = salonServiceItemRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.salonClosureRepository = salonClosureRepository;
        this.timeOffRepository = timeOffRepository;
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.casClient = casClient;
    }

    @Transactional
    public StaffMemberResponse addStaff(RequestContext ctx, Long salonId, AddStaffRequest request) {
        Salon salon = requireOwnedSalon(ctx, salonId);
        CasClient.UserSummary user = casClient.findByEmail(request.email().strip().toLowerCase());
        if (staffMemberRepository.existsBySalonIdAndUserId(salonId, user.id())) {
            throw new ApiException(HttpStatus.CONFLICT, "STAFF_ALREADY_EXISTS",
                    "Този потребител вече е служител в обекта");
        }
        casClient.assignStaff(user.id(), salon.getCompanyId());

        String displayName = request.displayName() != null && !request.displayName().isBlank()
                ? request.displayName().strip()
                : (user.firstName() + " " + user.lastName()).strip();

        StaffMember staff = staffMemberRepository.save(StaffMember.builder()
                .salonId(salonId)
                .userId(user.id())
                .displayName(displayName)
                .title(request.title() != null ? request.title().strip() : null)
                .active(true)
                .build());
        return toResponse(staff);
    }

    @Transactional
    public StaffMemberResponse createStaff(RequestContext ctx, Long salonId, CreateStaffRequest request) {
        Salon salon = requireOwnedSalon(ctx, salonId);
        CasClient.UserSummary user = casClient.createStaffUser(
                request.email().strip().toLowerCase(),
                request.password(),
                request.firstName().strip(),
                request.lastName().strip(),
                request.phone().strip(),
                salon.getCompanyId());

        if (staffMemberRepository.existsBySalonIdAndUserId(salonId, user.id())) {
            throw new ApiException(HttpStatus.CONFLICT, "STAFF_ALREADY_EXISTS",
                    "Този потребител вече е служител в обекта");
        }

        String displayName = request.displayName() != null && !request.displayName().isBlank()
                ? request.displayName().strip()
                : (user.firstName() + " " + user.lastName()).strip();

        StaffMember staff = staffMemberRepository.save(StaffMember.builder()
                .salonId(salonId)
                .userId(user.id())
                .displayName(displayName)
                .title(request.title() != null ? request.title().strip() : null)
                .active(true)
                .build());
        return toResponse(staff);
    }

    @Transactional(readOnly = true)
    public List<StaffMemberResponse> listStaff(RequestContext ctx, Long salonId) {
        requireOwnedSalon(ctx, salonId);
        return staffMemberRepository.findBySalonIdAndActiveTrueOrderByDisplayNameAsc(salonId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<WorkingHoursResponse> replaceStaffWorkingHours(RequestContext ctx,
                                                               Long staffId,
                                                               ReplaceSalonWorkingHoursRequest request) {
        StaffMember staff = requireOwnedStaff(ctx, staffId);
        List<WorkingHours> salonHours =
                workingHoursRepository.findBySalonIdAndStaffIdIsNullOrderByDayOfWeekAsc(staff.getSalonId());
        StaffScheduleValidator.validateWithinSalonHours(request.workingHours(), salonHours);

        workingHoursRepository.deleteBySalonIdAndStaffId(staff.getSalonId(), staff.getId());
        List<WorkingHours> saved = workingHoursRepository.saveAll(request.workingHours().stream()
                .map(day -> WorkingHours.builder()
                        .salonId(staff.getSalonId())
                        .staffId(staff.getId())
                        .dayOfWeek(day.dayOfWeek().shortValue())
                        .startTime(day.openTime())
                        .endTime(day.closeTime())
                        .build())
                .toList());
        return saved.stream().map(WorkingHoursResponse::from).toList();
    }

    @Transactional
    public StaffMemberResponse replaceStaffServices(RequestContext ctx,
                                                    Long staffId,
                                                    ReplaceStaffServicesRequest request) {
        StaffMember staff = requireOwnedStaff(ctx, staffId);
        List<Long> serviceIds = request.serviceIds() == null ? List.of() : request.serviceIds();
        if (!serviceIds.isEmpty()) {
            List<SalonServiceItem> services =
                    salonServiceItemRepository.findBySalonIdInAndActiveTrueOrderByNameAsc(List.of(staff.getSalonId()));
            var allowed = services.stream().map(SalonServiceItem::getId).collect(Collectors.toSet());
            for (Long serviceId : serviceIds) {
                if (!allowed.contains(serviceId)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SERVICE_NOT_IN_SALON",
                            "Услуга " + serviceId + " не принадлежи на обекта");
                }
            }
        }
        staffServiceLinkRepository.deleteByStaffId(staffId);
        if (!serviceIds.isEmpty()) {
            staffServiceLinkRepository.saveAll(serviceIds.stream()
                    .distinct()
                    .map(id -> StaffServiceLink.builder().staffId(staffId).serviceId(id).build())
                    .toList());
        }
        return toResponse(staff);
    }

    @Transactional
    public SalonClosureResponse closeSalonDay(RequestContext ctx, Long salonId, CreateSalonClosureRequest request) {
        requireOwnedSalon(ctx, salonId);
        if (salonClosureRepository.existsBySalonIdAndClosedOn(salonId, request.closedOn())) {
            throw new ApiException(HttpStatus.CONFLICT, "SALON_ALREADY_CLOSED",
                    "Обектът вече е затворен за тази дата");
        }
        SalonClosure closure = salonClosureRepository.save(SalonClosure.builder()
                .salonId(salonId)
                .closedOn(request.closedOn())
                .reason(request.reason())
                .createdBy(ctx.userId())
                .build());
        return SalonClosureResponse.from(closure);
    }

    @Transactional(readOnly = true)
    public List<SalonClosureResponse> listClosures(RequestContext ctx, Long salonId, LocalDate from, LocalDate to) {
        requireOwnedSalon(ctx, salonId);
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start.plusMonths(2);
        return salonClosureRepository.findBySalonIdAndClosedOnBetweenOrderByClosedOnAsc(salonId, start, end).stream()
                .map(SalonClosureResponse::from)
                .toList();
    }

    @Transactional
    public void deleteClosure(RequestContext ctx, Long salonId, Long closureId) {
        requireOwnedSalon(ctx, salonId);
        SalonClosure closure = salonClosureRepository.findByIdAndSalonId(closureId, salonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CLOSURE_NOT_FOUND",
                        "Затварянето не е намерено"));
        salonClosureRepository.delete(closure);
    }

    @Transactional
    public TimeOffResponse createOwnerTimeOff(RequestContext ctx, Long staffId, CreateTimeOffRequest request) {
        StaffMember staff = requireOwnedStaff(ctx, staffId);
        validateRange(request.startsAt(), request.endsAt());
        TimeOff timeOff = timeOffRepository.save(TimeOff.builder()
                .staffId(staff.getId())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .reason(request.reason())
                .source(TimeOffSource.OWNER)
                .createdBy(ctx.userId())
                .build());
        return TimeOffResponse.from(timeOff);
    }

    @Transactional(readOnly = true)
    public List<TimeOffResponse> listTimeOff(RequestContext ctx, Long staffId) {
        requireOwnedStaff(ctx, staffId);
        return timeOffRepository.findByStaffIdOrderByStartsAtAsc(staffId).stream()
                .map(TimeOffResponse::from)
                .toList();
    }

    @Transactional
    public TimeOffRequestResponse createMyTimeOffRequest(RequestContext ctx, CreateTimeOffRequest request) {
        requireStaffRole(ctx);
        StaffMember staff = requireActiveStaffForUser(ctx.userId());
        validateRange(request.startsAt(), request.endsAt());
        TimeOffRequest entity = timeOffRequestRepository.save(TimeOffRequest.builder()
                .staffId(staff.getId())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .reason(request.reason())
                .status(TimeOffRequestStatus.PENDING)
                .build());
        return TimeOffRequestResponse.from(entity);
    }

    @Transactional(readOnly = true)
    public List<TimeOffRequestResponse> listMyTimeOffRequests(RequestContext ctx) {
        requireStaffRole(ctx);
        List<StaffMember> memberships = staffMemberRepository.findByUserIdAndActiveTrue(ctx.userId());
        return memberships.stream()
                .flatMap(s -> timeOffRequestRepository.findByStaffIdOrderByCreatedAtDesc(s.getId()).stream())
                .map(TimeOffRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeOffRequestResponse> listPendingRequestsForSalon(RequestContext ctx, Long salonId) {
        requireOwnedSalon(ctx, salonId);
        List<Long> staffIds = staffMemberRepository.findBySalonIdAndActiveTrueOrderByDisplayNameAsc(salonId).stream()
                .map(StaffMember::getId)
                .toList();
        if (staffIds.isEmpty()) {
            return List.of();
        }
        return timeOffRequestRepository
                .findByStaffIdInAndStatusOrderByCreatedAtAsc(staffIds, TimeOffRequestStatus.PENDING).stream()
                .map(TimeOffRequestResponse::from)
                .toList();
    }

    @Transactional
    public TimeOffRequestResponse approveRequest(RequestContext ctx, Long requestId) {
        TimeOffRequest request = timeOffRequestRepository
                .findByIdAndStatus(requestId, TimeOffRequestStatus.PENDING)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND",
                        "Заявката не е намерена или вече е обработена"));
        requireOwnedStaff(ctx, request.getStaffId());

        request.setStatus(TimeOffRequestStatus.APPROVED);
        request.setReviewedBy(ctx.userId());
        request.setReviewedAt(Instant.now());
        timeOffRequestRepository.save(request);

        timeOffRepository.save(TimeOff.builder()
                .staffId(request.getStaffId())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .reason(request.getReason())
                .source(TimeOffSource.REQUEST)
                .createdBy(ctx.userId())
                .build());
        return TimeOffRequestResponse.from(request);
    }

    @Transactional
    public TimeOffRequestResponse rejectRequest(RequestContext ctx, Long requestId) {
        TimeOffRequest request = timeOffRequestRepository
                .findByIdAndStatus(requestId, TimeOffRequestStatus.PENDING)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND",
                        "Заявката не е намерена или вече е обработена"));
        requireOwnedStaff(ctx, request.getStaffId());
        request.setStatus(TimeOffRequestStatus.REJECTED);
        request.setReviewedBy(ctx.userId());
        request.setReviewedAt(Instant.now());
        return TimeOffRequestResponse.from(timeOffRequestRepository.save(request));
    }

    private StaffMemberResponse toResponse(StaffMember staff) {
        List<Long> serviceIds = staffServiceLinkRepository.findByStaffId(staff.getId()).stream()
                .map(StaffServiceLink::getServiceId)
                .toList();
        List<WorkingHours> hours =
                workingHoursRepository.findBySalonIdAndStaffIdOrderByDayOfWeekAsc(staff.getSalonId(), staff.getId());
        return StaffMemberResponse.from(staff, serviceIds, hours);
    }

    private static void validateRange(Instant startsAt, Instant endsAt) {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE",
                    "Краят трябва да е след началото");
        }
    }

    private StaffMember requireOwnedStaff(RequestContext ctx, Long staffId) {
        StaffMember staff = staffMemberRepository.findById(staffId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND",
                        "Служителят не е намерен"));
        requireOwnedSalon(ctx, staff.getSalonId());
        return staff;
    }

    private Salon requireOwnedSalon(RequestContext ctx, Long salonId) {
        requireAuthenticated(ctx);
        Salon salon = salonRepository.findWithCityById(salonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SALON_NOT_FOUND", "Салонът не е намерен"));
        companyRepository.findByIdAndOwnerUserId(salon.getCompanyId(), ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NOT_COMPANY_OWNER",
                        "Нямате права върху тази фирма"));
        return salon;
    }

    private StaffMember requireActiveStaffForUser(Long userId) {
        List<StaffMember> memberships = staffMemberRepository.findByUserIdAndActiveTrue(userId);
        if (memberships.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_STAFF",
                    "Не сте активен служител в обект");
        }
        // За MVP: първият активен membership (един user → един основен обект за заявки).
        return memberships.getFirst();
    }

    private static void requireAuthenticated(RequestContext ctx) {
        if (!ctx.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Изисква се автентикация");
        }
    }

    private static void requireStaffRole(RequestContext ctx) {
        requireAuthenticated(ctx);
        if (!ctx.hasRole(ROLE_STAFF)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Изисква се роля STAFF");
        }
    }
}
