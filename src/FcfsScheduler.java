import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Owner: Justin Panghulan
public final class FcfsScheduler implements CpuScheduler {
    @Override
    public String getName() {
        return "First-Come, First-Served (FCFS)";
    }

    @Override
    public boolean usesPriority() {
        return false;
    }

    @Override
    public CpuResult schedule(List<CpuProcess> processes) {
        List<CpuProcess> ordered = new ArrayList<>(processes);
        ordered.sort(Comparator.comparingInt((CpuProcess p) -> p.arrival).thenComparingInt(p -> p.id));

        List<CpuResult.Row> rows = new ArrayList<>();
        List<GanttSegment> gantt = new ArrayList<>();
        int time = 0;

        for (CpuProcess p : ordered) {
            if (time < p.arrival) {
                CpuResult.addSegment(gantt, GanttSegment.IDLE, 0, time, p.arrival);
                time = p.arrival;
            }
            int start = time;
            time += p.burst;
            CpuResult.addSegment(gantt, p.name(), p.id, start, time);
            rows.add(new CpuResult.Row(p, start, time));
        }
        return new CpuResult(rows, gantt, time);
    }
}
