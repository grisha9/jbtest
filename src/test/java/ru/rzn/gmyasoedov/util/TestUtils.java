package ru.rzn.gmyasoedov.util;

import ru.rzn.gmyasoedov.ReportProcessor;
import ru.rzn.gmyasoedov.service.FileProcessorService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static java.lang.Thread.sleep;

public class TestUtils {
    private TestUtils() {
    }

    public static String writeContent(Path path, int i) {
        String content = String.format("<test%s/>", i);
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static void whaitTerminate(ReportProcessor processor, int delay, int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            sleep(delay);
            if (processor.isTerminated()) {
                break;
            }
        }
    }

    public static Instant getLastUpdateTime(Path path) {
        try {
            return FileProcessorService.getLastUpdateTime(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
