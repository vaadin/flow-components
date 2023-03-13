package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.util.List;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-spreadsheet")
public class GroupingIT extends AbstractSpreadsheetIT {

    private static final String IMAGE_CSS_SELECTOR = "div.sheet-image";

    @Before
    public void init() {
        open();
    }

    @Test
    public void grouping_headersAreHidden_groupingElementsHaveCorrectSize()
            throws Exception {
        loadFile("grouping_without_headers.xlsx");

        WebElement colGrouping = findElementInShadowRoot(
                By.cssSelector(".col-group-pane .grouping.minus"));
        WebElement rowGrouping = findElementInShadowRoot(
                By.cssSelector(".row-group-pane .grouping.minus"));
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
}
