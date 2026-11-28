# CPU and Disk Scheduling Simulator (ICS26012 - Operating Systems)

A Java Swing application that simulates two CPU and two disk scheduling algorithms.

| Category | Algorithms |
|---|---|
| CPU Scheduling | First-Come, First-Served (non-preemptive); Priority (preemptive) |
| Disk Scheduling | C-SCAN; First-Come, First-Served |

Repository: https://github.com/mixsld/Scheduling-Algorithm

## Requirements
- JDK/JRE 17 or newer (built and tested with JDK 22). Nothing else to install.

## Run
```
java -jar SchedulingSimulator.jar
```
or double-click `SchedulingSimulator.jar`.

## Build from source
From the project folder (the one containing `src`):
```
javac -d out src/*.java
jar cfm SchedulingSimulator.jar manifest.txt -C out .
```

## Using the program
1. Pick an algorithm on the main menu (grouped as CPU Scheduling and Disk Scheduling).
2. Enter the data, or press **Load Sample Data**. **Reset / Clear** empties the screen.
3. Press **Run Simulation**. Invalid input shows a clear message under the buttons.

## Conventions used
- Time starts at 0 in whole units; context-switch time is 0; idle CPU is shown in the Gantt chart.
- CPU ties: earlier arrival, then lower PID. Lower priority number = higher priority; no aging.
- Preemptive Priority preempts only on a strictly higher priority.
- Turnaround Time = Completion Time - Arrival Time; Waiting Time = Turnaround Time - Burst Time; Response Time = First CPU Time - Arrival Time;
  CPU Utilization = busy time / total time x 100; Throughput = processes / total time. The CPU screens show Completion, Turnaround,
  Waiting and Response Time for each process, the averages (in ms), CPU Utilization, Throughput, and a Gantt chart.
- Disk cylinders run from 0 to N-1 (default N = 200). Head movement is the sum of absolute differences
  between successive positions. C-SCAN travels to the physical end of the disk; the return jump is counted
  and shown in the path. Duplicate requests are served consecutively (zero movement).

## Code layout
- `CpuScheduler` / `DiskScheduler` - shared interfaces. Algorithm classes (`FcfsScheduler`,
  `PreemptivePriorityScheduler`, `CScanScheduler`, `DiskFcfsScheduler`) contain no UI code.
- `SchedulingSimulator` (menu), `CpuModulePanel`, `DiskModulePanel`, `GanttChartPanel`, `HeadPathPanel`, `InputCheck` - interface.
