/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

class MultipleSheetsTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        var workbook = new XSSFWorkbook();
        workbook.createSheet("foo");
        workbook.createSheet("bar");

        spreadsheet = new Spreadsheet(workbook);
    }

    @Test
    void deleteSheet_invalidSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.deleteSheet(-1));
    }

    @Test
    void deleteSheet_tooHighSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.deleteSheet(2));
    }

    @Test
    void deleteSheet_lastSheet_throws() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.deleteSheet(0);
            spreadsheet.deleteSheet(0);
        });
    }

    @Test
    void deleteFirstSheet_hasOneSheet() {
        spreadsheet.deleteSheet(0);
        Assertions.assertEquals(1, spreadsheet.getNumberOfSheets());
    }

    @Test
    void deleteSheetWithPOIIndex_deleteInvalidSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.deleteSheetWithPOIIndex(-1));
    }

    @Test
    void deleteSheetWithPOIIndex_deleteTooHighSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.deleteSheetWithPOIIndex(2));
    }

    @Test
    void deleteSheetWithPOIIndex_deleteLastSheet_throws() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.deleteSheetWithPOIIndex(0);
            spreadsheet.deleteSheetWithPOIIndex(0);
        });
    }

    @Test
    void deleteSheetWithPOIIndex_deleteFirstSheet_hasOneSheet() {
        spreadsheet.deleteSheetWithPOIIndex(0);
        Assertions.assertEquals(1, spreadsheet.getNumberOfSheets());
    }

    @Test
    void deleteFirstSheet_activeSheetUpdated() {
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.deleteSheet(0);
        Assertions.assertEquals(0, spreadsheet.getActiveSheetIndex());
        Assertions.assertEquals(0, spreadsheet.getActiveSheetPOIIndex());
    }

    @Test
    void setSheetName_sheetNamesUpdated() {
        spreadsheet.setSheetName(0, "baz");
        spreadsheet.setSheetName(1, "qux");
        Assertions.assertEquals("[\"baz\",\"qux\"]",
                spreadsheet.getElement().getProperty("sheetNames"));
        Assertions.assertEquals("baz", spreadsheet.getVisibleSheetNames()[0]);
        Assertions.assertEquals("qux", spreadsheet.getVisibleSheetNames()[1]);
    }

    @Test
    void setSheetName_invalidSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.setSheetName(-1, "baz"));
    }

    @Test
    void setSheetName_TooHighSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.setSheetName(2, "baz"));
    }

    @Test
    void setSheetHidden_invalidSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.setSheetHidden(-1, SheetVisibility.HIDDEN));
    }

    @Test
    void setSheetHidden_tooHighSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.setSheetHidden(2, SheetVisibility.HIDDEN));
    }

    @Test
    void setSheetHidden_lastSheet_throws() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);
            spreadsheet.setSheetHidden(1, SheetVisibility.HIDDEN);
        });
    }

    @Test
    void setSheetHidden_activeSheetUpdated() {
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);
        Assertions.assertEquals(0, spreadsheet.getActiveSheetIndex());
        // POI index includes hidden sheets
        Assertions.assertEquals(1, spreadsheet.getActiveSheetPOIIndex());
    }

    @Test
    void setSheetHidden_numberOfVisibleSheetsUpdated() {
        Assertions.assertEquals(2, spreadsheet.getNumberOfVisibleSheets());
        spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);
        Assertions.assertEquals(1, spreadsheet.getNumberOfVisibleSheets());
        Assertions.assertEquals(2, spreadsheet.getNumberOfSheets());
    }

    @Test
    void addSheetChangeListener_invokesOnSheetChange() {
        var listenerInvoked = new AtomicBoolean(false);
        spreadsheet.addSheetChangeListener(event -> {
            listenerInvoked.set(true);
            Assertions.assertEquals(1, spreadsheet.getActiveSheetIndex());
            Assertions.assertEquals(spreadsheet, event.getSource());
            Assertions.assertEquals(1, event.getNewSheetVisibleIndex());
            Assertions.assertEquals("bar", event.getNewSheet().getSheetName());
        });

        TestHelper.fireClientEvent(spreadsheet, "sheetSelected", "[1, 0, 0]");
        Assertions.assertTrue(listenerInvoked.get());
    }

    @Test
    void createNewSheet_emptyName_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.createNewSheet("", 1, 1));
    }

    @Test
    void createNewSheet_tooLongName_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.createNewSheet("a".repeat(32), 1, 1));
    }

    @Test
    void createNewSheet_sameName_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.createNewSheet("foo", 1, 1));
    }

    @Test
    void createNewSheet_changesActiveSheet() {
        spreadsheet.createNewSheet("baz", 1, 1);
        Assertions.assertEquals(2, spreadsheet.getActiveSheetIndex());
    }

    @Test
    void createNewSheet_firesSheetChangeEvent() {
        var listenerInvoked = new AtomicBoolean(false);
        spreadsheet.addSheetChangeListener(event -> listenerInvoked.set(true));
        spreadsheet.createNewSheet("baz", 1, 1);
        Assertions.assertTrue(listenerInvoked.get());
    }

    @Test
    void setSheetProtected_invalidSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.setSheetProtected(-1, "password"));
    }

    @Test
    void setSheetProtected_tooHighSheetIndex_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.setSheetProtected(2, "password"));
    }

    @Test
    void setSheetProtected_shoulBeProtected() {
        Assertions.assertFalse(spreadsheet.isSheetProtected(0));
        spreadsheet.setSheetProtected(0, "password");
        Assertions.assertTrue(spreadsheet.isSheetProtected(0));
        Assertions.assertTrue(spreadsheet.isActiveSheetProtected());
    }
}
