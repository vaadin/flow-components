package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class SheetTabSheetIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        createNewSpreadsheet();
    }

    @Test
    public void focus_createTab_sheetIsFocused() {
        // Force sheet initial focus
        clickCell("A1");
        verifySheetFocused();
        getSpreadsheet().addSheet();
        verifySheetFocused();
    }

    @Test
    public void focus_changeTab_sheetIsFocused() {
        SpreadsheetElement spreadsheet = getSpreadsheet();
        // Force sheet initial focus
        clickCell("A1");
        verifySheetFocused();
        spreadsheet.addSheet();
        spreadsheet.addSheet();
        verifySheetFocused();
        spreadsheet.selectSheetAt(0);
        verifySheetFocused();
        spreadsheet.selectSheetAt(1);
        verifySheetFocused();
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3230 is fixed")
    @Test
    public void cellFocus_moveFromSheetOneToSheetTwoAndBack_cellSelectionRemains()
            throws InterruptedException {
        SpreadsheetElement spreadsheet = getSpreadsheet();

        clickCell("C8");
        spreadsheet.addSheet();
        spreadsheet.selectSheetAt(1);
        selectRegion("C3", "G14");
        spreadsheet.selectSheetAt(0);
        getCommandExecutor().waitForVaadin();
        Assert.assertTrue(spreadsheet.getCellAt("C8").isCellSelected());
        spreadsheet.selectSheetAt(1);
        getCommandExecutor().waitForVaadin();
        String[] cols = { "C", "D", "E", "F", "G" };
        for (String column : cols) {
            for (int row = 3; row <= 14; row++) {
                Assert.assertTrue("Cell " + column + row + " is not selected",
                        spreadsheet.getCellAt(column + "" + row)
                                .isCellSelected());
            }
        }
    }

    @Test
    public void cellFocus_selectCellThenDeleteSheetAndMoveToNextSheet_cellSelectionIsDefault() {
        SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.addSheet();
        spreadsheet.addSheet();
        spreadsheet.selectSheetAt(1);
        clickCell("C4");

        loadTestFixture(TestFixtures.RemoveFixture);
        getCommandExecutor().waitForVaadin();
        Assert.assertTrue(spreadsheet.getCellAt("A1").isCellSelected());
    }

    /**
     * Uses JavaScript to determine the currently focused element.
     *
     * @return Focused element or null
     */
    protected WebElement getFocusedElement() {
        Object focusedElement = executeScript("return document.activeElement");
        if (null != focusedElement) {
            return (WebElement) focusedElement;
        } else {
            return null;
        }
    }

    private void verifySheetFocused() {
        Assert.assertTrue("Sheet lost focus", getFocusedElement()
                .getAttribute("class").contains("bottom-right-pane"));
    }
}
