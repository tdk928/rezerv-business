package bg.rezerv.business.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceSalonWorkingHoursRequest(
        @NotEmpty List<@Valid WorkingHoursDayRequest> workingHours) {
}
