package ru.rzn.gmyasoedov;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.service.CatalogScannerService;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.FileProcessorHolder;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReportProcessor {
    private final Duration schedulePeriod;
    private final CatalogScannerService catalogScannerService;
    private final FileProcessorHolder fileProcessorHolder;
    private final ReentrantReadWriteLock readWriteLock;
    private ReportStatus status;

    public ReportProcessor(@NotNull Duration schedulePeriod,
                           int reportProcessorPoolSize) {
        Preconditions.checkNotNull(schedulePeriod);
        Preconditions.checkArgument(schedulePeriod.getSeconds() > 0);
        Preconditions.checkArgument(reportProcessorPoolSize > 0);

        this.schedulePeriod = schedulePeriod;
        this.fileProcessorHolder = new FileProcessorHolder();
        this.catalogScannerService = new CatalogScannerService(fileProcessorHolder, reportProcessorPoolSize);
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
                () -> fileProcessorHolder.addProcessor(processor),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void removeProcessor(@NotNull FileProcessor processor) {
        performAction(
                () -> fileProcessorHolder.removeProcessor(processor),
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

    private void performAction(Runnable action, Lock lock, Set<ReportStatus> validStatuses, ReportStatus newStatus) {
        try {
            lock.lock();
            Preconditions.checkArgument(validStatuses.contains(status));
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
