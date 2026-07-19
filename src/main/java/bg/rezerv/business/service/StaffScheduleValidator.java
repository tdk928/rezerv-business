package bg.rezerv.business.service;

import bg.rezerv.business.domain.WorkingHours;
import bg.rezerv.business.web.dto.WorkingHoursDayRequest;
import bg.rezerv.business.web.error.ApiException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

/** Служителят не може да работи извън часовете на обекта. */
public final class StaffScheduleValidator {

    private StaffScheduleValidator() {
    }

    public static void validateWithinSalonHours(List<WorkingHoursDayRequest> staffDays,
                                                List<WorkingHours> salonHours) {
        WorkingHoursValidator.validateSalonDays(staffDays);
        Map<Short, WorkingHours> byDay = salonHours.stream()
                .collect(Collectors.toMap(WorkingHours::getDayOfWeek, Function.identity(), (a, b) -> a));

        for (WorkingHoursDayRequest day : staffDays) {
            WorkingHours salonDay = byDay.get(day.dayOfWeek().shortValue());
            if (salonDay == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "STAFF_DAY_OUTSIDE_SALON",
                        "Обектът не работи в ден " + day.dayOfWeek());
            }
            if (day.openTime().isBefore(salonDay.getStartTime())
                    || day.closeTime().isAfter(salonDay.getEndTime())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "STAFF_HOURS_OUTSIDE_SALON",
                        "Смяната в ден " + day.dayOfWeek() + " трябва да е в рамките на часовете на обекта");
            }
        }
    }
}
