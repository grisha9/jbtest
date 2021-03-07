package ru.rzn.gmyasoedov.model;

import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Задание на обработку файла. (какой файл каким процессором был обработан)
 */
public class ReportTask {
    private final Path reportPath;
    private final ReportType reportType;
    private final Instant lastModifyFileTime;

    public ReportTask(Path reportPath, ReportType reportType, Instant lastModifyFileTime) {
        this.reportPath = Objects.requireNonNull(reportPath);
        this.reportType = Objects.requireNonNull(reportType);
        this.lastModifyFileTime = Objects.requireNonNull(lastModifyFileTime);
    }


    public Path getReportPath() {
        return reportPath;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public Instant getLastModifyFileTime() {
        return lastModifyFileTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportTask that = (ReportTask) o;
        return reportPath.equals(that.reportPath) &&
                reportType.equals(that.reportType) &&
                lastModifyFileTime.equals(that.lastModifyFileTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportPath, reportType, lastModifyFileTime);
    }
}
