import java.util.ArrayList;
import java.util.List;

// Owner: Justin Panghulan
public final class PreemptivePriorityScheduler implements CpuScheduler {
    @Override
    public String getName() {
        return "Priority (Preemptive)";
    }

    @Override
    public boolean usesPriority() {
        return true;
    }

    @Override
    public CpuResult schedule(List<CpuProcess> processes) {
        int n = processes.size();
        int[] remaining = new int[n];
        int[] firstStart = new int[n];
        int[] completion = new int[n];
        for (int i = 0; i < n; i++) {
            remaining[i] = processes.get(i).burst;
        }

        List<GanttSegment> gantt = new ArrayList<>();
        int running = -1;
        int time = 0;
        int busy = 0;
        int done = 0;

        while (done < n) {
            int best = bestReady(processes, remaining, time);

            if (running == -1) {
                running = best;
            } else if (best != -1 && processes.get(best).priority < processes.get(running).priority) {
                running = best;
            }

            if (running == -1) {
                time = idleUntilNextArrival(processes, remaining, time, gantt);
                continue;
            }

            CpuProcess current = processes.get(running);
            if (remaining[running] == current.burst) {
                firstStart[running] = time;
            }

            CpuResult.addSegment(gantt, current.name(), current.id, time, time + 1);
            remaining[running]--;
            time++;
            busy++;

            if (remaining[running] == 0) {
                completion[running] = time;
                done++;
                running = -1;
            }
        }

        List<CpuResult.Row> rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            rows.add(new CpuResult.Row(processes.get(i), firstStart[i], completion[i]));
        }
        return new CpuResult(rows, gantt, busy, time);
    }

    private static int bestReady(List<CpuProcess> processes, int[] remaining, int time) {
        int best = -1;
        for (int i = 0; i < processes.size(); i++) {
            CpuProcess p = processes.get(i);
            if (p.arrival > time || remaining[i] == 0) {
                continue;
            }
            if (best == -1 || isBetter(p, processes.get(best))) {
                best = i;
            }
        }
        return best;
    }

    private static boolean isBetter(CpuProcess candidate, CpuProcess current) {
        if (candidate.priority != current.priority) {
            return candidate.priority < current.priority;
        }
        if (candidate.arrival != current.arrival) {
            return candidate.arrival < current.arrival;
        }
        return candidate.id < current.id;
    }

    private static int idleUntilNextArrival(List<CpuProcess> processes, int[] remaining, int time,
                                            List<GanttSegment> gantt) {
        int next = Integer.MAX_VALUE;
        for (int i = 0; i < processes.size(); i++) {
            if (remaining[i] > 0 && processes.get(i).arrival > time) {
                next = Math.min(next, processes.get(i).arrival);
            }
        }
        CpuResult.addSegment(gantt, GanttSegment.IDLE, 0, time, next);
        return next;
    }
}
