package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddStaffRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 200) String displayName,
        @Size(max = 120) String title) {
}
