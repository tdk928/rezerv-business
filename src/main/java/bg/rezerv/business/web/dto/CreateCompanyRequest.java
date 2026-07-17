package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{9}$", message = "ЕИК трябва да е 9 цифри")
        String eik,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String legalName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 30) String phone) {
}
