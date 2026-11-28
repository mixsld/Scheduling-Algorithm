import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class SchedulingSimulator extends JFrame {
    private static final String GROUP_LABEL = "Group 6: All Star";
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
        root.add(new CpuModulePanel(new FcfsScheduler(), FCFS_SAMPLE, this::showMenu), "cpu-fcfs");
        root.add(new CpuModulePanel(new PreemptivePriorityScheduler(), PRIORITY_SAMPLE, this::showMenu), "cpu-priority");
        root.add(new DiskModulePanel(new CScanScheduler(), this::showMenu), "disk-cscan");
        root.add(new DiskModulePanel(new DiskFcfsScheduler(), this::showMenu), "disk-fcfs");

        setContentPane(root);
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    private void showMenu() {
        cards.show(root, MENU);
    }

    private JPanel buildMenu() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("CPU and Disk Scheduling Simulator", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        page.add(title, BorderLayout.NORTH);

        JPanel cpu = new JPanel(new GridLayout(0, 1, 10, 10));
        cpu.setBorder(BorderFactory.createTitledBorder("CPU Scheduling"));
        cpu.setPreferredSize(new Dimension(260, 120));
        cpu.add(menuButton("FCFS", "cpu-fcfs"));
        cpu.add(menuButton("Priority (Preemptive)", "cpu-priority"));

        JPanel disk = new JPanel(new GridLayout(0, 1, 10, 10));
        disk.setBorder(BorderFactory.createTitledBorder("Disk Scheduling"));
        disk.setPreferredSize(new Dimension(260, 120));
        disk.add(menuButton("C-SCAN", "disk-cscan"));
        disk.add(menuButton("FCFS", "disk-fcfs"));

        JPanel groups = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 60));
        groups.add(cpu);
        groups.add(disk);
        page.add(groups, BorderLayout.CENTER);

        JButton about = new JButton("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, aboutText(), "About", JOptionPane.INFORMATION_MESSAGE));
        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel();
        bottom.add(about);
        bottom.add(exit);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JButton menuButton(String text, String target) {
        JButton button = new JButton(text);
        button.addActionListener(e -> cards.show(root, target));
        return button;
    }

    static String aboutText() {
        StringBuilder text = new StringBuilder(GROUP_LABEL).append("\n\n");
        for (String member : MEMBERS) {
            text.append(member).append("\n");
        }
        return text.toString();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new SchedulingSimulator().setVisible(true));
    }
}
