package ru.rzn.gmyasoedov.model;

public class AddCatalogEvent extends CatalogEvent {
    private final String reportType;

    public AddCatalogEvent(String canonicalPath, String reportType) {
        super(canonicalPath);
        this.reportType = reportType;
    }

    public String getReportType() {
        return reportType;
    }
}
