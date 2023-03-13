package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet")
public class ContextMenuIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        loadFile("conditional_formatting_with_formula_on_second_sheet.xlsx");
    }

    @Test
    public void documentScroll_overlayPosition() {
        // Make sure to use a small enough viewport so that the document needs
        // to be scrolled to see the cell
        getDriver().manage().window().setSize(WINDOW_SIZE_SMALL);
        // Scroll the document horizontally
        executeScript("document.documentElement.scrollLeft = 100");

        var cell = getSpreadsheet().getCellAt("B2");
        // Open context menu for the cell
        cell.contextClick();

        var cellRect = cell.getRect();
        var overlayRect = findElement(By.className("v-contextmenu")).getRect();
        // Assert that the overlay is positioned over the cell
        assertInRange(cellRect.x, overlayRect.x, cellRect.x + cellRect.width);
        assertInRange(cellRect.y, overlayRect.y, cellRect.y + cellRect.height);
    }

    @Test
    public void testSingleCell() {
        loadTestFixture(TestFixtures.Action);
        SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.click();

        b2.contextClick();
        clickItem("Number");
        Assert.assertEquals("42", b2.getValue());
    }

    @Test
    public void testMultipleCells() {
        loadTestFixture(TestFixtures.Action);

        selectCell("B2");
        selectCell("C3", true, false);
        selectCell("D4", false, true);

        getSpreadsheet().getCellAt("C3").contextClick();
        clickItem("Number");

        selectCell("A1");

        selectCell("B2");
        SheetCellElement c3 = getSpreadsheet().getCellAt("C3");
        new Actions(getDriver()).keyDown(Keys.CONTROL).keyDown(Keys.COMMAND)
                .click(c3).keyUp(Keys.CONTROL).keyUp(Keys.COMMAND)
                .contextClick(c3).build().perform();
        clickItem("Double cell values");

        selectCell("A1");

        Assert.assertEquals("84", getCellContent("B2"));
        Assert.assertEquals("84", getCellContent("C3"));
        Assert.assertEquals("42", getCellContent("C4"));
        Assert.assertEquals("42", getCellContent("D3"));
        Assert.assertEquals("42", getCellContent("D4"));
    }

    @Test
    public void testHeaders() throws InterruptedException {
        loadTestFixture(TestFixtures.Action);

        getSpreadsheet().getColumnHeader(3).contextClick();
        clickItem("Column action");

        Assert.assertEquals("first column", getCellContent("C3"));
        Assert.assertEquals("last column", getCellContent("C4"));
    }
}
