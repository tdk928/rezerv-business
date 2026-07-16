package bg.rezerv.business.service;

import org.springframework.stereotype.Component;

/** Валидация на български ЕИК (9 цифри) с контролна сума. */
@Component
public class EikValidator {

    public boolean isValid(String eik) {
        if (eik == null || !eik.matches("^[0-9]{9}$")) {
            return false;
        }
        int[] digits = eik.chars().map(c -> c - '0').toArray();
        return isValidNineDigitChecksum(digits);
    }

    private static boolean isValidNineDigitChecksum(int[] digits) {
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += digits[i] * (i + 1);
        }
        int check = sum % 11;
        if (check == 10) {
            sum = 0;
            for (int i = 0; i < 8; i++) {
                sum += digits[i] * (i + 3);
            }
            check = sum % 11;
            if (check == 10) {
                check = 0;
            }
        }
        return digits[8] == check;
    }
}
