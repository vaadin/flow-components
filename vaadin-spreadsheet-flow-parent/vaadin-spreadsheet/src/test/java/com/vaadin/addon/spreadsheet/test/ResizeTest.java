package com.vaadin.addon.spreadsheet.test;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.elements.SheetHeaderElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.TestBenchElement;

public class ResizeTest extends Test1 {

    @Test
    @Ignore("Fails in Phantom")
    public void testColumnResize() {

        double originalWidth = getSize(sheetController.getCellStyle("C1",
                "width"));

        TestBenchElement resizeHandle = $(SpreadsheetElement.class).first()
                .getColumnHeader(3).getResizeHandle();
        SheetHeaderElement target = $(SpreadsheetElement.class).first()
                .getColumnHeader(5);

        new Actions(driver).dragAndDrop(resizeHandle, target).perform();

        double newWidth = getSize(sheetController.getCellStyle("C1", "width"));

        assertInRange(2.5 * originalWidth, newWidth, 3.5 * originalWidth);
    }

    @Test
    @Ignore("Fails in Phantom")
    public void testRowResize() {
        sheetController.selectCell("A2");
        sheetController.selectCell("A1");

        double originalHeight = getSize(sheetController.getCellStyle("A3",
                "height"));

        new Actions(driver).dragAndDrop(
                $(SpreadsheetElement.class).first().getRowHeader(3)
                        .getResizeHandle(),
                $(SpreadsheetElement.class).first().getRowHeader(5)).perform();

        double newHeight = getSize(sheetController.getCellStyle("A3", "height"));

        assertInRange(2.5 * originalHeight, newHeight, 3.5 * originalHeight);
    }

    @Test
    @Ignore("Fails in all the browsers")
    public void testColumnAutoResize() {
        sheetController.selectCell("B2");
        sheetController.insertAndRet("text");

        $(SpreadsheetElement.class).first().getColumnHeader(2)
                .getResizeHandle().doubleClick();

        assertInRange(25, getSize(sheetController.getCellStyle("B2", "width")),
                35);

        sheetController.selectCell("D2");
        sheetController.insertAndRet("very long text inserted in D2.");

        $(SpreadsheetElement.class).first().getColumnHeader(4)
                .getResizeHandle().doubleClick();

        assertInRange(50, getSize(sheetController.getCellStyle("D2", "width")),
                100);
    }
}
