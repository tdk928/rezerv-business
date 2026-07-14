package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.City;

public record CityResponse(Long id, String name, String slug) {

    public static CityResponse from(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getSlug());
    }
}
