package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateSalonClosureRequest(
        @NotNull LocalDate closedOn,
        @Size(max = 500) String reason) {
}
