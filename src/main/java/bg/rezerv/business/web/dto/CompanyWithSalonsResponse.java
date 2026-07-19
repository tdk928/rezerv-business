package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.Company;
import bg.rezerv.business.domain.CompanyStatus;
import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonServiceItem;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Фирма на текущия owner + нейните обекти (салони). */
public record CompanyWithSalonsResponse(
        Long id,
        String eik,
        String name,
        String legalName,
        String email,
        String phone,
        Long ownerUserId,
        CompanyStatus status,
        Instant createdAt,
        List<SalonResponse> salons) {

    public static CompanyWithSalonsResponse from(
            Company company, List<Salon> salons, Map<Long, List<SalonServiceItem>> servicesBySalon) {
        return new CompanyWithSalonsResponse(
                company.getId(),
                company.getEik(),
                company.getName(),
                company.getLegalName(),
                company.getEmail(),
                company.getPhone(),
                company.getOwnerUserId(),
                company.getStatus(),
                company.getCreatedAt(),
                salons.stream()
                        .map(s -> SalonResponse.from(s, servicesBySalon.getOrDefault(s.getId(), List.of())))
                        .toList());
    }
}
