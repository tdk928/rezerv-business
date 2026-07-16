package bg.rezerv.business.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Пълен изглед на салон — страницата на салона. */
public record SalonDetailResponse(
        Long id,
        String name,
        String description,
        CityResponse city,
        String address,
        Double lat,
        Double lng,
        String email,
        String phone,
        BigDecimal ratingAvg,
        Integer ratingCount,
        List<String> photos,
        List<ServiceGroup> serviceGroups) {

    /** Услугите на салона, групирани по категория. */
    public record ServiceGroup(
            Long categoryId,
            String categoryName,
            String categorySlug,
            List<ServiceItem> services) {
    }

    public record ServiceItem(
            Long id,
            String name,
            Integer durationMin,
            BigDecimal price) {
    }
}
