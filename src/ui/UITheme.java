package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * ==============================================================================
 * Warehouse Manager - Clean Desktop UI Theme
 * Simple, minimalist, classic desktop styling (calm slate, gray, white).
 * Cross-platform custom button renderer guaranteeing 100% visibility of all
 * button backgrounds and text under Windows Look & Feel.
 * ==============================================================================
 */
public final class UITheme {

    private UITheme() {
    }

    // Classic desktop colors (calm slate, non-glowing)
    public static final Color BG_MAIN = new Color(245, 247, 250);       // Soft light gray
    public static final Color BG_SIDEBAR = new Color(45, 55, 72);       // Muted slate gray
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(200, 210, 222);  // Visible soft gray border
    public static final Color INPUT_BORDER = new Color(160, 174, 192);  // Crisp input border

    public static final Color TEXT_DARK = new Color(30, 41, 59);        // Dark slate text
    public static final Color TEXT_MUTED = new Color(100, 115, 130);    // Muted gray text

    public static final Color BTN_PRIMARY = new Color(45, 55, 72);      // Muted slate
    public static final Color BTN_GREEN = new Color(34, 110, 48);       // Classic forest green
    public static final Color BTN_RED = new Color(185, 28, 28);         // Classic dark red

    // Standard Arial fonts
    public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Arial", Font.BOLD, 14);
    public static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 13);
    public static final Font FONT_INPUT = new Font("Arial", Font.PLAIN, 13);
    public static final Font FONT_BTN = new Font("Arial", Font.BOLD, 13);

    // Simple white card container
    public static JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    // Clean label for form fields
    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_DARK);
        return label;
    }

    // Clean text field with visible border and proper padding
    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_INPUT);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(TEXT_DARK);
        tf.setPreferredSize(new Dimension(140, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    // Clean combo box with visible border
    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(FONT_INPUT);
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT_DARK);
        cb.setPreferredSize(new Dimension(150, 32));
        cb.setBorder(BorderFactory.createLineBorder(INPUT_BORDER, 1));
        return cb;
    }

    /**
     * Cross-platform custom-painted button.
     * Overrides paintComponent with setContentAreaFilled(false) so Windows Look & Feel
     * CANNOT override the background with native white gradient or hide the white text.
     */
    public static class CustomButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color borderColor;

        public CustomButton(String text, Color normalBg, Color hoverBg, Color pressedBg, Color fgColor, Color borderColor) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            this.borderColor = borderColor;

            setForeground(fgColor);
            setFont(FONT_BTN);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            if (!isEnabled()) {
                bg = new Color(225, 230, 235);
            } else if (getModel().isPressed()) {
                bg = pressedBg;
            } else if (getModel().isRollover()) {
                bg = hoverBg;
            } else {
                bg = normalBg;
            }

            g2.setColor(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Standard secondary / neutral button (White with dark slate text & visible border)
    public static JButton createButton(String text) {
        return new CustomButton(
                text,
                Color.WHITE,
                new Color(241, 245, 249),
                new Color(226, 232, 240),
                TEXT_DARK,
                INPUT_BORDER
        );
    }

    // Primary action button (Solid slate background with 100% visible white bold text)
    public static JButton createPrimaryButton(String text) {
        return new CustomButton(
                text,
                BTN_PRIMARY,
                new Color(60, 75, 98),
                new Color(30, 41, 59),
                Color.WHITE,
                new Color(30, 41, 59)
        );
    }

    // Green button (Solid forest green background with 100% visible white bold text)
    public static JButton createGreenButton(String text) {
        return new CustomButton(
                text,
                BTN_GREEN,
                new Color(42, 130, 58),
                new Color(25, 85, 36),
                Color.WHITE,
                new Color(25, 85, 36)
        );
    }

    // Red button (Solid classic dark red background with 100% visible white bold text)
    public static JButton createRedButton(String text) {
        return new CustomButton(
                text,
                BTN_RED,
                new Color(210, 40, 40),
                new Color(150, 20, 20),
                Color.WHITE,
                new Color(150, 20, 20)
        );
    }

    // Apply simple, clean table styling
    public static void applyTableStyle(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 235, 242));
        table.setSelectionBackground(new Color(215, 228, 245));
        table.setSelectionForeground(TEXT_DARK);

        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(238, 242, 247));
        table.getTableHeader().setForeground(new Color(45, 55, 72));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }
}
