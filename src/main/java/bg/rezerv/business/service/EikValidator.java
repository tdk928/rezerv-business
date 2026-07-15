package bg.rezerv.business.service;

import org.springframework.stereotype.Component;

/** Валидация на български ЕИК (9 или 13 цифри) с контролна сума. */
@Component
public class EikValidator {

    public boolean isValid(String eik) {
        if (eik == null || !eik.matches("^[0-9]{9}$|^[0-9]{13}$")) {
            return false;
        }
        int[] digits = eik.chars().map(c -> c - '0').toArray();
        if (!isValidNineDigitBase(digits)) {
            return false;
        }
        return eik.length() == 9 || isValidThirteenDigitExtension(digits);
    }

    private static boolean isValidNineDigitBase(int[] digits) {
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

    private static boolean isValidThirteenDigitExtension(int[] digits) {
        int sum = digits[8] * 2 + digits[9] * 7 + digits[10] * 3 + digits[11] * 5;
        int check = sum % 11;
        if (check == 10) {
            sum = digits[8] * 4 + digits[9] * 9 + digits[10] * 5 + digits[11] * 7;
            check = sum % 11;
            if (check == 10) {
                check = 0;
            }
        }
        return digits[12] == check;
    }
}
