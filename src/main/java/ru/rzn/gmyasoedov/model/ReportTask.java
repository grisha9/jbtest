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
    private final String processorId;
    private final Instant lastModifyFileTime;

    public ReportTask(Path reportPath, ReportType reportType, String processorId, Instant lastModifyFileTime) {
        this.reportPath = Objects.requireNonNull(reportPath);
        this.processorId = Objects.requireNonNull(processorId);
        this.reportType = Objects.requireNonNull(reportType);
        this.lastModifyFileTime = Objects.requireNonNull(lastModifyFileTime);
    }


    public Path getReportPath() {
        return reportPath;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getProcessorId() {
        return processorId;
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
                processorId.equals(that.processorId) &&
                lastModifyFileTime.equals(that.lastModifyFileTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportPath, reportType, processorId, lastModifyFileTime);
    }
}
