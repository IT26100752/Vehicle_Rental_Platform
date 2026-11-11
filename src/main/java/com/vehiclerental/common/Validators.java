package com.vehiclerental.common;

import java.util.regex.Pattern;

/** Small, reusable input checks (kept in one place so every module validates the same way). */
public final class Validators {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    /** 07XXXXXXXX, 7XXXXXXXX, +947XXXXXXXX, 947XXXXXXXX, or a 9-15 digit international number. */
    private static final Pattern PHONE = Pattern.compile("^(\\+?\\d{9,15})$");
    /** Old NIC: 9 digits + V/X.  New NIC: 12 digits. */
    private static final Pattern NIC = Pattern.compile("^(\\d{9}[VvXx]|\\d{12})$");
    private static final Pattern PASSWORD_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile(".*\\d.*");

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= 120 && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.replaceAll("[\\s-]", "")).matches();
    }

    public static boolean isValidNic(String nic) {
        return nic != null && NIC.matcher(nic.trim()).matches();
    }

    /** At least 8 characters with a letter and a digit. */
    public static boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8
                && PASSWORD_LETTER.matcher(password).matches()
                && PASSWORD_DIGIT.matcher(password).matches();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Luhn checksum used for the simulated card payment. */
    public static boolean passesLuhn(String digits) {
        if (digits == null || !digits.matches("\\d{13,19}")) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
