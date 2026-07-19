package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonServiceItem;
import bg.rezerv.business.domain.SalonStatus;
import java.util.List;

public record SalonResponse(
        Long id,
        Long companyId,
        String name,
        String description,
        CityResponse city,
        String address,
        Double lat,
        Double lng,
        String email,
        String phone,
        SalonStatus status,
        List<SalonServiceResponse> services) {

    public static SalonResponse from(Salon salon) {
        return from(salon, List.of());
    }

    public static SalonResponse from(Salon salon, List<SalonServiceItem> services) {
        return new SalonResponse(
                salon.getId(),
                salon.getCompanyId(),
                salon.getName(),
                salon.getDescription(),
                CityResponse.from(salon.getCity()),
                salon.getAddress(),
                salon.getLat(),
                salon.getLng(),
                salon.getEmail(),
                salon.getPhone(),
                salon.getStatus(),
                services.stream().map(SalonServiceResponse::from).toList());
    }
}
