import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JPanel;

final class GanttChartPanel extends JPanel {
    private static final int MARGIN = 24;
    private static final int BAR_Y = 20;
    private static final int BAR_HEIGHT = 48;
    private static final Color[] PALETTE = {
        new Color(0x76ABAE), new Color(0xF2946A), new Color(0xD8C3A0), new Color(0xE3B23C),
        new Color(0xA3C4A0), new Color(0x9FB4C7), new Color(0xD99A9A), new Color(0xB9A9CC),
        new Color(0x8CC3C6), new Color(0xE8C9A0)
    };

    private List<GanttSegment> segments = new ArrayList<>();
    private int totalTime;
    private static final int MIN_UNIT = 8;
    private static final int MAX_UNIT = 40;
    private int pixelsPerUnit = MAX_UNIT;

    GanttChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(Ui.size(400, 110));
    }

    void setResult(List<GanttSegment> segments, int totalTime) {
        this.segments = new ArrayList<>(segments);
        this.totalTime = totalTime;

        setPreferredSize(Ui.size(MARGIN * 2 + totalTime * MIN_UNIT, 110));
        revalidate();
        repaint();
    }

    void clear() {
        segments = new ArrayList<>();
        totalTime = 0;
        setPreferredSize(Ui.size(400, 110));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Ui.BASE);
        FontMetrics fm = g2.getFontMetrics();

        if (segments.isEmpty()) {
            g2.setColor(Ui.MUTED);
            g2.drawString("Run the algorithm to draw the Gantt chart here.", MARGIN, BAR_Y + BAR_HEIGHT / 2);
            g2.dispose();
            return;
        }

        pixelsPerUnit = Math.max(MIN_UNIT, Math.min(MAX_UNIT, (getWidth() - 2 * MARGIN) / Math.max(1, totalTime)));
        for (GanttSegment segment : segments) {
            drawSegment(g2, fm, segment);
        }
        drawTimeAxis(g2, fm);
        g2.dispose();
    }

    private void drawSegment(Graphics2D g2, FontMetrics fm, GanttSegment segment) {
        int x = MARGIN + segment.start * pixelsPerUnit;
        int width = (segment.end - segment.start) * pixelsPerUnit;

        g2.setColor(segment.isIdle() ? new Color(0xE6DDCF) : PALETTE[segment.processId % PALETTE.length]);
        g2.fillRect(x, BAR_Y, width, BAR_HEIGHT);
        if (segment.isIdle()) {
            Graphics2D hatch = (Graphics2D) g2.create();
            hatch.clipRect(x, BAR_Y, width, BAR_HEIGHT);
            hatch.setColor(new Color(0xC9BBA5));
            for (int offset = -BAR_HEIGHT; offset < width; offset += 10) {
                hatch.drawLine(x + offset, BAR_Y + BAR_HEIGHT, x + offset + BAR_HEIGHT, BAR_Y);
            }
            hatch.dispose();
        }
        g2.setColor(Ui.TEXT);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawRect(x, BAR_Y, width, BAR_HEIGHT);

        int labelWidth = fm.stringWidth(segment.label);
        if (labelWidth + 2 <= width) {
            g2.setFont(Ui.BOLD);
            g2.setColor(segment.isIdle() ? Ui.MUTED : Ui.TEXT);
            g2.drawString(segment.label, x + (width - g2.getFontMetrics().stringWidth(segment.label)) / 2,
                    BAR_Y + BAR_HEIGHT / 2 + fm.getAscent() / 2 - 2);
            g2.setFont(Ui.BASE);
        }
    }

    private void drawTimeAxis(Graphics2D g2, FontMetrics fm) {
        int axisY = BAR_Y + BAR_HEIGHT + 8;
        g2.setColor(Ui.MUTED);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(MARGIN, axisY, MARGIN + totalTime * pixelsPerUnit, axisY);

        if (pixelsPerUnit >= 8) {
            for (int t = 0; t <= totalTime; t++) {
                int x = MARGIN + t * pixelsPerUnit;
                g2.drawLine(x, axisY, x, axisY + 3);
            }
        }

        TreeSet<Integer> boundaries = new TreeSet<>();
        for (GanttSegment segment : segments) {
            boundaries.add(segment.start);
            boundaries.add(segment.end);
        }
        g2.setColor(Ui.TEXT);
        int lastRight = Integer.MIN_VALUE;
        for (int t : boundaries) {
            int x = MARGIN + t * pixelsPerUnit;
            g2.drawLine(x, axisY, x, axisY + 8);
            String label = String.valueOf(t);
            int half = fm.stringWidth(label) / 2;
            if (x - half > lastRight + 4) {
                g2.drawString(label, x - half, axisY + 8 + fm.getAscent() + 2);
                lastRight = x + half;
            }
        }
    }
}
