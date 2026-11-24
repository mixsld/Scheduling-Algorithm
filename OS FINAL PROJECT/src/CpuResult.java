import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CpuResult {
    static final class Row {
        final CpuProcess process;
        final int firstStart;
        final int completion;
        final int turnaround;
        final int waiting;
        final int response;

        Row(CpuProcess process, int firstStart, int completion) {
            this.process = process;
            this.firstStart = firstStart;
            this.completion = completion;
            this.turnaround = completion - process.arrival;
            this.waiting = turnaround - process.burst;
            this.response = firstStart - process.arrival;
        }
    }

    final List<Row> rows;
    final List<GanttSegment> gantt;
    final int busyTime;
    final int totalTime;

    CpuResult(List<Row> rows, List<GanttSegment> gantt, int busyTime, int totalTime) {
        this.rows = new ArrayList<>(rows);
        this.rows.sort(Comparator.comparingInt((Row r) -> r.process.id));
        this.gantt = gantt;
        this.busyTime = busyTime;
        this.totalTime = totalTime;
    }

    double avgTurnaround() {
        return rows.stream().mapToInt(r -> r.turnaround).average().orElse(0);
    }

    double avgWaiting() {
        return rows.stream().mapToInt(r -> r.waiting).average().orElse(0);
    }

    double avgResponse() {
        return rows.stream().mapToInt(r -> r.response).average().orElse(0);
    }

    double utilization() {
        return totalTime == 0 ? 0 : busyTime * 100.0 / totalTime;
    }

    double throughput() {
        return totalTime == 0 ? 0 : rows.size() / (double) totalTime;
    }

    static void addSegment(List<GanttSegment> gantt, String label, int processId, int start, int end) {
        if (end <= start) {
            return;
        }
        if (!gantt.isEmpty()) {
            GanttSegment last = gantt.get(gantt.size() - 1);
            if (last.processId == processId && last.end == start) {
                last.end = end;
                return;
            }
        }
        gantt.add(new GanttSegment(label, processId, start, end));
    }
}
