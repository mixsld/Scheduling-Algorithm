import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Owner: Jared Esguerra
public final class CScanScheduler implements DiskScheduler {
    @Override
    public String getName() {
        return "Circular SCAN (C-SCAN)";
    }

    @Override
    public boolean usesDirection() {
        return true;
    }

    @Override
    public DiskResult schedule(int diskSize, int head, List<Integer> requests, Direction direction) {
        boolean up = direction == Direction.HIGHER;

        List<Integer> ahead = new ArrayList<>();
        List<Integer> behind = new ArrayList<>();
        for (int request : requests) {
            boolean isAhead = up ? request >= head : request <= head;
            (isAhead ? ahead : behind).add(request);
        }
        Collections.sort(ahead);
        Collections.sort(behind);
        if (!up) {
            Collections.reverse(ahead);
            Collections.reverse(behind);
        }

        int end = up ? diskSize - 1 : 0;
        int opposite = up ? 0 : diskSize - 1;

        DiskResult result = new DiskResult(diskSize, head, requests.size());
        for (int request : ahead) {
            result.moveTo(request, DiskResult.SERVICE);
        }

        if (result.lastPosition() != end) {
            result.moveTo(end, DiskResult.END_OF_DISK);
        }
        if (!behind.isEmpty()) {
            if (result.lastPosition() != opposite) {
                result.moveTo(opposite, DiskResult.RETURN_JUMP);
            }
            for (int request : behind) {
                result.moveTo(request, DiskResult.SERVICE);
            }
        }
        return result;
    }
}
