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

    private static final String IMAGE_CSS_SELECTOR = "div.sheet-image";

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

        // The outline spans the cells it groups. The GWT client drew
        // one element per group that carried the +/- glyph as well, so
        // its size included the button and its padding and the numbers
        // to check against were fixed; the Lit client draws a line over
        // the range and a button beside it, so the size to check is the
        // range itself.
        Assert.assertEquals("Column outline should span the grouped columns",
                spanOfColumns(1, 4),
                outlineSize(".col-grouping-line", "width"));
        Assert.assertEquals("Row outline should span the grouped rows",
                spanOfRows(1, 5), outlineSize(".row-grouping-line", "height"));
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
                By.cssSelector(".rh.row" + LAST_ROW)).isEmpty());

        // The Lit gutter draws the group's bracket and its button as
        // separate elements; the bracket is what spans the rows.
        WebElement bracket = findElementInShadowRoot(
                By.cssSelector(".row-grouping-line"));
        int rowHeight = spreadsheetElement.getRowHeader(LAST_ROW).getSize()
                .getHeight();
        Assert.assertTrue("Row grouping height",
                bracket.getSize().getHeight() >= GROUPED_ROWS * rowHeight);

        // the collapse icon is at the end of the group, not at its start
        WebElement collapseIcon = findElementInShadowRoot(
                By.cssSelector(".row-group-pane .grouping.minus"));
        Assert.assertTrue("Collapse icon position",
                collapseIcon.getLocation().getY() >= spreadsheetElement
                        .getRowHeader(LAST_ROW).getLocation().getY());

        collapseIcon.click();
        waitUntil(driver -> !spreadsheetElement.getRowHeader(LAST_ROW)
                .isDisplayed());

        findElementInShadowRoot(
                By.cssSelector(".row-group-pane .grouping.plus")).click();
        waitUntil(driver -> spreadsheetElement.getRowHeader(LAST_ROW)
                .isDisplayed());
    }

    // HELPERS
    private SpreadsheetElement loadImageFile() {
        loadFile("group_image.xlsx");
        return $(SpreadsheetElement.class).first();
    }

    private void collapseColumn(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(
                By.cssSelector(".col-group-pane .grouping.minus"));
        colGrouping.click();
    }

    private void expandColumn(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(
                By.cssSelector(".col-group-pane .grouping.plus"));
        colGrouping.click();
    }

    private void collapseRow(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(
                By.cssSelector(".row-group-pane .grouping.minus"));
        colGrouping.click();
    }

    private void expandRow(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = findElementInShadowRoot(
                By.cssSelector(".row-group-pane .grouping.plus"));
        colGrouping.click();
    }

    private WebElement getOverlay(SpreadsheetElement spreadsheetElement) {
        List<WebElement> overlayDivs = findElementsInShadowRoot(
                (By.cssSelector(IMAGE_CSS_SELECTOR)));
        Assert.assertFalse("Can not find image overlay", overlayDivs.isEmpty());
        return overlayDivs.get(0);
    }

    /** Width or height of the first grouping outline, rounded to a pixel. */
    private int outlineSize(String selector, String dimension) {
        return ((Number) executeScript("""
                const host = arguments[0];
                for (const strip of ['column', 'row']) {
                    const el = host.shadowRoot.querySelector(
                        'vaadin-spreadsheet-internal-' + strip + '-header');
                    const line = el && el.shadowRoot
                        ? el.shadowRoot.querySelector(arguments[1]) : null;
                    if (line) {
                        const r = line.getBoundingClientRect();
                        return Math.round(
                            arguments[2] === 'width' ? r.width : r.height);
                    }
                }
                return -1;
                """, getSpreadsheet(), selector, dimension)).intValue();
    }

    /** How wide columns first..last are together, measured from the cells. */
    private int spanOfColumns(int first, int last) {
        return ((Number) executeScript(
                """
                        const g = arguments[0].shadowRoot.querySelector(
                            'vaadin-spreadsheet-internal-cell-grid');
                        const a = g.shadowRoot.querySelector('.col' + arguments[1] + '.row1');
                        const b = g.shadowRoot.querySelector('.col' + arguments[2] + '.row1');
                        return Math.round(b.getBoundingClientRect().right
                            - a.getBoundingClientRect().left);
                        """,
                getSpreadsheet(), first, last)).intValue();
    }

    /** How tall rows first..last are together, measured from the cells. */
    private int spanOfRows(int first, int last) {
        return ((Number) executeScript(
                """
                        const g = arguments[0].shadowRoot.querySelector(
                            'vaadin-spreadsheet-internal-cell-grid');
                        const a = g.shadowRoot.querySelector('.col1.row' + arguments[1]);
                        const b = g.shadowRoot.querySelector('.col1.row' + arguments[2]);
                        return Math.round(b.getBoundingClientRect().bottom
                            - a.getBoundingClientRect().top);
                        """,
                getSpreadsheet(), first, last)).intValue();
    }
}
