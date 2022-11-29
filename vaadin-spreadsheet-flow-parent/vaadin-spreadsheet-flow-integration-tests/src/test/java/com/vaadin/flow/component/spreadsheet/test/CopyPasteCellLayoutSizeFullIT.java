package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-spreadsheet")
public class CopyPasteCellLayoutSizeFullIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void spreadsheetHandlerOnPaste_PasteCellsWhichOtherCellsDependingOn_UpdatesDependentCells() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("A1").setValue("1");
        spreadsheet.getCellAt("A2").setValue("2");
        spreadsheet.getCellAt("A3").setValue("3");
        spreadsheet.getCellAt("B3").setValue("=A3+1");
        spreadsheet.getCellAt("E4").setValue("=E3+10");

        copyPasteRegion("A3", "B3", "D3", false);
        final String expectedValue = "14";

        waitUntil(webDriver -> getCellValue("E4").equals(expectedValue));
    }

    private void copyPasteRegion(String startCopyCell, String endCopyCell,
            String pasteStartCell, boolean clearLog) {
        selectRegion(startCopyCell, endCopyCell);
        copy();
        clickCell(pasteStartCell);
        paste();
    }

}
