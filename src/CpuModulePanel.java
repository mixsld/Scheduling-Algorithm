import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
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
    private final JLabel avgCompletion = new JLabel();
    private final JLabel avgTurnaround = new JLabel();
    private final JLabel avgWaiting = new JLabel();
    private final JLabel avgResponse = new JLabel();
    private final JLabel utilization = new JLabel();
    private final JLabel throughput = new JLabel();

    CpuModulePanel(CpuScheduler scheduler, int[][] sample, Runnable onBack) {
        this.scheduler = scheduler;
        this.sample = sample;
        this.usesPriority = scheduler.usesPriority();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton back = new JButton("Back to Main Menu");
        back.addActionListener(e -> onBack.run());
        JLabel title = new JLabel("CPU Scheduling - " + scheduler.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(back);
        top.add(title);
        add(top, BorderLayout.NORTH);

        String[] inputColumns = usesPriority
                ? new String[] {"PID", "Arrival Time", "Burst Time", "Priority"}
                : new String[] {"PID", "Arrival Time", "Burst Time"};
        inputModel = new DefaultTableModel(inputColumns, 0);
        inputTable = new JTable(inputModel);

        String[] resultColumns = usesPriority
                ? new String[] {"PID", "Arrival Time", "Burst Time", "Priority", "Start", "Completion", "Turnaround", "Waiting", "Response"}
                : new String[] {"PID", "Arrival Time", "Burst Time", "Start", "Completion", "Turnaround", "Waiting", "Response"};
        resultModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultModel);
        resultScroll = new JScrollPane(resultTable);

        add(buildInputPanel(), BorderLayout.WEST);
        add(buildOutputPanel(), BorderLayout.CENTER);

        resizeInputRows(3);
        clearOutput();
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Input"));

        JPanel countRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countRow.add(new JLabel("Number of processes (1-" + MAX_PROCESSES + "):"));
        countSpinner.addChangeListener(e -> resizeInputRows((Integer) countSpinner.getValue()));
        countRow.add(countSpinner);
        countRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, countRow.getPreferredSize().height));
        panel.add(countRow);

        JScrollPane tableScroll = new JScrollPane(inputTable);
        tableScroll.setPreferredSize(new Dimension(340, 300));
        panel.add(tableScroll);

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

        JScrollPane ganttScroll = new JScrollPane(gantt);
        ganttScroll.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));
        ganttScroll.setPreferredSize(new Dimension(400, 135));
        panel.add(ganttScroll);

        resultScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        panel.add(resultScroll);

        JPanel averages = new JPanel(new GridLayout(6, 2, 10, 2));
        averages.setBorder(BorderFactory.createTitledBorder("Averages"));
        averages.add(new JLabel("Avg. Completion Time:"));
        averages.add(avgCompletion);
        averages.add(new JLabel("Avg. Turn Around Time:"));
        averages.add(avgTurnaround);
        averages.add(new JLabel("Avg. Waiting Time:"));
        averages.add(avgWaiting);
        averages.add(new JLabel("Avg. Response Time:"));
        averages.add(avgResponse);
        averages.add(new JLabel("CPU Utilization:"));
        averages.add(utilization);
        averages.add(new JLabel("Throughput:"));
        averages.add(throughput);
        averages.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        panel.add(averages);
        return panel;
    }

    private void runSimulation() {
        try {
            List<CpuProcess> processes = readProcesses();
            status.setText(" ");
            show(scheduler.schedule(processes));
        } catch (InputCheck.InputException ex) {
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

    private List<CpuProcess> readProcesses() throws InputCheck.InputException {
        stopEditing();
        List<CpuProcess> processes = new ArrayList<>();
        Map<Integer, Integer> rowOfPid = new HashMap<>();

        for (int row = 0; row < inputModel.getRowCount(); row++) {
            int rowNumber = row + 1;
            String pidText = cell(row, 0).replaceFirst("^[Pp]", "");
            int id = InputCheck.parseInt(pidText, "PID in row " + rowNumber, 1, 999);
            if (rowOfPid.containsKey(id)) {
                throw new InputCheck.InputException("PID P" + id + " is used in both row " + rowOfPid.get(id)
                        + " and row " + rowNumber + ". Every PID must be different.");
            }
            rowOfPid.put(id, rowNumber);

            int arrival = InputCheck.parseInt(cell(row, 1), "Arrival time for P" + id, 0, MAX_VALUE);
            int burst = InputCheck.parseInt(cell(row, 2), "Burst time for P" + id, 1, MAX_VALUE);
            int priority = usesPriority ? InputCheck.parseInt(cell(row, 3), "Priority for P" + id, 0, MAX_VALUE) : 0;
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
            cells.add(row.process.arrival);
            cells.add(row.process.burst);
            if (usesPriority) {
                cells.add(row.process.priority);
            }
            cells.add(row.firstStart);
            cells.add(row.completion);
            cells.add(row.turnaround);
            cells.add(row.waiting);
            cells.add(row.response);
            resultModel.addRow(cells.toArray());
        }
        resultScroll.setPreferredSize(new Dimension(400, 24 * (result.rows.size() + 1) + 40));

        avgCompletion.setText(String.format(Locale.US, "%.2f ms", result.avgCompletion()));
        avgTurnaround.setText(String.format(Locale.US, "%.2f ms", result.avgTurnaround()));
        avgWaiting.setText(String.format(Locale.US, "%.2f ms", result.avgWaiting()));
        avgResponse.setText(String.format(Locale.US, "%.2f ms", result.avgResponse()));
        utilization.setText(String.format(Locale.US, "%.2f %%", result.utilization()));
        throughput.setText(String.format(Locale.US, "%.4f processes/ms", result.throughput()));
        revalidate();
    }

    private void clearOutput() {
        gantt.clear();
        resultModel.setRowCount(0);
        resultScroll.setPreferredSize(new Dimension(400, 70));
        avgCompletion.setText("-");
        avgTurnaround.setText("-");
        avgWaiting.setText("-");
        avgResponse.setText("-");
        utilization.setText("-");
        throughput.setText("-");
        revalidate();
    }

    private void showError(String message) {
        status.setText("<html><body style='width:280px'>Error: " + message + "</body></html>");
    }
}
