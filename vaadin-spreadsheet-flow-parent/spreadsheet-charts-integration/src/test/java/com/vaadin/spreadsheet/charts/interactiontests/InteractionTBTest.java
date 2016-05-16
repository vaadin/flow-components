package com.vaadin.spreadsheet.charts.interactiontests;

import com.vaadin.addon.spreadsheet.test.AbstractSpreadsheetTestCase;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.addon.spreadsheet.test.testutil.OverlayHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Arrays;

public class InteractionTBTest extends AbstractSpreadsheetTestCase {

    private OverlayHelper overlayHelper;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        overlayHelper=new OverlayHelper(driver);
    }

    @After
    public void tearDown() {
        getDriver().close();
    }

    @Test
    public void userChangesInSpreadsheet_chartsUpdated() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile("InteractionSample.xlsx", this);
        spreadsheetPage.getCellAt(1,14).setValue("10");
        compareScreen("chartsUpdatedOnDataChange");
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
        for (String cell : cells)
            assertCellInSelectionRange(cell);
    }

    private WebElement getCellElement(String cell) {
        int[] coordinates = overlayHelper.numericCoordinates(cell);

        WebElement element = driver.findElement(By.cssSelector(".cell.col"
                + coordinates[0] + ".row" + coordinates[1]));

        return element;
    }
}
