/** One block of the Gantt chart: a process (or IDLE) running from start to end. */
public final class GanttSegment {
    static final String IDLE = "IDLE";

    final String label;
    final int processId; // 0 for idle
    final int start;
    int end;

    GanttSegment(String label, int processId, int start, int end) {
        this.label = label;
        this.processId = processId;
        this.start = start;
        this.end = end;
    }

    boolean isIdle() {
        return processId == 0;
    }
}
