package bg.rezerv.business.web.dto;

import bg.rezerv.business.client.CasClient;
import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.CompanyStatus;
import java.time.Instant;

/** Админ изглед: фирма + собственик от CAS. */
public record AdminCompanyResponse(
        Long id,
        String eik,
        String name,
        String legalName,
        String email,
        String phone,
        CompanyStatus status,
        Instant createdAt,
        Instant updatedAt,
        OwnerSummary owner) {

    public record OwnerSummary(Long id, String email, String firstName, String lastName) {
        public static OwnerSummary from(CasClient.UserSummary user) {
            return new OwnerSummary(user.id(), user.email(), user.firstName(), user.lastName());
        }

        public static OwnerSummary unknown(Long ownerUserId) {
            return new OwnerSummary(ownerUserId, null, null, null);
        }
    }

    public static AdminCompanyResponse from(Company company, OwnerSummary owner) {
        return new AdminCompanyResponse(
                company.getId(),
                company.getEik(),
                company.getName(),
                company.getLegalName(),
                company.getEmail(),
                company.getPhone(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt(),
                owner);
    }
}
