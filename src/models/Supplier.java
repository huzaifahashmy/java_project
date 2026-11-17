package models;

/**
 * ==============================================================================
 * TEAM MEMBER 2: SUPPLIER & OOP HIERARCHY LEAD
 * File: Supplier.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Inheritance: Extends BaseEntity using the 'extends' keyword.
 * 2. super Keyword: Invokes parent constructor using super(id, name).
 * 3. Method Overriding: Overrides the abstract toTableRow() method.
 * ==============================================================================
 */
public class Supplier extends BaseEntity {

    private String contact;
    private String email;

    /**
     * Default constructor for Gson JSON deserialization.
     */
    public Supplier() {
        super();
    }

    /**
     * Parameterized constructor initializing both parent and child fields.
     *
     * @param id      Unique supplier identifier (e.g., "SUP-101")
     * @param name    Supplier or company name
     * @param contact Phone number or primary contact info
     * @param email   Official supplier email address
     */
    public Supplier(String id, String name, String contact, String email) {
        super(id, name); // Calls BaseEntity constructor
        this.contact = contact;
        this.email = email;
    }

    // --- Getters and Setters ---

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Converts Supplier data into an array of objects to populate JTable rows.
     * Order matches table columns: [Supplier ID, Name, Contact, Email]
     */
    @Override
    public Object[] toTableRow() {
        return new Object[]{ id, name, contact, email };
    }

    @Override
    public String toString() {
        return "Supplier [" + super.toString() + ", Contact: " + contact + ", Email: " + email + "]";
    }
}

