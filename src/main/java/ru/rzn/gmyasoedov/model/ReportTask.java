package ru.rzn.gmyasoedov.model;

import java.nio.file.Path;
import java.util.Objects;

public class ReportTask {
    private final Path reportPath;
    private final Class processorClass;

    public ReportTask(Path reportPath, Class reportClass) {
        this.reportPath = reportPath;
        this.processorClass = reportClass;
    }

    public Path getReportPath() {
        return reportPath;
    }

    public Class getProcessorClass() {
        return processorClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportTask that = (ReportTask) o;
        return Objects.equals(reportPath, that.reportPath) &&
                Objects.equals(processorClass, that.processorClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportPath, processorClass);
    }
}
