package ru.rzn.gmyasoedov.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.FileProcessorProxy;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.util.List;
import java.util.stream.Collectors;

import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_2;

class FileProcessorHolderTest {

    private FileProcessorHolder fileProcessorHolder;
    private Constants.TestProcessor1 processor1;
    private Constants.TestProcessor2 processor2;

    @BeforeEach
    void setUp() {
        fileProcessorHolder = new FileProcessorHolder();
        processor1 = new Constants.TestProcessor1();
        processor2 = new Constants.TestProcessor2();
    }

    @Test
    void addProcessor() {
        fileProcessorHolder.addProcessor(processor1);
        fileProcessorHolder.addProcessor(processor1);
        fileProcessorHolder.addProcessor(processor2);

        List<Constants.TestProcessor1> expected1 = List.of(processor1, processor1);
        List<Constants.TestProcessor2> expected2 = List.of(processor2);
        Assertions.assertEquals(expected1,
                getFileProcessors(fileProcessorHolder.getProcessorByType(REPORT_TYPE_1)));
        Assertions.assertEquals(expected1,
                getFileProcessors(fileProcessorHolder
                        .getProcessorByType(new ReportType(REPORT_TYPE_1.getType().toLowerCase()))));
        Assertions.assertEquals(expected1,
                getFileProcessors(fileProcessorHolder
                        .getProcessorByType(new ReportType(REPORT_TYPE_1.getType().toUpperCase()))));

        Assertions.assertEquals(expected2,
                getFileProcessors(fileProcessorHolder.getProcessorByType(REPORT_TYPE_2)));
        Assertions.assertEquals(expected2,
                getFileProcessors(fileProcessorHolder
                        .getProcessorByType(new ReportType(REPORT_TYPE_2.getType().toLowerCase()))));
        Assertions.assertEquals(expected2,
                getFileProcessors(fileProcessorHolder
                        .getProcessorByType(new ReportType(REPORT_TYPE_2.getType().toUpperCase()))));
    }

    @Test
    void removeProcessorOne() {
        fileProcessorHolder.addProcessor(processor1);
        Assertions.assertFalse(fileProcessorHolder.getProcessorByType(processor1.getReportType()).isEmpty());
        fileProcessorHolder.removeProcessor(processor1.getReportType());
        Assertions.assertTrue(fileProcessorHolder.getProcessorByType(processor1.getReportType()).isEmpty());
    }

    @Test
    void removeProcessorMany() {
        fileProcessorHolder.addProcessor(processor1);
        fileProcessorHolder.addProcessor(processor1);
        Assertions.assertFalse(fileProcessorHolder.getProcessorByType(processor1.getReportType()).isEmpty());
        fileProcessorHolder.removeProcessor(processor1.getReportType());
        Assertions.assertFalse(fileProcessorHolder.getProcessorByType(processor1.getReportType()).isEmpty());
        fileProcessorHolder.removeProcessor(processor1.getReportType());
        Assertions.assertTrue(fileProcessorHolder.getProcessorByType(processor1.getReportType()).isEmpty());
    }

    private List<FileProcessor> getFileProcessors(List<FileProcessorProxy> proxies) {
        return proxies.stream().map(FileProcessorProxy::getFileProcessor).collect(Collectors.toList());
    }
}