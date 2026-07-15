package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.CompanyStatus;
import java.time.Instant;

public record CompanyResponse(
        Long id,
        String eik,
        String name,
        String legalName,
        Long ownerUserId,
        CompanyStatus status,
        Instant createdAt) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getEik(),
                company.getName(),
                company.getLegalName(),
                company.getOwnerUserId(),
                company.getStatus(),
                company.getCreatedAt());
    }
}
