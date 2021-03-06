package ru.rzn.gmyasoedov.service.processors;

import java.nio.file.Path;

public interface FileProcessor {

    ReportType getReportType();

    void process(Path path);
}
