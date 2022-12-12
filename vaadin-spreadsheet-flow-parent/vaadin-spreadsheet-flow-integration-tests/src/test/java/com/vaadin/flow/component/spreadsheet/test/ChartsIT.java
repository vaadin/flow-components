package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class ChartsIT extends AbstractSpreadsheetIT {

    private static final String CHART1_CELL = "C2";
    private static final String CHART1_PADDING_LEFT = "28.6667px";
    private static final int CHART1_WIDTH = 425;
    private static final int CHART1_HEIGHT = 304;
    private static final int CHART_MINIMIZED_WIDTH = 28;
    private static final int CHART_MINIMIZED_HIGHT = 16;

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
                CHART1_PADDING_LEFT);

        assertOverlayProperties("A5", 425, 293, "29.3333px");
        assertOverlayProperties("C10", 352, 307, "69.5591px");
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
                CHART1_PADDING_LEFT);

        WebElement chartWrapperElement = getOverlayElement(CHART1_CELL);
        WebElement minimizeButton = getMinimizeButton(chartWrapperElement);

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART_MINIMIZED_WIDTH,
                CHART_MINIMIZED_HIGHT, CHART1_PADDING_LEFT);

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);
    }

    @Test
    public void minimizeButtonMouseOver_noErrors() throws IOException {
        loadFile("charts.xlsx");

        // Get any element from the shadow root of the minimize button
        var shadowRootElement = $("vaadin-button")
                .attribute("class", "minimize-button").first().$("div")
                .attribute("class", "vaadin-button-container").first();

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
                .attribute("class", "minimize-button").first().$("div")
                .attribute("class", "vaadin-button-container").first();

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
                .findElements(By.cssSelector(".highcharts-series-0 > rect"))
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
    public void openNumbersCreatedExcelFile_noExceptionsRaised_withCharts() {
        loadFile("NumbersCreatedExcelFile.xlsx");
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void userClicksColumn_spreadsheetSelectionUpdated()
            throws Exception {
        loadFile("chart_with_filtered_out_column.xlsx");

        getChartInShadowRoot(getOverlayElement("G11"))
                .findElements(By.cssSelector(".highcharts-series-0 > rect"))
                .get(0).click();

        assertSelection("G4", "H4", "I4", "J4", "K4", "L4", "M4", "N4", "O4");
    }

    private void assertSelection(String... cells) {
        for (String cell : cells) {
            assertSelectedCell(cell);
        }
    }

    private void assertOverlayProperties(String cell, double width,
            double height, String paddingLeft) {
        WebElement elementC10 = getOverlayElement(cell);
        Assert.assertEquals(width, elementC10.getSize().width, 1);
        Assert.assertEquals(height, elementC10.getSize().height, 1);
        Assert.assertEquals(paddingLeft,
                elementC10.getCssValue("padding-left"));
        // could not compare padding top as it is set in pt and the browsers
        // report it in px
    }

    private TestBenchElement getChartInShadowRoot(WebElement overlayElement) {
        var slot = overlayElement.findElement(By.tagName("slot"));
        var slotName = slot.getAttribute("name");
        var chart = getSpreadsheet().findElement(
                By.cssSelector("[slot=\"" + slotName + "\"] vaadin-chart"));
        return chart.$(DivElement.class).first();
    }

    private TestBenchElement getMinimizeButton(WebElement overlayElement) {
        var slot = overlayElement.findElement(By.tagName("slot"));
        var slotName = slot.getAttribute("name");
        return getSpreadsheet().findElement(
                By.cssSelector("[slot=\"" + slotName + "\"] .minimize-button"));
    }

    private WebElement getOverlayElement(String cell) {
        int[] coordinates = numericCoordinates(cell);

        WebElement element = findElementInShadowRoot(By.cssSelector(
                ".sheet-image.col" + coordinates[0] + ".row" + coordinates[1]));

        return element;
    }

    private boolean isOverlayPresent(String cell) {
        int[] coordinates = numericCoordinates(cell);

        List<WebElement> elements = findElementsInShadowRoot(By.cssSelector(
                ".sheet-image.col" + coordinates[0] + ".row" + coordinates[1]));
        return elements.size() > 0;
    }

    private int[] numericCoordinates(String cell) {
        CellReference cellReference = new CellReference(cell);
        return new int[] { cellReference.getCol() + 1,
                cellReference.getRow() + 1 };
    }
}
