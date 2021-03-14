package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static org.mockito.Mockito.times;
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
        getFilesPath(reportPath1).forEach(p -> expectedTasks.add(
                new ReportTask(p, processor1.getReportType(), processor1.getId(), getLastUpdateTime(p))
        ));
        getFilesPath(reportPath1).forEach(p -> expectedTasks.add(
                new ReportTask(p, processor2.getReportType(), processor2.getId(), getLastUpdateTime(p))
        ));
        getFilesPath(reportPath2).forEach(p -> expectedTasks.add(
                new ReportTask(p, processor1.getReportType(), processor1.getId(), getLastUpdateTime(p))
        ));
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

    @Timeout(value = 10)
    @ParameterizedTest()
    @ValueSource(ints = {1, 2, 3})
    void manyOneTypeProcessors(int processorCount) throws InterruptedException {
        writeContent(Path.of(catalogPath.toString(), "file0.xml"), 0);

        Constants.TestProcessor1 processor = Mockito.spy(Constants.TestProcessor1.class);
        IntStream.range(0, processorCount).forEach(i -> reportProcessor.addProcessor(processor));
        reportProcessor.addCatalog(catalogPath.toString(), processor.getReportType());
        reportProcessor.start();
        reportProcessor.shutdown();

        TestUtils.whaitTerminate(reportProcessor, 100, 10);
        Mockito.verify(processor, times(processorCount)).process(Mockito.any());
    }

    private List<Path> getFilesPath(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("xml"))
                    .collect(Collectors.toList());
        }
    }
}