package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bg.rezerv.business.domain.WorkingHours;
import bg.rezerv.business.web.dto.WorkingHoursDayRequest;
import bg.rezerv.business.web.error.ApiException;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class StaffScheduleValidatorTest {

    private final List<WorkingHours> salonHours = List.of(
            WorkingHours.builder().dayOfWeek((short) 1).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(18, 0)).build(),
            WorkingHours.builder().dayOfWeek((short) 6).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(14, 0)).build());

    @Test
    void acceptsStaffHoursInsideSalon() {
        assertThatCode(() -> StaffScheduleValidator.validateWithinSalonHours(List.of(
                new WorkingHoursDayRequest(1, LocalTime.of(10, 0), LocalTime.of(17, 0)),
                new WorkingHoursDayRequest(6, LocalTime.of(9, 0), LocalTime.of(13, 0))), salonHours))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsDayWhenSalonClosed() {
        assertThatThrownBy(() -> StaffScheduleValidator.validateWithinSalonHours(List.of(
                new WorkingHoursDayRequest(7, LocalTime.of(10, 0), LocalTime.of(12, 0))), salonHours))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).code())
                .isEqualTo("STAFF_DAY_OUTSIDE_SALON");
    }

    @Test
    void rejectsHoursOutsideSalonWindow() {
        assertThatThrownBy(() -> StaffScheduleValidator.validateWithinSalonHours(List.of(
                new WorkingHoursDayRequest(1, LocalTime.of(8, 0), LocalTime.of(18, 0))), salonHours))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).code())
                .isEqualTo("STAFF_HOURS_OUTSIDE_SALON");
    }
}
