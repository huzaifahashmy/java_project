package ui;

import services.*;
import storage.DataManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * TEAM MEMBER 6: MAIN FUNCTION, UI & INTEGRATION LEAD
 * File: MainFrame.java
 *
 * Clean, minimalistic desktop window with sidebar navigation and CardLayout.
 * Title: Warehouse Manager
 * Classic non-glowing desktop style with 100% visible buttons on all platforms.
 * ==============================================================================
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final List<JButton> navButtons = new ArrayList<>();
    private JButton currentActiveButton = null;

    private final DashboardPanel dashboardPanel;
    private final ProductPanel productPanel;
    private final SupplierPanel supplierPanel;
    private final InventoryPanel inventoryPanel;
    private final OrderPanel orderPanel;

    public MainFrame(DataManager dataManager,
                     ProductService productService,
                     SupplierService supplierService,
                     InventoryService inventoryService,
                     PurchaseOrderService orderService,
                     ReportService reportService) {

        super("Warehouse Manager");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        setSize(1140, 720);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 720));

        JPanel topSidebar = new JPanel();
        topSidebar.setLayout(new BoxLayout(topSidebar, BoxLayout.Y_AXIS));
        topSidebar.setOpaque(false);
        topSidebar.setBorder(BorderFactory.createEmptyBorder(24, 14, 20, 14));

        // Sidebar Header
        JLabel titleLabel = new JLabel("Warehouse Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 24, 0));
        topSidebar.add(titleLabel);

        // Sidebar Navigation Buttons
        JPanel navPanel = new JPanel(new GridLayout(5, 1, 0, 8));
        navPanel.setOpaque(false);
        navPanel.setMaximumSize(new Dimension(200, 220));

        // Subsystem Panels
        dashboardPanel = new DashboardPanel(reportService, productService);
        productPanel = new ProductPanel(productService, supplierService);
        supplierPanel = new SupplierPanel(supplierService);
        inventoryPanel = new InventoryPanel(inventoryService, productService, supplierService);
        orderPanel = new OrderPanel(orderService, productService, supplierService);

        // Main Panel (CardLayout)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(UITheme.BG_MAIN);

        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(productPanel, "Products");
        mainPanel.add(supplierPanel, "Suppliers");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(orderPanel, "Orders");

        // Plain text sidebar buttons with custom paintComponent (immune to Windows L&F overrides)
        JButton btnDashboard = addNavButton(navPanel, "Dashboard", "Dashboard", () -> dashboardPanel.refreshData());
        addNavButton(navPanel, "Products", "Products", () -> productPanel.refreshData());
        addNavButton(navPanel, "Suppliers", "Suppliers", () -> supplierPanel.refreshData());
        addNavButton(navPanel, "Inventory", "Inventory", () -> inventoryPanel.refreshData());
        addNavButton(navPanel, "Orders", "Orders", () -> orderPanel.refreshData());

        topSidebar.add(navPanel);
        sidebar.add(topSidebar, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        setActiveButton(btnDashboard);
        setVisible(true);
    }

    private JButton addNavButton(JPanel container, String text, String cardName, Runnable onSelect) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg;
                if (this == currentActiveButton) {
                    bg = new Color(75, 95, 125); // Active button background
                } else if (getModel().isRollover()) {
                    bg = new Color(60, 75, 98);  // Hover background
                } else {
                    bg = new Color(45, 55, 72);  // Normal sidebar button background
                }

                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(190, 40));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        button.addActionListener(e -> {
            cardLayout.show(mainPanel, cardName);
            setActiveButton(button);
            if (onSelect != null) {
                onSelect.run();
            }
        });

        navButtons.add(button);
        container.add(button);
        return button;
    }

    private void setActiveButton(JButton activeButton) {
        this.currentActiveButton = activeButton;
        for (JButton btn : navButtons) {
            if (btn == activeButton) {
                btn.setFont(new Font("Arial", Font.BOLD, 14));
            } else {
                btn.setFont(new Font("Arial", Font.PLAIN, 14));
            }
            btn.repaint();
        }
    }
}
