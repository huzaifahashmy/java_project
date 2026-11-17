package ui;

import exceptions.ProductNotFoundException;
import models.OrderStatus;
import models.Product;
import models.PurchaseOrder;
import models.Supplier;
import services.ProductService;
import services.PurchaseOrderService;
import services.SupplierService;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: OrderPanel.java
 *
 * Clean, minimalistic purchase order management:
 * - Auto-generated Order ID (read-only, no manual typing)
 * - Cascading Supplier -> Product dropdown selection
 * - Real-time estimated order cost calculation
 * - Order fulfillment: Mark as Received (updates inventory) and Cancel
 * ==============================================================================
 */
public class OrderPanel extends JPanel {

    private final PurchaseOrderService orderService;
    private final ProductService productService;
    private final SupplierService supplierService;

    // Form inputs
    private JTextField txtOrderId;
    private JComboBox<String> cmbSupplier;
    private JComboBox<String> cmbProduct;
    private JTextField txtQuantity;
    private JLabel lblEstimatedCost;

    // Table
    private JTable orderTable;
    private DefaultTableModel tableModel;

    public OrderPanel(PurchaseOrderService orderService,
                      ProductService productService,
                      SupplierService supplierService) {

        this.orderService = orderService;
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

        JLabel heading = new JLabel("Purchase Orders");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        topContainer.add(heading, BorderLayout.NORTH);

        // White Card for Form Input
        JPanel formCard = UITheme.createCard();

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Create Purchase Order");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        formTitle.setForeground(UITheme.TEXT_DARK);

        lblEstimatedCost = new JLabel("Estimated Total: $0.00");
        lblEstimatedCost.setFont(new Font("Arial", Font.BOLD, 13));
        lblEstimatedCost.setForeground(UITheme.TEXT_DARK);

        cardHeader.add(formTitle, BorderLayout.WEST);
        cardHeader.add(lblEstimatedCost, BorderLayout.EAST);
        formCard.add(cardHeader, BorderLayout.NORTH);

        // Form Grid (2 Rows x 4 Columns)
        JPanel formGrid = new JPanel(new GridLayout(2, 4, 14, 10));
        formGrid.setOpaque(false);

        txtOrderId = UITheme.createTextField();
        txtOrderId.setEditable(false);
        txtOrderId.setBackground(new Color(245, 247, 250));

        cmbSupplier = UITheme.createComboBox();
        cmbProduct = UITheme.createComboBox();
        txtQuantity = UITheme.createTextField();

        // Event listeners for cascading selection & cost estimation
        cmbSupplier.addActionListener(e -> updateProductDropdown());
        cmbProduct.addActionListener(e -> updateEstimatedCost());

        txtQuantity.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateEstimatedCost(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateEstimatedCost(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateEstimatedCost(); }
        });

        formGrid.add(UITheme.createFieldLabel("Order ID (Auto-Generated):"));
        formGrid.add(txtOrderId);
        formGrid.add(UITheme.createFieldLabel("Select Supplier:"));
        formGrid.add(cmbSupplier);

        formGrid.add(UITheme.createFieldLabel("Select Product:"));
        formGrid.add(cmbProduct);
        formGrid.add(UITheme.createFieldLabel("Order Quantity:"));
        formGrid.add(txtQuantity);

        formCard.add(formGrid, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonRow.setOpaque(false);

        JButton btnCreate = UITheme.createPrimaryButton("Create Order");
        JButton btnReceive = UITheme.createGreenButton("Mark as Received");
        JButton btnCancel = UITheme.createRedButton("Cancel Order");
        JButton btnClear = UITheme.createButton("Clear Form");

        btnCreate.addActionListener(e -> createOrder());
        btnReceive.addActionListener(e -> markAsReceived());
        btnCancel.addActionListener(e -> cancelSelectedOrder());
        btnClear.addActionListener(e -> {
            clearForm();
            refreshData();
        });

        buttonRow.add(btnCreate);
        buttonRow.add(btnReceive);
        buttonRow.add(btnCancel);
        buttonRow.add(btnClear);

        formCard.add(buttonRow, BorderLayout.SOUTH);
        topContainer.add(formCard, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: TABLE CARD ---
        JPanel tableCard = UITheme.createCard();

        JLabel tableTitle = new JLabel("Order Tracking List");
        tableTitle.setFont(UITheme.FONT_SUBTITLE);
        tableTitle.setForeground(UITheme.TEXT_DARK);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        // Table
        String[] columns = {"Order ID", "Product ID", "Product Name", "Supplier ID", "Quantity", "Order Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        UITheme.applyTableStyle(orderTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        orderTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        orderTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        orderTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Custom status renderer for clear color coding
        orderTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Arial", Font.BOLD, 12));
                if (value != null) {
                    String status = value.toString();
                    if ("RECEIVED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.BTN_GREEN);
                    } else if ("CANCELLED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.BTN_RED);
                    } else {
                        c.setForeground(new Color(180, 110, 0)); // Warm amber for PENDING
                    }
                }
                return c;
            }
        });

