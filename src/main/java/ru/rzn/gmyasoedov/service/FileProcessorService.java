package ru.rzn.gmyasoedov.service;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rzn.gmyasoedov.model.CatalogData;
import ru.rzn.gmyasoedov.model.ReportTask;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Обработчик файлов в одном конктерном каталоге.
 */
public class FileProcessorService {
    private static final Set<String> SUPPORT_FILE_EXTENSION = ImmutableSet.of("xml");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService reportProcessorPool;

    public FileProcessorService(int reportProcessorPoolSize) {
        this.reportProcessorPool = Executors.newFixedThreadPool(reportProcessorPoolSize);
    }

    public void shutdown() {
        reportProcessorPool.shutdown();
    }

    public void shutdownNow() {
        reportProcessorPool.shutdownNow();
    }

    public boolean isTerminated() {
        return reportProcessorPool.isTerminated();
    }

    public void processFiles(CatalogData catalogData, List<FileProcessor> processors) {
        if (processors.isEmpty()) {
            return;
        }

        try (Stream<Path> pathStream = Files.walk(Path.of(catalogData.getCanonicalPath()))) {
            pathStream.filter(Files::isRegularFile)
                    .filter(this::isSupportFile)
                    .forEach(file -> submitTasks(catalogData, file, processors));
        } catch (Exception e) {
            logger.error("error process path {}", catalogData.getCanonicalPath(), e);
        }
    }

    private void submitTasks(CatalogData catalogData, Path filePath, List<FileProcessor> processors) {
        try {
            Instant lastModifyTime = getLastUpdateTime(filePath);
            for (FileProcessor processor : processors) {
                ReportTask task = new ReportTask(filePath, processor.getReportType(), lastModifyTime);
                if (catalogData.addProcessingTasks(task)) {
                    reportProcessorPool.submit(() -> processor.process(filePath));
                }
            }
        } catch (IOException e) {
            logger.error("error start process file {}", filePath, e);
        }
    }

    public static Instant getLastUpdateTime(Path filePath) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
        return attributes.lastModifiedTime().toInstant();
    }

    private boolean isSupportFile(Path path) {
        return SUPPORT_FILE_EXTENSION
                .contains(com.google.common.io.Files.getFileExtension(path.getFileName().toString()));
    }

}
