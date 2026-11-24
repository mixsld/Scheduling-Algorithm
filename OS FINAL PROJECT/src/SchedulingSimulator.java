import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class SchedulingSimulator {
    private static final Scanner INPUT = new Scanner(System.in);

    private static final class Process {
        final int id;
        final int arrival;
        final int burst;
        final int priority;

        Process(int id, int arrival, int burst, int priority) {
            this.id = id;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
        }

        String name() {
            return "P" + id;
        }
    }

    private static final class ProcessResult {
        final Process process;
        final int firstStart;
        final int completion;
        final int turnaround;
        final int waiting;
        final int response;

        ProcessResult(Process process, int firstStart, int completion) {
            this.process = process;
            this.firstStart = firstStart;
            this.completion = completion;
            this.turnaround = completion - process.arrival;
            this.waiting = turnaround - process.burst;
            this.response = firstStart - process.arrival;
        }
    }

    private static final class Segment {
        final String label;
        final int start;
        int end;

        Segment(String label, int start, int end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("  CPU AND DISK SCHEDULING SIMULATOR");
        System.out.println("===============================================");
        System.out.println("A. CPU First-Come, First-Serve (FCFS)");
        System.out.println("B. CPU Priority Scheduling (Preemptive)");
        System.out.println("C. Disk Circular SCAN (C-SCAN)");
        System.out.println("D. Disk First-Come, First-Serve (FCFS)");
        System.out.println();

        String choice = readChoice("Choose A, B, C, or D: ", "ABCD");

        System.out.println();

        if (choice.equals("A")) {
            runCpuFcfs();
        } else if (choice.equals("B")) {
            runPreemptivePriority();
        } else if (choice.equals("C")) {
            runCScan();
        } else {
            runDiskFcfs();
        }
    }

    // =========================================================
    // A. CPU FIRST-COME, FIRST-SERVE
    // =========================================================

    private static void runCpuFcfs() {
        System.out.println("CPU FIRST-COME, FIRST-SERVE (FCFS)");

        List<Process> processes = readCpuProcesses(false);
        List<Process> ordered = new ArrayList<>(processes);

        ordered.sort(
                Comparator.comparingInt((Process process) -> process.arrival)
                        .thenComparingInt(process -> process.id)
        );

        List<ProcessResult> results = new ArrayList<>();
        List<Segment> gantt = new ArrayList<>();

        int time = 0;
        int busyTime = 0;

        for (Process process : ordered) {
            if (time < process.arrival) {
                addSegment(gantt, "IDLE", time, process.arrival);
                time = process.arrival;
            }

            int firstStart = time;

            time += process.burst;
            busyTime += process.burst;

            addSegment(gantt, process.name(), firstStart, time);

            results.add(
                    new ProcessResult(process, firstStart, time)
            );
        }

        results.sort(
                Comparator.comparingInt(result -> result.process.id)
        );

        printCpuResults(
                results,
                gantt,
                busyTime,
                time,
                false
        );
    }

    // =========================================================
    // B. CPU PRIORITY SCHEDULING - PREEMPTIVE
    // =========================================================

    private static void runPreemptivePriority() {
        System.out.println("CPU PRIORITY SCHEDULING (PREEMPTIVE)");
        System.out.println("Lower priority number = higher priority.");
        System.out.println(
                "Equal priority does not preempt the running process."
        );

        List<Process> processes = readCpuProcesses(true);

        Map<Process, Integer> remaining = new HashMap<>();
        Map<Process, Integer> firstStart = new HashMap<>();
        Map<Process, Integer> completion = new HashMap<>();

        for (Process process : processes) {
            remaining.put(process, process.burst);
        }

        List<Segment> gantt = new ArrayList<>();

        Process running = null;

        int time = 0;
        int busyTime = 0;
        int completed = 0;

        while (completed < processes.size()) {
            Process best = bestReadyProcess(
                    processes,
                    remaining,
                    time
            );

            if (running == null) {
                running = best;
            } else if (
                    best != null
                    && best.priority < running.priority
            ) {
                /*
                 * Preempt only when the new process has a
                 * strictly higher priority.
                 */
                running = best;
            }

            if (running == null) {
                int nextArrival = Integer.MAX_VALUE;

                for (Process process : processes) {
                    if (
                            remaining.get(process) > 0
                            && process.arrival > time
                    ) {
                        nextArrival = Math.min(
                                nextArrival,
                                process.arrival
                        );
                    }
                }

                addSegment(
                        gantt,
                        "IDLE",
                        time,
                        nextArrival
                );

                time = nextArrival;
                continue;
            }

            if (!firstStart.containsKey(running)) {
                firstStart.put(running, time);
            }

            addSegment(
                    gantt,
                    running.name(),
                    time,
                    time + 1
            );

            remaining.put(
                    running,
                    remaining.get(running) - 1
            );

            time++;
            busyTime++;

            if (remaining.get(running) == 0) {
                completion.put(running, time);
                completed++;
                running = null;
            }
        }

        List<ProcessResult> results = new ArrayList<>();

        for (Process process : processes) {
            results.add(
                    new ProcessResult(
                            process,
                            firstStart.get(process),
                            completion.get(process)
                    )
            );
        }

        results.sort(
                Comparator.comparingInt(result -> result.process.id)
        );

        printCpuResults(
                results,
                gantt,
                busyTime,
                time,
                true
        );
    }

    private static Process bestReadyProcess(
            List<Process> processes,
            Map<Process, Integer> remaining,
            int time
    ) {
        Process best = null;

        for (Process process : processes) {
            boolean isReady =
                    process.arrival <= time
                    && remaining.get(process) > 0;

            if (!isReady) {
                continue;
            }

            if (
                    best == null
                    || process.priority < best.priority
                    || (
                            process.priority == best.priority
                            && process.arrival < best.arrival
                    )
                    || (
                            process.priority == best.priority
                            && process.arrival == best.arrival
                            && process.id < best.id
                    )
            ) {
                best = process;
            }
        }

        return best;
    }

    private static List<Process> readCpuProcesses(
            boolean needsPriority
    ) {
        int count = readInt(
                "Number of processes (1-20): ",
                1,
                20
        );

        List<Process> processes = new ArrayList<>();

        for (int id = 1; id <= count; id++) {
            System.out.println();
            System.out.println("Process P" + id);

            int arrival = readInt(
                    "  Arrival time: ",
                    0,
                    Integer.MAX_VALUE
            );

            int burst = readInt(
                    "  Burst time: ",
                    1,
                    Integer.MAX_VALUE
            );

            int priority = 0;

            if (needsPriority) {
                priority = readInt(
                        "  Priority number: ",
                        0,
                        Integer.MAX_VALUE
                );
            }

            processes.add(
                    new Process(
                            id,
                            arrival,
                            burst,
                            priority
                    )
            );
        }

        return processes;
    }

    private static void addSegment(
            List<Segment> gantt,
            String label,
            int start,
            int end
    ) {
        if (end <= start) {
            return;
        }

        if (!gantt.isEmpty()) {
            Segment last = gantt.get(gantt.size() - 1);

            if (
                    last.label.equals(label)
                    && last.end == start
            ) {
                last.end = end;
                return;
            }
        }

        gantt.add(
                new Segment(label, start, end)
        );
    }

    private static void printCpuResults(
            List<ProcessResult> results,
            List<Segment> gantt,
            int busyTime,
            int totalTime,
            boolean showPriority
    ) {
        System.out.println();
        System.out.println("GANTT CHART");

        for (Segment segment : gantt) {
            System.out.printf(
                    "| %s [%d-%d] ",
                    segment.label,
                    segment.start,
                    segment.end
            );
        }

        System.out.println("|");
        System.out.println();

        if (showPriority) {
            System.out.printf(
                    "%-5s %-5s %-5s %-5s %-5s "
                    + "%-5s %-5s %-5s %-5s%n",
                    "PID",
                    "AT",
                    "BT",
                    "PR",
                    "FST",
                    "CT",
                    "TAT",
                    "WT",
                    "RT"
            );
        } else {
            System.out.printf(
                    "%-5s %-5s %-5s %-5s "
                    + "%-5s %-5s %-5s %-5s%n",
                    "PID",
                    "AT",
                    "BT",
                    "FST",
                    "CT",
                    "TAT",
                    "WT",
                    "RT"
            );
        }

        double totalCompletion = 0;
        double totalTurnaround = 0;
        double totalWaiting = 0;
        double totalResponse = 0;

        for (ProcessResult result : results) {
            Process process = result.process;

            if (showPriority) {
                System.out.printf(
                        "%-5s %-5d %-5d %-5d %-5d "
                        + "%-5d %-5d %-5d %-5d%n",
                        process.name(),
                        process.arrival,
                        process.burst,
                        process.priority,
                        result.firstStart,
                        result.completion,
                        result.turnaround,
                        result.waiting,
                        result.response
                );
            } else {
                System.out.printf(
                        "%-5s %-5d %-5d %-5d "
                        + "%-5d %-5d %-5d %-5d%n",
                        process.name(),
                        process.arrival,
                        process.burst,
                        result.firstStart,
                        result.completion,
                        result.turnaround,
                        result.waiting,
                        result.response
                );
            }

            totalCompletion += result.completion;
            totalTurnaround += result.turnaround;
            totalWaiting += result.waiting;
            totalResponse += result.response;
        }

        int count = results.size();

        System.out.println();

        System.out.printf(
                Locale.US,
                "Average completion time : %.2f%n",
                totalCompletion / count
        );

        System.out.printf(
                Locale.US,
                "Average turnaround time : %.2f%n",
                totalTurnaround / count
        );

        System.out.printf(
                Locale.US,
                "Average waiting time    : %.2f%n",
                totalWaiting / count
        );

        System.out.printf(
                Locale.US,
                "Average response time   : %.2f%n",
                totalResponse / count
        );

        System.out.printf(
                Locale.US,
                "CPU utilization         : %.2f%%%n",
                busyTime * 100.0 / totalTime
        );

        System.out.printf(
                Locale.US,
                "Throughput              : "
                + "%.4f processes/time unit%n",
                count * 1.0 / totalTime
        );
    }

    // =========================================================
    // C. C-SCAN DISK SCHEDULING
    // =========================================================

    private static void runCScan() {
        System.out.println("DISK CIRCULAR SCAN (C-SCAN)");

        int diskSize = readInt(
                "Disk size: ",
                1,
                Integer.MAX_VALUE
        );

        int head = readInt(
                "Initial head position (0-"
                + (diskSize - 1)
                + "): ",
                0,
                diskSize - 1
        );

        List<Integer> requests =
                readDiskRequests(diskSize);

        String direction = readChoice(
                "Direction - H for higher, L for lower: ",
                "HL"
        );

        List<Integer> beforeWrap = new ArrayList<>();
        List<Integer> afterWrap = new ArrayList<>();

        for (int request : requests) {
            if (direction.equals("H")) {
                if (request >= head) {
                    beforeWrap.add(request);
                } else {
                    afterWrap.add(request);
                }
            } else {
                if (request <= head) {
                    beforeWrap.add(request);
                } else {
                    afterWrap.add(request);
                }
            }
        }

        if (direction.equals("H")) {
            Collections.sort(beforeWrap);
            Collections.sort(afterWrap);
        } else {
            beforeWrap.sort(
                    Collections.reverseOrder()
            );

            afterWrap.sort(
                    Collections.reverseOrder()
            );
        }

        List<Integer> path = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        path.add(head);

        for (int request : beforeWrap) {
            appendDiskStep(
                    path,
                    reasons,
                    request,
                    "Service request"
            );
        }

        if (!afterWrap.isEmpty()) {
            int endpoint;
            int oppositeEndpoint;

            if (direction.equals("H")) {
                endpoint = diskSize - 1;
                oppositeEndpoint = 0;
            } else {
                endpoint = 0;
                oppositeEndpoint = diskSize - 1;
            }

            if (
                    path.get(path.size() - 1)
                    != endpoint
            ) {
                appendDiskStep(
                        path,
                        reasons,
                        endpoint,
                        "Physical endpoint"
                );
            }

            appendDiskStep(
                    path,
                    reasons,
                    oppositeEndpoint,
                    "Wrap-around"
            );

            for (int request : afterWrap) {
                appendDiskStep(
                        path,
                        reasons,
                        request,
                        "Service request"
                );
            }
        }

        String directionText;

        if (direction.equals("H")) {
            directionText =
                    "Toward higher cylinders";
        } else {
            directionText =
                    "Toward lower cylinders";
        }

        printDiskResults(
                path,
                reasons,
                requests.size(),
                directionText
        );
    }

    // =========================================================
    // D. DISK FIRST-COME, FIRST-SERVE
    // =========================================================

    private static void runDiskFcfs() {
        System.out.println(
                "DISK FIRST-COME, FIRST-SERVE (FCFS)"
        );

        int diskSize = readInt(
                "Disk size: ",
                1,
                Integer.MAX_VALUE
        );

        int head = readInt(
                "Initial head position (0-"
                + (diskSize - 1)
                + "): ",
                0,
                diskSize - 1
        );

        List<Integer> requests =
                readDiskRequests(diskSize);

        List<Integer> path = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        path.add(head);

        for (int request : requests) {
            appendDiskStep(
                    path,
                    reasons,
                    request,
                    "Service request"
            );
        }

        printDiskResults(
                path,
                reasons,
                requests.size(),
                "Not applicable"
        );
    }

    private static List<Integer> readDiskRequests(
            int diskSize
    ) {
        int count = readInt(
                "Number of requests (1-20): ",
                1,
                20
        );

        List<Integer> requests = new ArrayList<>();

        for (
                int number = 1;
                number <= count;
                number++
        ) {
            int request = readInt(
                    "  Request "
                    + number
                    + " (0-"
                    + (diskSize - 1)
                    + "): ",
                    0,
                    diskSize - 1
            );

            requests.add(request);
        }

        return requests;
    }

    private static void appendDiskStep(
            List<Integer> path,
            List<String> reasons,
            int cylinder,
            String reason
    ) {
        path.add(cylinder);
        reasons.add(reason);
    }

    private static void printDiskResults(
            List<Integer> path,
            List<String> reasons,
            int requestCount,
            String direction
    ) {
        int totalMovement = 0;

        System.out.println();
        System.out.println("MOVEMENT TABLE");

        System.out.printf(
                "%-6s %-8s %-8s %-10s %-20s%n",
                "Step",
                "From",
                "To",
                "Movement",
                "Reason"
        );

        for (
                int index = 1;
                index < path.size();
                index++
        ) {
            int from = path.get(index - 1);
            int to = path.get(index);
            int movement = Math.abs(to - from);

            totalMovement += movement;

            System.out.printf(
                    "%-6d %-8d %-8d %-10d %-20s%n",
                    index,
                    from,
                    to,
                    movement,
                    reasons.get(index - 1)
            );
        }

        System.out.println();
        System.out.print("Complete head path: ");

        for (
                int index = 0;
                index < path.size();
                index++
        ) {
            if (index > 0) {
                System.out.print(" -> ");
            }

            System.out.print(path.get(index));
        }

        System.out.println();

        System.out.println(
                "Initial direction : " + direction
        );

        System.out.println(
                "Total movement    : "
                + totalMovement
                + " cylinders"
        );

        System.out.printf(
                Locale.US,
                "Average seek      : "
                + "%.2f cylinders/request%n",
                totalMovement * 1.0 / requestCount
        );
    }

    // =========================================================
    // INPUT VALIDATION
    // =========================================================

    private static int readInt(
            String prompt,
            int minimum,
            int maximum
    ) {
        while (true) {
            System.out.print(prompt);

            String text = INPUT.nextLine().trim();

            try {
                int value = Integer.parseInt(text);

                if (
                        value < minimum
                        || value > maximum
                ) {
                    System.out.println(
                            "Enter a value from "
                            + minimum
                            + " to "
                            + maximum
                            + "."
                    );
                } else {
                    return value;
                }
            } catch (NumberFormatException exception) {
                System.out.println(
                        "Enter a valid whole number."
                );
            }
        }
    }

    private static String readChoice(
            String prompt,
            String allowed
    ) {
        while (true) {
            System.out.print(prompt);

            String value = INPUT.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (
                    value.length() == 1
                    && allowed.contains(value)
            ) {
                return value;
            }

            System.out.println(
                    "Valid choices: " + allowed
            );
        }
    }
}