package ui;

import exceptions.DuplicateProductException;
import exceptions.ProductNotFoundException;
import models.Product;
import models.Supplier;
import services.ProductService;
import services.SupplierService;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: ProductPanel.java
 *
 * Simple, clean product management:
 * - Auto-suggested ID
 * - Supplier dropdown selection
 * - Live search filter
 * - Form and Table in clean cards
 * ==============================================================================
 */
public class ProductPanel extends JPanel {

    private final ProductService productService;
    private final SupplierService supplierService;

    // Form inputs
    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> cmbSupplier;
    private JTextField txtPrice;
    private JTextField txtQuantity;

    // Search bar
    private JTextField txtSearch;

    // Table components
    private JTable productTable;
    private DefaultTableModel tableModel;

    public ProductPanel(ProductService productService, SupplierService supplierService) {
        this.productService = productService;
        this.supplierService = supplierService;

        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(18, 18));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshData();
    }

    public ProductPanel(ProductService productService) {
        this(productService, null);
    }

    private void initComponents() {
        // --- TOP: HEADING & FORM CARD ---
        JPanel topContainer = new JPanel(new BorderLayout(12, 12));
        topContainer.setOpaque(false);

        JLabel heading = new JLabel("Products");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        topContainer.add(heading, BorderLayout.NORTH);

        // White Card for Form Input
        JPanel formCard = UITheme.createCard();

        JLabel formTitle = new JLabel("Product Details");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        formTitle.setForeground(UITheme.TEXT_DARK);
        formCard.add(formTitle, BorderLayout.NORTH);

        // Form Grid (2 Rows x 6 Columns)
        JPanel formGrid = new JPanel(new GridLayout(2, 6, 12, 8));
        formGrid.setOpaque(false);

        txtId = UITheme.createTextField();
        txtName = UITheme.createTextField();
        cmbSupplier = UITheme.createComboBox();
        txtPrice = UITheme.createTextField();
        txtQuantity = UITheme.createTextField();

        formGrid.add(new JLabel("Product ID:"));
        formGrid.add(txtId);
        formGrid.add(new JLabel("Product Name:"));
        formGrid.add(txtName);
        formGrid.add(new JLabel("Supplier:"));
        formGrid.add(cmbSupplier);

        formGrid.add(new JLabel("Price ($):"));
        formGrid.add(txtPrice);
        formGrid.add(new JLabel("Quantity:"));
        formGrid.add(txtQuantity);
        formGrid.add(new JLabel("")); // Empty cells
        formGrid.add(new JLabel(""));

        formCard.add(formGrid, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonRow.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Product");
        JButton btnUpdate = UITheme.createButton("Update Product");
        JButton btnDelete = UITheme.createRedButton("Delete Product");
        JButton btnClear = UITheme.createButton("Clear Form");

        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> {
            clearForm();
            refreshData();
        });

        buttonRow.add(btnAdd);
        buttonRow.add(btnUpdate);
        buttonRow.add(btnDelete);
        buttonRow.add(btnClear);

        formCard.add(buttonRow, BorderLayout.SOUTH);
        topContainer.add(formCard, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: TABLE CARD ---
        JPanel tableCard = UITheme.createCard();

        // Table Header with Search
        JPanel tableHeader = new JPanel(new BorderLayout(10, 10));
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Product List");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableTitle.setForeground(UITheme.TEXT_DARK);
        tableHeader.add(tableTitle, BorderLayout.WEST);

        // Search Bar on Right
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_LABEL);

        txtSearch = UITheme.createTextField();
        txtSearch.setPreferredSize(new Dimension(180, 32));

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        searchBox.add(searchLbl);
        searchBox.add(txtSearch);
        tableHeader.add(searchBox, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        // Table
        String[] columns = {"Product ID", "Product Name", "Supplier ID", "Price ($)", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        UITheme.applyTableStyle(productTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                String id = tableModel.getValueAt(row, 0).toString();
                String name = tableModel.getValueAt(row, 1).toString();
                String supId = tableModel.getValueAt(row, 2).toString();
                String price = tableModel.getValueAt(row, 3).toString();
                String qty = tableModel.getValueAt(row, 4).toString();

                txtId.setText(id);
                txtName.setText(name);
                txtPrice.setText(price);
                txtQuantity.setText(qty);

                selectSupplierInDropdown(supId);
                txtId.setEditable(false);
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private String suggestNextProductId() {
        int maxNum = 0;
        for (Product p : productService.getAllProducts()) {
            try {
                String digits = p.getId().replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    int val = Integer.parseInt(digits);
                    if (val > maxNum) maxNum = val;
                }
            } catch (Exception ignored) {}
        }
        return String.format("PROD-%03d", maxNum + 1);
    }

    private void updateSupplierDropdown() {
        String current = (String) cmbSupplier.getSelectedItem();
        cmbSupplier.removeAllItems();

        if (supplierService != null && !supplierService.getAllSuppliers().isEmpty()) {
            for (Supplier s : supplierService.getAllSuppliers()) {
                cmbSupplier.addItem(s.getId() + " - " + s.getName());
            }
        } else {
            cmbSupplier.addItem("(No suppliers registered)");
        }

        if (current != null) {
            cmbSupplier.setSelectedItem(current);
        }
    }

    private void selectSupplierInDropdown(String supplierId) {
        if (supplierId == null) return;
        for (int i = 0; i < cmbSupplier.getItemCount(); i++) {
            String item = cmbSupplier.getItemAt(i);
            if (item != null && item.startsWith(supplierId + " -")) {
                cmbSupplier.setSelectedIndex(i);
                return;
            }
        }
    }

    private String getSelectedSupplierId() {
        String item = (String) cmbSupplier.getSelectedItem();
        if (item == null || item.startsWith("(")) return null;
        return item.split(" - ")[0].trim();
    }

    private void addProduct() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String supplierId = getSelectedSupplierId();
        String priceStr = txtPrice.getText().trim();
        String qtyStr = txtQuantity.getText().trim();

        if (!InputValidator.isValidString(id) || !InputValidator.isValidString(name)) {
            JOptionPane.showMessageDialog(this, "Please enter Product ID and Name.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (supplierId == null) {
            JOptionPane.showMessageDialog(this, "Please select a Supplier from the dropdown.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isPositiveDouble(priceStr)) {
            JOptionPane.showMessageDialog(this, "Price must be a positive number (> 0).", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isNonNegativeInt(qtyStr)) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid non-negative integer (>= 0).", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price = Double.parseDouble(priceStr);
        int quantity = Integer.parseInt(qtyStr);

        Product newProduct = new Product(id, name, supplierId, price, quantity);

        try {
            productService.addProduct(newProduct);
            JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (DuplicateProductException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Duplicate Product", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateProduct() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String supplierId = getSelectedSupplierId();
        String priceStr = txtPrice.getText().trim();
        String qtyStr = txtQuantity.getText().trim();

        if (!InputValidator.isValidString(id)) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (supplierId == null) {
            JOptionPane.showMessageDialog(this, "Please select a Supplier.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isPositiveDouble(priceStr) || !InputValidator.isNonNegativeInt(qtyStr)) {
            JOptionPane.showMessageDialog(this, "Please enter valid price and quantity.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price = Double.parseDouble(priceStr);
        int quantity = Integer.parseInt(qtyStr);

        Product updatedProduct = new Product(id, name, supplierId, price, quantity);

        try {
            productService.updateProduct(updatedProduct);
            JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (ProductNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Not Found", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteProduct() {
        String id = txtId.getText().trim();
        if (!InputValidator.isValidString(id)) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Product '" + id + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productService.deleteProduct(id);
                JOptionPane.showMessageDialog(this, "Product deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshData();
            } catch (ProductNotFoundException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Product p : productService.getAllProducts()) {
            if (query.isEmpty() ||
                p.getId().toLowerCase().contains(query) ||
                p.getName().toLowerCase().contains(query) ||
                p.getSupplierId().toLowerCase().contains(query)) {

                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getSupplierId(),
                        String.format("%.2f", p.getPrice()),
                        p.getQuantity()
                });
            }
        }
    }

    private void clearForm() {
        txtId.setText(suggestNextProductId());
        txtId.setEditable(true);
        txtName.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");
        txtSearch.setText("");
        if (cmbSupplier.getItemCount() > 0) {
            cmbSupplier.setSelectedIndex(0);
        }
        productTable.clearSelection();
    }

    public void refreshData() {
        updateSupplierDropdown();
        txtId.setText(suggestNextProductId());
        filterTable();
    }
}
