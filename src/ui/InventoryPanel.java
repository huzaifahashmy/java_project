package ui;

import exceptions.InsufficientStockException;
import exceptions.ProductNotFoundException;
import models.Product;
import models.Supplier;
import models.Transaction;
import services.InventoryService;
import services.ProductService;
import services.SupplierService;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: InventoryPanel.java
 *
 * Clean, minimalistic inventory management:
 * - Cascading Supplier -> Product dropdown selection
 * - Live stock level display
 * - Stock-In and Stock-Out operations with persistence
 * - Transaction audit history table with product names
 * ==============================================================================
 */
public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final SupplierService supplierService;

    // Form inputs
    private JComboBox<String> cmbSupplier;
    private JComboBox<String> cmbProduct;
    private JTextField txtQuantity;
    private JLabel lblAvailableStock;

    // Table
    private JTable txnTable;
    private DefaultTableModel tableModel;

    public InventoryPanel(InventoryService inventoryService,
                          ProductService productService,
                          SupplierService supplierService) {

        this.inventoryService = inventoryService;
        this.productService = productService;
        this.supplierService = supplierService;

        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(18, 18));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- TOP: HEADING & FORM CARD ---
        JPanel topContainer = new JPanel(new BorderLayout(12, 12));
        topContainer.setOpaque(false);

        JLabel heading = new JLabel("Inventory");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        topContainer.add(heading, BorderLayout.NORTH);

        // White Card for Stock Operations
        JPanel formCard = UITheme.createCard();

        JLabel formTitle = new JLabel("Stock Movement");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        formTitle.setForeground(UITheme.TEXT_DARK);
        formCard.add(formTitle, BorderLayout.NORTH);

        // Form Grid (2 Rows x 4 Columns)
        JPanel formGrid = new JPanel(new GridLayout(2, 4, 14, 10));
        formGrid.setOpaque(false);

        cmbSupplier = UITheme.createComboBox();
        cmbProduct = UITheme.createComboBox();
        txtQuantity = UITheme.createTextField();

        lblAvailableStock = new JLabel("Select a product");
        lblAvailableStock.setFont(new Font("Arial", Font.BOLD, 13));
        lblAvailableStock.setForeground(UITheme.TEXT_DARK);

        // Event listeners for cascading dropdown
        cmbSupplier.addActionListener(e -> updateProductDropdown());
        cmbProduct.addActionListener(e -> updateStockDisplay());

        formGrid.add(UITheme.createFieldLabel("Filter by Supplier:"));
        formGrid.add(cmbSupplier);
        formGrid.add(UITheme.createFieldLabel("Select Product:"));
        formGrid.add(cmbProduct);

        formGrid.add(UITheme.createFieldLabel("Movement Quantity:"));
        formGrid.add(txtQuantity);
        formGrid.add(UITheme.createFieldLabel("Current Available Stock:"));
        formGrid.add(lblAvailableStock);

        formCard.add(formGrid, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonRow.setOpaque(false);

        JButton btnStockIn = UITheme.createGreenButton("Stock In");
        JButton btnStockOut = UITheme.createRedButton("Stock Out");
        JButton btnViewLog = UITheme.createButton("View Audit Log");
        JButton btnClear = UITheme.createButton("Clear Form");

        btnStockIn.addActionListener(e -> handleStockIn());
        btnStockOut.addActionListener(e -> handleStockOut());
        btnViewLog.addActionListener(e -> showAuditLogDialog());
        btnClear.addActionListener(e -> {
            clearForm();
            refreshData();
        });

        buttonRow.add(btnStockIn);
        buttonRow.add(btnStockOut);
        buttonRow.add(btnViewLog);
        buttonRow.add(btnClear);

        formCard.add(buttonRow, BorderLayout.SOUTH);
        topContainer.add(formCard, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: TABLE CARD ---
        JPanel tableCard = UITheme.createCard();

        JLabel tableTitle = new JLabel("Transaction History");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableTitle.setForeground(UITheme.TEXT_DARK);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        // Table
        String[] columns = {"Transaction ID", "Product ID", "Product Name", "Movement Type", "Quantity", "Date & Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        txnTable = new JTable(tableModel);
        UITheme.applyTableStyle(txnTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        txnTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        txnTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        txnTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        txnTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(txnTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void updateSupplierDropdown() {
        String selected = (String) cmbSupplier.getSelectedItem();
        cmbSupplier.removeAllItems();

        cmbSupplier.addItem("(All Suppliers)");
        if (supplierService != null) {
            for (Supplier s : supplierService.getAllSuppliers()) {
                cmbSupplier.addItem(s.getId() + " - " + s.getName());
            }
        }

        if (selected != null) {
            cmbSupplier.setSelectedItem(selected);
        }
    }

    private void updateProductDropdown() {
        cmbProduct.removeAllItems();

        String selectedSupplier = (String) cmbSupplier.getSelectedItem();
        String supplierId = null;

        if (selectedSupplier != null && !selectedSupplier.startsWith("(") && selectedSupplier.contains(" - ")) {
            supplierId = selectedSupplier.split(" - ")[0].trim();
        }

        List<Product> products = new ArrayList<>();
        if (supplierId == null) {
            products.addAll(productService.getAllProducts());
        } else {
            for (Product p : productService.getAllProducts()) {
                if (supplierId.equalsIgnoreCase(p.getSupplierId())) {
                    products.add(p);
                }
            }
        }

        if (products.isEmpty()) {
            cmbProduct.addItem("(No products available)");
        } else {
            for (Product p : products) {
                cmbProduct.addItem(p.getId() + " - " + p.getName());
            }
        }

        updateStockDisplay();
    }

    private String getSelectedProductId() {
        String item = (String) cmbProduct.getSelectedItem();
        if (item == null || item.startsWith("(")) return null;
        return item.split(" - ")[0].trim();
    }

    private void updateStockDisplay() {
        String productId = getSelectedProductId();
        if (productId == null) {
            lblAvailableStock.setText("Select a product");
            lblAvailableStock.setForeground(UITheme.TEXT_MUTED);
            return;
        }

        Product p = productService.getProductById(productId);
        if (p != null) {
            lblAvailableStock.setText(p.getQuantity() + " units in stock");
            if (p.getQuantity() < 10) {
                lblAvailableStock.setForeground(UITheme.BTN_RED);
            } else {
                lblAvailableStock.setForeground(UITheme.TEXT_DARK);
            }
        } else {
            lblAvailableStock.setText("Product not found");
            lblAvailableStock.setForeground(UITheme.BTN_RED);
        }
    }

    private void handleStockIn() {
        String productId = getSelectedProductId();
        String qtyStr = txtQuantity.getText().trim();

        if (productId == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid product.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isPositiveInt(qtyStr)) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive integer (> 0).", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = Integer.parseInt(qtyStr);

        try {
            inventoryService.stockIn(productId, quantity);
            JOptionPane.showMessageDialog(this, "Successfully added " + quantity + " units to " + productId + "!", "Stock In Completed", JOptionPane.INFORMATION_MESSAGE);
            txtQuantity.setText("");
            refreshData();
        } catch (ProductNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleStockOut() {
        String productId = getSelectedProductId();
        String qtyStr = txtQuantity.getText().trim();

        if (productId == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid product.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isPositiveInt(qtyStr)) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive integer (> 0).", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = Integer.parseInt(qtyStr);

        try {
            inventoryService.stockOut(productId, quantity);
            JOptionPane.showMessageDialog(this, "Successfully dispatched " + quantity + " units from " + productId + "!", "Stock Out Completed", JOptionPane.INFORMATION_MESSAGE);
            txtQuantity.setText("");
            refreshData();
        } catch (InsufficientStockException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
        } catch (ProductNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showAuditLogDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Warehouse Audit Log", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(800, 540);
        dialog.setMinimumSize(new Dimension(680, 420));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(14, 14));

        // --- TOP: HEADER & STATS SUMMARY ---
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 20, 14, 20)
        ));

        JLabel titleLbl = new JLabel("Warehouse Transaction Audit Log");
        titleLbl.setFont(new Font("Arial", Font.BOLD, 18));
        titleLbl.setForeground(UITheme.TEXT_DARK);

        JLabel subLbl = new JLabel("Chronological stock movement history generated via StringBuilder and java.util.Iterator.");
        subLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        subLbl.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);
        titleBox.add(titleLbl);
        titleBox.add(subLbl);
        topPanel.add(titleBox, BorderLayout.NORTH);

        // Stats summary chips
        List<Transaction> txns = inventoryService.getAllTransactions();
        int totalIn = 0;
        int totalOut = 0;
        for (Transaction t : txns) {
            if ("IN".equalsIgnoreCase(t.getType())) {
                totalIn += t.getQuantity();
            } else if ("OUT".equalsIgnoreCase(t.getType())) {
                totalOut += t.getQuantity();
            }
        }

        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        statsBar.setOpaque(false);
        statsBar.add(createLogMetricBadge("Total Events", String.valueOf(txns.size())));
        statsBar.add(createLogMetricBadge("Total Inbound", totalIn + " units"));
        statsBar.add(createLogMetricBadge("Total Outbound", totalOut + " units"));
        statsBar.add(createLogMetricBadge("Net Warehouse Stock", String.format("%+d units", totalIn - totalOut)));
        topPanel.add(statsBar, BorderLayout.SOUTH);

        dialog.add(topPanel, BorderLayout.NORTH);

        // --- CENTER: FORMATTED LOG TEXT AREA ---
        JTextArea logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setBackground(new Color(248, 250, 252));
        logArea.setForeground(UITheme.TEXT_DARK);
        logArea.setText(inventoryService.getFormattedTransactionLog());
        logArea.setCaretPosition(0);
        logArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 0, 16),
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1)
        ));
        dialog.add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM: ACTION BUTTONS ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottomBar.setBackground(UITheme.BG_MAIN);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton btnCopy = UITheme.createButton("Copy to Clipboard");
        JButton btnReload = UITheme.createButton("Refresh");
        JButton btnClose = UITheme.createPrimaryButton("Close");

        btnCopy.addActionListener(e -> {
            try {
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(logArea.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
                JOptionPane.showMessageDialog(dialog, "Audit log copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Could not copy: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnReload.addActionListener(e -> {
            logArea.setText(inventoryService.getFormattedTransactionLog());
            logArea.setCaretPosition(0);
        });

        btnClose.addActionListener(e -> dialog.dispose());

        bottomBar.add(btnCopy);
        bottomBar.add(btnReload);
        bottomBar.add(btnClose);

        dialog.add(bottomBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createLogMetricBadge(String label, String value) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badge.setBackground(new Color(241, 245, 249));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 232), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.BOLD, 12));
        val.setForeground(UITheme.TEXT_DARK);

        badge.add(lbl);
        badge.add(val);
        return badge;
    }

    private void clearForm() {
        txtQuantity.setText("");
        if (cmbSupplier.getItemCount() > 0) {
            cmbSupplier.setSelectedIndex(0);
        }
        updateProductDropdown();
    }

    public void refreshData() {
        updateSupplierDropdown();
        updateProductDropdown();
        populateTransactionTable();
    }

    private void populateTransactionTable() {
        tableModel.setRowCount(0);
        List<Transaction> txns = inventoryService.getAllTransactions();

        for (Transaction t : txns) {
            String productName = "—";
            try {
                Product p = productService.getProductById(t.getProductId());
                if (p != null) {
                    productName = p.getName();
                }
            } catch (Exception ignored) {
            }

            tableModel.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getProductId(),
                    productName,
                    t.getType(),
                    t.getQuantity(),
                    t.getDate()
            });
        }
    }
}
