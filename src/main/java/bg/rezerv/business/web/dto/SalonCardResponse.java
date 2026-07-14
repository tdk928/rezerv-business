package bg.rezerv.business.web.dto;

import java.math.BigDecimal;

/** Карта на салон в списъка с резултати / "Топ салони". */
public record SalonCardResponse(
        Long id,
        String name,
        CityResponse city,
        String address,
        BigDecimal ratingAvg,
        Integer ratingCount,
        String photoUrl,
        BigDecimal priceFrom) {
}
