package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;

import com.vaadin.addon.spreadsheet.test.demoapps.SpreadsheetDemoUI;
import com.vaadin.addon.spreadsheet.test.pageobjects.HeaderPage;
import com.vaadin.addon.spreadsheet.test.tb3.MultiBrowserTest;

public abstract class AbstractSpreadsheetTestCase extends MultiBrowserTest {

    protected HeaderPage headerPage;

    @Before
    public void setUp() throws Exception {
        openTestURL();
        headerPage = new HeaderPage(getDriver());
    }

    @Override
    protected Class<?> getUIClass() {
        return SpreadsheetDemoUI.class;
    }

    protected File getTestSheetFile(String testSheetFileName) {
        File file = null;

        try {
            file = new File(Test1.class
                    .getClassLoader()
                    .getResource(
                            "test_sheets" + File.separator + testSheetFileName)
                    .toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull("Spreadsheet file null", file);
        Assert.assertTrue("Spreadsheet file does not exist", file.exists());
        return file;
    }

    protected void assertAddressFieldValue(String expected, String actual) {
        assertEquals("Expected " + expected + " on addressField, actual:"
                + actual, expected, actual);
    }

    protected void assertNotSelectedCell(String cell, boolean selected) {
        assertFalse("Cell " + cell + " should not be selected cell", selected);
    }

    protected void assertSelectedCell(String cell, boolean selected) {
        assertTrue("Cell " + cell + " should be the selected cell", selected);
    }
}
