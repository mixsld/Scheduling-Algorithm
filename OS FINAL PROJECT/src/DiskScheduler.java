import java.util.List;

public interface DiskScheduler {
    enum Direction { HIGHER, LOWER }

    String getName();

    boolean usesDirection();

    DiskResult schedule(int diskSize, int head, List<Integer> requests, Direction direction);
}
