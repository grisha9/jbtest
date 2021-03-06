package ru.rzn.gmyasoedov.service;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import ru.rzn.gmyasoedov.model.AddCatalogEvent;
import ru.rzn.gmyasoedov.model.CatalogData;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * хранилище текущих каталогов
 */
public class CatalogDataHolder {
    final Map<String, CatalogData> catalogDataMap;

    public CatalogDataHolder() {
        this.catalogDataMap = new ConcurrentHashMap<>();
    }

    Collection<CatalogData> getCatalogs() {
        return catalogDataMap.values();
    }

    void addDirectory(@NotNull AddCatalogEvent event) {
        Preconditions.checkNotNull(event);

        catalogDataMap.compute(event.getCanonicalPath(), (path, catalogData) -> {
            if (catalogData == null) {
                catalogData = new CatalogData(event.getReportType(), event.getCanonicalPath());
            } else {
                catalogData.addReportTypes(event.getReportType());
            }
            return catalogData;
        });
    }

    void removeDirectory(@NotNull String canonicalPath) {
        Preconditions.checkNotNull(canonicalPath);
        catalogDataMap.remove(canonicalPath);
    }
}
