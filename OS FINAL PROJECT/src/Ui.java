import java.awt.BorderLayout;
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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

final class Ui {
    static final Color INK = new Color(0x323A45);
    static final Color TEAL = new Color(0x76ABAE);
    static final Color TEAL_DARK = new Color(0x3F7F83);
    static final Color ORANGE = new Color(0xFF5A20);
    static final Color BG = new Color(0xEFE6DA);
    static final Color PANEL = new Color(0xF8F3EA);
    static final Color BORDER = new Color(0xC9BBA5);
    static final Color TEXT = INK;
    static final Color MUTED = new Color(0x6C6A66);
    static final Color ERROR = new Color(0xB8330E);

    static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font BOLD = BASE.deriveFont(Font.BOLD);
    static final Font SMALL = BASE.deriveFont(12f);
    static final Font HEADING = new Font("Arial", Font.BOLD, 22);

    private Ui() {
    }

    static final class InputException extends Exception {
        InputException(String message) {
            super(message);
        }
    }

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

    static final class FlatButton extends JButton {
        private final Color fill;
        private boolean hover;

        FlatButton(String text, Color fill, Color foreground) {
            super(text);
            this.fill = fill;
            setFont(BOLD);
            setForeground(foreground);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
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
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            if (fill != null) {
                g2.setColor(getModel().isPressed() ? fill.darker() : hover ? fill.darker() : fill);
                g2.fillRoundRect(0, 0, w, h, 4, 4);
            } else {
                if (hover || getModel().isPressed()) {
                    g2.setColor(TEAL);
                    g2.fillRoundRect(0, 0, w, h, 4, 4);
                }
                g2.setColor(INK);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w, h, 4, 4);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static JButton primaryButton(String text) {
        return new FlatButton(text, ORANGE, INK);
    }

    static JButton secondaryButton(String text) {
        return new FlatButton(text, null, INK);
    }

    static JPanel card(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(INK, 1), " " + title + " ");
        border.setTitleFont(BOLD);
        border.setTitleColor(INK);
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(6, 10, 10, 10)));
        return panel;
    }

    static JPanel headerBar(String title, String subtitle, JButton rightButton) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(INK);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, ORANGE),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(HEADING);
        t.setForeground(BG);
        text.add(t);
        if (subtitle != null) {
            JLabel s = new JLabel(subtitle);
            s.setFont(BASE);
            s.setForeground(TEAL);
            text.add(s);
        }
        bar.add(text, BorderLayout.CENTER);
        if (rightButton != null) {
            rightButton.setForeground(BG);
            bar.add(rightButton, BorderLayout.EAST);
        }
        return bar;
    }

    static JPanel metricTile(String caption, JLabel valueLabel) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setOpaque(false);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, ORANGE),
                BorderFactory.createEmptyBorder(2, 10, 2, 6)));
        JLabel c = new JLabel(caption);
        c.setFont(SMALL);
        c.setForeground(MUTED);
        valueLabel.setFont(BASE.deriveFont(Font.BOLD, 17f));
        valueLabel.setForeground(INK);
        valueLabel.setText("-");
        tile.add(c);
        tile.add(valueLabel);
        return tile;
    }

    static void styleTable(JTable table) {
        table.setFont(BASE);
        table.setRowHeight(26);
        table.setForeground(INK);
        table.setGridColor(new Color(0xE0D6C6));
        table.setSelectionBackground(new Color(0xD9E8E8));
        table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD);
        header.setBackground(TEAL);
        header.setForeground(INK);
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    static String wrap(String message, int widthPx) {
        String safe = message.replace("&", "&amp;").replace("<", "&lt;");
        return "<html><body style='width:" + widthPx + "px'>" + safe + "</body></html>";
    }

    static Dimension size(int w, int h) {
        return new Dimension(w, h);
    }
}
