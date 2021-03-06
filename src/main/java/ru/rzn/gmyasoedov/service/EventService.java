package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.model.AddCatalogEvent;
import ru.rzn.gmyasoedov.model.Event;
import ru.rzn.gmyasoedov.model.EventType;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.rzn.gmyasoedov.model.EventType.ADD_CATALOG;
import static ru.rzn.gmyasoedov.model.EventType.ADD_PROCESSOR;
import static ru.rzn.gmyasoedov.model.EventType.REMOVE_CATALOG;
import static ru.rzn.gmyasoedov.model.EventType.REMOVE_PROCESSOR;
import static ru.rzn.gmyasoedov.model.EventType.SHUTDOWN;
import static ru.rzn.gmyasoedov.model.EventType.SHUTDOWN_NOW;

public class EventService {
    private final LinkedBlockingQueue<Event> eventsQueue;

    public EventService() {
        this.eventsQueue = new LinkedBlockingQueue<>();
    }

    public Map<EventType, List<Event>> pollEvents() {
        int eventSize = eventsQueue.size();
        return IntStream.range(0, eventSize)
                .mapToObj(i -> eventsQueue.poll())
                .collect(Collectors.groupingBy(Event::getType));
    }

    public void addCatalogEvent(@NotNull String pathString, @NotNull ReportType reportType) {
        Preconditions.checkNotNull(pathString);
        Preconditions.checkNotNull(reportType);
        File file = new File(pathString);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.isDirectory());
        String canonicalPath = getCanonicalPath(file);
        eventsQueue.add(new Event<>(ADD_CATALOG, new AddCatalogEvent(canonicalPath, reportType)));
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

    public void removeProcessor(@NotNull ReportType reportType) {
        Preconditions.checkNotNull(reportType);
        eventsQueue.add(new Event<>(REMOVE_PROCESSOR, reportType));
    }

    public void shutdown() {
        eventsQueue.add(new Event<>(SHUTDOWN, null));
    }

    public void shutdownNow() {
        eventsQueue.add(new Event<>(SHUTDOWN_NOW, null));
    }

    private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
