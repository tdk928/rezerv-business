package bg.rezerv.business.web.dto.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Агрегат за rezerv-booking — слотове без N+1 заявки. */
public record AvailabilityContextResponse(
        Long salonId,
        String timezone,
        List<DayHours> salonWorkingHours,
        List<ClosureDay> closures,
        List<StaffAvailability> staff,
        List<ServiceInfo> services) {

    public record DayHours(int dayOfWeek, LocalTime openTime, LocalTime closeTime) {
    }

    public record ClosureDay(LocalDate closedOn, String reason) {
    }

    public record StaffAvailability(
            Long id,
            String displayName,
            List<Long> serviceIds,
            List<DayHours> workingHours,
            List<TimeOffInterval> timeOff) {
    }

    public record TimeOffInterval(Instant startsAt, Instant endsAt) {
    }

    public record ServiceInfo(Long id, String name, int durationMin, BigDecimal price, boolean active) {
    }
}
