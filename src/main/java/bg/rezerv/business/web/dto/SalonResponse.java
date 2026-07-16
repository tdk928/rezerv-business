package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.Salon;
import bg.rezerv.business.domain.SalonStatus;

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
        SalonStatus status) {

    public static SalonResponse from(Salon salon) {
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
                salon.getStatus());
    }
}
