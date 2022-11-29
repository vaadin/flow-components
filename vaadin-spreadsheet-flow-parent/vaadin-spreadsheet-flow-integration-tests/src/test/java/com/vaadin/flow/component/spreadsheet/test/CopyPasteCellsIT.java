package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

@TestPath("vaadin-spreadsheet")
public class CopyPasteCellsIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void spreadsheetHandlerOnPaste_PasteCellsWhichOtherCellsDependingOn_UpdatesDependentCells() {
        createNewSpreadsheet();
        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .first();
        spreadsheet.getCellAt("A1").setValue("1");
        spreadsheet.getCellAt("A2").setValue("2");
        spreadsheet.getCellAt("A3").setValue("3");
        spreadsheet.getCellAt("B3").setValue("=A3+1");
        spreadsheet.getCellAt("E4").setValue("=E3+10");

        copyPasteRegion("A3", "B3", "D3", false);
        final String expectedValue = "14";
        waitUntil(new ExpectedCondition<Object>() {
            @Override
            public Object apply(WebDriver webDriver) {
                return spreadsheet.getCellAt("E4").getValue()
                        .equals(expectedValue);
            }
        });
    }

    private void copyPasteRegion(String startCopyCell, String endCopyCell,
            String pasteStartCell, boolean clearLog) {
        selectRegion(startCopyCell, endCopyCell);
        copy();
        clickCell(pasteStartCell);

        waitUntil(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return "D3".equals(getSelectionFormula());
            }
        });
        paste();
    }
}
