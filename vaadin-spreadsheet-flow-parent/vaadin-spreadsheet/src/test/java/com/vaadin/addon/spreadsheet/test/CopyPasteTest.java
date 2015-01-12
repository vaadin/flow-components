package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.SpreadsheetHandlerImpl;

public class CopyPasteTest {

    public static class TestHandler extends SpreadsheetHandlerImpl {
        public TestHandler(Spreadsheet spreadsheet) {
            super(spreadsheet);
        }

        public Double checkForNumber(String cellContent) {
            return super.checkForNumber(cellContent);
        }
    }

    @Test
    public void testNumberParsingWithLocale() {

        Spreadsheet sheet = new Spreadsheet();
        sheet.setLocale(new Locale("en"));
        TestHandler handler = new TestHandler(sheet);

        testNumbers(handler);
    }

    @Test
    public void testNumberParsingWithoutLocale() {

        Spreadsheet sheet = new Spreadsheet();
        // sheet.setLocale(null); is default
        TestHandler handler = new TestHandler(sheet);

        testNumbers(handler);
    }

    private void testNumbers(TestHandler handler) {
        Double result = handler.checkForNumber(null);
        assertNull(result);

        result = handler.checkForNumber("");
        assertNull(result);

        result = handler.checkForNumber("s42");
        assertNull(result);

        result = handler.checkForNumber("42s");
        assertNull(result);

        result = handler.checkForNumber("42");
        assertNotNull(result);

        result = handler.checkForNumber("4.2");
        assertNotNull(result);

        result = handler.checkForNumber("4,3");
        assertNotNull(result);

        result = handler.checkForNumber("4E2");
        assertNotNull(result);

        result = handler.checkForNumber("4.2E2");
        assertNotNull(result);

        result = handler.checkForNumber("4 002");
        assertNotNull(result);

        result = handler.checkForNumber("4 002.42");
        assertNotNull(result);
    }

}
