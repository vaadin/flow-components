package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class ScrollingWithMergedTest extends AbstractSpreadsheetTestCase {

    @Test
    public void scrolling_mergedCellsAtTop_notMovedToTheBottom()
            throws Exception {
        loadPage("demo", "mergedA1B2.xlsx");
        waitForElementPresent(By.className("v-spreadsheet"));

        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();
        Assert.assertNotNull(spreadsheetElement.getCellAt("A1"));

        spreadsheetElement.scroll(spreadsheetElement.findElement(
                By.className("floater")).getSize().height + 100);
        Thread.sleep(1000);

        try {
            spreadsheetElement.getCellAt("A200");
        } catch (NoSuchElementException e) {
            Assert.fail("final row not found");
        }

        try {
            SheetCellElement mergedCells = spreadsheetElement.getCellAt("A1");
            if (mergedCells != null && mergedCells.getLocation().getY() > 0) {
                Assert.fail("Merged cells visible when they shouldn't have been.");
            }
        } catch (NoSuchElementException e) {
            // this is fine too
        }
    }

    @Test
    public void scrolling_mergedCellsAtRight_notMovedToTheLeft()
            throws Exception {
        loadPage("demo", "mergedAY1AZ2.xlsx");
        waitForElementPresent(By.className("v-spreadsheet"));

        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();

        ensureMergedRegionNotVisibleWhenScrolledLeft(spreadsheetElement);

        // scroll all the way to right
        int scrollLeft = spreadsheetElement
                .findElement(By.className("floater")).getSize().width + 100;
        spreadsheetElement.scrollLeft(scrollLeft);
        Thread.sleep(1000);

        Assert.assertNotNull(spreadsheetElement.getCellAt("AY1"));

        // scroll back to left
        spreadsheetElement.scrollLeft(-scrollLeft);
        Thread.sleep(1000);

        try {
            spreadsheetElement.getCellAt("A1");
        } catch (NoSuchElementException e) {
            Assert.fail("first column not found");
        }

        ensureMergedRegionNotVisibleWhenScrolledLeft(spreadsheetElement);
    }

    private void ensureMergedRegionNotVisibleWhenScrolledLeft(
            final SpreadsheetElement spreadsheetElement) {
        try {
            SheetCellElement mergedCells = spreadsheetElement.getCellAt("AY1");
            if (mergedCells != null
                    && mergedCells.getLocation().getX() < spreadsheetElement
                            .getLocation().getX()
                            + spreadsheetElement.getSize().getWidth()) {
                Assert.fail("Merged cells visible when they shouldn't have been.");
            }
        } catch (NoSuchElementException e) {
            // this would be fine too
        }
    }
}
