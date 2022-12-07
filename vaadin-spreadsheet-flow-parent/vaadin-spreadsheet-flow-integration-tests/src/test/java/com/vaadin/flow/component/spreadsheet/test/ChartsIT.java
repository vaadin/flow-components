package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;

public class ChartsIT extends AbstractSpreadsheetIT {

    private static final String CHART1_CELL = "C2";
    private static final String CHART1_PADDING_LEFT = "28px";
    private static final int CHART1_WIDTH = 425;
    private static final int CHART1_HEIGHT = 304;
    private static final int CHART_MINIMIZED_WIDTH = 28;
    private static final int CHART_MINIMIZED_HIGHT = 16;

    private OverlayHelper overlayHelper;

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        overlayHelper = new OverlayHelper(driver);
    }


    @Test
    public void sampleWith3overlays_loadFile_overlaysPresentAndHaveCorrectSize()
            throws IOException {
        loadFile("charts.xlsx");

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);

        assertOverlayProperties("A5", 425, 293, "29px");
        assertOverlayProperties("C10", 352, 307, "69px");
    }

    @Test
    public void sampleWith1overlay_disableOverlay_overlayIsNotPresent()
            throws IOException {
        String cell = "A5";

        loadFile("chart.xlsx");

        Assert.assertTrue("Overlay should be visible",
                overlayHelper.isOverlayPresent(cell));

        loadTestFixture(TestFixtures.DisableChartOverlays);

        Assert.assertFalse("Overlay shouldn't be visible",
                overlayHelper.isOverlayPresent(cell));
    }

    @Test
    public void sampleWith3overlays_minimizeAndRestore_success()
            throws IOException {
        loadFile("charts.xlsx");

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);

        WebElement chartWrapperElement = overlayHelper
                .getOverlayElement(CHART1_CELL);
        WebElement minimizeButton = chartWrapperElement.findElement(By
                .className("minimize-button"));

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART_MINIMIZED_WIDTH,
                CHART_MINIMIZED_HIGHT, CHART1_PADDING_LEFT);

        minimizeButton.click();

        assertOverlayProperties(CHART1_CELL, CHART1_WIDTH, CHART1_HEIGHT,
                CHART1_PADDING_LEFT);
    }

    @Test
    public void userSelectsPoint_spreadsheetSelectionUpdated() throws Exception {
        loadFile("InteractionSample.xlsx");
        overlayHelper.getOverlayElement("B1")
                .findElements(By.cssSelector(".highcharts-series-0 > rect"))
                .get(0).click();

        assertSelection("A12", "A13", "A14", "A15", "A16");
        assertNotCellInSelectionRange("A11");
        assertNotCellInSelectionRange("A17");
    }

    @Test
    public void pieChart_labelDataInSeparateSheet_labelIsShown()
            throws Exception {
        loadFile("pie_labels.xlsx");
        WebElement dataLabel = overlayHelper.getOverlayElement("A4")
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

        overlayHelper.getOverlayElement("G11")
                .findElements(By.cssSelector(".highcharts-series-0 > rect"))
                .get(0).click();

        assertSelection("G4", "H4", "I4", "J4", "K4", "L4", "M4", "N4", "O4");
    }
    

    private void assertCellInSelectionRange(String cell) {
        Assert.assertTrue("Cell " + cell + " is not selected",
                cellHasCellRangeClass(cell) || cellIsSpecialSelected(cell));
    }

    private void assertNotCellInSelectionRange(String cell) {
        Assert.assertFalse("Cell " + cell + "is selected",
                cellHasCellRangeClass(cell) || cellIsSpecialSelected(cell));
    }

    private boolean cellIsSpecialSelected(String cell) {
        WebElement addressfield = driver.findElement(By
                .cssSelector(".addressfield"));
        return cell.equals(addressfield.getAttribute("value"));
    }

    private boolean cellHasCellRangeClass(String cell) {
        return Arrays.asList(
                getCellElement(cell).getAttribute("class").split(" "))
                .contains("cell-range");
    }

    private void assertSelection(String... cells) {
        for (String cell : cells) {
            assertCellInSelectionRange(cell);
        }
    }


    private void assertOverlayProperties(String cell, double width,
            double height, String paddingLeft) {
        WebElement elementC10 = overlayHelper.getOverlayElement(cell);
        Assert.assertEquals(width, elementC10.getSize().width, 1);
        Assert.assertEquals(height, elementC10.getSize().height, 1);
        Assert.assertEquals(paddingLeft, elementC10.getCssValue("padding-left"));
        // could not compare padding top as it is set in pt and the browsers
        // report it in px
    }
}
