package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.rzn.gmyasoedov.model.ReportTask;
import ru.rzn.gmyasoedov.service.Constants;
import ru.rzn.gmyasoedov.util.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_2;
import static ru.rzn.gmyasoedov.util.TestUtils.getLastUpdateTime;
import static ru.rzn.gmyasoedov.util.TestUtils.writeContent;

class ReportProcessorTest {

    private ReportProcessor reportProcessor;
    private List<ReportTask> performedTasks;
    private Path catalogPath = Paths.get(Paths.get("").toAbsolutePath().toString(), Constants.PATH_CORRECT_3);
    private Path tempFilePath = Paths.get(catalogPath.toString(), "temp.xml");

    @BeforeEach
    void setUp() throws IOException {
        reportProcessor = new ReportProcessor(Duration.ofMillis(10), 1);
        performedTasks = new CopyOnWriteArrayList<>();
        Arrays.stream(catalogPath.toFile().listFiles()).forEach(File::delete);
    }

    @Timeout(value = 10)
    @ParameterizedTest()
    @CsvSource({"100, 5"})
    void processFiles(int shutdownDelayMillis, int attemptShutdownCount) throws IOException, InterruptedException {
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

        TestUtils.whaitTerminate(reportProcessor, shutdownDelayMillis, attemptShutdownCount);

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
    @CsvSource({"10, 100, 5"})
    void processFilesCreateAndUpdate(int shutdownDelayMillis, int processDelayMillis, int attemptShutdownCount)
            throws InterruptedException {
        List<String> expectedContent = new ArrayList<>();

        reportProcessor.addCatalog(catalogPath.toString(), REPORT_TYPE_1);
        Constants.TestProcessor3 processor = new Constants.TestProcessor3();
        reportProcessor.addProcessor(processor);
        reportProcessor.start();

        expectedContent.add(writeContent(tempFilePath, 0));
        sleep(processDelayMillis);
        expectedContent.add(writeContent(tempFilePath, 0));
        sleep(processDelayMillis);
        expectedContent.add(writeContent(tempFilePath, 1));
        sleep(processDelayMillis);
        expectedContent.add(writeContent(tempFilePath, 2));
        sleep(processDelayMillis);

        reportProcessor.shutdown();
        TestUtils.whaitTerminate(reportProcessor, shutdownDelayMillis, attemptShutdownCount);
        Assertions.assertEquals(expectedContent, processor.getActualContent());
    }
}