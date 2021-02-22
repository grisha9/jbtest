package ru.rzn.gmyasoedov.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.rzn.gmyasoedov.model.AddCatalogEvent;
import ru.rzn.gmyasoedov.model.CatalogData;

import java.util.Map;

import static ru.rzn.gmyasoedov.service.Constants.PATH_CORRECT_1;
import static ru.rzn.gmyasoedov.service.Constants.PATH_CORRECT_2;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_1;
import static ru.rzn.gmyasoedov.service.Constants.REPORT_TYPE_2;

class CatalogDataHolderTest {

    private CatalogDataHolder catalogDataHolder;


    @BeforeEach
    void setUp() {
        catalogDataHolder = new CatalogDataHolder();
    }

    @Test
    void addDirectory() {
        catalogDataHolder.addDirectory(new AddCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_1));
        catalogDataHolder.addDirectory(new AddCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_2));
        catalogDataHolder.addDirectory(new AddCatalogEvent(PATH_CORRECT_2, REPORT_TYPE_2));

        Map<String, CatalogData> catalogDataMap = catalogDataHolder.catalogDataMap;
        Assertions.assertEquals(2, catalogDataHolder.getCatalogs().size());

        CatalogData path1Actual = catalogDataMap.get(PATH_CORRECT_1);
        CatalogData path2Actual = catalogDataMap.get(PATH_CORRECT_2);

        Assertions.assertNotNull(path1Actual);
        Assertions.assertNotNull(path2Actual);

        Assertions.assertEquals(2, path1Actual.getReportTypes().size());
        Assertions.assertTrue(path1Actual.getReportTypes().contains(REPORT_TYPE_1));
        Assertions.assertTrue(path1Actual.getReportTypes().contains(REPORT_TYPE_2));

        Assertions.assertEquals(1, path2Actual.getReportTypes().size());
        Assertions.assertTrue(path2Actual.getReportTypes().contains(REPORT_TYPE_2));
    }

    @Test
    void removeDirectory() {
        catalogDataHolder.addDirectory(new AddCatalogEvent(PATH_CORRECT_1, REPORT_TYPE_1));
        Assertions.assertFalse(catalogDataHolder.getCatalogs().isEmpty());
        catalogDataHolder.removeDirectory(PATH_CORRECT_1);
        Assertions.assertTrue(catalogDataHolder.getCatalogs().isEmpty());
    }
}