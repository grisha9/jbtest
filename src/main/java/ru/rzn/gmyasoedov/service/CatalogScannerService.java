package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rzn.gmyasoedov.model.AddCatalogEvent;
import ru.rzn.gmyasoedov.model.CatalogData;
import ru.rzn.gmyasoedov.model.Event;
import ru.rzn.gmyasoedov.model.EventType;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.FileProcessorProxy;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.rzn.gmyasoedov.model.EventType.ADD_CATALOG;
import static ru.rzn.gmyasoedov.model.EventType.ADD_PROCESSOR;
import static ru.rzn.gmyasoedov.model.EventType.REMOVE_CATALOG;
import static ru.rzn.gmyasoedov.model.EventType.REMOVE_PROCESSOR;
import static ru.rzn.gmyasoedov.model.EventType.SHUTDOWN;
import static ru.rzn.gmyasoedov.model.EventType.SHUTDOWN_NOW;

/**
 * сканер всех зарегистрированных каталогов
 */
public class CatalogScannerService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CatalogDataHolder catalogDataHolder;
    private final FileProcessorHolder fileProcessorHolder;
    private final FileProcessorService fileProcessorService;
    private final EventService eventService;
    private volatile ScheduledExecutorService catalogScanScheduler;

    public CatalogScannerService(FileProcessorService fileProcessorService,
                                 CatalogDataHolder catalogDataHolder,
                                 FileProcessorHolder fileProcessorHolder,
                                 EventService eventService) {
        this.fileProcessorService = fileProcessorService;
        this.catalogDataHolder = catalogDataHolder;
        this.fileProcessorHolder = fileProcessorHolder;
        this.eventService = eventService;
    }

    public void start(Duration schedulePeriod) {
        Preconditions.checkArgument(catalogScanScheduler == null);
        catalogScanScheduler = Executors.newSingleThreadScheduledExecutor();
        catalogScanScheduler.scheduleWithFixedDelay(
                this::scanCatalog, 0, schedulePeriod.toMillis(), TimeUnit.MILLISECONDS
        );
    }

    public boolean isTerminated() {
        return catalogScanScheduler != null && catalogScanScheduler.isTerminated()
                && fileProcessorService.isTerminated();
    }

    void scanCatalog() {
        try {
            Map<EventType, List<Event>> events = eventService.pollEvents();
            processAddCatalogEvents(events);
            processAddProcessorEvents(events);
            processShutdownNow(events);

            catalogDataHolder.getCatalogs().forEach(this::processFiles);

            processRemoveCatalogEvents(events);
            processRemoveProcessorEvents(events);
            processShutdown(events);
        } catch (Exception e) {
            logger.error("scan catalog error", e);
        }
    }

    private void processFiles(CatalogData catalogData) {
        List<FileProcessorProxy> processors = catalogData.getReportTypes()
                .stream()
                .flatMap(type -> fileProcessorHolder.getProcessorByType(type).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        fileProcessorService.processFiles(catalogData, processors);
    }

    private void processAddCatalogEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(ADD_CATALOG, Collections.emptyList()).stream()
                .map(e -> (AddCatalogEvent) e.getPayload())
                .forEach(catalogDataHolder::addDirectory);
    }


    private void processAddProcessorEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(ADD_PROCESSOR, Collections.emptyList()).stream()
                .map(e -> (FileProcessor) e.getPayload())
                .forEach(fileProcessorHolder::addProcessor);
    }

    private void processRemoveCatalogEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(REMOVE_CATALOG, Collections.emptyList()).stream()
                .map(e -> (String) e.getPayload())
                .forEach(catalogDataHolder::removeDirectory);
    }

    private void processRemoveProcessorEvents(Map<EventType, List<Event>> events) {
        events.getOrDefault(REMOVE_PROCESSOR, Collections.emptyList()).stream()
                .map(e -> (ReportType) e.getPayload())
                .forEach(fileProcessorHolder::removeProcessor);
    }

    private void processShutdown(Map<EventType, List<Event>> events) {
        if (events.containsKey(SHUTDOWN)) {
            catalogScanScheduler.shutdown();
            fileProcessorService.shutdown();
        }
    }

    private void processShutdownNow(Map<EventType, List<Event>> events) {
        if (events.containsKey(SHUTDOWN_NOW)) {
            catalogScanScheduler.shutdownNow();
            fileProcessorService.shutdownNow();
        }
    }
}
