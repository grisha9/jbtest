package ru.rzn.gmyasoedov.model;

import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Состояние обработки каталога
 * processingTasks - обработанные/обрабатываемые таски в каталоге
 * reportTypes - типы отчетов в каталоге
 */
public class CatalogData {
    private final String canonicalPath;
    private final Set<ReportTask> processingTasks;
    private final Set<ReportType> reportTypes;

    public CatalogData(ReportType reportType, String canonicalPath) {
        this.canonicalPath = canonicalPath;
        this.processingTasks = ConcurrentHashMap.newKeySet();
        this.reportTypes = ConcurrentHashMap.newKeySet();
        this.reportTypes.add(reportType);
    }

    public void addReportTypes(ReportType reportType) {
        reportTypes.add(reportType);
    }

    public Set<ReportType> getReportTypes() {
        return reportTypes;
    }

    public boolean addProcessingTasks(ReportTask reportTask) {
        return processingTasks.add(reportTask);
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }
}
