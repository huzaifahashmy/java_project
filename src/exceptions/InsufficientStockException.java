package exceptions;

/**
 * ==============================================================================
 * TEAM MEMBER 3: INVENTORY & TRANSACTIONS LEAD
 * File: InsufficientStockException.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * - Custom Checked Exception: Thrown when a stock-out operation attempts to withdraw
 *   more units of a product than are currently available in the warehouse.
 * ==============================================================================
 */
public class InsufficientStockException extends Exception {

    private int availableStock;
    private int requestedStock;

    /**
     * Constructor accepting an explanatory error message.
     *
     * @param message Detailed reason for the exception
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * Detailed constructor that tracks the numerical discrepancy.
     *
     * @param available Available inventory units
     * @param requested Requested units for stock-out
     */
    public InsufficientStockException(int available, int requested) {
        super("Insufficient stock: Requested " + requested + " units, but only " + available + " units are available.");
        this.availableStock = available;
        this.requestedStock = requested;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedStock() {
        return requestedStock;
    }
}

