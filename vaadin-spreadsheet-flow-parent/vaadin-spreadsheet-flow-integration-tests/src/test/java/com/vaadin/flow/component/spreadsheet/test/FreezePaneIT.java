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
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
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

    @Test
    public void hideColumnAndFreeze_shouldScrollCorrectly() {
        // Create a new spreadsheet
        createNewSpreadsheet();

        // Load the hide second column fixture
        loadTestFixture(TestFixtures.HideSecondColumn);

        // Add freeze pane to the spreadsheet
        addFreezePane();

        // Verify that column A is still visible after freeze pane
        SheetHeaderElement firstColumn = getSpreadsheet().getColumnHeader(1);
        Assert.assertEquals("Column A should be visible", "A",
                firstColumn.getText());

        // Verify that the first column after the split is still visible
        var columnF = getSpreadsheet().getColumnHeader(6);
        Assert.assertEquals("Column F should be visible", "F",
                columnF.getText());

        // Scroll all the way to the right
        getSpreadsheet().scrollLeft(10000);
        getCommandExecutor().waitForVaadin();

        // Check that the last column header is visible
        // By default, there are 52 columns (A, B, ..., AZ)
        var rightmostHeader = findElementInShadowRoot(
                By.cssSelector(".ch.col52"));
        Assert.assertNotNull("Should have the last column header visible",
                rightmostHeader);

        // Get the rightmost visible column header
        var headerRect = rightmostHeader.getRect();
        var topRightPane = findElementInShadowRoot(
                By.cssSelector(".top-right-pane"));
        var topRightPaneRect = topRightPane.getRect();

        // The rightmost header should be positioned correctly within the
        // spreadsheet bounds
        Assert.assertEquals(
                "Rightmost column header should be within spreadsheet bounds",
                topRightPaneRect.getX() + topRightPaneRect.getWidth(),
                headerRect.getX() + headerRect.getWidth());
    }

    @Test
    public void hideRowAndFreeze_shouldScrollCorrectly() {
        // Create a new spreadsheet
        createNewSpreadsheet();

        // Load the hide second row fixture
        loadTestFixture(TestFixtures.HideSecondRow);

        // Add freeze pane to the spreadsheet
        addFreezePane();

        // Verify that row 1 is still visible after freeze pane
        SheetHeaderElement firstRow = getSpreadsheet().getRowHeader(1);
        Assert.assertEquals("Row 1 should be visible", "1", firstRow.getText());

        // Verify that the first row after the split is still visible
        var row6 = getSpreadsheet().getRowHeader(6);
        Assert.assertEquals("Row 6 should be visible", "6", row6.getText());

        // Scroll all the way down
        getSpreadsheet().scroll(10000);
        getCommandExecutor().waitForVaadin();

        // Check that the last row header is visible
        // By default, there are 200 rows
        var bottommostHeader = findElementInShadowRoot(
                By.cssSelector(".rh.row200"));
        Assert.assertNotNull("Should have the last column header visible",
                bottommostHeader);

        // Get the bottommost visible row header
        var headerRect = bottommostHeader.getRect();
        var bottomLeftPane = findElementInShadowRoot(
                By.cssSelector(".bottom-left-pane"));
        var bottomLeftPaneRect = bottomLeftPane.getRect();

        // The bottommost header should be positioned correctly within the
        // spreadsheet bounds
        Assert.assertEquals(
                "Bottommost row header should be within spreadsheet bounds",
                bottomLeftPaneRect.getY() + bottomLeftPaneRect.getHeight(),
                headerRect.getY() + headerRect.getHeight());
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
