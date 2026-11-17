package services;

import exceptions.ProductNotFoundException;
import models.OrderStatus;
import models.PurchaseOrder;
import storage.DataManager;
import utils.InputValidator;

import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 4: PURCHASE ORDERS & REPORTS LEAD
 * File: PurchaseOrderService.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Enum State Transitions: Manages lifecycle of Purchase Orders using OrderStatus
 *    (PENDING -> RECEIVED or PENDING -> CANCELLED).
 * 2. Inter-Service Collaboration: Calling receiveOrder() automatically triggers
 *    stockIn() inside InventoryService, illustrating component integration.
 * 3. Validation & Business Rule Enforcement.
 * ==============================================================================
 */
public class PurchaseOrderService {

    private final DataManager dataManager;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final SupplierService supplierService;

    /**
     * Constructor injecting all necessary services for full order lifecycle management.
     *
     * @param dataManager      Central storage manager
     * @param inventoryService Service to apply stock-in when order is received
     * @param productService   Service to verify products
     * @param supplierService  Service to verify suppliers
     */
    public PurchaseOrderService(DataManager dataManager,
                                InventoryService inventoryService,
                                ProductService productService,
                                SupplierService supplierService) {
        this.dataManager = dataManager;
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.supplierService = supplierService;
    }

    /**
     * Returns all purchase orders currently registered.
     *
     * @return List of PurchaseOrder objects
     */
    public List<PurchaseOrder> getAllOrders() {
        return dataManager.getOrders();
    }

    /**
     * Creates and records a new purchase order with PENDING status.
     *
     * @param orderId    Unique order code
     * @param productId  Target product ID
     * @param supplierId Supplying vendor ID
     * @param quantity   Quantity ordered (> 0)
     * @throws IllegalArgumentException If inputs are invalid or referenced IDs do not exist
     */
    public void createOrder(String orderId, String productId, String supplierId, int quantity)
            throws IllegalArgumentException {

        if (!InputValidator.isValidString(orderId)) {
            throw new IllegalArgumentException("Order ID cannot be empty.");
        }
        if (!InputValidator.isValidString(productId)) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }
        if (!InputValidator.isValidString(supplierId)) {
            throw new IllegalArgumentException("Supplier ID cannot be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero.");
        }

        // Check for duplicate order ID
        for (PurchaseOrder po : dataManager.getOrders()) {
            if (po.getId().equalsIgnoreCase(orderId.trim())) {
                throw new IllegalArgumentException("An order with ID '" + orderId + "' already exists!");
            }
        }

        // Verify product exists
        if (productService.getProductById(productId) == null) {
            throw new IllegalArgumentException("Product ID '" + productId + "' does not exist in inventory.");
        }

        // Verify supplier exists
        if (supplierService != null && !supplierService.hasSupplier(supplierId)) {
            throw new IllegalArgumentException("Supplier ID '" + supplierId + "' does not exist.");
        }

        PurchaseOrder newOrder = new PurchaseOrder(orderId.trim(), productId.trim(), supplierId.trim(), quantity);
        dataManager.getOrders().add(newOrder);
        dataManager.saveAllData();
    }

    /**
     * Marks a PENDING order as RECEIVED.
     * AUTOMATIC ACTION: Triggers inventoryService.stockIn() to increase warehouse inventory
     * and log an 'IN' audit transaction.
     *
     * @param orderId Target order ID
     * @throws ProductNotFoundException If product is missing
     * @throws IllegalStateException    If order status is not PENDING
     * @throws IllegalArgumentException If order is not found
     */
    public void receiveOrder(String orderId)
            throws ProductNotFoundException, IllegalStateException, IllegalArgumentException {

        PurchaseOrder order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Purchase Order '" + orderId + "' was not found.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be marked as RECEIVED. Current status: " + order.getStatus());
        }

        // Update status
        order.setStatus(OrderStatus.RECEIVED);

        // Automatically trigger inventory Stock-In
        inventoryService.stockIn(order.getProductId(), order.getQuantity());

        dataManager.saveAllData();
    }

    /**
     * Cancels a PENDING purchase order.
     * No inventory changes take place.
     *
     * @param orderId Target order ID
     * @throws IllegalStateException    If order is not in PENDING status
     * @throws IllegalArgumentException If order is not found
     */
    public void cancelOrder(String orderId) throws IllegalStateException, IllegalArgumentException {
        PurchaseOrder order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Purchase Order '" + orderId + "' was not found.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        dataManager.saveAllData();
    }

    /**
     * Searches for a PurchaseOrder by ID.
     *
     * @param orderId Target order ID
     * @return PurchaseOrder if found, null otherwise
     */
    public PurchaseOrder findOrderById(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (PurchaseOrder po : dataManager.getOrders()) {
            if (po.getId().equalsIgnoreCase(orderId.trim())) {
                return po;
            }
        }
        return null;
    }
}

