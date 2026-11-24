import java.util.List;

// Owner: Deinhiel Gonzales
public final class DiskFcfsScheduler implements DiskScheduler {
    @Override
    public String getName() {
        return "First-Come, First-Served (FCFS)";
    }

    @Override
    public boolean usesDirection() {
        return false;
    }

    @Override
    public DiskResult schedule(int diskSize, int head, List<Integer> requests, Direction direction) {
        DiskResult result = new DiskResult(diskSize, head, requests.size());
        for (int request : requests) {
            result.moveTo(request, DiskResult.SERVICE);
        }
        return result;
    }
}