        // Click row to select order
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderTable.getSelectedRow() != -1) {
                int row = orderTable.getSelectedRow();
                String selectedOrderId = tableModel.getValueAt(row, 0).toString();
                txtOrderId.setText(selectedOrderId);
            }
        });

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private String suggestNextOrderId() {
        int maxNum = 2000;
        for (PurchaseOrder po : orderService.getAllOrders()) {
            try {
                String digits = po.getId().replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    int val = Integer.parseInt(digits);
                    if (val > maxNum) maxNum = val;
                }
            } catch (Exception ignored) {}
        }
        return "ORD-" + (maxNum + 1);
    }

    private void updateSupplierDropdown() {
        String selected = (String) cmbSupplier.getSelectedItem();
        cmbSupplier.removeAllItems();

        if (supplierService != null && !supplierService.getAllSuppliers().isEmpty()) {
            for (Supplier s : supplierService.getAllSuppliers()) {
                cmbSupplier.addItem(s.getId() + " - " + s.getName());
            }
        } else {
            cmbSupplier.addItem("(No suppliers registered)");
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
        if (supplierId != null) {
            for (Product p : productService.getAllProducts()) {
                if (supplierId.equalsIgnoreCase(p.getSupplierId())) {
                    products.add(p);
                }
            }
        }

        if (products.isEmpty()) {
            cmbProduct.addItem("(No products for this supplier)");
        } else {
            for (Product p : products) {
                cmbProduct.addItem(p.getId() + " - " + p.getName());
            }
        }

        updateEstimatedCost();
    }

    private String getSelectedSupplierId() {
        String item = (String) cmbSupplier.getSelectedItem();
        if (item == null || item.startsWith("(")) return null;
        return item.split(" - ")[0].trim();
    }

    private String getSelectedProductId() {
        String item = (String) cmbProduct.getSelectedItem();
        if (item == null || item.startsWith("(")) return null;
        return item.split(" - ")[0].trim();
    }

    private void updateEstimatedCost() {
        String productId = getSelectedProductId();
        String qtyText = txtQuantity.getText().trim();

        if (productId != null && InputValidator.isPositiveInt(qtyText)) {
            try {
                Product p = productService.getProductById(productId);
                int qty = Integer.parseInt(qtyText);
                double total = p.getPrice() * qty;
                lblEstimatedCost.setText(String.format("Estimated Total: $%,.2f ($%.2f / unit)", total, p.getPrice()));
                return;
            } catch (Exception ignored) {
            }
        }
        lblEstimatedCost.setText("Estimated Total: $0.00");
    }

    private void createOrder() {
        String orderId = txtOrderId.getText().trim();
        String supplierId = getSelectedSupplierId();
        String productId = getSelectedProductId();
        String qtyText = txtQuantity.getText().trim();

        if (supplierId == null) {
            JOptionPane.showMessageDialog(this, "Please select a Supplier.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (productId == null) {
            JOptionPane.showMessageDialog(this, "Please select a Product from this supplier.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isPositiveInt(qtyText)) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive integer (> 0).", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = Integer.parseInt(qtyText);

        try {
            orderService.createOrder(orderId, productId, supplierId, quantity);

            JOptionPane.showMessageDialog(this, "Purchase Order " + orderId + " created successfully (PENDING).", "Order Created", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAsReceived() {
        String orderId = txtOrderId.getText().trim();
        if (!InputValidator.isValidString(orderId)) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to receive.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PurchaseOrder po = orderService.findOrderById(orderId);
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Order " + orderId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (po.getStatus() != OrderStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "Only PENDING orders can be received.\nCurrent Status: " + po.getStatus(), "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            orderService.receiveOrder(orderId);
            JOptionPane.showMessageDialog(this,
                    "Order " + orderId + " received!\nWarehouse inventory was automatically incremented by " + po.getQuantity() + " units.",
                    "Goods Received",
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            refreshData();
        } catch (ProductNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelSelectedOrder() {
        String orderId = txtOrderId.getText().trim();
        if (!InputValidator.isValidString(orderId)) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PurchaseOrder po = orderService.findOrderById(orderId);
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Order " + orderId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (po.getStatus() != OrderStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "Only PENDING orders can be cancelled.\nCurrent Status: " + po.getStatus(), "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel Order '" + orderId + "'?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                orderService.cancelOrder(orderId);
                JOptionPane.showMessageDialog(this, "Order " + orderId + " has been cancelled.", "Order Cancelled", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtOrderId.setText(suggestNextOrderId());
        txtQuantity.setText("");
        if (cmbSupplier.getItemCount() > 0) {
            cmbSupplier.setSelectedIndex(0);
        }
        updateProductDropdown();
        orderTable.clearSelection();
    }

    public void refreshData() {
        updateSupplierDropdown();
        updateProductDropdown();
        txtOrderId.setText(suggestNextOrderId());
        populateOrderTable();
    }

    private void populateOrderTable() {
        tableModel.setRowCount(0);
        List<PurchaseOrder> orders = orderService.getAllOrders();

        // Newest orders first
        for (int i = orders.size() - 1; i >= 0; i--) {
            PurchaseOrder po = orders.get(i);
            String productName = "—";
            try {
                Product p = productService.getProductById(po.getProductId());
                if (p != null) {
                    productName = p.getName();
                }
            } catch (Exception ignored) {
            }

            tableModel.addRow(new Object[]{
                    po.getId(),
                    po.getProductId(),
                    productName,
                    po.getSupplierId(),
                    po.getQuantity(),
                    po.getStatus().name()
            });
        }
    }
}
