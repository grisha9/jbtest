package ru.rzn.gmyasoedov.model;

public class AddCatalogEvent {
    private final String reportType;
    private final String canonicalPath;

    public AddCatalogEvent(String canonicalPath, String reportType) {
        this.reportType = reportType;
        this.canonicalPath = canonicalPath;
    }

    public String getReportType() {
        return reportType;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }
}
