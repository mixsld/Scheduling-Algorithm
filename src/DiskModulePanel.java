import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

final class DiskModulePanel extends JPanel {
    static final int DEFAULT_DISK_SIZE = 200;
    static final int MAX_DISK_SIZE = 9999;
    static final int MAX_REQUESTS = 50;

    private static final String SAMPLE_REQUESTS = "82, 170, 43, 140, 24, 16, 190";
    private static final int SAMPLE_HEAD = 50;

    private final DiskScheduler scheduler;

    private final JTextField diskSizeField = new JTextField(String.valueOf(DEFAULT_DISK_SIZE), 8);
    private final JTextField headField = new JTextField(8);
    private final JComboBox<String> directionBox = new JComboBox<>(new String[] {"Higher cylinders", "Lower cylinders"});
    private final JTextArea requestsArea = new JTextArea(5, 20);
    private final JLabel status = new JLabel(" ");

    private final JTextArea sequenceArea = new JTextArea(2, 30);
    private final JLabel totalMovement = new JLabel();
    private final JLabel averageSeek = new JLabel();
    private final HeadPathPanel pathPanel = new HeadPathPanel();
    private final DefaultTableModel movementModel;
    private final JTable movementTable;
    private final JScrollPane movementScroll;

    DiskModulePanel(DiskScheduler scheduler, Runnable onBack) {
        this.scheduler = scheduler;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton back = new JButton("Back to Main Menu");
        back.addActionListener(e -> onBack.run());
        JLabel title = new JLabel("Disk Scheduling - " + scheduler.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(back);
        top.add(title);
        add(top, BorderLayout.NORTH);

        movementModel = new DefaultTableModel(new String[] {"Step", "From", "To", "Head Movement"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movementTable = new JTable(movementModel);
        movementScroll = new JScrollPane(movementTable);

        add(buildInputPanel(), BorderLayout.WEST);
        add(buildOutputPanel(), BorderLayout.CENTER);
        clearOutput();
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Input"));

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Disk size:"));
        form.add(diskSizeField);
        form.add(new JLabel("Initial head position:"));
        form.add(headField);
        if (scheduler.usesDirection()) {
            form.add(new JLabel("Initial direction:"));
            form.add(directionBox);
        }
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, form.getPreferredSize().height));
        panel.add(form);

        JPanel requestsLabel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        requestsLabel.add(new JLabel("Request queue (separate with commas or spaces):"));
        requestsLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, requestsLabel.getPreferredSize().height));
        panel.add(requestsLabel);
        requestsArea.setLineWrap(true);
        JScrollPane requestsScroll = new JScrollPane(requestsArea);
        requestsScroll.setPreferredSize(new Dimension(300, 110));
        panel.add(requestsScroll);

        JButton run = new JButton("Run Simulation");
        run.addActionListener(e -> runSimulation());
        JButton load = new JButton("Load Sample Data");
        load.addActionListener(e -> loadSample());
        JButton reset = new JButton("Reset / Clear");
        reset.addActionListener(e -> reset());
        JPanel buttons = new JPanel(new GridLayout(3, 1, 5, 5));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        buttons.add(run);
        buttons.add(load);
        buttons.add(reset);
        panel.add(buttons);

        status.setForeground(Color.RED);
        panel.add(status);
        return panel;
    }

    private JPanel buildOutputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        sequenceArea.setEditable(false);
        sequenceArea.setLineWrap(true);
        JScrollPane sequenceScroll = new JScrollPane(sequenceArea);
        sequenceScroll.setBorder(BorderFactory.createTitledBorder("Seek Sequence"));
        sequenceScroll.setPreferredSize(new Dimension(400, 75));
        panel.add(sequenceScroll);

        JPanel totals = new JPanel(new GridLayout(2, 1));
        totals.setBorder(BorderFactory.createTitledBorder("Totals"));
        totals.add(totalMovement);
        totals.add(averageSeek);
        panel.add(totals);

        JScrollPane graphScroll = new JScrollPane(pathPanel);
        graphScroll.setBorder(BorderFactory.createTitledBorder("Graph of the Head's Path"));
        graphScroll.setPreferredSize(new Dimension(400, 270));
        panel.add(graphScroll);

        movementScroll.setBorder(BorderFactory.createTitledBorder("Head Movement per Step"));
        movementScroll.setPreferredSize(new Dimension(400, 170));
        panel.add(movementScroll);
        return panel;
    }

    private void runSimulation() {
        try {
            int diskSize = InputCheck.parseInt(diskSizeField.getText(), "Disk size", 2, MAX_DISK_SIZE);
            int head = InputCheck.parseInt(headField.getText(), "Initial head position", 0, diskSize - 1);
            List<Integer> requests = readRequests(diskSize);
            DiskScheduler.Direction direction = directionBox.getSelectedIndex() == 0
                    ? DiskScheduler.Direction.HIGHER : DiskScheduler.Direction.LOWER;

            status.setText(" ");
            show(scheduler.schedule(diskSize, head, requests, direction));
        } catch (InputCheck.InputException ex) {
            showError(ex.getMessage());
        }
    }

    private List<Integer> readRequests(int diskSize) throws InputCheck.InputException {
        String text = requestsArea.getText().trim();
        if (text.isEmpty()) {
            throw new InputCheck.InputException("The request queue is empty. Enter at least one cylinder number.");
        }
        String[] tokens = text.split("[,;\\s]+");
        if (tokens.length > MAX_REQUESTS) {
            throw new InputCheck.InputException("Too many requests (" + tokens.length + "). The maximum is " + MAX_REQUESTS + ".");
        }
        List<Integer> requests = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            requests.add(InputCheck.parseInt(tokens[i], "Request " + (i + 1), 0, diskSize - 1));
        }
        return requests;
    }

    private void loadSample() {
        diskSizeField.setText(String.valueOf(DEFAULT_DISK_SIZE));
        headField.setText(String.valueOf(SAMPLE_HEAD));
        directionBox.setSelectedIndex(0);
        requestsArea.setText(SAMPLE_REQUESTS);
        clearOutput();
        status.setText(" ");
    }

    private void reset() {
        diskSizeField.setText(String.valueOf(DEFAULT_DISK_SIZE));
        headField.setText("");
        directionBox.setSelectedIndex(0);
        requestsArea.setText("");
        clearOutput();
        status.setText(" ");
    }

    private void show(DiskResult result) {
        StringBuilder order = new StringBuilder(String.valueOf(result.path.get(0)));
        for (int i = 1; i < result.path.size(); i++) {
            order.append(" -> ").append(result.path.get(i));
        }
        sequenceArea.setText(order.toString());

        movementModel.setRowCount(0);
        for (int step = 0; step < result.reasons.size(); step++) {
            movementModel.addRow(new Object[] {
                step + 1, result.path.get(step), result.path.get(step + 1), result.movementAt(step)});
        }

        totalMovement.setText("Total head movement: " + result.totalMovement() + " cylinders");
        averageSeek.setText(String.format(Locale.US, "Average seek distance: %.2f cylinders", result.averageSeek()));
        pathPanel.setResult(result);
        revalidate();
    }

    private void clearOutput() {
        sequenceArea.setText("");
        movementModel.setRowCount(0);
        totalMovement.setText("Total head movement: -");
        averageSeek.setText("Average seek distance: -");
        pathPanel.clear();
        revalidate();
    }

    private void showError(String message) {
        status.setText("<html><body style='width:280px'>Error: " + message + "</body></html>");
    }
}
