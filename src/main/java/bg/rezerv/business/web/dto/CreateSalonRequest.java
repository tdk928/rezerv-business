package bg.rezerv.business.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateSalonRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @NotNull Long cityId,
        @NotBlank @Size(max = 300) String address,
        Double lat,
        Double lng,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 30) String phone,
        @NotEmpty List<@Valid WorkingHoursDayRequest> workingHours) {
}
