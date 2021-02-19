package ru.rzn.gmyasoedov;

import java.nio.file.Path;

public class CatalogEvent {
    private final CatalogEventType type;
    private final Path path;
    private final String reportType;

    public CatalogEvent(CatalogEventType type, Path path, String reportType) {
        this.type = type;
        this.path = path;
        this.reportType = reportType;
    }

    public CatalogEventType getType() {
        return type;
    }

    public Path getPath() {
        return path;
    }

    public String getReportType() {
        return reportType;
    }
}
