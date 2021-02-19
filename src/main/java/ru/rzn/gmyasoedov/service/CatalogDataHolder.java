package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.model.CatalogEvent;
import ru.rzn.gmyasoedov.model.CatalogEventType;
import ru.rzn.gmyasoedov.model.CatalogData;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

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

        catalogDataMap.compute(event.getPath(), (path, catalogData) -> {
            if (catalogData == null) {
                catalogData = new CatalogData(event.getReportType(), event.getPath());
            } else {
                catalogData.addReportTypes(event.getReportType());
            }
            return catalogData;
        });
    }

    public void removeDirectory(@NotNull CatalogEvent event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkArgument(event.getType() == CatalogEventType.REMOVE);
        catalogDataMap.remove(event.getPath());
    }
}
