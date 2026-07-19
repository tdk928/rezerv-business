package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.SalonClosure;
import java.time.Instant;
import java.time.LocalDate;

public record SalonClosureResponse(
        Long id,
        Long salonId,
        LocalDate closedOn,
        String reason,
        Long createdBy,
        Instant createdAt) {

    public static SalonClosureResponse from(SalonClosure closure) {
        return new SalonClosureResponse(
                closure.getId(),
                closure.getSalonId(),
                closure.getClosedOn(),
                closure.getReason(),
                closure.getCreatedBy(),
                closure.getCreatedAt());
    }
}
