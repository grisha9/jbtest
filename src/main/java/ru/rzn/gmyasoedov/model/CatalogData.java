package ru.rzn.gmyasoedov.model;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CatalogData {
    private final Path path;
    private final Set<ReportTask> processingTasks;
    private final Set<String> reportTypes;

    public CatalogData(String reportType, Path path) {
        this.path = path;
        this.processingTasks = ConcurrentHashMap.newKeySet();
        this.reportTypes = ConcurrentHashMap.newKeySet();
        this.reportTypes.add(reportType);
    }

    public void addReportTypes(String reportType) {
        reportTypes.add(reportType);
    }

    public Set<String> getReportTypes() {
        return reportTypes;
    }

    public boolean isProcessingTask(Path filePath, Class processorClass) {
        return processingTasks.contains(new ReportTask(filePath, processorClass));
    }

    public void addProcessingTasks(Path filePath, Class processorClass) {
        processingTasks.add(new ReportTask(filePath, processorClass));
    }

    public Path getPath() {
        return path;
    }
}
