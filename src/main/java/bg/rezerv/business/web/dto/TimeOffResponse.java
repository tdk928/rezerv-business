package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.TimeOff;
import bg.rezerv.business.domain.TimeOffSource;
import java.time.Instant;

public record TimeOffResponse(
        Long id,
        Long staffId,
        Instant startsAt,
        Instant endsAt,
        String reason,
        TimeOffSource source,
        Long createdBy,
        Instant createdAt) {

    public static TimeOffResponse from(TimeOff timeOff) {
        return new TimeOffResponse(
                timeOff.getId(),
                timeOff.getStaffId(),
                timeOff.getStartsAt(),
                timeOff.getEndsAt(),
                timeOff.getReason(),
                timeOff.getSource(),
                timeOff.getCreatedBy(),
                timeOff.getCreatedAt());
    }
}
