package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.CatalogEvent;
import ru.rzn.gmyasoedov.CatalogEventType;
import ru.rzn.gmyasoedov.model.CatalogData;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatalogDataHolder {
    private final Map<Path, CatalogData> catalogDataMap;

    public CatalogDataHolder() {
        this.catalogDataMap = new ConcurrentHashMap<>();
    }

    public Collection<CatalogData> getCatalogs() {
        return catalogDataMap.values();
    }

    public void addDirectory(@NotNull CatalogEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkArgument(event.getType() == CatalogEventType.ADD);
        catalogDataMap
                .computeIfAbsent(event.getPath(), key -> new CatalogData(event.getReportType(), event.getPath()));
    }

    public void removeDirectory(@NotNull CatalogEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkArgument(event.getType() == CatalogEventType.REMOVE);
        catalogDataMap.remove(event.getPath());
    }
}
