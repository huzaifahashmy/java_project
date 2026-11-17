package utils;

/**
 * ==============================================================================
 * TEAM MEMBER 1: PRODUCT & VALIDATION LEAD
 * File: InputValidator.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Static Utility Class: Private constructor prevents instantiation; all methods
 *    are static and can be called directly without creating an object.
 * 2. Defensive Programming & Data Validation: Validates user input before allowing
 *    it into business logic or storage.
 * 3. Exception Handling: Catches NumberFormatException internally to return boolean flags.
 * ==============================================================================
 */
public final class InputValidator {

    /**
     * Private constructor to prevent instantiating this utility class.
     */
    private InputValidator() {
        // Enforce non-instantiability
    }

    /**
     * Checks if a string is non-null and not empty (after trimming spaces).
     *
     * @param str The string to test
     * @return true if valid, false otherwise
     */
    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Checks if a string parses into a positive double (> 0.0).
     *
     * @param str The string to parse
     * @return true if positive double, false otherwise
     */
    public static boolean isPositiveDouble(String str) {
        if (!isValidString(str)) {
            return false;
        }
        try {
            double value = Double.parseDouble(str.trim());
            return value > 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string parses into a non-negative double (>= 0.0).
     *
     * @param str The string to parse
     * @return true if >= 0.0, false otherwise
     */
    public static boolean isNonNegativeDouble(String str) {
        if (!isValidString(str)) {
            return false;
        }
        try {
            double value = Double.parseDouble(str.trim());
            return value >= 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string parses into a positive integer (> 0).
     *
     * @param str The string to parse
     * @return true if positive integer, false otherwise
     */
    public static boolean isPositiveInt(String str) {
        if (!isValidString(str)) {
            return false;
        }
        try {
            int value = Integer.parseInt(str.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string parses into a non-negative integer (>= 0).
     *
     * @param str The string to parse
     * @return true if >= 0, false otherwise
     */
    public static boolean isNonNegativeInt(String str) {
        if (!isValidString(str)) {
            return false;
        }
        try {
            int value = Integer.parseInt(str.trim());
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Basic check for email format (contains '@' and at least one '.').
     *
     * @param email The email to test
     * @return true if valid format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (!isValidString(email)) {
            return false;
        }
        String trimmed = email.trim();
        return trimmed.contains("@") && trimmed.contains(".") && trimmed.indexOf("@") < trimmed.lastIndexOf(".");
    }
}

