import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

class GanttChartPanel extends JPanel {
    private static final Color[] COLORS = {
        new Color(173, 216, 230), new Color(255, 218, 185), new Color(204, 238, 204),
        new Color(255, 204, 229), new Color(255, 245, 157), new Color(204, 204, 255)
    };

    private List<GanttSegment> segments = new ArrayList<>();
    private int totalTime;

    GanttChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 90));
    }

    void setResult(List<GanttSegment> segments, int totalTime) {
        this.segments = new ArrayList<>(segments);
        this.totalTime = totalTime;
        setPreferredSize(new Dimension(40 + totalTime * 8, 90));
        revalidate();
        repaint();
    }

    void clear() {
        segments = new ArrayList<>();
        totalTime = 0;
        setPreferredSize(new Dimension(400, 90));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (segments.isEmpty()) {
            return;
        }
        int unit = Math.min(40, Math.max(8, (getWidth() - 40) / totalTime));
        FontMetrics fm = g.getFontMetrics();
        int lastLabelEnd = -100;

        for (GanttSegment s : segments) {
            int x = 20 + s.start * unit;
            int w = (s.end - s.start) * unit;
            g.setColor(s.isIdle() ? Color.LIGHT_GRAY : COLORS[s.processId % COLORS.length]);
            g.fillRect(x, 15, w, 35);
            g.setColor(Color.BLACK);
            g.drawRect(x, 15, w, 35);
            int labelWidth = fm.stringWidth(s.label);
            if (labelWidth < w) {
                g.drawString(s.label, x + (w - labelWidth) / 2, 38);
            }
            lastLabelEnd = drawTime(g, fm, s.start, x, lastLabelEnd);
        }
        GanttSegment last = segments.get(segments.size() - 1);
        drawTime(g, fm, last.end, 20 + last.end * unit, lastLabelEnd);
    }

    private int drawTime(Graphics g, FontMetrics fm, int time, int x, int lastLabelEnd) {
        String text = String.valueOf(time);
        int labelX = x - fm.stringWidth(text) / 2;
        if (labelX > lastLabelEnd + 3) {
            g.drawString(text, labelX, 68);
            return labelX + fm.stringWidth(text);
        }
        return lastLabelEnd;
    }
}
