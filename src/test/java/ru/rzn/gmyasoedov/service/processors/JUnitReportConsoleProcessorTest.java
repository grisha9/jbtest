package ru.rzn.gmyasoedov.service.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;

class JUnitReportConsoleProcessorTest {

    private JUnitReportConsoleProcessor processor;

    @BeforeEach
    void setUp() {
        processor = Mockito.spy(new JUnitReportConsoleProcessor());
    }

    @Test
    void process() {
        Path path = Paths.get("").toAbsolutePath();
        Path reportPath = Paths.get(path.toString(), "src/test/resources/junit-report.xml");
        processor.process(reportPath);
        Mockito.verify(processor, times(3)).processItem(Mockito.any());
    }
}