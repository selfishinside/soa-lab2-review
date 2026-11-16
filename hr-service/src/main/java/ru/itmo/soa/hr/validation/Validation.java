package ru.itmo.soa.hr.validation;

import java.math.BigDecimal;
import ru.itmo.soa.hr.exception.ApiException;

public final class Validation {

    private Validation() {
    }

    public static long workerId(String value) {
        return positiveInteger(value, Integer.MAX_VALUE, "workerId");
    }

    public static long organizationId(String value) {
        return positiveInteger(value, Long.MAX_VALUE, "organizationId");
    }

    public static BigDecimal coefficient(String value) {
        if (value == null || value.isBlank() || value.length() > 64) {
            throw new ApiException(400, "Invalid coefficient");
        }

        try {
            BigDecimal coefficient = new BigDecimal(value);
            double doubleValue = coefficient.doubleValue();
            if (coefficient.signum() <= 0
                    || !Double.isFinite(doubleValue)
                    || doubleValue <= 0) {
                throw new ApiException(400, "Invalid coefficient");
            }
            return coefficient;
        } catch (NumberFormatException exception) {
            throw new ApiException(400, "Invalid coefficient");
        }
    }

    private static long positiveInteger(String value, long maximum, String name) {
        if (value == null || !value.matches("[0-9]+")) {
            throw new ApiException(400, name + " must be a positive integer");
        }

        try {
            long result = Long.parseLong(value);
            if (result < 1 || result > maximum) {
                throw new ApiException(400, name + " is out of range");
            }
            return result;
        } catch (NumberFormatException exception) {
            throw new ApiException(400, name + " is out of range");
        }
    }
}
