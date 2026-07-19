package bg.rezerv.business.web;

import bg.rezerv.business.service.AvailabilityInternalService;
import bg.rezerv.business.web.dto.internal.AvailabilityContextResponse;
import bg.rezerv.business.web.dto.internal.SalonActorResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service — gateway НЕ route-ва /internal/**.
 * Вика се от rezerv-booking.
 */
@RestController
@RequestMapping("/internal/salons")
public class InternalSalonController {

    private final AvailabilityInternalService availabilityInternalService;

    public InternalSalonController(AvailabilityInternalService availabilityInternalService) {
        this.availabilityInternalService = availabilityInternalService;
    }

    @GetMapping("/{salonId}/availability-context")
    public AvailabilityContextResponse availabilityContext(
            @PathVariable Long salonId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return availabilityInternalService.availabilityContext(salonId, from, to);
    }

    @GetMapping("/{salonId}/actors/{userId}")
    public SalonActorResponse actor(@PathVariable Long salonId, @PathVariable Long userId) {
        return availabilityInternalService.actor(salonId, userId);
    }
}
