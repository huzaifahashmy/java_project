package models;

/**
 * ==============================================================================
 * TEAM MEMBER 4: PURCHASE ORDERS & REPORTS LEAD
 * File: PurchaseOrder.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Composition: Associates a purchase order with a product and supplier using string IDs.
 * 2. Enum Usage: Employs OrderStatus enum for strict status checking.
 * 3. Constructor Overloading: Provides multiple constructors with sensible defaults.
 * ==============================================================================
 */
public class PurchaseOrder {

    private String id;
    private String productId;
    private String supplierId;
    private int quantity;
    private OrderStatus status;

    /**
     * Default constructor for Gson JSON deserialization.
     */
    public PurchaseOrder() {
        this.status = OrderStatus.PENDING;
    }

    /**
     * Overloaded constructor: Creates a new purchase order defaulting to PENDING status.
     *
     * @param id         Unique order identifier (e.g., "ORD-1001")
     * @param productId  ID of the product being ordered
     * @param supplierId ID of the supplier fulfilling the order
     * @param quantity   Quantity ordered (must be positive)
     */
    public PurchaseOrder(String id, String productId, String supplierId, int quantity) {
        this(id, productId, supplierId, quantity, OrderStatus.PENDING);
    }

    /**
     * Parameterized constructor specifying all fields including explicit status.
     *
     * @param id         Order ID
     * @param productId  Product ID
     * @param supplierId Supplier ID
     * @param quantity   Order Quantity
     * @param status     Current order status
     */
    public PurchaseOrder(String id, String productId, String supplierId, int quantity, OrderStatus status) {
        this.id = id;
        this.productId = productId;
        this.supplierId = supplierId;
        this.quantity = quantity;
        this.status = (status != null) ? status : OrderStatus.PENDING;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Converts Purchase Order into an array of objects for JTable rows.
     * Columns: [Order ID, Product ID, Supplier ID, Quantity, Status]
     */
    public Object[] toTableRow() {
        return new Object[]{ id, productId, supplierId, quantity, status.name() };
    }

    @Override
    public String toString() {
        return "Order #" + id + " [Product: " + productId + ", Supplier: " + supplierId +
                ", Qty: " + quantity + ", Status: " + status + "]";
    }
}

