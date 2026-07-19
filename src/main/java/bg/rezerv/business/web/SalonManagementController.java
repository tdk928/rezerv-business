package bg.rezerv.business.web;

import bg.rezerv.business.service.CompanyOnboardingService;
import bg.rezerv.business.web.dto.CreateSalonPhotoRequest;
import bg.rezerv.business.web.dto.CreateSalonRequest;
import bg.rezerv.business.web.dto.CreateSalonServiceRequest;
import bg.rezerv.business.web.dto.ReplaceSalonWorkingHoursRequest;
import bg.rezerv.business.web.dto.SalonPhotoResponse;
import bg.rezerv.business.web.dto.SalonResponse;
import bg.rezerv.business.web.dto.SalonServiceResponse;
import bg.rezerv.business.web.dto.WorkingHoursResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class SalonManagementController {

    private final CompanyOnboardingService onboardingService;

    public SalonManagementController(CompanyOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/companies/{companyId}/salons")
    public SalonResponse createSalon(HttpServletRequest request,
                                     @PathVariable Long companyId,
                                     @Valid @RequestBody CreateSalonRequest body) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.createSalon(ctx, companyId, body);
    }

    @PostMapping("/salons/{salonId}/services")
    public SalonServiceResponse addService(HttpServletRequest request,
                                           @PathVariable Long salonId,
                                           @Valid @RequestBody CreateSalonServiceRequest body) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.addService(ctx, salonId, body);
    }

    @DeleteMapping("/salons/{salonId}/services/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeService(HttpServletRequest request,
                              @PathVariable Long salonId,
                              @PathVariable Long serviceId) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        onboardingService.removeService(ctx, salonId, serviceId);
    }

    @PostMapping("/salons/{salonId}/photos")
    public SalonPhotoResponse addPhoto(HttpServletRequest request,
                                       @PathVariable Long salonId,
                                       @Valid @RequestBody CreateSalonPhotoRequest body) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.addPhoto(ctx, salonId, body);
    }

    @PutMapping("/salons/{salonId}/working-hours")
    public List<WorkingHoursResponse> replaceWorkingHours(HttpServletRequest request,
                                                          @PathVariable Long salonId,
                                                          @Valid @RequestBody ReplaceSalonWorkingHoursRequest body) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.replaceSalonWorkingHours(ctx, salonId, body);
    }
}
