import java.awt.BorderLayout;
import java.awt.Color;
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

/**
 * Screen for one CPU scheduling algorithm: input table, Gantt chart, result table and metrics.
 * All scheduling is done by the {@link CpuScheduler} it is given; this class only handles the interface.
 */
final class CpuModulePanel extends JPanel {
    static final int MAX_PROCESSES = 20;
    private static final int MAX_VALUE = 9999;

    // Built-in practice data (includes an idle gap). Replace with the facilitator's sets if desired.
    private static final int[][] SAMPLE = {
        // PID, arrival, burst, priority
        {1, 0, 5, 3}, {2, 1, 3, 1}, {3, 2, 8, 4}, {4, 3, 6, 2}, {5, 30, 4, 1}
    };

    private final CpuScheduler scheduler;
    private final boolean usesPriority;

    private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(3, 1, MAX_PROCESSES, 1));
    private final DefaultTableModel inputModel;
    private final JTable inputTable;
    private final JLabel status = new JLabel(" ");

    private final GanttChartPanel gantt = new GanttChartPanel();
    private final DefaultTableModel resultModel;
    private final JTable resultTable;
    private final JScrollPane resultScroll;
    private final JLabel avgCompletion = new JLabel();
    private final JLabel avgTurnaround = new JLabel();
    private final JLabel avgWaiting = new JLabel();
    private final JLabel avgResponse = new JLabel();
    private final JLabel utilization = new JLabel();
    private final JLabel throughput = new JLabel();

    CpuModulePanel(CpuScheduler scheduler, Runnable onBack) {
        this.scheduler = scheduler;
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
                ? new String[] {"PID", "Arrival", "Burst", "Priority", "Completion", "Turnaround", "Waiting", "Response"}
                : new String[] {"PID", "Arrival", "Burst", "Completion", "Turnaround", "Waiting", "Response"};
        resultModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultModel);
        Ui.styleTable(resultTable);
        int[] widths = usesPriority
                ? new int[] {50, 70, 60, 70, 105, 105, 75, 90}
                : new int[] {50, 70, 60, 105, 105, 75, 90};
        for (int col = 0; col < widths.length; col++) {
            resultTable.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        }
        resultScroll = new JScrollPane(resultTable);

        add(buildInputSide(), BorderLayout.WEST);
        add(buildOutputSide(), BorderLayout.CENTER);

        resizeInputRows(3);
    }

    // ---------------------------------------------------------------- layout

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
        tableScroll.setPreferredSize(new Dimension(320, 330));

        String hint = usesPriority
                ? "Click a cell to edit it. A lower priority number means a higher priority."
                : "Click a cell to edit it. Time starts at 0 in whole time units.";
        JLabel hintLabel = new JLabel(Ui.wrap(hint, 300));
        hintLabel.setFont(Ui.SMALL);
        hintLabel.setForeground(Ui.MUTED);

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

        for (JComponent part : new JComponent[] {countRow, tableScroll, hintLabel, buttons, status}) {
            part.setAlignmentX(LEFT_ALIGNMENT);
        }
        card.add(countRow);
        card.add(Box.createVerticalStrut(8));
        card.add(tableScroll);
        card.add(Box.createVerticalStrut(6));
        card.add(hintLabel);
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
        metricsCard.add(Ui.metricTile("Average completion time", avgCompletion));
        metricsCard.add(Ui.metricTile("Average turnaround time", avgTurnaround));
        metricsCard.add(Ui.metricTile("Average waiting time", avgWaiting));
        metricsCard.add(Ui.metricTile("Average response time", avgResponse));
        metricsCard.add(Ui.metricTile("CPU utilization", utilization));
        metricsCard.add(Ui.metricTile("Throughput", throughput));

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

    // ---------------------------------------------------------------- actions

    private void runSimulation() {
        try {
            List<CpuProcess> processes = readProcesses();
            show(scheduler.schedule(processes));
            setStatus("Simulation complete.", false);
        } catch (Ui.InputException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void loadSample() {
        countSpinner.setValue(SAMPLE.length);
        resizeInputRows(SAMPLE.length);
        for (int row = 0; row < SAMPLE.length; row++) {
            inputModel.setValueAt("P" + SAMPLE[row][0], row, 0);
            inputModel.setValueAt(String.valueOf(SAMPLE[row][1]), row, 1);
            inputModel.setValueAt(String.valueOf(SAMPLE[row][2]), row, 2);
            if (usesPriority) {
                inputModel.setValueAt(String.valueOf(SAMPLE[row][3]), row, 3);
            }
        }
        clearOutput();
        setStatus("Sample data loaded. Press Run Simulation.", false);
    }

    private void reset() {
        stopEditing();
        countSpinner.setValue(3);
        inputModel.setRowCount(0);
        resizeInputRows(3);
        clearOutput();
        setStatus(" ", false);
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

    // ---------------------------------------------------------------- input -> model

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

    // ---------------------------------------------------------------- output

    private void show(CpuResult result) {
        gantt.setResult(result.gantt, result.totalTime);

        resultModel.setRowCount(0);
        for (CpuResult.Row row : result.rows) {
            List<Object> cells = new ArrayList<>();
            cells.add(row.process.name());
            cells.add(row.process.arrival);
            cells.add(row.process.burst);
            if (usesPriority) {
                cells.add(row.process.priority);
            }
            cells.add(row.completion);
            cells.add(row.turnaround);
            cells.add(row.waiting);
            cells.add(row.response);
            resultModel.addRow(cells.toArray());
        }
        int height = resultTable.getRowHeight() * (result.rows.size() + 1) + 6;
        resultScroll.setPreferredSize(new Dimension(400, Math.min(height, 330)));

        avgCompletion.setText(String.format(Locale.US, "%.2f", result.avgCompletion()));
        avgTurnaround.setText(String.format(Locale.US, "%.2f", result.avgTurnaround()));
        avgWaiting.setText(String.format(Locale.US, "%.2f", result.avgWaiting()));
        avgResponse.setText(String.format(Locale.US, "%.2f", result.avgResponse()));
        utilization.setText(String.format(Locale.US, "%.2f%%", result.utilization()));
        throughput.setText(String.format(Locale.US, "%.4f proc/unit", result.throughput()));
        revalidate();
    }

    private void clearOutput() {
        gantt.clear();
        resultModel.setRowCount(0);
        resultScroll.setPreferredSize(new Dimension(400, 80));
        for (JLabel label : new JLabel[] {avgCompletion, avgTurnaround, avgWaiting, avgResponse, utilization, throughput}) {
            label.setText("-");
        }
        revalidate();
    }

    private void setStatus(String message, boolean error) {
        status.setForeground(error ? Ui.ERROR : Ui.OK);
        status.setText(Ui.wrap((error ? "Error: " : "") + message, 300));
    }
}
