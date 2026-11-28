import java.util.List;

public interface CpuScheduler {
    String getName();

    boolean usesPriority();

    CpuResult schedule(List<CpuProcess> processes);
}
