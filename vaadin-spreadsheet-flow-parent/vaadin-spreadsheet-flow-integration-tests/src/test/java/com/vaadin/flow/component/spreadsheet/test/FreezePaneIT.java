/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.spreadsheet.testbench.SheetHeaderElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class FreezePaneIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void addFreezePane_verticalAndHorizontal_firstHeaderIsPlacedCorrectly()
            throws Exception {
        createNewSpreadsheet();

        addFreezePane();

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void addFreezePane_onlyVertical_firstHeaderIsPlacedCorrectly()
            throws Exception {
        createNewSpreadsheet();

        addFreezePane(0, 1);

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void addFreezePane_onlyHorizontal_firstHeaderIsPlacedCorrectly()
            throws Exception {
        createNewSpreadsheet();

        addFreezePane(1, 0);

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void largeSheet_addFreezePane_verticalAndHorizontal_firstHeaderIsPlacedCorrectly()
            throws Exception {
        loadFile("100_000_rows.xlsx");

        addFreezePane();

        SheetHeaderElement firstColumnHeader = getSpreadsheet()
                .getColumnHeader(1);
        SheetHeaderElement firstRowHeader = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("A", firstColumnHeader.getText());
        Assert.assertEquals("0px",
                firstColumnHeader.getWrappedElement().getCssValue("left"));
        Assert.assertEquals("1", firstRowHeader.getText());
        Assert.assertEquals("0px",
                firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void freezeAndResizeColumn_numberOfRenderedColumnsUnchanged()
            throws Exception {
        createNewSpreadsheet();
        // Freeze 5 rows and 10 columns
        addFreezePane(10, 5);

        // Count rendered column headers before resize
        int countBefore = getHeaderCount(".ch");

        resizeFirstColumn();

        // Count rendered column headers after resize
        int countAfter = getHeaderCount(".ch");

        Assert.assertEquals(
                "Number of rendered columns should remain the same after resize",
                countBefore, countAfter);
    }

    @Test
    public void freezeAndResizeColumn_numberOfRenderedRowsUnchanged()
            throws Exception {
        createNewSpreadsheet();
        int spreadsheetHeight = getSpreadsheet().getSize().getHeight();
        // Count rendered column headers before resize
        int countBefore = getHeaderCount(".rh");

        // Freeze 5 rows and 10 columns
        addFreezePane(10, 20);

        resizeFirstColumn();

        // Scroll a few rows down on the bottom-right panel after resize
        getSpreadsheet().scroll(500);
        getSpreadsheet().scroll(0);

        // Count rendered column headers after resize
        int countAfter = getHeaderCount(".rh");

        Assert.assertEquals("Height should be the same", spreadsheetHeight,
                getSpreadsheet().getSize().getHeight());
        Assert.assertEquals(
                "Number of rendered rows should remain the same after resize",
                countBefore, countAfter);
    }

    private int getHeaderCount(String selector) {
        var headers = findElementsInShadowRoot(By.cssSelector(selector));
        return headers.size();
    }

    private void resizeFirstColumn() {
        // Resize first column slightly by dragging its header edge
        var spreadsheet = getSpreadsheet();
        var resizeHandle = spreadsheet.getColumnHeader(1).getResizeHandle();
        var target = spreadsheet.getColumnHeader(2);
        new Actions(driver).dragAndDrop(resizeHandle, target).perform();
        getCommandExecutor().waitForVaadin();
    }

}
