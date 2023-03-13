package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetHeaderElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet")
public class ResizeIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void testColumnResize() {

        double originalWidth = getSize(getCellStyle("C1", "width"));

        TestBenchElement resizeHandle = $(SpreadsheetElement.class).first()
                .getColumnHeader(3).getResizeHandle();
        SheetHeaderElement target = $(SpreadsheetElement.class).first()
                .getColumnHeader(5);

        new Actions(driver).dragAndDrop(resizeHandle, target).perform();

        double newWidth = getSize(getCellStyle("C1", "width"));

        assertInRange(2.5 * originalWidth, newWidth, 3.5 * originalWidth);

        checkLogsForErrors();
    }

    @Test
    public void testRowResize() {
        selectCell("A2");
        selectCell("A1");

        double originalHeight = getSize(getCellStyle("A3", "height"));

        new Actions(driver)
                .dragAndDrop(
                        $(SpreadsheetElement.class).first().getRowHeader(3)
                                .getResizeHandle(),
                        $(SpreadsheetElement.class).first().getRowHeader(5))
                .perform();

        double newHeight = getSize(getCellStyle("A3", "height"));

        assertInRange(2.3 * originalHeight, newHeight, 3.5 * originalHeight);

        checkLogsForErrors();
    }

    @Test
    public void testColumnAutoResize() {
        setCellValue("B2", "text");

        $(SpreadsheetElement.class).first().getColumnHeader(2).getResizeHandle()
                .doubleClick();
        getCommandExecutor().waitForVaadin();
        assertInRange(25, getSize(getCellStyle("B2", "width")), 35);

        setCellValue("D2", "very long text inserted in D2.");

        $(SpreadsheetElement.class).first().getColumnHeader(4).getResizeHandle()
                .doubleClick();

        assertInRange(100, getSize(getCellStyle("D2", "width")), 200);
    }

}
