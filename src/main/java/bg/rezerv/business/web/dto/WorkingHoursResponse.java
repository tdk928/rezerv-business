package bg.rezerv.business.web.dto;

import bg.rezerv.business.domain.WorkingHours;
import java.time.LocalTime;

public record WorkingHoursResponse(
        int dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime) {

    public static WorkingHoursResponse from(WorkingHours hours) {
        return new WorkingHoursResponse(
                hours.getDayOfWeek(),
                hours.getStartTime(),
                hours.getEndTime());
    }
}
