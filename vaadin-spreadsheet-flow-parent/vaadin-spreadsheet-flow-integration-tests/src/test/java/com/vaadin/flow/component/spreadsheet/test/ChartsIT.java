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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class ChartsIT extends AbstractSpreadsheetIT {

    private static final String CHART1_CELL = "C2";
    private static final double CHART1_OFFSET_LEFT = 28.6667;
    private static final int CHART1_WIDTH = 397;
    private static final int CHART1_HEIGHT = 288;
    private static final int CHART_MINIMIZED_WIDTH = 0;
    private static final int CHART_MINIMIZED_HEIGHT = 0;

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void sampleWith3overlays_loadFile_overlaysPresentAndHaveCorrectSize()
            throws IOException {
        loadFile("charts.xlsx");

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_OFFSET_LEFT);

        assertOverlayProperties("A5", 397, 288, 29.3333);
        assertOverlayProperties("C10", 282, 288, 69.5591);
    }

    @Test
    public void sampleWith1overlay_disableOverlay_overlayIsNotPresent()
            throws IOException {
        String cell = "A5";

        loadFile("chart.xlsx");

        Assert.assertTrue("Overlay should be visible", isOverlayPresent(cell));

        loadTestFixture(TestFixtures.DisableChartOverlays);

        Assert.assertFalse("Overlay shouldn't be visible",
                isOverlayPresent(cell));
    }

    @Test
    public void sampleWith3overlays_minimizeAndRestore_success()
            throws IOException {
        loadFile("charts.xlsx");

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_OFFSET_LEFT);

        WebElement chartWrapperElement = getOverlayElement(CHART1_CELL);
        WebElement minimizeButton = getMinimizeButton(chartWrapperElement);

        minimizeButton.click();

        // Minimized, the chart gives its space back: the overlay stays
        // where it was, but takes no room in either direction. The button to
        // bring the chart back is still there to click.
        assertOverlayProperties(CHART1_CELL, CHART_MINIMIZED_WIDTH,
                CHART_MINIMIZED_HEIGHT, CHART1_OFFSET_LEFT);
        Assert.assertTrue("Restore button should still be there",
                minimizeButton.isDisplayed());

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_OFFSET_LEFT);
    }

    @Test
    public void minimizeButtonMouseOver_noErrors() throws IOException {
        loadFile("charts.xlsx");

        // Get any element from the shadow root of the minimize button
        var shadowRootElement = $("vaadin-button")
                .withClassName("minimize-button").first().$("div")
                .withClassName("vaadin-button-container").first();

        // Dispatch a mouseover event to the shadow root element
        executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('mouseover', { bubbles: true, composed: true }))",
                shadowRootElement);
        selectCell("A1");

        checkLogsForErrors();
    }

    @Test
    public void minimizeButtonDoubleClick_noErrors() throws IOException {
        loadFile("charts.xlsx");

        // Get any element from the shadow root of the minimize button
        var shadowRootElement = $("vaadin-button")
                .withClassName("minimize-button").first().$("div")
                .withClassName("vaadin-button-container").first();

        // Dispatch a dblclick event to the shadow root element
        executeScript(
                "arguments[0].dispatchEvent(new CustomEvent('dblclick', { bubbles: true, composed: true }))",
                shadowRootElement);
        selectCell("A1");

        checkLogsForErrors();
    }

    @Test
    public void scrollOutOfViewportAndBack_oneChartVisible()
            throws IOException {
        loadFile("chart.xlsx");
        Assert.assertEquals(1, findElements(By.tagName("vaadin-chart")).size());

        // Scroll the chart out of viewport (the slot element gets removed from
        // the cell)
        getSpreadsheet().scrollLeft(1000);
        // Scroll the chart back to the viewport (a new slot with the same name
        // is created for the cell)
        getSpreadsheet().scrollLeft(0);
        selectCell("A1");

        // There should only be one chart visible. The first chart should have
        // been removed from the DOM when the associated slot got removed from
        // the cell.
        Assert.assertEquals(1, findElements(By.tagName("vaadin-chart")).size());
    }

    @Test
    public void userSelectsPoint_spreadsheetSelectionUpdated()
            throws Exception {
        loadFile("InteractionSample.xlsx");

        getChartInShadowRoot(getOverlayElement("B1"))
                .findElements(By.cssSelector(
                        ".highcharts-series-0 > path.highcharts-point"))
                .get(0).click();

        assertSelection("A12", "A13", "A14", "A15", "A16");
        assertNotSelectedCell("A11");
        assertNotSelectedCell("A17");
    }

    @Test
    public void pieChart_labelDataInSeparateSheet_labelIsShown()
            throws Exception {
        loadFile("pie_labels.xlsx");
        WebElement dataLabel = getChartInShadowRoot(getOverlayElement("A4"))
                .findElements(By.tagName("tspan")).get(0);

        Assert.assertEquals("Header 1", dataLabel.getText());
    }

    @Test
    public void openFile_fileHas3dChart_noExceptionsRaised() {
        loadFile("3DChart.xlsx");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void numberFormatting_hoverOverPointWorks() {
        loadFile("FeatureSample - Blanks as zeros.xlsm");

        var chart = getChartInShadowRoot(getOverlayElement("B14"));

        var actions = new Actions(getDriver());

        // Move to the tracker line to make simulating hovering over points
        // work
        TestBenchElement trackerLine = chart
                .findElement(By.cssSelector(".highcharts-tracker-line"));
        Assert.assertNotNull(trackerLine);
        actions.moveToElement(trackerLine).perform();

        // Hover over the first point of the first series to show the
        // tooltip
        var firstPoint = chart.findElements(
                By.cssSelector(".highcharts-series-0 > path.highcharts-point"))
                .get(0);
        Assert.assertNotNull(firstPoint);
        actions.moveToElement(firstPoint).perform();

        var tooltip = waitUntil(driver -> chart
                .findElement(By.cssSelector(".highcharts-tooltip text")), 2);
        Assert.assertEquals(
                "Series \"Packaging Cost per Unit\" Point \"Controllo di gestione\" 0",
                tooltip.getText());
    }

    @Test
    public void openNumbersCreatedExcelFile_noExceptionsRaised_withCharts() {
        loadFile("NumbersCreatedExcelFile.xlsx");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void userClicksColumn_spreadsheetSelectionUpdated()
            throws Exception {
        loadFile("chart_with_filtered_out_column.xlsx");

        getChartInShadowRoot(getOverlayElement("G11"))
                .findElements(By.cssSelector(
                        ".highcharts-series-0 > path.highcharts-point"))
                .get(0).click();

        assertSelection("G4", "H4", "I4", "J4", "K4", "L4", "M4", "N4", "O4");
    }

    private void assertSelection(String... cells) {
        for (String cell : cells) {
            assertSelectedCell(cell);
        }
    }

    /**
     * The overlay is the size the workbook gives it, and starts the workbook's
     * own offset in from the cell it is anchored to.
     * <p>
     * The element is placed at the offset, not padded by it, so its box covers
     * the picture and nothing of the cell's corner.
     */
    private void assertOverlayProperties(String cell, double width,
            double height, double offsetLeft) {
        WebElement overlay = getOverlayElement(cell);
        Assert.assertEquals("Overlay width", width, overlay.getSize().width, 1);
        Assert.assertEquals("Overlay height", height, overlay.getSize().height,
                1);
        Assert.assertEquals("Overlay offset from its anchor cell", offsetLeft,
                overlay.getRect().getX()
                        - getSpreadsheet().getCellAt(cell).getRect().getX(),
                1);
    }

    private TestBenchElement getChartInShadowRoot(WebElement overlayElement) {
        var slot = overlayElement.findElement(By.tagName("slot"));
        var slotName = slot.getDomAttribute("name");
        var chart = getSpreadsheet().findElement(
                By.cssSelector("[slot=\"" + slotName + "\"] vaadin-chart"));
        return chart.$(DivElement.class).first();
    }

    private TestBenchElement getMinimizeButton(WebElement overlayElement) {
        var slot = overlayElement.findElement(By.tagName("slot"));
        var slotName = slot.getDomAttribute("name");
        return getSpreadsheet().findElement(
                By.cssSelector("[slot=\"" + slotName + "\"] .minimize-button"));
    }

    private WebElement getOverlayElement(String cell) {
        return findElementInShadowRoot(By.cssSelector(overlayAt(cell)));
    }

    private boolean isOverlayPresent(String cell) {
        return !findElementsInShadowRoot(By.cssSelector(overlayAt(cell)))
                .isEmpty();
    }
}
