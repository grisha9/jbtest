package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * хранилище текущих обработчиков отчетов
 */
public class FileProcessorHolder {

    private final Map<ReportType, FileProcessor> fileProcessorsByType;

    public FileProcessorHolder() {
        fileProcessorsByType = new ConcurrentHashMap<>();
    }

    @Nullable
    public FileProcessor getProcessorByType(@NotNull ReportType reportType) {
        Preconditions.checkNotNull(reportType);
        return fileProcessorsByType.get(reportType);
    }

    public void addProcessor(@NotNull FileProcessor processor) {
        Preconditions.checkNotNull(processor);
        fileProcessorsByType.put(processor.getReportType(), processor);
    }

    public void removeProcessor(@NotNull ReportType reportType) {
        Preconditions.checkNotNull(reportType);
        fileProcessorsByType.remove(reportType);
    }
}
