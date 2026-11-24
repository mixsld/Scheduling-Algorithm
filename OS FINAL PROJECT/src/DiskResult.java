import java.util.ArrayList;
import java.util.List;

public final class DiskResult {
    static final String SERVICE = "Service request";
    static final String END_OF_DISK = "Travel to end of disk";
    static final String RETURN_JUMP = "Return jump";

    final int diskSize;
    final List<Integer> path = new ArrayList<>();
    final List<String> reasons = new ArrayList<>();
    final int requestCount;

    DiskResult(int diskSize, int head, int requestCount) {
        this.diskSize = diskSize;
        this.requestCount = requestCount;
        path.add(head);
    }

    void moveTo(int cylinder, String reason) {
        path.add(cylinder);
        reasons.add(reason);
    }

    int lastPosition() {
        return path.get(path.size() - 1);
    }

    int movementAt(int step) {
        return Math.abs(path.get(step + 1) - path.get(step));
    }

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
}
