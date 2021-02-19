package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileProcessorHolder {

    private final Map<String, List<FileProcessor>> fileProcessorsByType;

    public FileProcessorHolder() {
        fileProcessorsByType = new ConcurrentHashMap<>();
    }

    public List<FileProcessor> getProcessorByType(@NotNull String type) {
        Preconditions.checkNotNull(type);
        return fileProcessorsByType.getOrDefault(type, Collections.emptyList());
    }

    public void addProcessor(@NotNull FileProcessor processor) {
        Preconditions.checkNotNull(processor);
        fileProcessorsByType.merge(
                processor.getType(),
                new CopyOnWriteArrayList<>(Collections.singletonList(processor)),
                (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                });
    }

    public void removeProcessor(@NotNull FileProcessor removeProcessor) {
        Preconditions.checkNotNull(removeProcessor);
        fileProcessorsByType.computeIfPresent(
                removeProcessor.getType(),
                (s, fileProcessors) -> {
                    fileProcessors.removeIf(processor -> processor.getClass() == removeProcessor.getClass());
                    return fileProcessors;
                });
    }
}
