package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSalonPhotoRequest(
        @NotBlank @Size(max = 500) String url,
        Integer position) {
}
