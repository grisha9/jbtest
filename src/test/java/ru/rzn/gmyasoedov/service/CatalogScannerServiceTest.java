package ru.rzn.gmyasoedov.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rzn.gmyasoedov.model.CatalogData;

import java.util.Map;

import static ru.rzn.gmyasoedov.service.Constants.PATH_CORRECT_1;
import static ru.rzn.gmyasoedov.service.Constants.PATH_CORRECT_1_NOT_CANONICAL;
import static ru.rzn.gmyasoedov.service.Constants.PATH_CORRECT_2;
import static ru.rzn.gmyasoedov.service.Constants.PATH_XML_CORRECT_FILE;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;

class CatalogScannerServiceTest {

    private CatalogScannerService service;
    private CatalogDataHolder catalogDataHolder;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        catalogDataHolder = new CatalogDataHolder();
        eventService = new EventService();
        service = new CatalogScannerService(Mockito.mock(FileProcessorService.class),
                catalogDataHolder,
                new FileProcessorHolder(),
                eventService
        );
    }

    @Test
    void addCatalogEvent() {
        eventService.addCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_1);
        eventService.addCatalogEvent(PATH_CORRECT_2, REPORT_TYPE_1);
        eventService.addCatalogEvent(PATH_CORRECT_1_NOT_CANONICAL, REPORT_TYPE_1);
        service.scanCatalog();

        Map<String, CatalogData> catalogDataMap = catalogDataHolder.catalogDataMap;
        Assertions.assertEquals(2, catalogDataMap.size());
    }

    @Test
    void addCatalogNotExistEvent() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> eventService.addCatalogEvent("test", REPORT_TYPE_1));
    }

    @Test
    void addNotCatalogEvent() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> eventService.addCatalogEvent(PATH_XML_CORRECT_FILE, REPORT_TYPE_1));
    }

    @Test
    void removeCatalogEvent() {
        eventService.addCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_1);
        eventService.addCatalogEvent(PATH_CORRECT_2, REPORT_TYPE_1);
        eventService.removeCatalogEvent(PATH_CORRECT_1);
        eventService.removeCatalogEvent(PATH_CORRECT_2);
        service.scanCatalog();

        Assertions.assertTrue(catalogDataHolder.getCatalogs().isEmpty());
    }
}