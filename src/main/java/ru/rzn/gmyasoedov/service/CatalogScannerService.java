package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.model.AddCatalogEvent;
import ru.rzn.gmyasoedov.model.CatalogEvent;
import ru.rzn.gmyasoedov.model.RemoveCatalogEvent;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CatalogScannerService {

    private final CatalogDataHolder catalogDataHolder;
    private final FileProcessorService fileProcessorService;
    private final LinkedBlockingQueue<CatalogEvent> catalogEvents;
    private volatile ScheduledExecutorService catalogScanScheduler;

    public CatalogScannerService(FileProcessorService fileProcessorHolder) {
        this(fileProcessorHolder, new CatalogDataHolder());
    }

    CatalogScannerService(FileProcessorService fileProcessorHolder, CatalogDataHolder catalogDataHolder) {
        this.catalogDataHolder = catalogDataHolder;
        this.fileProcessorService = fileProcessorHolder;
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
        File file = new File(pathString);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isDirectory());
        String canonicalPath = getCanonicalPath(file);
        catalogEvents.add(new AddCatalogEvent(canonicalPath, type.toLowerCase()));
    }

    public void removeCatalogEvent(@NotNull String pathString) {
        Preconditions.checkNotNull(pathString);
        File file = new File(pathString);
        String canonicalPath = getCanonicalPath(file);
        catalogEvents.add(new RemoveCatalogEvent(canonicalPath));
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
        CatalogEvents catalogEvents = pollEvents();
        catalogEvents.getAddEvents().forEach(catalogDataHolder::addDirectory);
        catalogDataHolder.getCatalogs().forEach(fileProcessorService::processFiles);
        catalogEvents.getRemoveEvents().forEach(catalogDataHolder::removeDirectory);
    }

    private CatalogEvents pollEvents() {
        List<AddCatalogEvent> addEvents = new ArrayList<>();
        List<RemoveCatalogEvent> removeEvents = new ArrayList<>();

        int eventSize = catalogEvents.size();
        for (int i = 0; i < eventSize; i++) {
            CatalogEvent event = catalogEvents.poll();
            if (event instanceof AddCatalogEvent) {
                addEvents.add((AddCatalogEvent) event);
            } else if (event instanceof RemoveCatalogEvent) {
                removeEvents.add((RemoveCatalogEvent) event);
            }
        }
        return new CatalogEvents(addEvents, removeEvents);
    }

    private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class CatalogEvents {
        private List<AddCatalogEvent> addEvents;
        private List<RemoveCatalogEvent> removeEvents;

        CatalogEvents(List<AddCatalogEvent> addEvents, List<RemoveCatalogEvent> removeEvents) {
            this.addEvents = addEvents;
            this.removeEvents = removeEvents;
        }

        List<AddCatalogEvent> getAddEvents() {
            return Objects.requireNonNullElse(addEvents, Collections.emptyList());
        }

        List<RemoveCatalogEvent> getRemoveEvents() {
            return Objects.requireNonNullElse(removeEvents, Collections.emptyList());
        }
    }
}
