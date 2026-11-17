package models;

/**
 * ==============================================================================
 * TEAM MEMBER 3: INVENTORY & TRANSACTIONS LEAD
 * File: Transaction.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Encapsulation: Private member variables with public accessors.
 * 2. Immutable Log Record Concept: Captures a point-in-time warehouse stock movement.
 * 3. JTable Row Integration: toTableRow() returns column-mapped objects.
 * ==============================================================================
 */
public class Transaction {

    private String transactionId;
    private String productId;
    private String type;     // "IN" (Stock added) or "OUT" (Stock removed)
    private int quantity;    // Number of units moved
    private String date;     // Timestamp string (e.g., "2026-11-08 17:30:00")

    /**
     * Default constructor for Gson JSON deserialization.
     */
    public Transaction() {
    }

    /**
     * Parameterized constructor.
     *
     * @param transactionId Unique transaction code (e.g., "TXN-1700000000")
     * @param productId     Target product ID
     * @param type          "IN" or "OUT"
     * @param quantity      Number of units involved
     * @param date          Formatted date/time string
     */
    public Transaction(String transactionId, String productId, String type, int quantity, String date) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
    }

    // --- Getters and Setters ---

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Converts transaction details into a JTable row object array.
     * Columns: [Transaction ID, Product ID, Type, Quantity, Date]
     */
    public Object[] toTableRow() {
        return new Object[]{ transactionId, productId, type, quantity, date };
    }

    @Override
    public String toString() {
        return "[" + date + "] TXN: " + transactionId + " | Type: " + type +
                " | Product: " + productId + " | Qty: " + quantity;
    }
}

