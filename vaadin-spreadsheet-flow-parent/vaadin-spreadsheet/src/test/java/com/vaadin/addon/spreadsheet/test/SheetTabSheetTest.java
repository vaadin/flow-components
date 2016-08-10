package com.vaadin.addon.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.parallel.Browser;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for sheet SheetTabSheet navigation.
 */

public class SheetTabSheetTest extends AbstractSpreadsheetTestCase {

    SpreadsheetPage spreadsheetPage;
    SheetController sheetController;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.createNewSpreadsheet();
        sheetController = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());
    }

    @Test
    public void focus_createTab_sheetIsFocused() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        SheetCellElement cell = spreadsheet.getCellAt("A1");
        // Force sheet initial focus
        cell.click();
        verifySheetFocused();
        spreadsheet.addSheet("");
        verifySheetFocused();
    }

    @Test
    public void focus_changeTab_sheetIsFocused() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        SheetCellElement cell = spreadsheet.getCellAt("A1");
        // Force sheet initial focus
        cell.click();
        verifySheetFocused();
        spreadsheet.addSheet("2");
        spreadsheet.addSheet("3");
        verifySheetFocused();
        loadSheet(0);
        verifySheetFocused();
        loadSheet(1);
        verifySheetFocused();
    }

    @Test
    public void cellFocus_moveFromSheetOneToSheetTwoAndBack_cellSelectionRemains() throws InterruptedException {
        skipBrowser("Shift/Ctrl select fails with Firefox and PhantomJS", Browser.FIREFOX, Browser.PHANTOMJS);

        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        SheetCellElement cell = spreadsheet.getCellAt("C8");
        cell.click();
        spreadsheet.addSheet("2");
        loadSheet(1);
        sheetController.selectRegion("C3", "G14");
        loadSheet(0);
        testBench(driver).waitForVaadin();
        assertTrue(
                spreadsheet.getCellAt("C8")
                        .isCellSelected());
        loadSheet(1);
        testBench(driver).waitForVaadin();
        String[] cols = {"C", "D", "E", "F", "G"};
        for (String column : cols) {
            for (int row = 3; row <= 14; row++) {
                assertTrue("Cell " + column + row + " is not selected",
                        spreadsheet.getCellAt(column + "" + row)
                                .isCellSelected());
            }
        }
    }

    @Test
    public void cellFocus_selectCellThenDeleteSheetAndMoveToNextSheet_cellSelectionIsDefault() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.addSheet("2");
        spreadsheet.addSheet("3");
        loadSheet(1);
        spreadsheet.getCellAt("C4").click();

        headerPage.loadTestFixture(TestFixtures.RemoveFixture);
        testBench(driver).waitForVaadin();
        assertTrue(spreadsheet.getCellAt("A1").isCellSelected());
    }

    private void loadSheet(int index) {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        spreadsheet.findElements(By.className("sheet-tabsheet-tab")).get(index)
                .click();
    }

    private void verifySheetFocused() {
        assertThat("Sheet lost focus", getFocusedElement()
                .getAttribute("class"), containsString("bottom-right-pane"));
    }
}
