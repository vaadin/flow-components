package com.vaadin.spreadsheet.charts.interactiontests;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.test.AbstractSpreadsheetTestCase;
import com.vaadin.addon.spreadsheet.test.demoapps.SpreadsheetChartsDemoUI;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.addon.spreadsheet.test.testutil.OverlayHelper;
import com.vaadin.testbench.By;

public class InteractionTBTest extends AbstractSpreadsheetTestCase {

    private OverlayHelper overlayHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        overlayHelper = new OverlayHelper(driver);
    }

    @Test
    public void userChangesInSpreadsheet_chartsUpdated() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile("InteractionSample.xlsx", this);
        spreadsheetPage.getCellAt(1,14).setValue("10");
        Thread.sleep(1000);
        compareScreen("chartsUpdatedOnDataChange");
    }

    @Test
    public void userChangesCategoryInSpreadsheet_chartsUpdated()
            throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "ChartsWithCategories.xlsx", this);

        // need to move selection so that fill indicator is not clicked while
        // selecting A3
        spreadsheetPage.getCellAt(5, 5).click();
        spreadsheetPage.getCellAt(1, 3).setValue("New Category Value");
        Thread.sleep(1000);
        compareScreen("chartsUpdatedOnCategoryChange");
    }

    @Test
    public void userChangesSizeValueInSpreadsheetForBubbleChart_chartsUpdated()
            throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile("Bubble.xlsx",
                this);

        // need to move selection so that fill indicator is not clicked while
        // selecting A2
        spreadsheetPage.getCellAt(1, 5).click();
        spreadsheetPage.getCellAt(3, 2).setValue("2");
        Thread.sleep(1000);
        compareScreen("chartsUpdatedOnSizeValuesChange");
    }

    @Test
    public void userChangesXValuesInSpreadsheetForScatterPlot_chartsUpdated()
            throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile("Scatter.xlsx",
                this);

        // need to move selection so that fill indicator is not clicked while
        // selecting A2
        spreadsheetPage.getCellAt(1, 5).click();
        spreadsheetPage.getCellAt(1, 2).setValue("50");
        Thread.sleep(1000);
        compareScreen("chartsUpdatedOnXValuesChange");
    }

    @Test
    public void displayingEmbeddedScatterPlotWithoutDashsWorks()
            throws Exception {
        headerPage.loadFile("scatter_lines.xlsx", this);

        compareScreen("scatter_with_and_without_lines");
    }

    @Test
    public void userSelectsPoint_spreadsheetSelectionUpdated() throws Exception {
        headerPage.loadFile("InteractionSample.xlsx", this);
        overlayHelper.getOverlayElement("B1")
                .findElements(By.cssSelector(".highcharts-series-0 > rect"))
                .get(0).click();

        assertSelection("A12", "A13", "A14", "A15", "A16");
        assertNotCellInSelectionRange("A11");
        assertNotCellInSelectionRange("A17");
    }

    @Test
    public void openFileWithNotSuportedForumla_noExceptionRaised() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile("InteractionSample.xlsx", this);
        spreadsheetPage.getCellAt(1,12).setValue("test");
        spreadsheetPage.getCellAt(1,13).setValue("");
        Thread.sleep(1000);
        compareScreen("chartIgnoreStringInput");
    }

    @Test
    public void openFileWithNotSuportedForumla_noExceptionRaised_noChart() throws Exception {
        headerPage.loadFile("unparsed_formula.xlsx", this);
        compareScreen("unparsedFormula");
    }

    @Test
    public void openFileWithNullCell_noExceptionRaised_ChartIsShown()
            throws Exception {
        headerPage.loadFile("ChartWithNullCell.xlsx", this);
        compareScreen("nullCellsInChart");
    }

    @Test
    public void chartWithShowDataInHiddenCells_rowIsHidden_chartsAreDifferent()
            throws Exception {
        headerPage.loadFile("Data_On_Hidden_Rows.xlsx", this);

        headerPage.loadTestFixture(TestFixtures.HideSecondRow);

        compareScreen("firstSeriesMissing");
    }

    @Test
    public void userChangesCellAffactingAFormulaInSpreadsheet_chartsUpdated()
            throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage
                .loadFile("FormulaValues.xlsx", this);

        // need to move selection so that fill indicator is not clicked while
        // selecting A2
        spreadsheetPage.getCellAt(1, 5).click();
        spreadsheetPage.getCellAt(2, 4).setValue("50");
        Thread.sleep(1000);
        compareScreen("chartsUpdatedOnFormulaChange");
    }
    
    @Test
    public void sheetWithGroupingAndChart_groupIsCollapsed_chartPointsAreHidden()
            throws Exception {
        headerPage
                .loadFile("chart and grouping.xlsx", this);
        WebElement marker =  driver.findElement(By
                .cssSelector(".grouping"));
        marker.click();
        Thread.sleep(1000);
        compareScreen("chartsUpdatedOnCollapse");
    }

    @Test
    public void openFile_fileHas3dChart_noExceptionsRaised() {
        headerPage.loadFile("3DChart.xlsx", this);
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void openNumbersCreatedExcelFile_noExceptionsRaised_withCharts() {
        headerPage.loadFile("NumbersCreatedExcelFile.xlsx", this);
        assertNoErrorIndicatorDetected();
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

    private WebElement getCellElement(String cell) {
        int[] coordinates = overlayHelper.numericCoordinates(cell);

        WebElement element = driver.findElement(By.cssSelector(".cell.col"
                + coordinates[0] + ".row" + coordinates[1]));

        return element;
    }

    @Override
    public Class<?> getUIClass() {
        return SpreadsheetChartsDemoUI.class;
    }
}
