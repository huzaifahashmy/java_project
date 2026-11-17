package storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.Product;
import models.PurchaseOrder;
import models.Supplier;
import models.Transaction;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 5: JSON STORAGE & GENERICS LEAD
 * File: DataManager.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Interface Implementation: Implements generic StorageService<Object>.
 * 2. Generics & Type Tokens: Overcomes Java Generic Type Erasure using Gson's TypeToken
 *    so JSON arrays correctly deserialize into strongly-typed lists.
 * 3. Exception Handling with try-catch-finally: Explicitly manages file resources
 *    and guarantees file stream closures in finally blocks.
 * 4. Resilient Persistence: Automatically creates missing data folders/files and
 *    recovers gracefully from corrupted JSON records.
 * ==============================================================================
 */
public class DataManager implements StorageService<Object> {

    // Relative file paths for flat JSON storage
    public static final String DATA_DIR = "data";
    public static final String PRODUCTS_FILE = DATA_DIR + File.separator + "products.json";
    public static final String SUPPLIERS_FILE = DATA_DIR + File.separator + "suppliers.json";
    public static final String ORDERS_FILE = DATA_DIR + File.separator + "orders.json";
    public static final String TRANSACTIONS_FILE = DATA_DIR + File.separator + "transactions.json";

    private final Gson gson;

    // In-memory master collections
    private List<Product> products;
    private List<Supplier> suppliers;
    private List<PurchaseOrder> orders;
    private List<Transaction> transactions;

    /**
     * Initializes the DataManager, configures Gson with pretty printing,
     * verifies storage directories, and loads all datasets into memory.
     */
    public DataManager() {
        // Pretty printing formats JSON with readable indentation
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.products = new ArrayList<>();
        this.suppliers = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.transactions = new ArrayList<>();

        // Ensure files and folders exist, then load data
        initializeStorage();
        loadAllData();
    }

    /**
     * Checks if the data/ directory and each JSON file exist.
     * If missing, creates them initialized with an empty JSON array: [].
     */
    private void initializeStorage() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        ensureEmptyJsonFile(PRODUCTS_FILE);
        ensureEmptyJsonFile(SUPPLIERS_FILE);
        ensureEmptyJsonFile(ORDERS_FILE);
        ensureEmptyJsonFile(TRANSACTIONS_FILE);
    }

    /**
     * Helper to write an empty JSON array "[]" if the file does not exist.
     */
    private void ensureEmptyJsonFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Writer writer = null;
            try {
                writer = new FileWriter(file);
                writer.write("[]");
            } catch (IOException e) {
                System.err.println("Could not create initial file: " + filePath + " - " + e.getMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        System.err.println("Error closing file writer: " + e.getMessage());
                    }
                }
            }
        }
    }

    // --- Generic Storage Implementation ---

    /**
     * Generic helper method to load a list of any model type from a JSON file.
     * Demonstrates try-catch-finally resource handling.
     *
     * @param <E>      Generic element type
     * @param filePath Path of the JSON file
     * @param type     Gson TypeToken representing List<E>
     * @return List of deserialized objects, or an empty list upon error
     */
    public <E> List<E> loadList(String filePath, Type type) {
        Reader reader = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                ensureEmptyJsonFile(filePath);
            }

            reader = new FileReader(filePath);
            List<E> data = gson.fromJson(reader, type);
            return (data != null) ? data : new ArrayList<>();

        } catch (Exception e) {
            // Handles missing, unreadable, or corrupted JSON gracefully
            System.err.println("Warning: Could not parse JSON from " + filePath +
                    ". Falling back to empty list. Details: " + e.getMessage());
            return new ArrayList<>();

        } finally {
            // finally block ensures file descriptor closure even if exceptions occur
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Failed to close reader for " + filePath + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Generic helper method to write a list of any model type to a JSON file.
     *
     * @param <E>      Generic element type
     * @param filePath Path of the destination JSON file
     * @param data     The list to serialize
     */
    public <E> void saveList(String filePath, List<E> data) {
        Writer writer = null;
        try {
            writer = new FileWriter(filePath);
            gson.toJson(data, writer);

        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());

        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Failed to close writer for " + filePath + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * StorageService interface implementation for generic loading.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Object> loadFromFile(String filePath, Type type) {
        return (List<Object>)(List<?>) loadList(filePath, type);
    }

    /**
     * StorageService interface implementation for generic saving.
     */
    @Override
    public void saveToFile(String filePath, List<Object> data) {
        saveList(filePath, data);
    }

    // --- Bulk Load and Save Methods ---

    /**
     * Reads all 4 JSON files into their respective in-memory collections.
     */
    public void loadAllData() {
        Type productType = new TypeToken<ArrayList<Product>>() {}.getType();
        this.products = loadList(PRODUCTS_FILE, productType);

        Type supplierType = new TypeToken<ArrayList<Supplier>>() {}.getType();
        this.suppliers = loadList(SUPPLIERS_FILE, supplierType);

        Type orderType = new TypeToken<ArrayList<PurchaseOrder>>() {}.getType();
        this.orders = loadList(ORDERS_FILE, orderType);

        Type txnType = new TypeToken<ArrayList<Transaction>>() {}.getType();
        this.transactions = loadList(TRANSACTIONS_FILE, txnType);
    }

    /**
     * Writes all in-memory collections to the 4 respective JSON files.
     * Centralized saving guarantees data consistency.
     */
    public void saveAllData() {
        saveList(PRODUCTS_FILE, products);
        saveList(SUPPLIERS_FILE, suppliers);
        saveList(ORDERS_FILE, orders);
        saveList(TRANSACTIONS_FILE, transactions);
    }

    // --- Accessors for In-Memory Master Collections ---

    public List<Product> getProducts() {
        return products;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public List<PurchaseOrder> getOrders() {
        return orders;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}

