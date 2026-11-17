package main;

import models.OrderStatus;
import models.Product;
import models.PurchaseOrder;
import models.Supplier;
import models.Transaction;
import services.*;
import storage.DataManager;
import ui.MainFrame;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: Main.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Entry Point: public static void main(String[] args).
 * 2. Dependency Injection: Instantiates core storage and services in proper
 *    topological order and wires them together via constructors.
 * 3. Swing Event Dispatch Thread (EDT): Launches the UI using SwingUtilities.invokeLater()
 *    to adhere to Java Swing thread-safety guidelines.
 * 4. Sample Data Initialization: Pre-populates realistic data if storage is empty,
 *    giving students an immediate, fully functional showcase for viva evaluation.
 * ==============================================================================
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize centralized storage manager (reads JSON files into memory)
        DataManager dataManager = new DataManager();

        // 2. Instantiate business services and wire dependencies
        ProductService productService = new ProductService(dataManager);
        SupplierService supplierService = new SupplierService(dataManager, productService);

        // Bi-directional wiring so ProductService can also validate supplier IDs
        productService.setSupplierService(supplierService);

        InventoryService inventoryService = new InventoryService(dataManager, productService);
        PurchaseOrderService orderService = new PurchaseOrderService(dataManager, inventoryService, productService, supplierService);
        ReportService reportService = new ReportService(productService, supplierService);

        // 3. Seed sample data if system is freshly initialized with empty collections
        seedSampleDataIfEmpty(dataManager);

        // 4. Launch Desktop GUI safely on Swing's Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            new MainFrame(
                    dataManager,
                    productService,
                    supplierService,
                    inventoryService,
                    orderService,
                    reportService
            );
        });
    }

    /**
     * Seeds initial realistic demo data if the system files are empty.
     * This provides the evaluation panel with immediate visual data to inspect
     * without having to manually type records first.
     */
    private static void seedSampleDataIfEmpty(DataManager dataManager) {
        if (dataManager.getSuppliers().isEmpty() && dataManager.getProducts().isEmpty()) {
            System.out.println("Empty warehouse detected: Seeding initial demo records...");

            // Initial Suppliers
            Supplier s1 = new Supplier("SUP-101", "Nexus Tech Components", "+1-555-0199", "sales@nexustech.com");
            Supplier s2 = new Supplier("SUP-102", "Apex Industrial Gear", "+1-555-0144", "orders@apexgear.com");
            Supplier s3 = new Supplier("SUP-103", "Prime Silicon Corp", "+1-555-0182", "contact@primesilicon.com");

            dataManager.getSuppliers().add(s1);
            dataManager.getSuppliers().add(s2);
            dataManager.getSuppliers().add(s3);

            // Initial Products (Some low stock (< 10) to showcase Stream API low-stock filter)
            Product p1 = new Product("PROD-001", "Ergonomic Mechanical Keyboard", "SUP-101", 89.99, 45);
            Product p2 = new Product("PROD-002", "27-inch 4K IPS Monitor", "SUP-101", 299.50, 4); // Low Stock alert!
            Product p3 = new Product("PROD-003", "Heavy Duty Steel Shelving Unit", "SUP-102", 150.00, 18);
            Product p4 = new Product("PROD-004", "Industrial Barcode Scanner", "SUP-102", 75.25, 6); // Low Stock alert!
            Product p5 = new Product("PROD-005", "Cat6 Ethernet Spool (1000ft)", "SUP-103", 119.00, 22);

            dataManager.getProducts().add(p1);
            dataManager.getProducts().add(p2);
            dataManager.getProducts().add(p3);
            dataManager.getProducts().add(p4);
            dataManager.getProducts().add(p5);

            // Initial Purchase Orders
            PurchaseOrder o1 = new PurchaseOrder("ORD-2001", "PROD-002", "SUP-101", 10, OrderStatus.PENDING);
            PurchaseOrder o2 = new PurchaseOrder("ORD-2002", "PROD-004", "SUP-102", 15, OrderStatus.RECEIVED);

            dataManager.getOrders().add(o1);
            dataManager.getOrders().add(o2);

            // Initial Transactions
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Transaction t1 = new Transaction("TXN-10001", "PROD-001", "IN", 45, now);
            Transaction t2 = new Transaction("TXN-10002", "PROD-002", "IN", 14, now);
            Transaction t3 = new Transaction("TXN-10003", "PROD-002", "OUT", 10, now);

            dataManager.getTransactions().add(t1);
            dataManager.getTransactions().add(t2);
            dataManager.getTransactions().add(t3);

            dataManager.saveAllData();
            System.out.println("Demo records initialized successfully.");
        }
    }
}

