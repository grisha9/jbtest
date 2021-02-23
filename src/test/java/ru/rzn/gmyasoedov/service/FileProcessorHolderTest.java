package ru.rzn.gmyasoedov.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals(2, fileProcessorHolder.getProcessorByType(REPORT_TYPE_1.toLowerCase()).size());
        Assertions.assertEquals(2, fileProcessorHolder.getProcessorByType(REPORT_TYPE_1.toUpperCase()).size());
        Assertions.assertEquals(1, fileProcessorHolder.getProcessorByType(REPORT_TYPE_2.toLowerCase()).size());
        Assertions.assertEquals(1, fileProcessorHolder.getProcessorByType(REPORT_TYPE_2.toUpperCase()).size());
    }

    @Test
    void removeProcessor() {
        fileProcessorHolder.addProcessor(processor1);
        Assertions.assertFalse(fileProcessorHolder.getProcessorByType(processor1.getType()).isEmpty());
        fileProcessorHolder.removeProcessor(processor1);
        Assertions.assertTrue(fileProcessorHolder.getProcessorByType(processor1.getType()).isEmpty());
    }
}