package ru.rzn.gmyasoedov.model;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CatalogData {
    private final String reportType;
    private final Path path;
    private final Set<ReportTask> processingTasks;

    public CatalogData(String reportType, Path path) {
        this.reportType = reportType;
        this.path = path;
        this.processingTasks = ConcurrentHashMap.newKeySet();
    }

    public String getReportType() {
        return reportType;
    }

    public boolean isProcessingTask(Path filePath, Class processorClass) {
        return processingTasks.contains(new ReportTask(filePath, processorClass));
    }

    public boolean addProcessingTasks(Path filePath, Class processorClass) {
        return processingTasks.add(new ReportTask(filePath, processorClass));
    }

    public Path getPath() {
        return path;
    }
}
