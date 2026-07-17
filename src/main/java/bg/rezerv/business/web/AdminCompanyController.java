package bg.rezerv.business.web;

import bg.rezerv.business.service.CompanyOnboardingService;
import bg.rezerv.business.web.dto.AdminCompanyResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Platform admin — всички фирми + собственици (CAS lookup). */
@RestController
@RequestMapping("/business/admin/companies")
public class AdminCompanyController {

    private final CompanyOnboardingService onboardingService;

    public AdminCompanyController(CompanyOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping
    public List<AdminCompanyResponse> listAll(HttpServletRequest request) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.listAllCompaniesForAdmin(ctx);
    }

    @PostMapping("/{companyId}/approve")
    public AdminCompanyResponse approve(HttpServletRequest request, @PathVariable Long companyId) {
        RequestContext ctx = RequestContext.requireAuthenticated(request);
        return onboardingService.approveCompany(ctx, companyId);
    }
}
