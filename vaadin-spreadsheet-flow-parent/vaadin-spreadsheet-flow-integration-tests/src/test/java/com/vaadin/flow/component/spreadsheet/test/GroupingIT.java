/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class GroupingIT extends AbstractSpreadsheetIT {

    private static final String IMAGE_CSS_SELECTOR = ".sheet-overlay-image";

    /**
     * The last row of row_group_at_last_row.xlsx, which is also the last row of
     * the row group in that file, 1-based.
     */
    private static final int LAST_ROW = 205;
    private static final int GROUPED_ROWS = 4;

    @Before
    public void init() {
        open();
    }

    @Test
    public void grouping_headersAreHidden_groupingElementsHaveCorrectSize()
            throws Exception {
        loadFile("grouping_without_headers.xlsx");

        // The brackets run from just inside the first grouped header to the
        // middle of the summary one.
        WebElement colGrouping = findElementInShadowRoot(
                By.cssSelector(".col-grouping-line"));
        WebElement rowGrouping = findElementInShadowRoot(
                By.cssSelector(".row-grouping-line"));
        Assert.assertTrue(colGrouping.getSize().getWidth() > 270);
        Assert.assertTrue(colGrouping.getSize().getWidth() <= 280);
        Assert.assertEquals(110, rowGrouping.getSize().getHeight());
    }

    @Test
    public void grouping_collapseColumnGroup_imageInsideGroupShrink()
            throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement = loadImageFile();
        collapseColumn(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay = getOverlay(spreadsheetElement);
        String width = overlay.getCssValue("width").replace("px", "");

        Assert.assertTrue("Image width", Float.parseFloat(width) < 20);
    }

    @Test
    public void grouping_expandColumnGroup_imageInsideGroupExpand()
            throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement = loadImageFile();
        collapseColumn(spreadsheetElement);
        Thread.sleep(1000);
        expandColumn(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay = getOverlay(spreadsheetElement);
        String width = overlay.getCssValue("width").replace("px", "");

        Assert.assertTrue("Image width", Float.parseFloat(width) > 500);
    }

    @Test
    public void grouping_collapseRowGroup_imageInsideGroupShrink()
            throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement = loadImageFile();
        collapseRow(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay = getOverlay(spreadsheetElement);
        String height = overlay.getCssValue("height").replace("px", "");

        Assert.assertTrue("Image height", Float.parseFloat(height) < 30);
    }

    @Test
    public void grouping_expandRowGroup_imageInsideRowExpand()
            throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement = loadImageFile();
        collapseRow(spreadsheetElement);
        expandRow(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay = getOverlay(spreadsheetElement);
        String height = overlay.getCssValue("height").replace("px", "");

        Assert.assertTrue("Image height", Float.parseFloat(height) > 300);
    }

    @Test
    public void grouping_rowGroupEndsOnLastRow_groupSpansRowsCollapsesAndExpands() {
        loadFile("row_group_at_last_row.xlsx");
        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class)
                .first();
        // the group is at the end of the sheet, scroll it into view
        spreadsheetElement.scroll(100000);
        waitUntil(driver -> !findElementsInShadowRoot(
                By.cssSelector(".row-header-cell.row" + LAST_ROW)).isEmpty());

        // The gutter draws the group's bracket and its button as separate
        // elements; the bracket is what spans the rows.
        WebElement bracket = findElementInShadowRoot(
                By.cssSelector(".row-grouping-line"));
        int rowHeight = spreadsheetElement.getRowHeader(LAST_ROW).getSize()
                .getHeight();
        Assert.assertTrue("Row grouping height",
                bracket.getSize().getHeight() >= GROUPED_ROWS * rowHeight);

        // the collapse icon is at the end of the group, not at its start
        WebElement collapseIcon = findElementInShadowRoot(By
                .cssSelector(".row-grouping-toggle[data-collapsed=\"false\"]"));
        Assert.assertTrue("Collapse icon position",
                collapseIcon.getLocation().getY() >= spreadsheetElement
                        .getRowHeader(LAST_ROW).getLocation().getY());

        collapseIcon.click();
        waitUntil(driver -> !isRowHeaderShown(LAST_ROW));

        findElementInShadowRoot(
                By.cssSelector(".row-grouping-toggle[data-collapsed=\"true\"]"))
                .click();
        waitUntil(driver -> isRowHeaderShown(LAST_ROW));
    }

    /**
     * Whether the row's header is on screen. The header draws nothing for a
     * hidden row.
     */
    private boolean isRowHeaderShown(int row) {
        return findElementsInShadowRoot(
                By.cssSelector(".row-header-cell.row" + row)).stream()
                .anyMatch(WebElement::isDisplayed);
    }

    // HELPERS
    private SpreadsheetElement loadImageFile() {
        loadFile("group_image.xlsx");
        return $(SpreadsheetElement.class).first();
    }

    private void collapseColumn(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(By
                .cssSelector(".col-grouping-toggle[data-collapsed=\"false\"]"));
        colGrouping.click();
    }

    private void expandColumn(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(By
                .cssSelector(".col-grouping-toggle[data-collapsed=\"true\"]"));
        colGrouping.click();
    }

    private void collapseRow(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(By
                .cssSelector(".row-grouping-toggle[data-collapsed=\"false\"]"));
        colGrouping.click();
    }

    private void expandRow(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(By
                .cssSelector(".row-grouping-toggle[data-collapsed=\"true\"]"));
        colGrouping.click();
    }

    private WebElement getOverlay(SpreadsheetElement spreadsheetElement) {
        List<WebElement> overlayDivs = findElementsInShadowRoot(
                (By.cssSelector(IMAGE_CSS_SELECTOR)));
        Assert.assertFalse("Can not find image overlay", overlayDivs.isEmpty());
        return overlayDivs.get(0);
    }
}
