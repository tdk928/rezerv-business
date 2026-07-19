package bg.rezerv.business.service;

import bg.rezerv.business.web.dto.WorkingHoursDayRequest;
import bg.rezerv.business.web.error.ApiException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;

public final class WorkingHoursValidator {

    private WorkingHoursValidator() {
    }

    public static void validateSalonDays(List<WorkingHoursDayRequest> days) {
        if (days == null || days.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WORKING_HOURS_REQUIRED",
                    "Изберете поне един работен ден");
        }
        Set<Integer> seen = new HashSet<>();
        for (WorkingHoursDayRequest day : days) {
            if (day.dayOfWeek() == null || day.dayOfWeek() < 1 || day.dayOfWeek() > 7) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DAY_OF_WEEK",
                        "Денят трябва да е между 1 (пн) и 7 (нд)");
            }
            if (!seen.add(day.dayOfWeek())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_WORKING_DAY",
                        "Ден " + day.dayOfWeek() + " е посочен повече от веднъж");
            }
            if (day.openTime() == null || day.closeTime() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "WORKING_HOURS_REQUIRED",
                        "Отворено и затворено са задължителни");
            }
            if (!day.openTime().isBefore(day.closeTime())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_WORKING_HOURS",
                        "Часът на отваряне трябва да е преди затваряне");
            }
        }
    }
}
