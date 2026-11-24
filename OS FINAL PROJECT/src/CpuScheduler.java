import java.util.List;

/** Shared contract for every CPU scheduling algorithm. Contains no UI code. */
public interface CpuScheduler {
    String getName();

    /** True when the algorithm needs a priority number per process. */
    boolean usesPriority();

    CpuResult schedule(List<CpuProcess> processes);
}
