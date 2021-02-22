package ru.rzn.gmyasoedov.service;

import ru.rzn.gmyasoedov.service.processors.FileProcessor;

import java.nio.file.Path;

public abstract class Constants {
    public static final String PATH_CORRECT_1 = "src/test/resources/dir1";
    public static final String PATH_CORRECT_2 = "src/test/resources/dir2";
    public static final String PATH_CORRECT_1_NOT_CANONICAL = "src/../src/test/resources/dir2";
    public static final String PATH_XML_CORRECT_FILE = "src/test/resources/junit-report.xml";
    public static final String REPORT_TYPE_1 = "type1";
    public static final String REPORT_TYPE_2 = "type2";
    public static final String TEST_PROCESSOR_1 = "TestProcessor1";
    public static final String TEST_PROCESSOR_2 = "TestProcessor2";

    public static class TestProcessor1 implements FileProcessor {

        @Override
        public String getType() {
            return TEST_PROCESSOR_1;
        }

        @Override
        public void process(Path path) {
        }
    }

    public static class TestProcessor2 implements FileProcessor {

        @Override
        public String getType() {
            return TEST_PROCESSOR_2;
        }

        @Override
        public void process(Path path) {
        }
    }

    Constants() {
    }
}
