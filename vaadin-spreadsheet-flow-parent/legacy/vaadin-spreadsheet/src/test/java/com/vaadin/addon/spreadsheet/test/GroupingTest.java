package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.By;

public class GroupingTest extends AbstractSpreadsheetTestCase {

    public static final String IMAGE_XPATH = "//div[contains(@class, 'sheet-image')]";

    /**
     * Ticket #18546
     *
     * Note that this screenshot tests different themes so hence page must be
     * loaded between screenshot comparisons.
     */
    @Test
    public void grouping_themeHasChanged_theSpreadsheetIsRenderedCorrectly() throws Exception {
        loadPage("demo-reindeer", "Groupingtest.xlsx");
        compareScreen("grouping_styling_legacy");

        loadPage("demo", "Groupingtest.xlsx");
        compareScreen("grouping_styling_demo");
    }

    /**
     * Ticket #18912
     */
    @Test
    public void grouping_headersAreHidden_groupingElementsHaveCorrectSize() throws Exception {
        loadPage("demo", "grouping_without_headers.xlsx");

        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();

        WebElement colGrouping = spreadsheetElement
            .findElement(By.cssSelector(".col-group-pane .grouping.minus"));
        WebElement rowGrouping = spreadsheetElement
            .findElement(By.cssSelector(".row-group-pane .grouping.minus"));
        assertEquals(280, colGrouping.getSize().getWidth());
        assertEquals(110, rowGrouping.getSize().getHeight());
    }

    @Test
    public void grouping_collapseColumnGroup_imageInsideGroupShrink() throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement =loadImageFile();
        collapseColumn(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay=getOverlay(spreadsheetElement);
        String width = overlay.getCssValue("width").replace("px","");

        assertThat("Image width", Integer.parseInt(width), lessThan(20));
    }


    @Test
    public void grouping_expandColumnGroup_imageInsideGroupExpand() throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement =loadImageFile();
        collapseColumn(spreadsheetElement);
        Thread.sleep(1000);
        expandColumn(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay=getOverlay(spreadsheetElement);
        String width = overlay.getCssValue("width").replace("px","");

        assertThat("Image width", Integer.parseInt(width), greaterThan(500));
    }

    @Test
    public void grouping_collapseRowGroup_imageInsideGroupShrink() throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement =loadImageFile();
        collapseRow(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay=getOverlay(spreadsheetElement);
        String height = overlay.getCssValue("height").replace("px","");

        assertThat("Image height", Double.parseDouble(height), lessThan(30.0));
    }

    @Test
    public void grouping_expandRowGroup_imageInsideRowExpand() throws IOException, InterruptedException {
        SpreadsheetElement spreadsheetElement =loadImageFile();
        collapseRow(spreadsheetElement);
        expandRow(spreadsheetElement);
        Thread.sleep(2000);

        WebElement overlay=getOverlay(spreadsheetElement);
        String height = overlay.getCssValue("height").replace("px","");

        assertThat("Image height", Double.parseDouble(height), greaterThan(300.0));
    }

    //HELPERS
    private SpreadsheetElement loadImageFile(){
        headerPage.loadFile("group_image.xlsx",this);
        return  $(SpreadsheetElement.class).first();
    }
    private void collapseColumn(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = spreadsheetElement
                .findElement(By.cssSelector(".col-group-pane .grouping.minus"));
        colGrouping.click();
    }
    private void expandColumn(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = spreadsheetElement
                .findElement(By.cssSelector(".col-group-pane .grouping.plus"));
        colGrouping.click();
    }
    private void collapseRow(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = spreadsheetElement
                .findElement(By.cssSelector(".row-group-pane .grouping.minus"));
        colGrouping.click();
    }
    private void expandRow(SpreadsheetElement spreadsheetElement) {
        WebElement colGrouping = spreadsheetElement
                .findElement(By.cssSelector(".row-group-pane .grouping.plus"));
        colGrouping.click();
    }
    private WebElement getOverlay(SpreadsheetElement spreadsheetElement){
        List<WebElement> overlayDivs = spreadsheetElement.findElements((By.xpath(IMAGE_XPATH)));
        Assert.assertFalse("Can not find image overlay",overlayDivs.isEmpty());
        return overlayDivs.get(0);
    }
}
