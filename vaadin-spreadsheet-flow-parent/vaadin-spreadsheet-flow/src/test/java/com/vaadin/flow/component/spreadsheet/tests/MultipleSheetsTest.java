package com.vaadin.flow.component.spreadsheet.tests;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class MultipleSheetsTest {

    private Spreadsheet spreadsheet;

    @Before
    public void init() {
        var workbook = new XSSFWorkbook();
        workbook.createSheet("foo");
        workbook.createSheet("bar");

        spreadsheet = new Spreadsheet(workbook);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSheet_invalidSheetIndex_throws() {
        spreadsheet.deleteSheet(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSheet_tooHighSheetIndex_throws() {
        spreadsheet.deleteSheet(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSheet_lastSheet_throws() {
        spreadsheet.deleteSheet(0);
        spreadsheet.deleteSheet(0);
    }

    @Test
    public void deleteFirstSheet_hasOneSheet() {
        spreadsheet.deleteSheet(0);
        Assert.assertEquals(1, spreadsheet.getNumberOfSheets());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSheetWithPOIIndex_deleteInvalidSheetIndex_throws() {
        spreadsheet.deleteSheetWithPOIIndex(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSheetWithPOIIndex_deleteTooHighSheetIndex_throws() {
        spreadsheet.deleteSheetWithPOIIndex(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteSheetWithPOIIndex_deleteLastSheet_throws() {
        spreadsheet.deleteSheetWithPOIIndex(0);
        spreadsheet.deleteSheetWithPOIIndex(0);
    }

    @Test
    public void deleteSheetWithPOIIndex_deleteFirstSheet_hasOneSheet() {
        spreadsheet.deleteSheetWithPOIIndex(0);
        Assert.assertEquals(1, spreadsheet.getNumberOfSheets());
    }

    @Test
    public void deleteFirstSheet_activeSheetUpdated() {
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.deleteSheet(0);
        Assert.assertEquals(0, spreadsheet.getActiveSheetIndex());
        Assert.assertEquals(0, spreadsheet.getActiveSheetPOIIndex());
    }

    @Test
    public void setSheetName_sheetNamesUpdated() {
        spreadsheet.setSheetName(0, "baz");
        spreadsheet.setSheetName(1, "qux");
        Assert.assertEquals("[\"baz\",\"qux\"]",
                spreadsheet.getElement().getProperty("sheetNames"));
        Assert.assertEquals("baz", spreadsheet.getVisibleSheetNames()[0]);
        Assert.assertEquals("qux", spreadsheet.getVisibleSheetNames()[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetName_invalidSheetIndex_throws() {
        spreadsheet.setSheetName(-1, "baz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetName_TooHighSheetIndex_throws() {
        spreadsheet.setSheetName(2, "baz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetHidden_invalidSheetIndex_throws() {
        spreadsheet.setSheetHidden(-1, SheetVisibility.HIDDEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetHidden_tooHighSheetIndex_throws() {
        spreadsheet.setSheetHidden(2, SheetVisibility.HIDDEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetHidden_lastSheet_throws() {
        spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);
        spreadsheet.setSheetHidden(1, SheetVisibility.HIDDEN);
    }

    @Test
    public void setSheetHidden_activeSheetUpdated() {
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);
        Assert.assertEquals(0, spreadsheet.getActiveSheetIndex());
        // POI index includes hidden sheets
        Assert.assertEquals(1, spreadsheet.getActiveSheetPOIIndex());
    }

    @Test
    public void setSheetHidden_numberOfVisibleSheetsUpdated() {
        Assert.assertEquals(2, spreadsheet.getNumberOfVisibleSheets());
        spreadsheet.setSheetHidden(0, SheetVisibility.HIDDEN);
        Assert.assertEquals(1, spreadsheet.getNumberOfVisibleSheets());
        Assert.assertEquals(2, spreadsheet.getNumberOfSheets());
    }

    @Test
    public void addSheetChangeListener_invokesOnSheetChange() {
        var listenerInvoked = new AtomicBoolean(false);
        spreadsheet.addSheetChangeListener(event -> {
            listenerInvoked.set(true);
            Assert.assertEquals(1, spreadsheet.getActiveSheetIndex());
            Assert.assertEquals(spreadsheet, event.getSource());
            Assert.assertEquals(1, event.getNewSheetVisibleIndex());
            Assert.assertEquals("bar", event.getNewSheet().getSheetName());
        });

        TestHelper.fireClientEvent(spreadsheet, "sheetSelected", "[1, 0, 0]");
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNewSheet_emptyName_throws() {
        spreadsheet.createNewSheet("", 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNewSheet_tooLongName_throws() {
        spreadsheet.createNewSheet("a".repeat(32), 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNewSheet_sameName_throws() {
        spreadsheet.createNewSheet("foo", 1, 1);
    }

    @Test
    public void createNewSheet_changesActiveSheet() {
        spreadsheet.createNewSheet("baz", 1, 1);
        Assert.assertEquals(2, spreadsheet.getActiveSheetIndex());
    }

    @Test
    public void createNewSheet_firesSheetChangeEvent() {
        var listenerInvoked = new AtomicBoolean(false);
        spreadsheet.addSheetChangeListener(event -> listenerInvoked.set(true));
        spreadsheet.createNewSheet("baz", 1, 1);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetProtected_invalidSheetIndex_throws() {
        spreadsheet.setSheetProtected(-1, "password");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSheetProtected_tooHighSheetIndex_throws() {
        spreadsheet.setSheetProtected(2, "password");
    }

    @Test
    public void setSheetProtected_shoulBeProtected() {
        Assert.assertFalse(spreadsheet.isSheetProtected(0));
        spreadsheet.setSheetProtected(0, "password");
        Assert.assertTrue(spreadsheet.isSheetProtected(0));
        Assert.assertTrue(spreadsheet.isActiveSheetProtected());
    }
}
