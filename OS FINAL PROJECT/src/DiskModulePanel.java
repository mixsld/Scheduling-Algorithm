import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
    private final JComboBox<String> directionBox = new JComboBox<>(new String[] {
        "Toward higher cylinders", "Toward lower cylinders"});
    private final JTextArea requestsArea = new JTextArea(4, 20);
    private final JLabel status = new JLabel(" ");

    private final JTextArea sequenceArea = new JTextArea(2, 20);
    private final DefaultTableModel movementModel;
    private final JTable movementTable;
    private final JScrollPane movementScroll;
    private final HeadPathPanel pathPanel = new HeadPathPanel();
    private final JLabel totalMovement = new JLabel();
    private final JLabel averageSeek = new JLabel();

    DiskModulePanel(DiskScheduler scheduler, Runnable onBack) {
        this.scheduler = scheduler;

        setLayout(new BorderLayout());
        setBackground(Ui.BG);

        JButton back = Ui.secondaryButton("← Main Menu");
        back.addActionListener(e -> onBack.run());
        add(Ui.headerBar("Disk Scheduling", scheduler.getName(), back), BorderLayout.NORTH);

        movementModel = new DefaultTableModel(new String[] {"Step", "From", "To", "Head Movement"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movementTable = new JTable(movementModel);
        Ui.styleTable(movementTable);
        movementScroll = new JScrollPane(movementTable);

        add(buildInputSide(), BorderLayout.WEST);
        add(buildOutputSide(), BorderLayout.CENTER);
    }

    private JPanel buildInputSide() {
        JPanel card = Ui.card("Input");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(labelled("Disk size (number of cylinders):", diskSizeField));
        card.add(Box.createVerticalStrut(8));
        card.add(labelled("Initial head position:", headField));
        if (scheduler.usesDirection()) {
            card.add(Box.createVerticalStrut(8));
            directionBox.setFont(Ui.BASE);
            card.add(labelled("Initial direction:", directionBox));
        }
        card.add(Box.createVerticalStrut(12));

        JLabel requestsLabel = new JLabel(Ui.wrap("Request queue (cylinder numbers separated by commas or spaces):", 300));
        requestsLabel.setFont(Ui.BASE);
        card.add(requestsLabel);
        card.add(Box.createVerticalStrut(4));
        requestsArea.setFont(Ui.BASE);
        requestsArea.setLineWrap(true);
        requestsArea.setWrapStyleWord(true);
        JScrollPane requestsScroll = new JScrollPane(requestsArea);
        requestsScroll.setAlignmentX(LEFT_ALIGNMENT);
        requestsScroll.setPreferredSize(new Dimension(320, 100));
        requestsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.add(requestsScroll);
        card.add(Box.createVerticalStrut(14));

        JButton run = Ui.primaryButton("Run Simulation");
        run.addActionListener(e -> runSimulation());
        JButton sample = Ui.secondaryButton("Load Sample Data");
        sample.addActionListener(e -> loadSample());
        JButton reset = Ui.secondaryButton("Reset / Clear");
        reset.addActionListener(e -> reset());
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 8));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.add(run);
        buttons.add(sample);
        buttons.add(reset);
        card.add(buttons);
        card.add(Box.createVerticalStrut(10));
        status.setFont(Ui.BASE);
        status.setAlignmentX(LEFT_ALIGNMENT);
        card.add(status);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Ui.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 7));
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel labelled(String text, java.awt.Component field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel label = new JLabel(text);
        label.setFont(Ui.BASE);
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(Ui.BASE);
        }
        row.add(label, BorderLayout.CENTER);
        row.add(field, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return row;
    }

    private JScrollPane buildOutputSide() {
        JPanel sequenceCard = Ui.card("Order of Service (Seek Sequence)");
        sequenceCard.setLayout(new BorderLayout());
        sequenceArea.setEditable(false);
        sequenceArea.setLineWrap(true);
        sequenceArea.setWrapStyleWord(true);
        sequenceArea.setFont(Ui.BASE.deriveFont(15f));
        sequenceArea.setForeground(Ui.INK);
        sequenceArea.setOpaque(false);
        sequenceCard.add(sequenceArea, BorderLayout.CENTER);

        JPanel tableCard = Ui.card("Head Movement per Step");
        tableCard.setLayout(new BorderLayout());
        movementScroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        movementScroll.setPreferredSize(new Dimension(400, 80));
        tableCard.add(movementScroll, BorderLayout.CENTER);

        JPanel metricsCard = Ui.card("Summary Metrics");
        metricsCard.setLayout(new GridLayout(1, 2, 10, 10));
        metricsCard.add(Ui.metricTile("Total head movement", totalMovement));
        metricsCard.add(Ui.metricTile("Average seek distance", averageSeek));

        JPanel graphCard = Ui.card("Graph of the Head's Path");
        graphCard.setLayout(new BorderLayout());
        pathPanel.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        graphCard.add(pathPanel, BorderLayout.CENTER);

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackground(Ui.BG);
        column.add(sequenceCard);
        column.add(Box.createVerticalStrut(12));
        column.add(metricsCard);
        column.add(Box.createVerticalStrut(12));
        column.add(graphCard);
        column.add(Box.createVerticalStrut(12));
        column.add(tableCard);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Ui.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 7, 14, 14));
        wrapper.add(column, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void runSimulation() {
        try {
            int diskSize = Ui.parseInt(diskSizeField.getText(), "Disk size", 2, MAX_DISK_SIZE);
            int head = Ui.parseInt(headField.getText(), "Initial head position", 0, diskSize - 1);
            List<Integer> requests = readRequests(diskSize);
            DiskScheduler.Direction direction = directionBox.getSelectedIndex() == 0
                    ? DiskScheduler.Direction.HIGHER : DiskScheduler.Direction.LOWER;

            status.setText(" ");
            show(scheduler.schedule(diskSize, head, requests, direction));
        } catch (Ui.InputException ex) {
            showError(ex.getMessage());
        }
    }

    private List<Integer> readRequests(int diskSize) throws Ui.InputException {
        String text = requestsArea.getText().trim();
        if (text.isEmpty()) {
            throw new Ui.InputException("The request queue is empty. Enter at least one cylinder number.");
        }
        String[] tokens = text.split("[,;\\s]+");
        if (tokens.length > MAX_REQUESTS) {
            throw new Ui.InputException("Too many requests (" + tokens.length + "). The maximum is " + MAX_REQUESTS + ".");
        }
        List<Integer> requests = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            requests.add(Ui.parseInt(tokens[i], "Request " + (i + 1), 0, diskSize - 1));
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
            order.append("  →  ").append(result.path.get(i));
        }
        sequenceArea.setText(order.toString());

        movementModel.setRowCount(0);
        for (int step = 0; step < result.reasons.size(); step++) {
            movementModel.addRow(new Object[] {
                step + 1, result.path.get(step), result.path.get(step + 1),
                result.movementAt(step)});
        }
        int height = movementTable.getRowHeight() * (result.reasons.size() + 1) + 6;
        movementScroll.setPreferredSize(new Dimension(400, Math.min(height, 360)));

        totalMovement.setText(result.totalMovement() + " cylinders");
        averageSeek.setText(String.format(Locale.US, "%.2f cylinders", result.averageSeek()));

        pathPanel.setResult(result);
        revalidate();
    }

    private void clearOutput() {
        sequenceArea.setText("");
        movementModel.setRowCount(0);
        movementScroll.setPreferredSize(new Dimension(400, 80));
        for (JLabel label : new JLabel[] {totalMovement, averageSeek}) {
            label.setText("-");
        }
        pathPanel.clear();
        revalidate();
    }

    private void showError(String message) {
        status.setForeground(Ui.ERROR);
        status.setText(Ui.wrap("Error: " + message, 300));
    }
}
