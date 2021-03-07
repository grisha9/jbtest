package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import static java.lang.Thread.sleep;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;

class ReportProcessorCreateUpdateTest {

    private ReportProcessor reportProcessor;
    private Path catalogPath = Paths.get(Paths.get("").toAbsolutePath().toString(), Constants.PATH_CORRECT_3);
    private Path tempFilePath = Paths.get(catalogPath.toString(), "temp.xml");

    @BeforeEach
    void setUp() throws IOException {
        reportProcessor = new ReportProcessor(Duration.ofMillis(10), 1);
        Files.deleteIfExists(tempFilePath);
    }

    @Timeout(value = 10000)
    @ParameterizedTest()
    @CsvSource({"100, 5, 3"})
    void processFiles(String shutdownDelayMillis, String attemptCount, String updateCount)
            throws IOException, InterruptedException {
        int delay = Integer.parseInt(shutdownDelayMillis);
        int count = Integer.valueOf(attemptCount);
        int updates = Integer.valueOf(updateCount);

        List<String> expectedContent = new ArrayList<>();

        reportProcessor.addCatalog(catalogPath.toString(), REPORT_TYPE_1);
        Constants.TestProcessor3 processor = new Constants.TestProcessor3();
        reportProcessor.addProcessor(processor);
        reportProcessor.start();


        for (int i = 0; i < updates; i++) {
            String content = String.format("<test%s/>", i);
            expectedContent.add(content);
            Files.writeString(tempFilePath, content, StandardCharsets.UTF_8);
            sleep(delay);
        }

        reportProcessor.shutdown();
        for (int i = 0; i < count; i++) {
            sleep(delay);
            if (reportProcessor.isTerminated()) {
                break;
            }
        }

        Assertions.assertEquals(expectedContent, processor.getActualContent());
    }

    //добавить тест на апдейт файла

    private static Instant getLastUpdateTime(Path path) {
        try {
            return FileProcessorService.getLastUpdateTime(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}