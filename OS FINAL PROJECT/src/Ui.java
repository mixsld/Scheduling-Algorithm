import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Shared look (colours, fonts, buttons, tables) and input-parsing helpers for the GUI. */
final class Ui {
    static final Color NAVY = new Color(0x1F3A5F);
    static final Color BLUE = new Color(0x2F6FB0);
    static final Color BG = new Color(0xF4F6FA);
    static final Color TEXT = new Color(0x1E2733);
    static final Color MUTED = new Color(0x5B6676);
    static final Color ERROR = new Color(0xB3261E);
    static final Color OK = new Color(0x1B7F4B);
    static final Color BORDER = new Color(0xD3D9E3);

    static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font BOLD = BASE.deriveFont(Font.BOLD);
    static final Font SMALL = BASE.deriveFont(12f);

    private Ui() {
    }

    // ---------------------------------------------------------------- input parsing

    /** Thrown for any invalid user input; the message is shown to the user as-is. */
    static final class InputException extends Exception {
        InputException(String message) {
            super(message);
        }
    }

    /** Parses a whole number in [min, max], with a clear message for empty, non-numeric and out-of-range input. */
    static int parseInt(String raw, String label, int min, int max) throws InputException {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            throw new InputException(label + " is empty. Enter a whole number from " + min + " to " + max + ".");
        }
        if (!text.matches("[+-]?\\d+")) {
            throw new InputException(label + " must be a whole number (you entered \"" + text + "\").");
        }
        long value;
        try {
            value = Long.parseLong(text);
        } catch (NumberFormatException tooLong) {
            value = text.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
        if (value < min || value > max) {
            throw new InputException(label + " must be between " + min + " and " + max
                    + " (you entered " + text + ").");
        }
        return (int) value;
    }

    // ---------------------------------------------------------------- widgets

    /** Rounded flat button that works with any colour under every look-and-feel. */
    static final class FlatButton extends JButton {
        private final Color base;
        private boolean hover;

        FlatButton(String text, Color base, Color foreground) {
            super(text);
            this.base = base;
            setFont(BOLD);
            setForeground(foreground);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = !isEnabled() ? BORDER
                    : getModel().isPressed() ? base.darker().darker()
                    : hover ? base.darker() : base;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static JButton primaryButton(String text) {
        return new FlatButton(text, BLUE, Color.WHITE);
    }

    static JButton secondaryButton(String text) {
        return new FlatButton(text, new Color(0xE3E8F0), TEXT);
    }

    /** White panel with a thin border and a bold title. */
    static JPanel card(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), " " + title + " ");
        border.setTitleFont(BOLD);
        border.setTitleColor(NAVY);
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        return panel;
    }

    /** Dark header bar with a title, a subtitle and an optional button on the right. */
    static JPanel headerBar(String title, String subtitle, JButton rightButton) {
        JPanel bar = new JPanel(new java.awt.BorderLayout());
        bar.setBackground(NAVY);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(BASE.deriveFont(Font.BOLD, 20f));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(BASE);
        s.setForeground(new Color(0xC7D5EA));
        text.add(t);
        text.add(s);
        bar.add(text, java.awt.BorderLayout.CENTER);
        if (rightButton != null) {
            bar.add(rightButton, java.awt.BorderLayout.EAST);
        }
        return bar;
    }

    /** A small "caption over value" tile for summary metrics. */
    static JPanel metricTile(String caption, JLabel valueLabel) {
        JPanel tile = new JPanel();
        tile.setLayout(new javax.swing.BoxLayout(tile, javax.swing.BoxLayout.Y_AXIS));
        tile.setBackground(BG);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel c = new JLabel(caption);
        c.setFont(SMALL);
        c.setForeground(MUTED);
        valueLabel.setFont(BASE.deriveFont(Font.BOLD, 17f));
        valueLabel.setForeground(NAVY);
        valueLabel.setText("-");
        tile.add(c);
        tile.add(valueLabel);
        return tile;
    }

    static void styleTable(JTable table) {
        table.setFont(BASE);
        table.setRowHeight(26);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(0xDCE8F7));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD);
        header.setBackground(new Color(0xE3E8F0));
        header.setForeground(NAVY);
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    /** Wraps a message so a JLabel breaks it across lines at the given pixel width. */
    static String wrap(String message, int widthPx) {
        String safe = message.replace("&", "&amp;").replace("<", "&lt;");
        return "<html><body style='width:" + widthPx + "px'>" + safe + "</body></html>";
    }

    static Dimension size(int w, int h) {
        return new Dimension(w, h);
    }
}
