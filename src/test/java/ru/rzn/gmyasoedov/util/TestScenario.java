package ru.rzn.gmyasoedov.util;

public class TestScenario {
    private final int periodMs;
    private final int processorThreadCount;
    private final int taskCount;
    private final int taskDelayMs;

    public TestScenario(String scenario) {
        String[] data = scenario.split(" ");
        this.periodMs = Integer.valueOf(data[0]);
        this.processorThreadCount = Integer.valueOf(data[1]);
        this.taskCount = Integer.valueOf(data[2]);
        this.taskDelayMs = Integer.valueOf(data[3]);
    }

    public int getPeriodMs() {
        return periodMs;
    }

    public int getProcessorThreadCount() {
        return processorThreadCount;
    }

    public int getTaskDelayMs() {
        return taskDelayMs;
    }

    public int getTaskCount() {
        return taskCount;
    }
}
