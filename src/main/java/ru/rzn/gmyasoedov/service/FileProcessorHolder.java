package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.FileProcessorProxy;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * хранилище текущих обработчиков отчетов
 */
public class FileProcessorHolder {

    private final Map<ReportType, List<FileProcessorProxy>> fileProcessorsByType;

    public FileProcessorHolder() {
        fileProcessorsByType = new ConcurrentHashMap<>();
    }

    public List<FileProcessorProxy> getProcessorByType(@NotNull ReportType type) {
        Preconditions.checkNotNull(type);
        return fileProcessorsByType.getOrDefault(type, Collections.emptyList());
    }

    public void addProcessor(@NotNull FileProcessor processor) {
        FileProcessorProxy processorProxy = new FileProcessorProxy(processor);
        fileProcessorsByType.compute(processor.getReportType(),
                (type, processors) -> {
                    if (processors == null) {
                        processors = new CopyOnWriteArrayList<>(Collections.singletonList(processorProxy));
                    } else {
                        processors.add(processorProxy);
                    }
                    return processors;
                });
    }

    public void removeProcessor(@NotNull ReportType reportType) {
        fileProcessorsByType.computeIfPresent(
                reportType,
                (s, fileProcessors) -> {
                    fileProcessors.remove(0);
                    return fileProcessors;
                });
    }
}
