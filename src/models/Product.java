package models;

/**
 * ==============================================================================
 * TEAM MEMBER 1: PRODUCT & VALIDATION LEAD
 * File: Product.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Inheritance: Extends BaseEntity to reuse id and name attributes.
 * 2. Constructor Overloading: Multiple constructors providing flexibility when
 *    instantiating a Product (e.g., with or without initial stock quantity).
 * 3. super Keyword: Explicitly calls the parent constructor BaseEntity(id, name).
 * 4. Method Overriding: Implements abstract toTableRow() from BaseEntity.
 * ==============================================================================
 */
public class Product extends BaseEntity {

    // Unique ID of the supplier providing this product (flat reference, no circular JSON nesting)
    private String supplierId;
    private double price;
    private int quantity;

    /**
     * Default constructor required for JSON serialization/deserialization by Gson.
     */
    public Product() {
        super();
    }

    /**
     * Full parameterized constructor initializing all attributes.
     *
     * @param id         Unique product code (e.g., "PROD-001")
     * @param name       Product name / description
     * @param supplierId ID of the supplier supplying this item
     * @param price      Unit price (must be positive)
     * @param quantity   Current inventory stock level (must be non-negative)
     */
    public Product(String id, String name, String supplierId, double price, int quantity) {
        super(id, name); // Passes id and name to BaseEntity
        this.supplierId = supplierId;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Overloaded constructor: Allows creating a product with an initial stock of 0.
     * Demonstrates Constructor Overloading for academic viva.
     *
     * @param id         Unique product code
     * @param name       Product name
     * @param supplierId ID of the supplier
     * @param price      Unit price
     */
    public Product(String id, String name, String supplierId, double price) {
        this(id, name, supplierId, price, 0); // Calls the 5-parameter constructor
    }

    // --- Getters and Setters ---

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Overrides abstract method in BaseEntity to return table-compatible row values.
     * Table columns: [Product ID, Product Name, Supplier ID, Price ($), Stock Quantity]
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{
            id,
            name,
            supplierId,
            String.format("%.2f", price),
            quantity
        };
    }

    @Override
    public String toString() {
        return "Product [" + super.toString() + ", SupplierID: " + supplierId +
                ", Price: $" + price + ", Qty: " + quantity + "]";
    }
}

