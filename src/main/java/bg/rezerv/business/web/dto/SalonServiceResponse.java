package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.SalonServiceItem;

public record SalonServiceResponse(
        Long id,
        Long salonId,
        Long categoryId,
        String categoryName,
        String name,
        Integer durationMin,
        java.math.BigDecimal price,
        Boolean active) {

    public static SalonServiceResponse from(SalonServiceItem item) {
        return new SalonServiceResponse(
                item.getId(),
                item.getSalonId(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getName(),
                item.getDurationMin(),
                item.getPrice(),
                item.getActive());
    }
}
