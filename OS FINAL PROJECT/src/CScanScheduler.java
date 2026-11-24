import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Disk scheduling: C-SCAN (circular SCAN).
 * Owner: [Member name]
 *
 * The head sweeps in the chosen direction serving requests, continues to the physical
 * end of the disk, then jumps back to the opposite end and sweeps the same way again for
 * the requests it skipped. The return jump is counted in the total head movement.
 * The jump is only made when requests are still waiting on the other side.
 */
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

        // Split into requests the sweep reaches first (ahead) and those left for after the jump.
        List<Integer> ahead = new ArrayList<>();
        List<Integer> behind = new ArrayList<>();
        for (int request : requests) {
            boolean isAhead = up ? request >= head : request <= head;
            (isAhead ? ahead : behind).add(request);
        }
        Collections.sort(ahead);
        Collections.sort(behind);
        if (!up) {
            Collections.reverse(ahead);   // sweep downward: nearest (highest) first
            Collections.reverse(behind);  // after the jump to the top, continue downward
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
