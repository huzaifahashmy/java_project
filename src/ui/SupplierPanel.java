package ui;

import models.Supplier;
import services.SupplierService;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: SupplierPanel.java
 *
 * Simple, clean supplier management:
 * - Auto-suggested ID
 * - Live search filter
 * - Form and Table in clean cards
 * ==============================================================================
 */
public class SupplierPanel extends JPanel {

    private final SupplierService supplierService;

    // Form inputs
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtContact;
    private JTextField txtEmail;

    // Search input
    private JTextField txtSearch;

    // Table components
    private JTable supplierTable;
    private DefaultTableModel tableModel;

    public SupplierPanel(SupplierService supplierService) {
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

        JLabel heading = new JLabel("Suppliers");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        topContainer.add(heading, BorderLayout.NORTH);

        // White Card for Form Input
        JPanel formCard = UITheme.createCard();

        JLabel formTitle = new JLabel("Supplier Details");
        formTitle.setFont(UITheme.FONT_SUBTITLE);
        formTitle.setForeground(UITheme.TEXT_DARK);
        formCard.add(formTitle, BorderLayout.NORTH);

        // Form Grid (2 Rows x 4 Columns)
        JPanel formGrid = new JPanel(new GridLayout(2, 4, 14, 10));
        formGrid.setOpaque(false);

        txtId = UITheme.createTextField();
        txtName = UITheme.createTextField();
        txtContact = UITheme.createTextField();
        txtEmail = UITheme.createTextField();

        formGrid.add(new JLabel("Supplier ID:"));
        formGrid.add(txtId);
        formGrid.add(new JLabel("Supplier Name:"));
        formGrid.add(txtName);

        formGrid.add(new JLabel("Contact:"));
        formGrid.add(txtContact);
        formGrid.add(new JLabel("Email:"));
        formGrid.add(txtEmail);

        formCard.add(formGrid, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttonRow.setOpaque(false);

        JButton btnAdd = UITheme.createPrimaryButton("Add Supplier");
        JButton btnUpdate = UITheme.createButton("Update Supplier");
        JButton btnDelete = UITheme.createRedButton("Delete Supplier");
        JButton btnClear = UITheme.createButton("Clear Form");

        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSupplier());
        btnDelete.addActionListener(e -> deleteSupplier());
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

        JLabel tableTitle = new JLabel("Supplier List");
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
        String[] columns = {"Supplier ID", "Supplier Name", "Contact", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        supplierTable = new JTable(tableModel);
        UITheme.applyTableStyle(supplierTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        supplierTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && supplierTable.getSelectedRow() != -1) {
                int row = supplierTable.getSelectedRow();
                txtId.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtContact.setText(tableModel.getValueAt(row, 2).toString());
                txtEmail.setText(tableModel.getValueAt(row, 3).toString());
                txtId.setEditable(false);
            }
        });

        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private String suggestNextSupplierId() {
        int maxNum = 100;
        for (Supplier s : supplierService.getAllSuppliers()) {
            try {
                String digits = s.getId().replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    int val = Integer.parseInt(digits);
                    if (val > maxNum) maxNum = val;
                }
            } catch (Exception ignored) {}
        }
        return "SUP-" + (maxNum + 1);
    }

    private void addSupplier() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();

        if (!InputValidator.isValidString(id) || !InputValidator.isValidString(name) ||
            !InputValidator.isValidString(contact) || !InputValidator.isValidString(email)) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Supplier supplier = new Supplier(id, name, contact, email);

        try {
            supplierService.addSupplier(supplier);
            JOptionPane.showMessageDialog(this, "Supplier added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSupplier() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();

        if (!InputValidator.isValidString(id)) {
            JOptionPane.showMessageDialog(this, "Please select a supplier from the table to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Supplier updatedSupplier = new Supplier(id, name, contact, email);

        try {
            supplierService.updateSupplier(updatedSupplier);
            JOptionPane.showMessageDialog(this, "Supplier updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshData();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSupplier() {
        String id = txtId.getText().trim();
        if (!InputValidator.isValidString(id)) {
            JOptionPane.showMessageDialog(this, "Please select a supplier from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Supplier '" + id + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                supplierService.deleteSupplier(id);
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshData();
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Safety Check", JOptionPane.WARNING_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Supplier s : supplierService.getAllSuppliers()) {
            if (query.isEmpty() ||
                s.getId().toLowerCase().contains(query) ||
                s.getName().toLowerCase().contains(query) ||
                s.getEmail().toLowerCase().contains(query) ||
                s.getContact().toLowerCase().contains(query)) {

                tableModel.addRow(s.toTableRow());
            }
        }
    }

    private void clearForm() {
        txtId.setText(suggestNextSupplierId());
        txtId.setEditable(true);
        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtSearch.setText("");
        supplierTable.clearSelection();
    }

    public void refreshData() {
        txtId.setText(suggestNextSupplierId());
        filterTable();
    }
}
