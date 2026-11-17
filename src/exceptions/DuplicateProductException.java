package exceptions;

/**
 * ==============================================================================
 * TEAM MEMBER 1: PRODUCT & VALIDATION LEAD
 * File: DuplicateProductException.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * - Custom Checked Exception: Thrown when an attempt is made to insert a product
 *   with an ID that already exists in the system.
 * ==============================================================================
 */
public class DuplicateProductException extends Exception {

    /**
     * Constructor accepting an explanatory error message.
     *
     * @param message Detailed reason for the exception
     */
    public DuplicateProductException(String message) {
        super(message);
    }

    /**
     * Constructor accepting an error message and a root cause.
     *
     * @param message Detailed reason
     * @param cause   Root throwable cause
     */
    public DuplicateProductException(String message, Throwable cause) {
        super(message, cause);
    }
}

