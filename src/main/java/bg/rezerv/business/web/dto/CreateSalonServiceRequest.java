package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateSalonServiceRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 200) String name,
        @NotNull @Positive Integer durationMin,
        @NotNull @DecimalMin("0.0") BigDecimal price) {
}
