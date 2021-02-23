package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.rzn.gmyasoedov.model.ReportTask;
import ru.rzn.gmyasoedov.service.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_2;

class ReportProcessorTest {

    private ReportProcessor reportProcessor;
    private List<ReportTask> performedTasks;

    @BeforeEach
    void setUp() {
        reportProcessor = new ReportProcessor(Duration.ofSeconds(1), 1);
        performedTasks = new CopyOnWriteArrayList<>();
    }

    @Test
    void processFiles() throws IOException {
        Constants.TestProcessor1 processor1 = new Constants.TestProcessor1(performedTasks);
        Constants.TestProcessor2 processor2 = new Constants.TestProcessor2(performedTasks);
        Path reportPath1 = Paths.get(Paths.get("").toAbsolutePath().toString(), Constants.PATH_CORRECT_1);
        Path reportPath2 = Paths.get(Paths.get("").toAbsolutePath().toString(), Constants.PATH_CORRECT_2);
        reportProcessor.addCatalog(reportPath1.toString(), REPORT_TYPE_1);
        reportProcessor.addProcessor(processor1);
        reportProcessor.start();
        reportProcessor.addCatalog(reportPath1.toString(), REPORT_TYPE_2);
        reportProcessor.addProcessor(processor2);
        reportProcessor.addCatalog(reportPath2.toString(), REPORT_TYPE_1);
        reportProcessor.shutdown();

        while (!reportProcessor.isTerminated()) {
        }

        List<ReportTask> expectedTasks = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(reportPath1)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("xml"))
                    .flatMap(path -> Stream.of(processor1, processor2).map(pr -> new ReportTask(path, pr.getClass())))
                    .forEach(expectedTasks::add);
        }
        try (Stream<Path> walk = Files.walk(reportPath2)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("xml"))
                    .flatMap(path -> Stream.of(processor1).map(pr -> new ReportTask(path, pr.getClass())))
                    .forEach(expectedTasks::add);
        }
        Assertions.assertEquals(expectedTasks.size(), performedTasks.size());
        performedTasks.forEach(pt -> Assertions.assertTrue(expectedTasks.contains(pt)));
    }
}