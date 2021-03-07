package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import ru.rzn.gmyasoedov.service.Constants;
import ru.rzn.gmyasoedov.util.TestScenario;
import ru.rzn.gmyasoedov.util.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static ru.rzn.gmyasoedov.util.TestUtils.writeContent;

class ReportProcessorShutdownTest {

    private ReportProcessor reportProcessor;
    private Path catalogPath = Paths.get(Paths.get("").toAbsolutePath().toString(), Constants.PATH_CORRECT_3);
    private String templateFileName = "file%s.xml";

    @BeforeEach
    void setUp() throws IOException {
        Arrays.stream(catalogPath.toFile().listFiles()).forEach(File::delete);
    }

    @Timeout(value = 10)
    @ParameterizedTest()
    @CsvSource({
            "10 1 5 100", //период опроса каталога 10 мс обработка в 1 поток, 5 тасков по 100мс
            "10 2 2 500"} //период опроса каталога 10 мс обработка в 2 потока, 2 таска по 500мс
    )
    void shutdown(TestScenario scenario) throws InterruptedException {
        reportProcessor = new ReportProcessor(
                Duration.ofMillis(scenario.getPeriodMs()),
                scenario.getProcessorThreadCount()
        );

        IntStream.range(0, scenario.getTaskCount())
                .forEach(i -> writeContent(Path.of(catalogPath.toString(), format(templateFileName, i)), i));

        Constants.DelayProcessor processor = Mockito.spy(new Constants.DelayProcessor(scenario.getTaskDelayMs()));
        reportProcessor.addProcessor(processor);
        reportProcessor.addCatalog(catalogPath.toString(), processor.getReportType());
        reportProcessor.start();
        reportProcessor.shutdown();

        TestUtils.whaitTerminate(reportProcessor, scenario.getTaskDelayMs(), scenario.getTaskCount() * 2);
        Mockito.verify(processor, times(scenario.getTaskCount())).process(Mockito.any());
    }

    @Timeout(value = 10)
    @ParameterizedTest()
    @CsvSource({
            "10 1 5 500", //период опроса каталога 10 мс обработка в 1 поток, 5 тасков по 500мс
            "10 2 4 500"} //период опроса каталога 10 мс обработка в 2 потока, 2 таска по 500мс
    )
    void shutdownNow(TestScenario scenario) throws InterruptedException {
        reportProcessor = new ReportProcessor(
                Duration.ofMillis(scenario.getPeriodMs()),
                scenario.getProcessorThreadCount()
        );

        IntStream.range(0, scenario.getTaskCount())
                .forEach(i -> writeContent(Path.of(catalogPath.toString(), format(templateFileName, i)), i));

        Constants.DelayProcessor processor = Mockito.spy(new Constants.DelayProcessor(scenario.getTaskDelayMs()));
        reportProcessor.addProcessor(processor);
        reportProcessor.addCatalog(catalogPath.toString(), processor.getReportType());
        reportProcessor.start();
        reportProcessor.shutdownNow();

        TestUtils.whaitTerminate(reportProcessor, scenario.getTaskDelayMs(), scenario.getTaskCount() * 2);
        Mockito.verify(processor, atMost(scenario.getProcessorThreadCount())).process(Mockito.any());
    }
}