package ru.rzn.gmyasoedov.model;

import ru.rzn.gmyasoedov.service.processors.ReportType;

public class AddCatalogEvent {
    private final ReportType reportType;
    private final String canonicalPath;

    public AddCatalogEvent(String canonicalPath, ReportType reportType) {
        this.reportType = reportType;
        this.canonicalPath = canonicalPath;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }
}
