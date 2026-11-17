package services;

import exceptions.InsufficientStockException;
import exceptions.ProductNotFoundException;
import models.Product;
import models.Transaction;
import storage.DataManager;
import utils.InputValidator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 3: INVENTORY & TRANSACTIONS LEAD
 * File: InventoryService.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Java Collections Framework - LinkedList: Uses LinkedList to maintain transaction
 *    history for fast O(1) insertions at the head of the log.
 * 2. Iterator Interface: Traverses transaction history using java.util.Iterator.
 * 3. StringBuilder: Efficiently builds string audit reports without memory bloat.
 * 4. Custom Checked Exceptions: Throws InsufficientStockException when stock-out
 *    exceeds existing on-hand quantity.
 * ==============================================================================
 */
public class InventoryService {

    private final DataManager dataManager;
    private final ProductService productService;

    // SYLLABUS CONCEPT: LinkedList to store sequential transaction events
    private final LinkedList<Transaction> transactionHistory;

    /**
     * Constructor initializing dependencies and synchronizing the transaction list.
     *
     * @param dataManager    Centralized JSON data manager
     * @param productService Service managing product records
     */
    public InventoryService(DataManager dataManager, ProductService productService) {
        this.dataManager = dataManager;
        this.productService = productService;

        // Initialize LinkedList from loaded data
        this.transactionHistory = new LinkedList<>(dataManager.getTransactions());
    }

    /**
     * Records incoming stock (Stock-In) for a product and logs a transaction.
     *
     * @param productId Target product ID
     * @param quantity  Number of units to add (must be > 0)
     * @throws ProductNotFoundException If product is not registered
     * @throws IllegalArgumentException If quantity is invalid
     */
    public void stockIn(String productId, int quantity) throws ProductNotFoundException, IllegalArgumentException {
        if (!InputValidator.isValidString(productId)) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock-in quantity must be greater than zero.");
        }

        Product product = productService.findProductById(productId);
        int newQuantity = product.getQuantity() + quantity;
        product.setQuantity(newQuantity);

        // Record audit transaction
        logTransaction(productId, "IN", quantity);

        // Persist state
        dataManager.saveAllData();
    }

    /**
     * Records outgoing stock (Stock-Out) from warehouse.
     * Throws InsufficientStockException if inventory cannot fulfill the request.
     *
     * @param productId Target product ID
     * @param quantity  Number of units to remove (must be > 0)
     * @throws ProductNotFoundException   If product is not found
     * @throws InsufficientStockException If requested quantity exceeds available stock
     * @throws IllegalArgumentException   If quantity is invalid
     */
    public void stockOut(String productId, int quantity)
            throws ProductNotFoundException, InsufficientStockException, IllegalArgumentException {

        if (!InputValidator.isValidString(productId)) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock-out quantity must be greater than zero.");
        }

        Product product = productService.findProductById(productId);

        // SYLLABUS CONCEPT: Custom Exception condition
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(product.getQuantity(), quantity);
        }

        int newQuantity = product.getQuantity() - quantity;
        product.setQuantity(newQuantity);

        // Record audit transaction
        logTransaction(productId, "OUT", quantity);

        // Persist state
        dataManager.saveAllData();
    }

    /**
     * Helper to create a new Transaction, add to LinkedList and DataManager,
     * timestamped with current system time.
     */
    private void logTransaction(String productId, String type, int quantity) {
        String txnId = "TXN-" + System.currentTimeMillis();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        Transaction txn = new Transaction(txnId, productId, type, quantity, timestamp);

        // Add to front of LinkedList so latest transactions appear first
        transactionHistory.addFirst(txn);

        // Sync with master DataManager collection
        dataManager.getTransactions().add(0, txn);
    }

    /**
     * Returns the list of all recorded transactions.
     *
     * @return List of Transaction objects
     */
    public List<Transaction> getAllTransactions() {
        return transactionHistory;
    }

    /**
     * Generates a formatted text summary of all transactions.
     * SYLLABUS CONCEPTS DEMONSTRATED:
     * - Uses StringBuilder for fast, efficient string concatenation.
     * - Uses java.util.Iterator to traverse elements of LinkedList.
     *
     * @return String formatted log
     */
    public String getFormattedTransactionLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                                WAREHOUSE AUDIT TRANSACTION LOG                         \n");
        sb.append("========================================================================================\n");

        if (transactionHistory.isEmpty()) {
            sb.append("No transactions recorded yet.\n");
            return sb.toString();
        }

        int totalIn = 0;
        int totalOut = 0;

        // SYLLABUS CONCEPT: Explicit Iterator usage
        Iterator<Transaction> iterator = transactionHistory.iterator();
        int counter = 1;

        while (iterator.hasNext()) {
            Transaction t = iterator.next();
            if ("IN".equalsIgnoreCase(t.getType())) {
                totalIn += t.getQuantity();
            } else if ("OUT".equalsIgnoreCase(t.getType())) {
                totalOut += t.getQuantity();
            }

            String productName = "—";
            try {
                Product p = productService.getProductById(t.getProductId());
                if (p != null) {
                    productName = p.getName();
                }
            } catch (Exception ignored) {
            }

            if (productName.length() > 22) {
                productName = productName.substring(0, 19) + "...";
            }

            sb.append(String.format(
                "%2d. [%s] %-18s | %-3s | %-10s | %-22s | Qty: %-5d\n",
                counter++,
                t.getDate(),
                t.getTransactionId(),
                t.getType(),
                t.getProductId(),
                productName,
                t.getQuantity()
            ));
        }

        sb.append("========================================================================================\n");
        sb.append(String.format("Total Logged Events: %d  |  Total Received (IN): %d units  |  Total Dispatched (OUT): %d units\n",
                transactionHistory.size(), totalIn, totalOut));
        sb.append(String.format("Net Warehouse Stock Balance Change: %+d units\n", totalIn - totalOut));
        sb.append("========================================================================================\n");
        return sb.toString();
    }
}

