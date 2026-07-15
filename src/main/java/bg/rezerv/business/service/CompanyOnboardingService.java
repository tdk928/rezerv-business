package bg.rezerv.business.service;

import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.CompanyStatus;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonPhoto;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.domain.SalonStatus;
import bg.rezerv.business.repository.CityRepository;
import bg.rezerv.business.repository.CompanyRepository;
import bg.rezerv.business.repository.SalonPhotoRepository;
import bg.rezerv.business.repository.SalonRepository;
import bg.rezerv.business.repository.SalonServiceItemRepository;
import bg.rezerv.business.repository.ServiceCategoryRepository;
import bg.rezerv.business.web.RequestContext;
import bg.rezerv.business.web.dto.CompanyResponse;
import bg.rezerv.business.web.dto.CreateCompanyRequest;
import bg.rezerv.business.web.dto.CreateSalonPhotoRequest;
import bg.rezerv.business.web.dto.CreateSalonRequest;
import bg.rezerv.business.web.dto.CreateSalonServiceRequest;
import bg.rezerv.business.web.dto.SalonPhotoResponse;
import bg.rezerv.business.web.dto.SalonResponse;
import bg.rezerv.business.web.dto.SalonServiceResponse;
import bg.rezerv.business.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyOnboardingService {

    private final CompanyRepository companyRepository;
    private final SalonRepository salonRepository;
    private final SalonServiceItemRepository salonServiceItemRepository;
    private final SalonPhotoRepository salonPhotoRepository;
    private final CityRepository cityRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final EikValidator eikValidator;

    public CompanyOnboardingService(CompanyRepository companyRepository,
                                    SalonRepository salonRepository,
                                    SalonServiceItemRepository salonServiceItemRepository,
                                    SalonPhotoRepository salonPhotoRepository,
                                    CityRepository cityRepository,
                                    ServiceCategoryRepository serviceCategoryRepository,
                                    EikValidator eikValidator) {
        this.companyRepository = companyRepository;
        this.salonRepository = salonRepository;
        this.salonServiceItemRepository = salonServiceItemRepository;
        this.salonPhotoRepository = salonPhotoRepository;
        this.cityRepository = cityRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.eikValidator = eikValidator;
    }

    @Transactional
    public CompanyResponse registerCompany(RequestContext ctx, CreateCompanyRequest request) {
        requireAuthenticated(ctx);
        if (companyRepository.existsByOwnerUserId(ctx.userId())) {
            throw new ApiException(HttpStatus.CONFLICT, "COMPANY_ALREADY_REGISTERED",
                    "Вече имате регистрирана фирма");
        }
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
                .ownerUserId(ctx.userId())
                .status(CompanyStatus.PENDING_APPROVAL)
                .build();
        return CompanyResponse.from(companyRepository.save(company));
    }

    @Transactional
    public SalonResponse createSalon(RequestContext ctx, Long companyId, CreateSalonRequest request) {
        Company company = requireOwnedCompany(ctx, companyId);
        var city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "CITY_NOT_FOUND", "Градът не е намерен"));

        Salon salon = Salon.builder()
                .companyId(company.getId())
                .name(request.name())
                .description(request.description())
                .city(city)
                .address(request.address())
                .lat(request.lat())
                .lng(request.lng())
                .phone(request.phone())
                .status(SalonStatus.ACTIVE)
                .build();
        return SalonResponse.from(salonRepository.save(salon));
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
}
