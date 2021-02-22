package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.model.AddCatalogEvent;
import ru.rzn.gmyasoedov.model.Event;
import ru.rzn.gmyasoedov.model.EventType;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;

import java.io.File;
import java.io.IOException;
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

import static ru.rzn.gmyasoedov.model.EventType.ADD_CATALOG;
import static ru.rzn.gmyasoedov.model.EventType.ADD_PROCESSOR;
import static ru.rzn.gmyasoedov.model.EventType.REMOVE_CATALOG;
import static ru.rzn.gmyasoedov.model.EventType.REMOVE_PROCESSOR;


public class CatalogScannerService {

    private final CatalogDataHolder catalogDataHolder;
    private final FileProcessorService fileProcessorService;
    private final LinkedBlockingQueue<Event> eventsQueue;
    private volatile ScheduledExecutorService catalogScanScheduler;

    public CatalogScannerService(FileProcessorService fileProcessorService) {
        this(fileProcessorService, new CatalogDataHolder());
    }

    CatalogScannerService(FileProcessorService fileProcessorService, CatalogDataHolder catalogDataHolder) {
        this.catalogDataHolder = catalogDataHolder;
        this.fileProcessorService = fileProcessorService;
        this.eventsQueue = new LinkedBlockingQueue<>();
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
        File file = new File(pathString);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isDirectory());
        String canonicalPath = getCanonicalPath(file);
        eventsQueue.add(new Event<>(ADD_CATALOG, new AddCatalogEvent(canonicalPath, type.toLowerCase())));
    }

    public void removeCatalogEvent(@NotNull String pathString) {
        Preconditions.checkNotNull(pathString);
        File file = new File(pathString);
        String canonicalPath = getCanonicalPath(file);
        eventsQueue.add(new Event<>(REMOVE_CATALOG, canonicalPath));
    }

    public void addProcessor(@NotNull FileProcessor processor) {
        Preconditions.checkNotNull(processor);
        eventsQueue.add(new Event<>(ADD_PROCESSOR, processor));
    }

    public void removeProcessor(@NotNull FileProcessor processor) {
        Preconditions.checkNotNull(processor);
        eventsQueue.add(new Event<>(REMOVE_PROCESSOR, processor));
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

    public boolean isTerminated() {
        return catalogScanScheduler != null && catalogScanScheduler.isTerminated()
                && fileProcessorService.isTerminated();
    }

    void scanCatalog() {
        Map<EventType, List<Event>> events = pollEvents();
        processAddCatalogEvents(events);
        processAddProcessorEvents(events);

        catalogDataHolder.getCatalogs().forEach(fileProcessorService::processFiles);

        processRemoveCatalogEvents(events);
        processRemoveProcessorEvents(events);
    }

    private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<EventType, List<Event>> pollEvents() {
        int eventSize = eventsQueue.size();
        return IntStream.range(0, eventSize)
                .mapToObj(i -> eventsQueue.poll())
                .collect(Collectors.groupingBy(Event::getType));
    }

    private void processAddCatalogEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(ADD_CATALOG, Collections.emptyList()).stream()
                .map(e -> (AddCatalogEvent) e.getPayload())
                .forEach(catalogDataHolder::addDirectory);
    }


    private void processAddProcessorEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(ADD_PROCESSOR, Collections.emptyList()).stream()
                .map(e -> (FileProcessor) e.getPayload())
                .forEach(fileProcessorService::addProcessor);
    }

    private void processRemoveCatalogEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(REMOVE_CATALOG, Collections.emptyList()).stream()
                .map(e -> (String) e.getPayload())
                .forEach(catalogDataHolder::removeDirectory);
    }

    private void processRemoveProcessorEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(REMOVE_PROCESSOR, Collections.emptyList()).stream()
                .map(e -> (FileProcessor) e.getPayload())
                .forEach(fileProcessorService::removeProcessor);
    }
}
