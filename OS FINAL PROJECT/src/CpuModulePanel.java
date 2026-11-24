import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

final class CpuModulePanel extends JPanel {
    static final int MAX_PROCESSES = 20;
    private static final int MAX_VALUE = 9999;

    private final CpuScheduler scheduler;
    private final boolean usesPriority;
    private final int[][] sample;

    private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(3, 1, MAX_PROCESSES, 1));
    private final DefaultTableModel inputModel;
    private final JTable inputTable;
    private final JLabel status = new JLabel(" ");

    private final GanttChartPanel gantt = new GanttChartPanel();
    private final DefaultTableModel resultModel;
    private final JTable resultTable;
    private final JScrollPane resultScroll;
    private final JLabel avgTurnaround = new JLabel();
    private final JLabel avgWaiting = new JLabel();
    private final JLabel avgResponse = new JLabel();
    private final JLabel utilization = new JLabel();
    private final JLabel throughput = new JLabel();

    CpuModulePanel(CpuScheduler scheduler, int[][] sample, Runnable onBack) {
        this.scheduler = scheduler;
        this.sample = sample;
        this.usesPriority = scheduler.usesPriority();

        setLayout(new BorderLayout());
        setBackground(Ui.BG);

        JButton back = Ui.secondaryButton("← Main Menu");
        back.addActionListener(e -> onBack.run());
        add(Ui.headerBar("CPU Scheduling", scheduler.getName(), back), BorderLayout.NORTH);

        String[] inputColumns = usesPriority
                ? new String[] {"PID", "Arrival Time", "Burst Time", "Priority"}
                : new String[] {"PID", "Arrival Time", "Burst Time"};
        inputModel = new DefaultTableModel(inputColumns, 0);
        inputTable = new JTable(inputModel);
        Ui.styleTable(inputTable);

        String[] resultColumns = usesPriority
                ? new String[] {"PID", "Completion", "Turnaround", "Waiting", "Response"}
                : new String[] {"PID", "Completion", "Turnaround", "Waiting", "Response"};
        resultModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultModel);
        Ui.styleTable(resultTable);
        resultScroll = new JScrollPane(resultTable);

        add(buildInputSide(), BorderLayout.WEST);
        add(buildOutputSide(), BorderLayout.CENTER);

        resizeInputRows(3);
    }

    private JPanel buildInputSide() {
        JPanel card = Ui.card("Input");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel countRow = new JPanel(new BorderLayout(8, 0));
        countRow.setOpaque(false);
        JLabel countLabel = new JLabel("Number of processes (1-" + MAX_PROCESSES + "):");
        countLabel.setFont(Ui.BASE);
        countSpinner.setFont(Ui.BASE);
        countSpinner.addChangeListener(e -> resizeInputRows((Integer) countSpinner.getValue()));
        countRow.add(countLabel, BorderLayout.CENTER);
        countRow.add(countSpinner, BorderLayout.EAST);
        countRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JScrollPane tableScroll = new JScrollPane(inputTable);
        tableScroll.setPreferredSize(new Dimension(380, 330));

        JButton run = Ui.primaryButton("Run Simulation");
        run.addActionListener(e -> runSimulation());
        JButton sample = Ui.secondaryButton("Load Sample Data");
        sample.addActionListener(e -> loadSample());
        JButton reset = Ui.secondaryButton("Reset / Clear");
        reset.addActionListener(e -> reset());
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 8));
        buttons.setOpaque(false);
        buttons.add(run);
        buttons.add(sample);
        buttons.add(reset);

        status.setFont(Ui.BASE);

        for (JComponent part : new JComponent[] {countRow, tableScroll, buttons, status}) {
            part.setAlignmentX(LEFT_ALIGNMENT);
        }
        card.add(countRow);
        card.add(Box.createVerticalStrut(8));
        card.add(tableScroll);
        card.add(Box.createVerticalStrut(12));
        card.add(buttons);
        card.add(Box.createVerticalStrut(10));
        card.add(status);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Ui.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 7));
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private JScrollPane buildOutputSide() {
        JPanel ganttCard = Ui.card("Gantt Chart");
        ganttCard.setLayout(new BorderLayout());
        JScrollPane ganttScroll = new JScrollPane(gantt,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        ganttScroll.setPreferredSize(new Dimension(400, 150));
        ganttCard.add(ganttScroll, BorderLayout.CENTER);

        JPanel tableCard = Ui.card("Results per Process");
        tableCard.setLayout(new BorderLayout());
        resultScroll.setBorder(BorderFactory.createLineBorder(Ui.BORDER));
        resultScroll.setPreferredSize(new Dimension(400, 80));
        tableCard.add(resultScroll, BorderLayout.CENTER);

        JPanel metricsCard = Ui.card("Summary Metrics");
        metricsCard.setLayout(new GridLayout(2, 3, 10, 10));
        metricsCard.add(Ui.metricTile("Average turnaround time", avgTurnaround));
        metricsCard.add(Ui.metricTile("Average waiting time", avgWaiting));
        metricsCard.add(Ui.metricTile("Average response time", avgResponse));
        metricsCard.add(Ui.metricTile("CPU utilization", utilization));
        metricsCard.add(Ui.metricTile("Throughput (processes per time unit)", throughput));

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackground(Ui.BG);
        column.add(ganttCard);
        column.add(Box.createVerticalStrut(12));
        column.add(tableCard);
        column.add(Box.createVerticalStrut(12));
        column.add(metricsCard);

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
            List<CpuProcess> processes = readProcesses();
            status.setText(" ");
            show(scheduler.schedule(processes));
        } catch (Ui.InputException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadSample() {
        countSpinner.setValue(sample.length);
        resizeInputRows(sample.length);
        for (int row = 0; row < sample.length; row++) {
            inputModel.setValueAt("P" + sample[row][0], row, 0);
            inputModel.setValueAt(String.valueOf(sample[row][1]), row, 1);
            inputModel.setValueAt(String.valueOf(sample[row][2]), row, 2);
            if (usesPriority) {
                inputModel.setValueAt(String.valueOf(sample[row][3]), row, 3);
            }
        }
        clearOutput();
        status.setText(" ");
    }

    private void reset() {
        stopEditing();
        countSpinner.setValue(3);
        inputModel.setRowCount(0);
        resizeInputRows(3);
        clearOutput();
        status.setText(" ");
    }

    private void resizeInputRows(int count) {
        stopEditing();
        int current = inputModel.getRowCount();
        if (count < current) {
            inputModel.setRowCount(count);
            return;
        }
        for (int row = current; row < count; row++) {
            Object[] cells = new Object[inputModel.getColumnCount()];
            for (int col = 0; col < cells.length; col++) {
                cells[col] = col == 0 ? "P" + (row + 1) : "";
            }
            inputModel.addRow(cells);
        }
    }

    private void stopEditing() {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }
    }

    private List<CpuProcess> readProcesses() throws Ui.InputException {
        stopEditing();
        List<CpuProcess> processes = new ArrayList<>();
        Map<Integer, Integer> rowOfPid = new HashMap<>();

        for (int row = 0; row < inputModel.getRowCount(); row++) {
            int rowNumber = row + 1;
            String pidText = cell(row, 0).replaceFirst("^[Pp]", "");
            int id = Ui.parseInt(pidText, "PID in row " + rowNumber, 1, 999);
            if (rowOfPid.containsKey(id)) {
                throw new Ui.InputException("PID P" + id + " is used in both row " + rowOfPid.get(id)
                        + " and row " + rowNumber + ". Every PID must be different.");
            }
            rowOfPid.put(id, rowNumber);

            int arrival = Ui.parseInt(cell(row, 1), "Arrival time for P" + id, 0, MAX_VALUE);
            int burst = Ui.parseInt(cell(row, 2), "Burst time for P" + id, 1, MAX_VALUE);
            int priority = usesPriority ? Ui.parseInt(cell(row, 3), "Priority for P" + id, 0, MAX_VALUE) : 0;
            processes.add(new CpuProcess(id, arrival, burst, priority));
        }
        return processes;
    }

    private String cell(int row, int column) {
        Object value = inputModel.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private void show(CpuResult result) {
        gantt.setResult(result.gantt, result.totalTime);

        resultModel.setRowCount(0);
        for (CpuResult.Row row : result.rows) {
            List<Object> cells = new ArrayList<>();
            cells.add(row.process.name());
            cells.add(row.completion);
            cells.add(row.turnaround);
            cells.add(row.waiting);
            cells.add(row.response);
            resultModel.addRow(cells.toArray());
        }
        int height = resultTable.getRowHeight() * (result.rows.size() + 1) + 6;
        resultScroll.setPreferredSize(new Dimension(400, Math.min(height, 330)));

        avgTurnaround.setText(String.format(Locale.US, "%.2f", result.avgTurnaround()));
        avgWaiting.setText(String.format(Locale.US, "%.2f", result.avgWaiting()));
        avgResponse.setText(String.format(Locale.US, "%.2f", result.avgResponse()));
        utilization.setText(String.format(Locale.US, "%.2f%%", result.utilization()));
        throughput.setText(String.format(Locale.US, "%.4f", result.throughput()));
        revalidate();
    }

    private void clearOutput() {
        gantt.clear();
        resultModel.setRowCount(0);
        resultScroll.setPreferredSize(new Dimension(400, 80));
        for (JLabel label : new JLabel[] {avgTurnaround, avgWaiting, avgResponse, utilization, throughput}) {
            label.setText("-");
        }
        revalidate();
    }

    private void showError(String message) {
        status.setForeground(Ui.ERROR);
        status.setText(Ui.wrap("Error: " + message, 300));
    }
}
