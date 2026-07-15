package bg.rezerv.business.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EikValidatorTest {

    private final EikValidator validator = new EikValidator();

    @Test
    void acceptsValidKnownEiks() {
        assertThat(validator.isValid("131529327")).isTrue();
        assertThat(validator.isValid("175074752")).isTrue();
    }

    @Test
    void rejectsDemoSeedEikWithBadChecksum() {
        // V2 demo seed съдържа 204815936 — невалидна контролна сума (само за dev данни).
        assertThat(validator.isValid("204815936")).isFalse();
    }

    @Test
    void rejectsInvalidChecksum() {
        assertThat(validator.isValid("204815935")).isFalse();
        assertThat(validator.isValid("123456789")).isFalse();
    }

    @Test
    void rejectsWrongLength() {
        assertThat(validator.isValid("12345")).isFalse();
        assertThat(validator.isValid("12345678901234")).isFalse();
        assertThat(validator.isValid(null)).isFalse();
    }
}
