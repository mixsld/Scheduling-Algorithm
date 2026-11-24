public final class CpuProcess {
    final int id;
    final int arrival;
    final int burst;
    final int priority;

    CpuProcess(int id, int arrival, int burst, int priority) {
        this.id = id;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
    }

    String name() {
        return "P" + id;
    }
}
