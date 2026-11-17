package ui;

import models.Product;
import services.ProductService;
import services.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: DashboardPanel.java
 *
 * Clean, minimalist dashboard:
 * - 4 clean statistic cards
 * - Plain English action buttons (no emojis, no overlapping layout)
 * - Clean JTable for all, low-stock, and sorted views
 * ==============================================================================
 */
public class DashboardPanel extends JPanel {

    private final ReportService reportService;
    private final ProductService productService;

    // Stat labels
    private JLabel lblTotalProducts;
    private JLabel lblTotalSuppliers;
    private JLabel lblLowStockCount;
    private JLabel lblTotalValue;

    // Table
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel tableHeadingLabel;

    public DashboardPanel(ReportService reportService, ProductService productService) {
        this.reportService = reportService;
        this.productService = productService;

        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout(18, 18));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- TOP: HEADING & 4 STATS CARDS ---
        JPanel topContainer = new JPanel(new BorderLayout(14, 14));
        topContainer.setOpaque(false);

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_DARK);
        topContainer.add(heading, BorderLayout.NORTH);

        // 4 Clean White Cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);

        lblTotalProducts = new JLabel("0");
        lblTotalSuppliers = new JLabel("0");
        lblLowStockCount = new JLabel("0");
        lblTotalValue = new JLabel("$0.00");

        cards.add(createStatCard("Total Products", lblTotalProducts));
        cards.add(createStatCard("Total Suppliers", lblTotalSuppliers));
        cards.add(createStatCard("Low Stock Items", lblLowStockCount));
        cards.add(createStatCard("Total Inventory Value", lblTotalValue));

        topContainer.add(cards, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER: TABLE CARD ---
        JPanel tableCard = UITheme.createCard();

        // Two-tier header: Title on top, buttons below (prevents any overlapping)
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);

        tableHeadingLabel = new JLabel("All Products");
        tableHeadingLabel.setFont(UITheme.FONT_SUBTITLE);
        tableHeadingLabel.setForeground(UITheme.TEXT_DARK);
        headerPanel.add(tableHeadingLabel, BorderLayout.NORTH);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonBar.setOpaque(false);

        JButton btnAll = UITheme.createButton("All Products");
        JButton btnLowStock = UITheme.createButton("Low Stock Items");
        JButton btnSortPrice = UITheme.createButton("Sort by Price");
        JButton btnSortQty = UITheme.createButton("Sort by Quantity");
        JButton btnSupplierCount = UITheme.createButton("Products per Supplier");
        JButton btnRefresh = UITheme.createButton("Refresh");

        btnAll.addActionListener(e -> showAllProducts());
        btnLowStock.addActionListener(e -> showLowStockTable());
        btnSortPrice.addActionListener(e -> showSortedByPrice());
        btnSortQty.addActionListener(e -> showSortedByQuantity());
        btnSupplierCount.addActionListener(e -> showSupplierProductBreakdown());
        btnRefresh.addActionListener(e -> refreshData());

        buttonBar.add(btnAll);
        buttonBar.add(btnLowStock);
        buttonBar.add(btnSortPrice);
        buttonBar.add(btnSortQty);
        buttonBar.add(btnSupplierCount);
        buttonBar.add(btnRefresh);

        headerPanel.add(buttonBar, BorderLayout.SOUTH);
        tableCard.add(headerPanel, BorderLayout.NORTH);

        // Data Table
        String[] columns = {"Product ID", "Product Name", "Supplier ID", "Price ($)", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        UITheme.applyTableStyle(reportTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        reportTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        reportTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(UITheme.TEXT_DARK);

        card.add(titleLabel);
        card.add(valueLabel);
        return card;
    }

    public void refreshData() {
        lblTotalProducts.setText(String.valueOf(reportService.getTotalProductsCount()));
        lblTotalSuppliers.setText(String.valueOf(reportService.getTotalSuppliersCount()));

        List<Product> lowStock = reportService.getLowStockProducts(10);
        lblLowStockCount.setText(String.valueOf(lowStock.size()));

        lblTotalValue.setText(String.format("$%,.2f", reportService.calculateTotalInventoryValue()));

        showAllProducts();
    }

    private void showAllProducts() {
        tableHeadingLabel.setText("All Products (Total: " + productService.getAllProducts().size() + ")");
        populateTable(productService.getAllProducts());
    }

    private void showLowStockTable() {
        List<Product> lowStock = reportService.getLowStockProducts(10);
        tableHeadingLabel.setText("Low Stock Items (Stock < 10, Total: " + lowStock.size() + ")");
        populateTable(lowStock);
    }

    private void showSortedByPrice() {
        tableHeadingLabel.setText("Products Sorted by Price (High to Low)");
        populateTable(reportService.getProductsSortedByPrice(false));
    }

    private void showSortedByQuantity() {
        tableHeadingLabel.setText("Products Sorted by Quantity (Low to High)");
        populateTable(reportService.getProductsSortedByQuantity(true));
    }

    private void populateTable(Collection<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getSupplierId(),
                    String.format("%.2f", p.getPrice()),
                    p.getQuantity()
            });
        }
    }

    private void showSupplierProductBreakdown() {
        Map<String, Integer> breakdown = reportService.getProductCountPerSupplier();
        StringBuilder sb = new StringBuilder();
        sb.append("Number of Products per Supplier:\n\n");

        if (breakdown.isEmpty()) {
            sb.append("No records found.");
        } else {
            for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                sb.append("  * ").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append(" product(s)\n");
            }
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Products per Supplier", JOptionPane.INFORMATION_MESSAGE);
    }
}
