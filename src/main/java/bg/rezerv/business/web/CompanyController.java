package bg.rezerv.business.web;

import bg.rezerv.business.service.CompanyOnboardingService;
import bg.rezerv.business.web.dto.CompanyResponse;
import bg.rezerv.business.web.dto.CompanyWithSalonsResponse;
import bg.rezerv.business.web.dto.CreateCompanyRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Protected B2B onboarding — gateway изисква JWT (/api/business/** без /public). */
@RestController
@RequestMapping("/business/companies")
public class CompanyController {

    private final CompanyOnboardingService onboardingService;

    public CompanyController(CompanyOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/mine")
    public List<CompanyWithSalonsResponse> listMine(HttpServletRequest request) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.listMyCompanies(ctx);
    }

    @PostMapping
    public CompanyResponse registerCompany(HttpServletRequest request,
                                           @Valid @RequestBody CreateCompanyRequest body) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.registerCompany(ctx, body);
    }
}
