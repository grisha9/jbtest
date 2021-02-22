package ru.rzn.gmyasoedov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.rzn.gmyasoedov.service.Constants;

import java.time.Duration;

import static ru.rzn.gmyasoedov.service.Constants.PATH_CORRECT_1;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;

class ReportProcessorStateTest {
    private static final String PATH_1 = "src/test/resources/dir1";

    private ReportProcessor reportProcessor;

    @BeforeEach
    void setUp() {
        reportProcessor = new ReportProcessor(Duration.ofSeconds(1), 1);
    }

    @Test
    void successfulState1() {
        Constants.TestProcessor1 processor = new Constants.TestProcessor1();
        reportProcessor.addCatalog(PATH_CORRECT_1, REPORT_TYPE_1);
        reportProcessor.addProcessor(processor);
        reportProcessor.removeCatalog(PATH_CORRECT_1);
        reportProcessor.removeProcessor(processor);
        reportProcessor.start();
        reportProcessor.shutdown();
    }

    @Test
    void successfulState2() {
        Constants.TestProcessor1 processor = new Constants.TestProcessor1();
        reportProcessor.start();
        reportProcessor.addCatalog(PATH_CORRECT_1, REPORT_TYPE_1);
        reportProcessor.addProcessor(processor);
        reportProcessor.removeCatalog(PATH_CORRECT_1);
        reportProcessor.removeProcessor(processor);
        reportProcessor.shutdown();
    }

    @Test
    void successfulState3() {
        reportProcessor.start();
        reportProcessor.shutdownNow();
    }

    @Test
    void illegalState1() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.shutdown();
            reportProcessor.addCatalog(PATH_CORRECT_1, REPORT_TYPE_1);
        });
    }

    @Test
    void illegalState2() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.shutdown();
            reportProcessor.addProcessor(new Constants.TestProcessor1());
        });
    }

    @Test
    void illegalState3() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.shutdownNow();
            reportProcessor.addCatalog(PATH_CORRECT_1, REPORT_TYPE_1);
        });
    }

    @Test
    void illegalState4() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.shutdownNow();
            reportProcessor.addProcessor(new Constants.TestProcessor1());
        });
    }

    @Test
    void illegalState5() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.shutdown();
        });
    }

    @Test
    void illegalState6() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.shutdownNow();
        });
    }

    @Test
    void illegalState7() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.start();
        });
    }

    @Test
    void illegalState8() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.shutdownNow();
            reportProcessor.shutdownNow();
        });
    }

    @Test
    void illegalState9() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.shutdown();
            reportProcessor.shutdown();
        });
    }

    @Test
    void illegalState10() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.shutdown();
            reportProcessor.start();
        });
    }

    @Test
    void illegalState11() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            reportProcessor.start();
            reportProcessor.shutdownNow();
            reportProcessor.shutdown();
        });
    }
}