package ru.rzn.gmyasoedov.service;

import ru.rzn.gmyasoedov.model.ReportTask;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class Constants {
    public static final String PATH_CORRECT_1 = "src/test/resources/dir1";
    public static final String PATH_CORRECT_2 = "src/test/resources/dir2";
    public static final String PATH_CORRECT_1_NOT_CANONICAL = "src/../src/test/resources/dir2";
    public static final String PATH_XML_CORRECT_FILE = "src/test/resources/junit-report.xml";
    public static final String REPORT_TYPE_1 = "type1";
    public static final String REPORT_TYPE_2 = "type2";

    public static class TestProcessor1 implements FileProcessor {
        private List<ReportTask> performedTasks;

        public TestProcessor1() {
            this(new ArrayList<>());
        }

        public TestProcessor1(List<ReportTask> performedTasks) {
            this.performedTasks = performedTasks;
        }

        @Override
        public String getType() {
            return REPORT_TYPE_1;
        }

        @Override
        public void process(Path path) {
            performedTasks.add(new ReportTask(path, getClass()));
        }
    }

    public static class TestProcessor2 implements FileProcessor {
        private List<ReportTask> performedTasks;

        public TestProcessor2() {
            this(new ArrayList<>());
        }

        public TestProcessor2(List<ReportTask> performedTasks) {
            this.performedTasks = performedTasks;
        }

        @Override
        public String getType() {
            return REPORT_TYPE_2;
        }

        @Override
        public void process(Path path) {
            performedTasks.add(new ReportTask(path, getClass()));
        }
    }

    Constants() {
    }
}
