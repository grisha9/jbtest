package ru.rzn.gmyasoedov.service;

import ru.rzn.gmyasoedov.model.ReportTask;
import ru.rzn.gmyasoedov.service.processors.FileProcessor;
import ru.rzn.gmyasoedov.service.processors.ReportType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Constants {
    public static final String PATH_CORRECT_1 = "src/test/resources/dir1";
    public static final String PATH_CORRECT_2 = "src/test/resources/dir2";
    public static final String PATH_CORRECT_3 = "src/test/resources/dir3";
    public static final String PATH_CORRECT_1_NOT_CANONICAL = "src/../src/test/resources/dir2";
    public static final String PATH_XML_CORRECT_FILE = "src/test/resources/junit-report.xml";
    public static final ReportType REPORT_TYPE_1 = new ReportType("type1");
    public static final ReportType REPORT_TYPE_2 = new ReportType("type2");

    public static class TestProcessor1 implements FileProcessor {
        private final List<ReportTask> performedTasks;
        private final String id = UUID.randomUUID().toString();

        public TestProcessor1() {
            this(new ArrayList<>());
        }

        public TestProcessor1(List<ReportTask> performedTasks) {
            this.performedTasks = performedTasks;
        }

        public String getId() {
            return id;
        }

        @Override
        public ReportType getReportType() {
            return REPORT_TYPE_1;
        }

        @Override
        public void process(Path path) {
            testProcess(path, performedTasks, getReportType(), id);
        }
    }

    public static class TestProcessor2 implements FileProcessor {
        private final List<ReportTask> performedTasks;
        private final String id = UUID.randomUUID().toString();

        public TestProcessor2() {
            this(new ArrayList<>());
        }

        public TestProcessor2(List<ReportTask> performedTasks) {
            this.performedTasks = performedTasks;
        }

        public String getId() {
            return id;
        }

        @Override
        public ReportType getReportType() {
            return REPORT_TYPE_2;
        }

        @Override
        public void process(Path path) {
            testProcess(path, performedTasks, getReportType(), id);
        }
    }

    public static class TestProcessor3 implements FileProcessor {
        private List<String> actualContent = new ArrayList<>();

        @Override
        public ReportType getReportType() {
            return REPORT_TYPE_1;
        }

        @Override
        public void process(Path path) {
            try {
                actualContent.add(Files.readString(path, StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public List<String> getActualContent() {
            return actualContent;
        }
    }

    public static class DelayProcessor implements FileProcessor {
        private final int delay;

        public DelayProcessor(int delay) {
            this.delay = delay;
        }

        @Override
        public ReportType getReportType() {
            return REPORT_TYPE_1;
        }

        @Override
        public void process(Path path) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void testProcess(Path path, List<ReportTask> performedTasks, ReportType reportType, String id) {
        try {
            performedTasks.add(new ReportTask(path,
                    reportType,
                    id,
                    FileProcessorService.getLastUpdateTime(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Constants() {
    }
}
