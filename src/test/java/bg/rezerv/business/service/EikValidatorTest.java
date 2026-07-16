package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EikValidatorTest {

    private final EikValidator validator = new EikValidator();

    @Test
    void acceptsValidKnownEiks() {
        assertThat(validator.isValid("131529327")).isTrue();
        assertThat(validator.isValid("175074752")).isTrue();
        assertThat(validator.isValid("100000001")).isTrue();
    }

    @Test
    void rejectsInvalidChecksum() {
        assertThat(validator.isValid("204815936")).isFalse();
        assertThat(validator.isValid("123456789")).isFalse();
    }

    @Test
    void rejectsWrongLength() {
        assertThat(validator.isValid("12345")).isFalse();
        assertThat(validator.isValid("1234567890123")).isFalse();
        assertThat(validator.isValid(null)).isFalse();
    }
}
