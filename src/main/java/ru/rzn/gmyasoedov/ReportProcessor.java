package ru.rzn.gmyasoedov;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.service.CatalogScannerService;
import ru.rzn.gmyasoedov.service.FileProcessorHolder;
import ru.rzn.gmyasoedov.service.FileProcessorService;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;

public class ReportProcessor {
    private final Duration schedulePeriod;
    private final CatalogScannerService catalogScannerService;
    private final ReentrantReadWriteLock readWriteLock;
    private ReportStatus status;

    /**
     * процессор отечетов.
     * @param schedulePeriod - период сканирования зарегистрированных каталогов
     * @param reportProcessorPoolSize - число потоков обработки файлов
     */
    public ReportProcessor(@NotNull Duration schedulePeriod,
                           int reportProcessorPoolSize) {
        Preconditions.checkNotNull(schedulePeriod);
        Preconditions.checkArgument(schedulePeriod.getSeconds() > 0);
        Preconditions.checkArgument(reportProcessorPoolSize > 0);

        this.schedulePeriod = schedulePeriod;
        this.catalogScannerService = new CatalogScannerService(
                new FileProcessorService(new FileProcessorHolder(), reportProcessorPoolSize)
        );
        this.readWriteLock = new ReentrantReadWriteLock();
        this.status = ReportStatus.CREATED;
    }

    public void start() {
        performAction(() -> catalogScannerService.start(schedulePeriod),
                readWriteLock.writeLock(),
                Set.of(ReportStatus.CREATED),
                ReportStatus.RUNNING
        );
    }

    public void addCatalog(@NotNull String path, @NotNull String type) {
        performAction(
                () -> catalogScannerService.addCatalogEvent(path, type),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void removeCatalog(@NotNull String path) {
        performAction(
                () -> catalogScannerService.removeCatalogEvent(path),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void addProcessor(@NotNull FileProcessor processor) {
        performAction(
                () -> catalogScannerService.addProcessor(processor),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void removeProcessor(@NotNull FileProcessor processor) {
        performAction(
                () -> catalogScannerService.removeProcessor(processor),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void shutdown() {
        performAction(catalogScannerService::shutdown,
                readWriteLock.writeLock(),
                Set.of(ReportStatus.RUNNING),
                ReportStatus.FINISHED);
    }

    public void shutdownNow() {
        performAction(catalogScannerService::shutdownNow,
                readWriteLock.writeLock(),
                Set.of(ReportStatus.RUNNING),
                ReportStatus.FINISHED);
    }

    public boolean isTerminated() {
        return catalogScannerService.isTerminated();
    }

    private void performAction(Runnable action, Lock lock, Set<ReportStatus> validStatuses, ReportStatus newStatus) {
        try {
            lock.lock();
            if (!validStatuses.contains(status)) {
                throw new IllegalStateException(format("not valid state current %s needs %s", status, validStatuses));
            };
            action.run();
            if (newStatus != null) {
                status = newStatus;
            }
        } finally {
            lock.unlock();
        }
    }

    private void performAction(Runnable action, Lock lock, Set<ReportStatus> validStatuses) {
        performAction(action, lock, validStatuses, null);
    }

    private enum ReportStatus {
        CREATED, RUNNING, FINISHED
    }
}
