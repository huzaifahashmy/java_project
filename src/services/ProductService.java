package services;

import exceptions.DuplicateProductException;
import exceptions.ProductNotFoundException;
import models.Product;
import storage.DataManager;
import utils.InputValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 1: PRODUCT & VALIDATION LEAD
 * File: ProductService.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Encapsulation of Business Logic: Controls all operations on product data.
 * 2. Custom Exception Handling: Throws DuplicateProductException and
 *    ProductNotFoundException when invalid state is encountered.
 * 3. Validation Integration: Uses InputValidator to safeguard model integrity.
 * 4. Centralized Data Access: Coordinates with DataManager for in-memory caching
 *    and automatic JSON disk persistence.
 * ==============================================================================
 */
public class ProductService {

    private final DataManager dataManager;
    private SupplierService supplierService; // Optional dependency for referential integrity

    /**
     * Constructor injecting DataManager dependency.
     *
     * @param dataManager Centralized storage manager
     */
    public ProductService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Injects SupplierService to verify supplier existence when adding/updating products.
     *
     * @param supplierService Service managing suppliers
     */
    public void setSupplierService(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Returns the master list of all products in memory.
     *
     * @return List of Product entities
     */
    public List<Product> getAllProducts() {
        return dataManager.getProducts();
    }

    /**
     * Adds a new product to the warehouse system after rigorous validations.
     *
     * @param product The product to add
     * @throws DuplicateProductException If a product with the same ID already exists
     * @throws IllegalArgumentException  If any input field fails validation
     */
    public void addProduct(Product product) throws DuplicateProductException, IllegalArgumentException {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        // Validate basic strings
        if (!InputValidator.isValidString(product.getId())) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }
        if (!InputValidator.isValidString(product.getName())) {
            throw new IllegalArgumentException("Product Name cannot be empty.");
        }
        if (!InputValidator.isValidString(product.getSupplierId())) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }

        // Check if referenced supplier exists (referential integrity)
        if (supplierService != null && !supplierService.hasSupplier(product.getSupplierId())) {
            throw new IllegalArgumentException("Supplier ID '" + product.getSupplierId() + "' does not exist. Please register the supplier first.");
        }

        // Validate numeric constraints
        if (product.getPrice() <= 0.0) {
            throw new IllegalArgumentException("Product Price must be greater than 0.");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Product Quantity cannot be negative.");
        }

        // Check for duplicate ID using a simple loop (easy for viva explanation)
        for (Product existing : dataManager.getProducts()) {
            if (existing.getId().equalsIgnoreCase(product.getId().trim())) {
                throw new DuplicateProductException("A product with ID '" + product.getId() + "' already exists!");
            }
        }

        // Add to collection and persist
        dataManager.getProducts().add(product);
        dataManager.saveAllData();
    }

    /**
     * Updates an existing product's attributes.
     *
     * @param updatedProduct The updated product entity
     * @throws ProductNotFoundException If the product does not exist
     * @throws IllegalArgumentException If fields are invalid
     */
    public void updateProduct(Product updatedProduct) throws ProductNotFoundException, IllegalArgumentException {
        if (updatedProduct == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        if (!InputValidator.isValidString(updatedProduct.getName())) {
            throw new IllegalArgumentException("Product Name cannot be empty.");
        }
        if (!InputValidator.isValidString(updatedProduct.getSupplierId())) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }
        if (supplierService != null && !supplierService.hasSupplier(updatedProduct.getSupplierId())) {
            throw new IllegalArgumentException("Supplier ID '" + updatedProduct.getSupplierId() + "' does not exist.");
        }
        if (updatedProduct.getPrice() <= 0.0) {
            throw new IllegalArgumentException("Product Price must be greater than 0.");
        }
        if (updatedProduct.getQuantity() < 0) {
            throw new IllegalArgumentException("Product Quantity cannot be negative.");
        }

        Product existing = findProductById(updatedProduct.getId());
        existing.setName(updatedProduct.getName());
        existing.setSupplierId(updatedProduct.getSupplierId());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());

        dataManager.saveAllData();
    }

    /**
     * Deletes a product from the warehouse inventory by its ID.
     *
     * @param productId Unique identifier of product to remove
     * @throws ProductNotFoundException If product is not found
     */
    public void deleteProduct(String productId) throws ProductNotFoundException {
        Product existing = findProductById(productId);
        dataManager.getProducts().remove(existing);
        dataManager.saveAllData();
    }

    /**
     * Finds and returns a product by ID, or throws a checked exception if not found.
     *
     * @param productId Product ID to search for
     * @return The matching Product entity
     * @throws ProductNotFoundException If no matching product exists
     */
    public Product findProductById(String productId) throws ProductNotFoundException {
        if (!InputValidator.isValidString(productId)) {
            throw new ProductNotFoundException("Product ID cannot be empty.");
        }

        for (Product product : dataManager.getProducts()) {
            if (product.getId().equalsIgnoreCase(productId.trim())) {
                return product;
            }
        }
        throw new ProductNotFoundException("Product with ID '" + productId + "' was not found in inventory.");
    }

    /**
     * Safe search helper returning null instead of throwing an exception.
     *
     * @param productId Product ID
     * @return Product if found, null otherwise
     */
    public Product getProductById(String productId) {
        try {
            return findProductById(productId);
        } catch (ProductNotFoundException e) {
            return null;
        }
    }

    /**
     * Checks if any product in the inventory is associated with a given supplier ID.
     * Used by SupplierService to prevent deleting suppliers that have active products.
     *
     * @param supplierId The supplier ID to check
     * @return true if one or more products reference this supplier, false otherwise
     */
    public boolean hasProductWithSupplier(String supplierId) {
        if (supplierId == null) {
            return false;
        }
        for (Product p : dataManager.getProducts()) {
            if (supplierId.equalsIgnoreCase(p.getSupplierId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to directly adjust stock levels during inventory transactions.
     *
     * @param productId   Target product ID
     * @param newQuantity The updated stock quantity
     * @throws ProductNotFoundException If product does not exist
     */
    public void updateQuantity(String productId, int newQuantity) throws ProductNotFoundException {
        Product product = findProductById(productId);
        product.setQuantity(newQuantity);
        dataManager.saveAllData();
    }
}

