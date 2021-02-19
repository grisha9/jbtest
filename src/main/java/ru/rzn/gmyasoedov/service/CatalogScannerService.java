package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.CatalogEvent;
import ru.rzn.gmyasoedov.CatalogEventType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.rzn.gmyasoedov.CatalogEventType.ADD;
import static ru.rzn.gmyasoedov.CatalogEventType.REMOVE;


public class CatalogScannerService {

    private final CatalogDataHolder catalogDataHolder;
    private final FileProcessorService fileProcessorService;
    private final LinkedBlockingQueue<CatalogEvent> catalogEvents;
    private volatile ScheduledExecutorService catalogScanScheduler;

    public CatalogScannerService(FileProcessorHolder fileProcessorHolder, int reportProcessorPoolSize) {
        this.catalogDataHolder = new CatalogDataHolder();
        this.fileProcessorService = new FileProcessorService(fileProcessorHolder, reportProcessorPoolSize);
        this.catalogEvents = new LinkedBlockingQueue<>();
    }

    public void start(Duration schedulePeriod) {
        Preconditions.checkArgument(catalogScanScheduler == null);
        catalogScanScheduler = Executors.newSingleThreadScheduledExecutor();
        catalogScanScheduler.scheduleWithFixedDelay(
                this::scanCatalog, 0, schedulePeriod.getSeconds(), TimeUnit.SECONDS
        );
    }

    public void addCatalogEvent(@NotNull String pathString, @NotNull String type) {
        Preconditions.checkNotNull(pathString);
        Preconditions.checkNotNull(type);
        Path path = Path.of(pathString);
        Preconditions.checkArgument(Files.exists(path));
        Preconditions.checkArgument(Files.isDirectory(path));
        catalogEvents.add(new CatalogEvent(ADD, path, type));
    }

    public void removeCatalogEvent(@NotNull String pathString) {
        Preconditions.checkNotNull(pathString);
        catalogEvents.add(new CatalogEvent(REMOVE, Path.of(pathString), null));
    }

    public void shutdown() {
        Preconditions.checkArgument(catalogScanScheduler != null);
        catalogScanScheduler.submit(this::scanCatalog);
        catalogScanScheduler.shutdown();
        fileProcessorService.shutdown();
    }

    public void shutdownNow() {
        Preconditions.checkArgument(catalogScanScheduler != null);
        catalogScanScheduler.shutdownNow();
        fileProcessorService.shutdownNow();
    }

    private void scanCatalog() {
        Map<CatalogEventType, List<CatalogEvent>> eventByType = pollEvents();
        eventByType.getOrDefault(ADD, Collections.emptyList()).forEach(catalogDataHolder::addDirectory);
        catalogDataHolder.getCatalogs().forEach(fileProcessorService::processFiles);
        eventByType.getOrDefault(REMOVE, Collections.emptyList()).forEach(catalogDataHolder::removeDirectory);
    }

    private Map<CatalogEventType, List<CatalogEvent>> pollEvents() {
        int eventSize = catalogEvents.size();
        return IntStream.range(0, eventSize)
                .mapToObj(i -> catalogEvents.poll())
                .collect(Collectors.groupingBy(CatalogEvent::getType));
    }

}
