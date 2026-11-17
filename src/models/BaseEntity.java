package models;

/**
 * ==============================================================================
 * TEAM MEMBER 2: SUPPLIER & OOP HIERARCHY LEAD
 * File: BaseEntity.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Abstraction: Defines an abstract class that cannot be directly instantiated.
 * 2. Encapsulation: Protects fields using protected/private access and provides
 *    public getters and setters.
 * 3. Polymorphism: Forces subclasses to implement the abstract method toTableRow(),
 *    allowing polymorphic row rendering in Swing JTables.
 * ==============================================================================
 */
public abstract class BaseEntity {

    // Common attributes shared by all domain entities (Product, Supplier, etc.)
    protected String id;
    protected String name;

    /**
     * Default constructor required for JSON serialization/deserialization by Gson.
     */
    public BaseEntity() {
    }

    /**
     * Parameterized constructor to initialize common entity attributes.
     *
     * @param id   The unique identification code
     * @param name The human-readable name of the entity
     */
    public BaseEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Abstract method that must be implemented by any concrete subclass.
     * This converts the entity's attributes into an Object array for easy
     * insertion into a Swing JTable DefaultTableModel.
     *
     * @return Object[] array containing table cell values
     */
    public abstract Object[] toTableRow();

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name;
    }
}

