package bg.rezerv.business.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceStaffServicesRequest(@NotNull List<Long> serviceIds) {
}
