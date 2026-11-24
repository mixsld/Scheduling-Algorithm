import java.util.List;

/** Shared contract for every disk scheduling algorithm. Contains no UI code. */
public interface DiskScheduler {

    enum Direction { HIGHER, LOWER }

    String getName();

    /** True when the algorithm needs an initial direction (SCAN family). */
    boolean usesDirection();

    /** Cylinders run from 0 to diskSize - 1. */
    DiskResult schedule(int diskSize, int head, List<Integer> requests, Direction direction);
}
