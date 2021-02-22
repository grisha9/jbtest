package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * хранилище текущих обработчиков отчетов
 */
public class FileProcessorHolder {

    private final Map<String, List<FileProcessor>> fileProcessorsByType;

    public FileProcessorHolder() {
        fileProcessorsByType = new ConcurrentHashMap<>();
    }

    public List<FileProcessor> getProcessorByType(@NotNull String type) {
        Preconditions.checkNotNull(type);
        return fileProcessorsByType.getOrDefault(type.toLowerCase(), Collections.emptyList());
    }

    public void addProcessor(@NotNull FileProcessor processor) {
        fileProcessorsByType.compute(processor.getType().toLowerCase(),
                (type, processors) -> {
                    if (processors == null) {
                        processors = new CopyOnWriteArrayList<>(Collections.singletonList(processor));
                    } else {
                        processors.add(processor);
                    }
                    return processors;
                });
    }

    public void removeProcessor(@NotNull FileProcessor removeProcessor) {
        fileProcessorsByType.computeIfPresent(
                removeProcessor.getType().toLowerCase(),
                (s, fileProcessors) -> {
                    fileProcessors.removeIf(processor -> processor.getClass() == removeProcessor.getClass());
                    return fileProcessors;
                });
    }
}
