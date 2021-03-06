package ru.rzn.gmyasoedov;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.service.CatalogDataHolder;
import ru.rzn.gmyasoedov.service.CatalogScannerService;
import ru.rzn.gmyasoedov.service.EventService;
import ru.rzn.gmyasoedov.service.FileProcessorHolder;
import ru.rzn.gmyasoedov.service.FileProcessorService;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;

public class ReportProcessor {
    private final Duration schedulePeriod;
    private final CatalogScannerService catalogScannerService;
    private final ReentrantReadWriteLock readWriteLock;
    private final EventService eventService;
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
        this.eventService = new EventService();
        this.readWriteLock = new ReentrantReadWriteLock(true);
        this.status = ReportStatus.CREATED;

        this.catalogScannerService = new CatalogScannerService(
                new FileProcessorService(reportProcessorPoolSize),
                new CatalogDataHolder(),
                new FileProcessorHolder(),
                eventService
        );
    }

    public void start() {
        performAction(() -> catalogScannerService.start(schedulePeriod),
                readWriteLock.writeLock(),
                Set.of(ReportStatus.CREATED),
                ReportStatus.RUNNING
        );
    }

    /**
     * добавление каталога и тип отчета в нем. может быть несколько одинаковых каталогов но с разными типами
     * @param path путь каталога
     * @param reportType тип отчета
     */
    public void addCatalog(@NotNull String path, @NotNull ReportType reportType) {
        performAction(
                () -> eventService.addCatalogEvent(path, reportType),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void removeCatalog(@NotNull String path) {
        performAction(
                () -> eventService.removeCatalogEvent(path),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    /**
     * добавление процессора отчетов. м.б. только один процессор с уникальным типом
     * @param processor процессор отчетов
     */
    public void addProcessor(@NotNull FileProcessor processor) {
        performAction(
                () -> eventService.addProcessor(processor),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void removeProcessor(@NotNull ReportType reportType) {
        performAction(
                () -> eventService.removeProcessor(reportType),
                readWriteLock.readLock(),
                Set.of(ReportStatus.CREATED, ReportStatus.RUNNING)
        );
    }

    public void shutdown() {
        performAction(eventService::shutdown,
                readWriteLock.writeLock(),
                Set.of(ReportStatus.RUNNING),
                ReportStatus.FINISHED);
    }

    public void shutdownNow() {
        performAction(eventService::shutdownNow,
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
