package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.TimeOffRequest;
import bg.rezerv.business.domain.TimeOffRequestStatus;
import java.time.Instant;

public record TimeOffRequestResponse(
        Long id,
        Long staffId,
        Instant startsAt,
        Instant endsAt,
        String reason,
        TimeOffRequestStatus status,
        Instant createdAt,
        Long reviewedBy,
        Instant reviewedAt) {

    public static TimeOffRequestResponse from(TimeOffRequest request) {
        return new TimeOffRequestResponse(
                request.getId(),
                request.getStaffId(),
                request.getStartsAt(),
                request.getEndsAt(),
                request.getReason(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getReviewedBy(),
                request.getReviewedAt());
    }
}
