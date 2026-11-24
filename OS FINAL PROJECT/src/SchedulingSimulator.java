import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point and main menu of the CPU and Disk Scheduling Simulator.
 * Shows the four assigned algorithms grouped as CPU Scheduling and Disk Scheduling,
 * plus About and Exit. Each algorithm opens in its own screen inside this window.
 */
public final class SchedulingSimulator extends JFrame {

    // TODO before submission: fill in the real group number, member names and assigned algorithms.
    private static final String GROUP_NUMBER = "Group [No.]";
    private static final String[][] MEMBERS = {
        {"[Member 1 name]", "FCFS (CPU)"},
        {"[Member 2 name]", "Priority - Preemptive (CPU)"},
        {"[Member 3 name]", "C-SCAN (Disk)"},
        {"[Member 4 name]", "FCFS (Disk)"},
    };

    private static final String MENU = "menu";
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private SchedulingSimulator() {
        super("CPU and Disk Scheduling Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        root.add(buildMenu(), MENU);
        addModule("cpu-fcfs", new CpuModulePanel(new FcfsScheduler(), this::showMenu));
        addModule("cpu-priority", new CpuModulePanel(new PreemptivePriorityScheduler(), this::showMenu));
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

    // ---------------------------------------------------------------- main menu

    private JPanel buildMenu() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Ui.BG);
        page.add(Ui.headerBar("CPU and Disk Scheduling Simulator", "ICS26012 – Operating Systems  |  Main Menu", null),
                BorderLayout.NORTH);

        JPanel groups = new JPanel(new GridLayout(1, 2, 24, 0));
        groups.setOpaque(false);
        groups.add(menuGroup("CPU Scheduling", "Gantt chart, per-process times, averages, utilization and throughput.",
                menuButton("First-Come, First-Served (FCFS)", "Non-preemptive", "cpu-fcfs"),
                menuButton("Priority", "Preemptive", "cpu-priority")));
        groups.add(menuGroup("Disk Scheduling", "Seek sequence, head movement per step, total movement and head-path graph.",
                menuButton("Circular SCAN (C-SCAN)", "Sweeps one way, then jumps back", "disk-cscan"),
                menuButton("First-Come, First-Served (FCFS)", "Serves requests in arrival order", "disk-fcfs")));

        JPanel center = new JPanel(new java.awt.GridBagLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        java.awt.GridBagConstraints fill = new java.awt.GridBagConstraints();
        fill.fill = java.awt.GridBagConstraints.HORIZONTAL;
        fill.weightx = 1;
        center.add(groups, fill);

        JButton about = Ui.secondaryButton("About");
        about.addActionListener(e -> showAbout());
        JButton exit = Ui.primaryButton("Exit");
        exit.addActionListener(e -> confirmExit());
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

    private JPanel menuGroup(String title, String description, JPanel... buttons) {
        JPanel card = Ui.card(title);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel desc = new JLabel(Ui.wrap(description, 380));
        desc.setFont(Ui.BASE);
        desc.setForeground(Ui.MUTED);
        desc.setAlignmentX(LEFT_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(14));
        for (JPanel button : buttons) {
            button.setAlignmentX(LEFT_ALIGNMENT);
            card.add(button);
            card.add(Box.createVerticalStrut(12));
        }
        return card;
    }

    private JPanel menuButton(String title, String subtitle, String target) {
        JButton button = Ui.primaryButton("<html><b>" + title + "</b><br><span style='font-weight:normal'>"
                + subtitle + "</span></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> cards.show(root, target));
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(button, BorderLayout.CENTER);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        holder.setPreferredSize(new Dimension(300, 64));
        return holder;
    }

    private void showAbout() {
        JDialog dialog = new JDialog(this, "About", true);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        body.add(aboutLine("CPU and Disk Scheduling Simulator", Ui.BASE.deriveFont(java.awt.Font.BOLD, 18f), Ui.NAVY));
        body.add(aboutLine("ICS26012 – Operating Systems", Ui.BASE, Ui.MUTED));
        body.add(Box.createVerticalStrut(14));
        body.add(aboutLine(GROUP_NUMBER, Ui.BASE.deriveFont(java.awt.Font.BOLD, 16f), Ui.TEXT));
        body.add(Box.createVerticalStrut(8));
        for (String[] member : MEMBERS) {
            body.add(aboutLine(member[0] + "  –  " + member[1], Ui.BASE, Ui.TEXT));
        }

        JButton close = Ui.primaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        bottom.add(close);

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static JLabel aboutLine(String text, java.awt.Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this, "Exit the simulator?", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default Swing look
        }
        SwingUtilities.invokeLater(() -> new SchedulingSimulator().setVisible(true));
    }
}
