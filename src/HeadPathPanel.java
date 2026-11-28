import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

class HeadPathPanel extends JPanel {
    private static final int LEFT = 45;
    private static final int RIGHT = 40;
    private static final int TOP = 40;
    private static final int ROW = 25;

    private DiskResult result;

    HeadPathPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 100));
    }

    void setResult(DiskResult result) {
        this.result = result;
        setPreferredSize(new Dimension(500, TOP + result.path.size() * ROW + 10));
        revalidate();
        repaint();
    }

    void clear() {
        result = null;
        setPreferredSize(new Dimension(500, 100));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (result == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        int lastCylinder = Math.max(1, result.diskSize - 1);
        double scale = (getWidth() - LEFT - RIGHT) / (double) lastCylinder;

        int step = Math.max(1, Math.round(lastCylinder / 4f / 10) * 10);
        for (int cylinder = 0; cylinder <= lastCylinder; cylinder += step) {
            int x = LEFT + (int) (cylinder * scale);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x, TOP - 10, x, getHeight());
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(cylinder), x - 8, 20);
        }

        for (int i = 1; i < result.path.size(); i++) {
            int x1 = LEFT + (int) (result.path.get(i - 1) * scale);
            int x2 = LEFT + (int) (result.path.get(i) * scale);
            int y1 = TOP + (i - 1) * ROW;
            int y2 = TOP + i * ROW;
            boolean jump = result.reasons.get(i - 1).equals(DiskResult.RETURN_JUMP);
            g2.setColor(Color.BLACK);
            if (jump) {
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {6f, 4f}, 0f));
            } else {
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < result.path.size(); i++) {
            int x = LEFT + (int) (result.path.get(i) * scale);
            int y = TOP + i * ROW;
            g2.setColor(Color.BLUE);
            g2.fillOval(x - 3, y - 3, 7, 7);
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(result.path.get(i)), x + 8, y + 4);
            g2.drawString(i == 0 ? "start" : String.valueOf(i), 4, y + 4);
        }
    }
}
