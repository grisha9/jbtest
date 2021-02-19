package ru.rzn.gmyasoedov.service;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rzn.gmyasoedov.model.CatalogData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileProcessorService {
    private static final Set<String> SUPPORT_FILE_EXTENSION = ImmutableSet.of(".xml");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final FileProcessorHolder fileProcessorHolder;
    private final ExecutorService reportProcessorPool;

    public FileProcessorService(FileProcessorHolder fileProcessorHolder, int reportProcessorPoolSize) {
        this.fileProcessorHolder = fileProcessorHolder;
        this.reportProcessorPool = Executors.newFixedThreadPool(reportProcessorPoolSize);
    }

    public void processFiles(CatalogData catalogData) {
        List<FileProcessor> processors = fileProcessorHolder.getProcessorByType(catalogData.getReportType());
        if (processors.isEmpty()) {
            return;
        }

        try (Stream<Path> walk = Files.walk(catalogData.getPath())) {
            List<Path> supportFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportFile)
                    .collect(Collectors.toList());
            for (Path filePath : supportFiles) {
                processors.stream()
                        .filter(processor -> !catalogData.isProcessingTask(filePath, processor.getClass()))
                        .forEach(processor -> {
                            catalogData.addProcessingTasks(filePath, processor.getClass());
                            reportProcessorPool.submit(() -> processor.process(filePath));
                        });
            }
        } catch (Exception e) {
            logger.error("error process path {}", catalogData.getPath(), e);
        }
    }

    public void shutdown() {
        reportProcessorPool.shutdown();
    }

    public void shutdownNow() {
        reportProcessorPool.shutdownNow();
    }

    private boolean isSupportFile(Path path) {
        return SUPPORT_FILE_EXTENSION
                .contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()));
    }

}
