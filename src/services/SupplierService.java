package services;

import models.Supplier;
import storage.DataManager;
import utils.InputValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ==============================================================================
 * TEAM MEMBER 2: SUPPLIER & OOP HIERARCHY LEAD
 * File: SupplierService.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Java Collections Framework: Uses HashSet to perform efficient O(1) checks
 *    for unique supplier IDs during registration.
 * 2. Referential Integrity & Safety Checks: Protects against deleting suppliers
 *    that still have dependent products in ProductService.
 * 3. Separation of Concerns: Encapsulates supplier operations independently from UI.
 * ==============================================================================
 */
public class SupplierService {

    private final DataManager dataManager;
    private ProductService productService;

    /**
     * Constructor accepting DataManager.
     *
     * @param dataManager Central storage manager
     */
    public SupplierService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Overloaded constructor accepting both DataManager and ProductService.
     *
     * @param dataManager    Central storage manager
     * @param productService Product service to enforce safety checks
     */
    public SupplierService(DataManager dataManager, ProductService productService) {
        this.dataManager = dataManager;
        this.productService = productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves all suppliers stored in memory.
     *
     * @return List of Supplier entities
     */
    public List<Supplier> getAllSuppliers() {
        return dataManager.getSuppliers();
    }

    /**
     * Registers a new supplier after verifying unique ID using a HashSet.
     *
     * @param supplier The supplier to register
     * @throws IllegalArgumentException If fields are empty or ID is already taken
     */
    public void addSupplier(Supplier supplier) throws IllegalArgumentException {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null.");
        }

        if (!InputValidator.isValidString(supplier.getId())) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }
        if (!InputValidator.isValidString(supplier.getName())) {
            throw new IllegalArgumentException("Supplier Name cannot be empty.");
        }
        if (!InputValidator.isValidString(supplier.getContact())) {
            throw new IllegalArgumentException("Supplier Contact cannot be empty.");
        }
        if (!InputValidator.isValidString(supplier.getEmail())) {
            throw new IllegalArgumentException("Supplier Email cannot be empty.");
        }
        if (!InputValidator.isValidEmail(supplier.getEmail())) {
            throw new IllegalArgumentException("Invalid email format (must contain '@' and domain).");
        }

        // SYLLABUS CONCEPT: Using HashSet to check for unique supplier IDs
        Set<String> existingIds = new HashSet<>();
        for (Supplier s : dataManager.getSuppliers()) {
            existingIds.add(s.getId().trim().toLowerCase());
        }

        if (existingIds.contains(supplier.getId().trim().toLowerCase())) {
            throw new IllegalArgumentException("Supplier ID '" + supplier.getId() + "' already exists! (Verified via HashSet)");
        }

        dataManager.getSuppliers().add(supplier);
        dataManager.saveAllData();
    }

    /**
     * Updates details of an existing supplier.
     *
     * @param updatedSupplier The supplier object with modified values
     * @throws IllegalArgumentException If supplier is not found or inputs are invalid
     */
    public void updateSupplier(Supplier updatedSupplier) throws IllegalArgumentException {
        if (updatedSupplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null.");
        }
        if (!InputValidator.isValidString(updatedSupplier.getName())) {
            throw new IllegalArgumentException("Supplier Name cannot be empty.");
        }
        if (!InputValidator.isValidString(updatedSupplier.getContact())) {
            throw new IllegalArgumentException("Supplier Contact cannot be empty.");
        }
        if (!InputValidator.isValidEmail(updatedSupplier.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        Supplier existing = findSupplierById(updatedSupplier.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Supplier with ID '" + updatedSupplier.getId() + "' does not exist.");
        }

        existing.setName(updatedSupplier.getName());
        existing.setContact(updatedSupplier.getContact());
        existing.setEmail(updatedSupplier.getEmail());

        dataManager.saveAllData();
    }

    /**
     * Deletes a supplier by ID.
     * SAFETY CHECK: Throws IllegalStateException if any product is linked to this supplier.
     *
     * @param supplierId ID of the supplier to delete
     * @throws IllegalStateException If products depend on this supplier
     * @throws IllegalArgumentException If supplier does not exist
     */
    public void deleteSupplier(String supplierId) throws IllegalStateException, IllegalArgumentException {
        if (!InputValidator.isValidString(supplierId)) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }

        Supplier existing = findSupplierById(supplierId);
        if (existing == null) {
            throw new IllegalArgumentException("Supplier with ID '" + supplierId + "' was not found.");
        }

        // SAFETY CHECK: Prevent deletion if any product uses this supplier ID
        if (productService != null && productService.hasProductWithSupplier(supplierId)) {
            throw new IllegalStateException(
                "Safety Check Failed: Cannot delete supplier '" + supplierId +
                "' because one or more products are currently linked to this supplier!"
            );
        }

        dataManager.getSuppliers().remove(existing);
        dataManager.saveAllData();
    }

    /**
     * Searches for a supplier by ID.
     *
     * @param supplierId Target ID
     * @return Supplier object if found, null otherwise
     */
    public Supplier findSupplierById(String supplierId) {
        if (supplierId == null) {
            return null;
        }
        for (Supplier s : dataManager.getSuppliers()) {
            if (s.getId().equalsIgnoreCase(supplierId.trim())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Quick check if a supplier exists in the system.
     *
     * @param supplierId The supplier ID
     * @return true if exists, false otherwise
     */
    public boolean hasSupplier(String supplierId) {
        return findSupplierById(supplierId) != null;
    }
}

