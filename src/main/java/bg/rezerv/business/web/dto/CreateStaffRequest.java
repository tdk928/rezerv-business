package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Owner създава нов CAS user + staff_members ред. */
public record CreateStaffRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Size(max = 32) String phone,
        @Size(max = 200) String displayName,
        @Size(max = 120) String title) {
}
