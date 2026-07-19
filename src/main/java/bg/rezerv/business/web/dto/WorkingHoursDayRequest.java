package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/** Един работен ден на обект. dayOfWeek: 1=пн … 7=нд (ISO). */
public record WorkingHoursDayRequest(
        @NotNull @Min(1) @Max(7) Integer dayOfWeek,
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime) {
}
