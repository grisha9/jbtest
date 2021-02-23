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

    @BeforeEach
    void setUp() {
        catalogDataHolder = new CatalogDataHolder();
        service = new CatalogScannerService(Mockito.mock(FileProcessorService.class), catalogDataHolder);
    }

    @Test
    void addCatalogEvent() {
        service.addCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_1);
        service.addCatalogEvent(PATH_CORRECT_2, REPORT_TYPE_1);
        service.addCatalogEvent(PATH_CORRECT_1_NOT_CANONICAL, REPORT_TYPE_1);
        service.scanCatalog();

        Map<String, CatalogData> catalogDataMap = catalogDataHolder.catalogDataMap;
        Assertions.assertEquals(2, catalogDataMap.size());
    }

    @Test
    void addCatalogNotExistEvent() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.addCatalogEvent("test", REPORT_TYPE_1));
    }

    @Test
    void addNotCatalogEvent() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.addCatalogEvent(PATH_XML_CORRECT_FILE, REPORT_TYPE_1));
    }

    @Test
    void removeCatalogEvent() {
        service.addCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_1);
        service.addCatalogEvent(PATH_CORRECT_2, REPORT_TYPE_1);
        service.removeCatalogEvent(PATH_CORRECT_1);
        service.removeCatalogEvent(PATH_CORRECT_2);
        service.scanCatalog();

        Assertions.assertTrue(catalogDataHolder.getCatalogs().isEmpty());
    }
}