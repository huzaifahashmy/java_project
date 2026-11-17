package exceptions;

/**
 * ==============================================================================
 * TEAM MEMBER 1: PRODUCT & VALIDATION LEAD
 * File: ProductNotFoundException.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * - Custom Checked Exception: Extends java.lang.Exception to indicate that a
 *   requested product does not exist in the warehouse inventory.
 * ==============================================================================
 */
public class ProductNotFoundException extends Exception {

    /**
     * Constructor accepting an explanatory error message.
     *
     * @param message Detailed reason for the exception
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor accepting an error message and a root cause.
     *
     * @param message Detailed reason
     * @param cause   Root throwable cause
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

