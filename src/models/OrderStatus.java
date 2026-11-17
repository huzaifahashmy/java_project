package models;

/**
 * ==============================================================================
 * TEAM MEMBER 4: PURCHASE ORDERS & REPORTS LEAD
 * File: OrderStatus.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * - Java Enums (Enumerations): A special Java type representing a fixed set of constants.
 *   Provides compile-time type safety over arbitrary string or integer status flags.
 * ==============================================================================
 */
public enum OrderStatus {
    /**
     * The purchase order has been placed with the supplier and is awaiting delivery.
     */
    PENDING,

    /**
     * The order has been delivered and received at the warehouse.
     * Transitions to this state automatically trigger stock-in for the product.
     */
    RECEIVED,

    /**
     * The purchase order was cancelled before arrival; no warehouse stock change occurs.
     */
    CANCELLED
}

