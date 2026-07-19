package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bg.rezerv.business.web.dto.WorkingHoursDayRequest;
import bg.rezerv.business.web.error.ApiException;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkingHoursValidatorTest {

    @Test
    void acceptsDistinctDaysWithOpenBeforeClose() {
        assertThatCode(() -> WorkingHoursValidator.validateSalonDays(List.of(
                new WorkingHoursDayRequest(1, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new WorkingHoursDayRequest(6, LocalTime.of(9, 0), LocalTime.of(14, 0)))))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsEmptyList() {
        assertThatThrownBy(() -> WorkingHoursValidator.validateSalonDays(List.of()))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).code())
                .isEqualTo("WORKING_HOURS_REQUIRED");
    }

    @Test
    void rejectsDuplicateDay() {
        assertThatThrownBy(() -> WorkingHoursValidator.validateSalonDays(List.of(
                new WorkingHoursDayRequest(1, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new WorkingHoursDayRequest(1, LocalTime.of(10, 0), LocalTime.of(17, 0)))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).code())
                .isEqualTo("DUPLICATE_WORKING_DAY");
    }

    @Test
    void rejectsOpenNotBeforeClose() {
        assertThatThrownBy(() -> WorkingHoursValidator.validateSalonDays(List.of(
                new WorkingHoursDayRequest(1, LocalTime.of(18, 0), LocalTime.of(9, 0)))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).code())
                .isEqualTo("INVALID_WORKING_HOURS");
    }
}
