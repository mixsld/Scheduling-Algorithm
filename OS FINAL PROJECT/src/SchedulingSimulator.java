import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class SchedulingSimulator extends JFrame {
    private static final String GROUP_NAME = "All Star";
    private static final String[] MEMBERS = {"Justin Panghulan", "Jared Esguerra", "Deinhiel Gonzales"};

    private static final int[][] FCFS_SAMPLE = {{1, 0, 21, 0}, {2, 0, 3, 0}, {3, 0, 6, 0}, {4, 0, 2, 0}};
    private static final int[][] PRIORITY_SAMPLE = {{1, 0, 11, 2}, {2, 5, 28, 0}, {3, 12, 2, 3}, {4, 2, 10, 1}, {5, 9, 16, 4}};

    private static final String MENU = "menu";
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private SchedulingSimulator() {
        super("CPU and Disk Scheduling Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        root.add(buildMenu(), MENU);
        addModule("cpu-fcfs", new CpuModulePanel(new FcfsScheduler(), FCFS_SAMPLE, this::showMenu));
        addModule("cpu-priority", new CpuModulePanel(new PreemptivePriorityScheduler(), PRIORITY_SAMPLE, this::showMenu));
        addModule("disk-cscan", new DiskModulePanel(new CScanScheduler(), this::showMenu));
        addModule("disk-fcfs", new DiskModulePanel(new DiskFcfsScheduler(), this::showMenu));

        setContentPane(root);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1280, 780);
        setLocationRelativeTo(null);
    }

    private void addModule(String name, JPanel panel) {
        root.add(panel, name);
    }

    private void showMenu() {
        cards.show(root, MENU);
    }

    private JPanel buildMenu() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Ui.BG);
        page.add(Ui.headerBar("CPU and Disk Scheduling Simulator", null, null), BorderLayout.NORTH);

        JPanel groups = new JPanel(new GridLayout(1, 2, 24, 0));
        groups.setOpaque(false);
        groups.add(menuGroup("CPU Scheduling",
                menuButton("FCFS  (non-preemptive)", "cpu-fcfs"),
                menuButton("Priority  (preemptive)", "cpu-priority")));
        groups.add(menuGroup("Disk Scheduling",
                menuButton("C-SCAN", "disk-cscan"),
                menuButton("FCFS", "disk-fcfs")));

        JPanel center = new JPanel(new java.awt.GridBagLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        java.awt.GridBagConstraints fill = new java.awt.GridBagConstraints();
        fill.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fill.weightx = 1;
        center.add(groups, fill);

        JButton about = Ui.secondaryButton("About");
        about.addActionListener(e -> showAbout());
        JButton exit = Ui.secondaryButton("Exit");
        exit.addActionListener(e -> System.exit(0));
        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setOpaque(false);
        footer.add(about);
        footer.add(exit);
        JPanel footerWrap = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        footerWrap.setOpaque(false);
        footerWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        footer.setPreferredSize(new Dimension(300, 44));
        footerWrap.add(footer);

        page.add(center, BorderLayout.CENTER);
        page.add(footerWrap, BorderLayout.SOUTH);
        return page;
    }

    private JPanel menuGroup(String title, JButton... buttons) {
        JPanel card = Ui.card(title);
        card.setLayout(new GridLayout(0, 1, 0, 10));
        for (JButton button : buttons) {
            card.add(button);
        }
        return card;
    }

    private JButton menuButton(String title, String target) {
        JButton button = Ui.secondaryButton(title);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(Ui.BASE.deriveFont(java.awt.Font.BOLD, 16f));
        button.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        button.addActionListener(e -> cards.show(root, target));
        return button;
    }

    private void showAbout() {
        JDialog dialog = new JDialog(this, "About", true);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Ui.PANEL);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        body.add(aboutLine(GROUP_NAME, Ui.BASE.deriveFont(java.awt.Font.BOLD, 18f)));
        body.add(Box.createVerticalStrut(10));
        for (String member : MEMBERS) {
            body.add(aboutLine(member, Ui.BASE));
        }

        JButton close = Ui.primaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        bottom.setBackground(Ui.PANEL);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        bottom.add(close);

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static JLabel aboutLine(String text, java.awt.Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(Ui.INK);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new SchedulingSimulator().setVisible(true));
    }
}
