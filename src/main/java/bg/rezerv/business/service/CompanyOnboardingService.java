package bg.rezerv.business.service;

import bg.rezerv.business.client.CasClient;
import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.CompanyStatus;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonPhoto;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.domain.SalonStatus;
import bg.rezerv.business.domain.WorkingHours;
import bg.rezerv.business.repository.CityRepository;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.SalonPhotoRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.repository.WorkingHoursRepository;
import bg.rezerv.business.web.RequestContext;
import bg.rezerv.business.web.dto.AdminCompanyResponse;
import bg.rezerv.business.web.dto.CompanyResponse;
import bg.rezerv.business.web.dto.CompanyWithSalonsResponse;
import bg.rezerv.business.web.dto.CreateCompanyRequest;
import bg.rezerv.business.web.dto.CreateSalonPhotoRequest;
import bg.rezerv.business.web.dto.CreateSalonRequest;
import bg.rezerv.business.web.dto.CreateSalonServiceRequest;
import bg.rezerv.business.web.dto.ReplaceSalonWorkingHoursRequest;
import bg.rezerv.business.web.dto.SalonPhotoResponse;
import bg.rezerv.business.web.dto.SalonResponse;
import bg.rezerv.business.web.dto.SalonServiceResponse;
import bg.rezerv.business.web.dto.WorkingHoursDayRequest;
import bg.rezerv.business.web.dto.WorkingHoursResponse;
import bg.rezerv.business.web.error.ApiException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyOnboardingService {

    private static final String ROLE_PLATFORM_ADMIN = "PLATFORM_ADMIN";

    private final CompanyRepository companyRepository;
    private final SalonRepository salonRepository;
    private final SalonServiceItemRepository salonServiceItemRepository;
    private final SalonPhotoRepository salonPhotoRepository;
    private final CityRepository cityRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final EikValidator eikValidator;
    private final CasClient casClient;

    public CompanyOnboardingService(CompanyRepository companyRepository,
                                    SalonRepository salonRepository,
                                    SalonServiceItemRepository salonServiceItemRepository,
                                    SalonPhotoRepository salonPhotoRepository,
                                    CityRepository cityRepository,
                                    ServiceCategoryRepository serviceCategoryRepository,
                                    WorkingHoursRepository workingHoursRepository,
                                    EikValidator eikValidator,
                                    CasClient casClient) {
        this.companyRepository = companyRepository;
        this.salonRepository = salonRepository;
        this.salonServiceItemRepository = salonServiceItemRepository;
        this.salonPhotoRepository = salonPhotoRepository;
        this.cityRepository = cityRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.eikValidator = eikValidator;
        this.casClient = casClient;
    }

    @Transactional
    public CompanyResponse registerCompany(RequestContext ctx, CreateCompanyRequest request) {
        requireAuthenticated(ctx);
        if (!eikValidator.isValid(request.eik())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EIK_INVALID", "Невалиден ЕИК (контролна сума)");
        }
        if (companyRepository.existsByEik(request.eik())) {
            throw new ApiException(HttpStatus.CONFLICT, "EIK_ALREADY_REGISTERED", "ЕИК вече е регистриран");
        }

        Company company = Company.builder()
                .eik(request.eik())
                .name(request.name())
                .legalName(request.legalName())
                .email(request.email().strip().toLowerCase())
                .phone(request.phone().strip())
                .ownerUserId(ctx.userId())
                .status(CompanyStatus.PENDING_APPROVAL)
                .build();
        company = companyRepository.save(company);
        casClient.assignCompany(ctx.userId(), company.getId());
        return CompanyResponse.from(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyWithSalonsResponse> listMyCompanies(RequestContext ctx) {
        requireAuthenticated(ctx);
        List<Company> companies = companyRepository.findByOwnerUserIdOrderByCreatedAtAsc(ctx.userId());
        if (companies.isEmpty()) {
            return List.of();
        }
        List<Long> companyIds = companies.stream().map(Company::getId).toList();
        List<Salon> salons = salonRepository.findByCompanyIdInOrderByNameAsc(companyIds);
        Map<Long, List<Salon>> salonsByCompany = salons.stream()
                .collect(Collectors.groupingBy(Salon::getCompanyId, LinkedHashMap::new, Collectors.toList()));
        List<Long> salonIds = salons.stream().map(Salon::getId).toList();
        Map<Long, List<SalonServiceItem>> servicesBySalon = salonIds.isEmpty()
                ? Map.of()
                : salonServiceItemRepository.findBySalonIdInAndActiveTrueOrderByNameAsc(salonIds).stream()
                        .collect(Collectors.groupingBy(SalonServiceItem::getSalonId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<WorkingHours>> hoursBySalon = salonIds.isEmpty()
                ? Map.of()
                : workingHoursRepository.findBySalonIdInAndStaffIdIsNullOrderBySalonIdAscDayOfWeekAsc(salonIds).stream()
                        .collect(Collectors.groupingBy(WorkingHours::getSalonId, LinkedHashMap::new, Collectors.toList()));
        return companies.stream()
                .map(c -> CompanyWithSalonsResponse.from(
                        c, salonsByCompany.getOrDefault(c.getId(), List.of()), servicesBySalon, hoursBySalon))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCompanyResponse> listAllCompaniesForAdmin(RequestContext ctx) {
        requireAuthenticated(ctx);
        requirePlatformAdmin(ctx);
        List<Company> companies = companyRepository.findAllByOrderByCreatedAtDesc();
        if (companies.isEmpty()) {
            return List.of();
        }
        List<Long> ownerIds = companies.stream()
                .map(Company::getOwnerUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, CasClient.UserSummary> ownersById = casClient.lookupUsers(ownerIds).stream()
                .collect(Collectors.toMap(CasClient.UserSummary::id, u -> u, (a, b) -> a));
        return companies.stream()
                .map(c -> {
                    CasClient.UserSummary owner = ownersById.get(c.getOwnerUserId());
                    AdminCompanyResponse.OwnerSummary summary = owner != null
                            ? AdminCompanyResponse.OwnerSummary.from(owner)
                            : AdminCompanyResponse.OwnerSummary.unknown(c.getOwnerUserId());
                    return AdminCompanyResponse.from(c, summary);
                })
                .toList();
    }

    @Transactional
    public AdminCompanyResponse approveCompany(RequestContext ctx, Long companyId) {
        requireAuthenticated(ctx);
        requirePlatformAdmin(ctx);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND",
                        "Фирмата не е намерена"));
        if (company.getStatus() == CompanyStatus.APPROVED) {
            return toAdminResponse(company);
        }
        if (company.getStatus() == CompanyStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.CONFLICT, "COMPANY_SUSPENDED",
                    "Спряна фирма не може да бъде одобрена директно");
        }
        company.setStatus(CompanyStatus.APPROVED);
        company.touch();
        companyRepository.save(company);

        List<Salon> salons = salonRepository.findByCompanyId(companyId);
        for (Salon salon : salons) {
            if (salon.getStatus() == SalonStatus.INACTIVE) {
                salon.setStatus(SalonStatus.ACTIVE);
            }
        }
        if (!salons.isEmpty()) {
            salonRepository.saveAll(salons);
        }
        return toAdminResponse(company);
    }

    private AdminCompanyResponse toAdminResponse(Company company) {
        List<CasClient.UserSummary> owners = casClient.lookupUsers(List.of(company.getOwnerUserId()));
        AdminCompanyResponse.OwnerSummary summary = owners.isEmpty()
                ? AdminCompanyResponse.OwnerSummary.unknown(company.getOwnerUserId())
                : AdminCompanyResponse.OwnerSummary.from(owners.getFirst());
        return AdminCompanyResponse.from(company, summary);
    }

    @Transactional
    public SalonResponse createSalon(RequestContext ctx, Long companyId, CreateSalonRequest request) {
        Company company = requireOwnedCompany(ctx, companyId);
        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new ApiException(HttpStatus.CONFLICT, "COMPANY_NOT_APPROVED",
                    "Обект може да се добави само към одобрена фирма");
        }
        WorkingHoursValidator.validateSalonDays(request.workingHours());
        var city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "CITY_NOT_FOUND", "Градът не е намерен"));

        SalonStatus status = resolveSalonStatusForCompany(company);

        Salon salon = Salon.builder()
                .companyId(company.getId())
                .name(request.name())
                .description(request.description())
                .city(city)
                .address(request.address())
                .lat(request.lat())
                .lng(request.lng())
                .email(request.email().strip().toLowerCase())
                .phone(request.phone().strip())
                .status(status)
                .build();
        salon = salonRepository.save(salon);
        List<WorkingHours> hours = persistSalonWorkingHours(salon.getId(), request.workingHours());
        return SalonResponse.from(salon, List.of(), hours);
    }

    @Transactional
    public List<WorkingHoursResponse> replaceSalonWorkingHours(RequestContext ctx,
                                                               Long salonId,
                                                               ReplaceSalonWorkingHoursRequest request) {
        requireOwnedSalon(ctx, salonId);
        WorkingHoursValidator.validateSalonDays(request.workingHours());
        workingHoursRepository.deleteBySalonIdAndStaffIdIsNull(salonId);
        return persistSalonWorkingHours(salonId, request.workingHours()).stream()
                .map(WorkingHoursResponse::from)
                .toList();
    }

    private List<WorkingHours> persistSalonWorkingHours(Long salonId, List<WorkingHoursDayRequest> days) {
        List<WorkingHours> entities = days.stream()
                .map(day -> WorkingHours.builder()
                        .salonId(salonId)
                        .staffId(null)
                        .dayOfWeek(day.dayOfWeek().shortValue())
                        .startTime(day.openTime())
                        .endTime(day.closeTime())
                        .build())
                .toList();
        return workingHoursRepository.saveAll(entities);
    }

    @Transactional
    public SalonServiceResponse addService(RequestContext ctx, Long salonId, CreateSalonServiceRequest request) {
        Salon salon = requireOwnedSalon(ctx, salonId);
        var category = serviceCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST, "CATEGORY_NOT_FOUND", "Категорията не е намерена"));

        SalonServiceItem item = SalonServiceItem.builder()
                .salonId(salon.getId())
                .category(category)
                .name(request.name())
                .durationMin(request.durationMin())
                .price(request.price())
                .active(true)
                .build();
        return SalonServiceResponse.from(salonServiceItemRepository.save(item));
    }

    @Transactional
    public void removeService(RequestContext ctx, Long salonId, Long serviceId) {
        requireOwnedSalon(ctx, salonId);
        SalonServiceItem item = salonServiceItemRepository.findByIdAndSalonId(serviceId, salonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND",
                        "Услугата не е намерена"));
        if (Boolean.FALSE.equals(item.getActive())) {
            return;
        }
        item.setActive(false);
        salonServiceItemRepository.save(item);
    }

    @Transactional
    public SalonPhotoResponse addPhoto(RequestContext ctx, Long salonId, CreateSalonPhotoRequest request) {
        requireOwnedSalon(ctx, salonId);
        int position = request.position() != null ? request.position() : 0;
        SalonPhoto photo = SalonPhoto.builder()
                .salonId(salonId)
                .url(request.url())
                .position(position)
                .build();
        return SalonPhotoResponse.from(salonPhotoRepository.save(photo));
    }

    private Company requireOwnedCompany(RequestContext ctx, Long companyId) {
        requireAuthenticated(ctx);
        return companyRepository.findByIdAndOwnerUserId(companyId, ctx.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NOT_COMPANY_OWNER",
                        "Нямате права върху тази фирма"));
    }

    private Salon requireOwnedSalon(RequestContext ctx, Long salonId) {
        Salon salon = salonRepository.findWithCityById(salonId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SALON_NOT_FOUND", "Салонът не е намерен"));
        requireOwnedCompany(ctx, salon.getCompanyId());
        return salon;
    }

    private static void requireAuthenticated(RequestContext ctx) {
        if (!ctx.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Изисква се автентикация");
        }
    }

    private static void requirePlatformAdmin(RequestContext ctx) {
        if (!ctx.hasRole(ROLE_PLATFORM_ADMIN)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Изисква се роля PLATFORM_ADMIN");
        }
    }

    /** ACTIVE само ако фирмата е APPROVED — иначе няма „активен обект без активна фирма“. */
    static SalonStatus resolveSalonStatusForCompany(Company company) {
        return company.getStatus() == CompanyStatus.APPROVED
                ? SalonStatus.ACTIVE
                : SalonStatus.INACTIVE;
    }
}
