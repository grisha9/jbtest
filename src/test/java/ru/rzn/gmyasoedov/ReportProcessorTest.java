package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.rzn.gmyasoedov.model.ReportTask;
import ru.rzn.gmyasoedov.service.Constants;
import ru.rzn.gmyasoedov.service.FileProcessorService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_2;

class ReportProcessorTest {

    private ReportProcessor reportProcessor;
    private List<ReportTask> performedTasks;
    private Path catalogPath = Paths.get(Paths.get("").toAbsolutePath().toString(), Constants.PATH_CORRECT_3);
    private Path tempFilePath = Paths.get(catalogPath.toString(), "temp.xml");

    @BeforeEach
    void setUp() throws IOException {
        reportProcessor = new ReportProcessor(Duration.ofMillis(10), 1);
        performedTasks = new CopyOnWriteArrayList<>();
        Files.deleteIfExists(tempFilePath);
    }

    @Timeout(value = 3)
    @ParameterizedTest()
    @CsvSource({"100, 5"})
    void processFiles(String shutdownDelayMillis, String attemptCount) throws IOException, InterruptedException {
        int delay = Integer.parseInt(shutdownDelayMillis);
        int count = Integer.valueOf(attemptCount);

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

        checkTerminate(delay, count);

        List<ReportTask> expectedTasks = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(reportPath1)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("xml"))
                    .flatMap(path -> Stream.of(processor1, processor2)
                            .map(pr -> new ReportTask(path, pr.getReportType(), getLastUpdateTime(path))))
                    .forEach(expectedTasks::add);
        }
        try (Stream<Path> walk = Files.walk(reportPath2)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("xml"))
                    .flatMap(path -> Stream.of(processor1)
                            .map(pr -> new ReportTask(path, pr.getReportType(), getLastUpdateTime(path))))
                    .forEach(expectedTasks::add);
        }
        Assertions.assertEquals(expectedTasks.size(), performedTasks.size());
        performedTasks.forEach(pt -> Assertions.assertTrue(expectedTasks.contains(pt)));
    }

    @Timeout(value = 10)
    @ParameterizedTest()
    @CsvSource({"100, 5"})
    void processFilesCreateAndUpdate(String shutdownDelayMillis, String attemptCount)
            throws IOException, InterruptedException {
        int delay = Integer.parseInt(shutdownDelayMillis);
        int count = Integer.valueOf(attemptCount);

        List<String> expectedContent = new ArrayList<>();

        reportProcessor.addCatalog(catalogPath.toString(), REPORT_TYPE_1);
        Constants.TestProcessor3 processor = new Constants.TestProcessor3();
        reportProcessor.addProcessor(processor);
        reportProcessor.start();

        writeContent(delay, expectedContent, 0);
        writeContent(delay, expectedContent, 0);
        writeContent(delay, expectedContent, 1);
        writeContent(delay, expectedContent, 2);

        reportProcessor.shutdown();
        checkTerminate(delay, count);
        Assertions.assertEquals(expectedContent, processor.getActualContent());
    }

    private void writeContent(int delay, List<String> expectedContent, int i) throws IOException, InterruptedException {
        String content = String.format("<test%s/>", i);
        expectedContent.add(content);
        Files.writeString(tempFilePath, content, StandardCharsets.UTF_8);
        sleep(delay);
    }

    private void checkTerminate(int delay, int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            sleep(delay);
            if (reportProcessor.isTerminated()) {
                break;
            }
        }
    }

    private static Instant getLastUpdateTime(Path path) {
        try {
            return FileProcessorService.getLastUpdateTime(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}