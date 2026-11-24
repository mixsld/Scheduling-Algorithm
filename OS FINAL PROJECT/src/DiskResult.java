import java.util.ArrayList;
import java.util.List;

/** Output of a disk scheduler: the head path, why each move happened, and summary metrics. */
public final class DiskResult {
    static final String SERVICE = "Service request";
    static final String END_OF_DISK = "Travel to end of disk";
    static final String RETURN_JUMP = "Return jump";

    final int diskSize;
    final List<Integer> path = new ArrayList<>();      // path[0] is the initial head position
    final List<String> reasons = new ArrayList<>();    // reasons.get(i) explains path[i] -> path[i+1]
    final List<Integer> serviceOrder = new ArrayList<>();
    final int requestCount;

    DiskResult(int diskSize, int head, int requestCount) {
        this.diskSize = diskSize;
        this.requestCount = requestCount;
        path.add(head);
    }

    /** Moves the head to a cylinder, recording the reason. */
    void moveTo(int cylinder, String reason) {
        path.add(cylinder);
        reasons.add(reason);
        if (reason.equals(SERVICE)) {
            serviceOrder.add(cylinder);
        }
    }

    int lastPosition() {
        return path.get(path.size() - 1);
    }

    int movementAt(int step) {
        return Math.abs(path.get(step + 1) - path.get(step));
    }

    /** Sum of absolute differences between successive head positions. */
    int totalMovement() {
        int total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            total += movementAt(i);
        }
        return total;
    }

    double averageSeek() {
        return requestCount == 0 ? 0 : totalMovement() / (double) requestCount;
    }

    long seekTime(int seekRate) {
        return (long) totalMovement() * seekRate;
    }
}
