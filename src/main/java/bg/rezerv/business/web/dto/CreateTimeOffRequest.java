package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateTimeOffRequest(
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        @Size(max = 500) String reason) {
}
