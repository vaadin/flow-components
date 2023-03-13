package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetHeaderElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet")
public class ResizeProtectedIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void resizing_protectedSheet_columnResizeFails() {
        loadFile("multiple_sheets_protected.xlsx");

        double originalWidth = getCellAt(2, 2).getSize().getWidth();

        TestBenchElement resizeHandle = $(SpreadsheetElement.class).first()
                .getColumnHeader(2).getResizeHandle();
        SheetHeaderElement target = $(SpreadsheetElement.class).first()
                .getColumnHeader(4);

        new Actions(driver).dragAndDrop(resizeHandle, target).perform();

        double newWidth = getCellAt(2, 2).getSize().getWidth();

        Assert.assertTrue(String.format(
                "Width changed when it shouldn't have. Was: %s, now: %s.",
                originalWidth, newWidth), originalWidth == newWidth);
    }

    @Test
    public void resizing_protectedSheetWithFormatColumnsEnabled_columnResizeSuccessful() {
        loadFile("protected_format_columns.xlsx");

        final double originalWidth = getCellAt(2, 2).getSize().getWidth();

        TestBenchElement resizeHandle = $(SpreadsheetElement.class).first()
                .getColumnHeader(2).getResizeHandle();
        SheetHeaderElement target = $(SpreadsheetElement.class).first()
                .getColumnHeader(4);

        new Actions(driver).dragAndDrop(resizeHandle, target).perform();

        double newWidth = getCellAt(2, 2).getSize().getWidth();

        assertInRange(2.5 * originalWidth, newWidth, 3.5 * originalWidth);
    }

    protected void assertInRange(double from, double value, double to) {
        Assert.assertTrue("Value [" + value + "] is not in range: [" + from
                + " - " + to + "]", value >= from && value <= to);
    }

}
