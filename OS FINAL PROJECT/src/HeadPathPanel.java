import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Graph of the disk head's path. The horizontal axis is the cylinder number and time runs
 * downward, one row per head move. Return jumps are drawn dashed.
 */
final class HeadPathPanel extends JPanel {
    private static final int LEFT = 44;
    private static final int RIGHT = 36;
    private static final int TOP = 46;
    private static final int ROW = 30;

    private DiskResult result;

    HeadPathPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(Ui.size(600, 120));
    }

    void setResult(DiskResult result) {
        this.result = result;
        setPreferredSize(Ui.size(600, TOP + result.path.size() * ROW + 16));
        revalidate();
        repaint();
    }

    void clear() {
        result = null;
        setPreferredSize(Ui.size(600, 120));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Ui.SMALL);
        FontMetrics fm = g2.getFontMetrics();

        if (result == null) {
            g2.setColor(Ui.MUTED);
            g2.setFont(Ui.BASE);
            g2.drawString("Run the algorithm to draw the head path here.", LEFT, 40);
            g2.dispose();
            return;
        }

        int lastCylinder = Math.max(1, result.diskSize - 1);
        double scale = (getWidth() - LEFT - RIGHT) / (double) lastCylinder;
        drawGrid(g2, fm, lastCylinder, scale);
        drawPath(g2, fm, scale);
        g2.dispose();
    }

    private void drawGrid(Graphics2D g2, FontMetrics fm, int lastCylinder, double scale) {
        int step = niceStep(lastCylinder / 8.0);
        g2.setStroke(new BasicStroke(1f));
        for (int c = 0; c <= lastCylinder; c += step) {
            drawGridLine(g2, fm, c, scale);
        }
        if (lastCylinder % step != 0 && lastCylinder * scale - (lastCylinder / step) * step * scale > 28) {
            drawGridLine(g2, fm, lastCylinder, scale);
        }
    }

    private void drawGridLine(Graphics2D g2, FontMetrics fm, int cylinder, double scale) {
        int x = (int) Math.round(LEFT + cylinder * scale);
        g2.setColor(new Color(0xE6EAF0));
        g2.drawLine(x, TOP - 10, x, getHeight() - 8);
        g2.setColor(Ui.MUTED);
        String label = String.valueOf(cylinder);
        g2.drawString(label, x - fm.stringWidth(label) / 2, TOP - 16);
    }

    private void drawPath(Graphics2D g2, FontMetrics fm, double scale) {
        int points = result.path.size();
        for (int i = 1; i < points; i++) {
            int x1 = (int) Math.round(LEFT + result.path.get(i - 1) * scale);
            int x2 = (int) Math.round(LEFT + result.path.get(i) * scale);
            int y1 = TOP + (i - 1) * ROW;
            int y2 = TOP + i * ROW;
            boolean jump = result.reasons.get(i - 1).equals(DiskResult.RETURN_JUMP);
            g2.setColor(jump ? new Color(0xD9822B) : Ui.BLUE);
            g2.setStroke(jump
                    ? new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {6f, 5f}, 0f)
                    : new BasicStroke(2f));
            g2.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < points; i++) {
            int x = (int) Math.round(LEFT + result.path.get(i) * scale);
            int y = TOP + i * ROW;
            g2.setColor(i == 0 ? Ui.OK : Ui.NAVY);
            g2.fillOval(x - 5, y - 5, 10, 10);

            g2.setColor(Ui.MUTED);
            g2.setFont(Ui.SMALL);
            g2.drawString(i == 0 ? "start" : String.valueOf(i), 4, y + 4);

            g2.setColor(Ui.TEXT);
            g2.setFont(Ui.BOLD.deriveFont(12f));
            String label = String.valueOf(result.path.get(i));
            int labelX = x + 8 + fm.stringWidth(label) > getWidth() - 4 ? x - 10 - fm.stringWidth(label) - 4 : x + 8;
            g2.drawString(label, labelX, y + 4);
        }
    }

    /** Rounds up to 1, 2, 5 x 10^k so grid labels fall on readable numbers. */
    private static int niceStep(double raw) {
        double power = Math.pow(10, Math.floor(Math.log10(Math.max(raw, 1))));
        for (int factor : new int[] {1, 2, 5, 10}) {
            if (raw <= factor * power) {
                return (int) (factor * power);
            }
        }
        return (int) (10 * power);
    }
}
