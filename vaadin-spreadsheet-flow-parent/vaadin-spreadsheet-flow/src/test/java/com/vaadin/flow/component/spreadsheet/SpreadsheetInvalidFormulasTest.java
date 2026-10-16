/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * A formula typed that the server cannot parse is marked on its sheet, and
 * stays marked there whichever other sheet is deleted.
 */
class SpreadsheetInvalidFormulasTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("First");
        workbook.createSheet("Second");
        workbook.createSheet("Third");
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
    }

    /**
     * Selects the cell of the sheet and types a formula that does not parse
     * into it, as the client does.
     */
    private void typeInvalidFormula(int sheet, String cell) {
        spreadsheet.setActiveSheetIndex(sheet);
        CellAddress address = new CellAddress(cell);
        int row = address.getRow() + 1;
        int col = address.getColumn() + 1;
        TestHelper.fireClientEvent(spreadsheet, "cellSelected",
                "{\"row\": " + row + ", \"column\": " + col
                        + ", \"oldSelectionRangeDiscarded\": true}");
        TestHelper.fireClientEvent(spreadsheet, "cellValueEdited", "{\"row\": "
                + row + ", \"col\": " + col + ", \"value\": \"=1+\"}");
    }

    private boolean isMarked(int sheet, String cell) {
        spreadsheet.setActiveSheetIndex(sheet);
        CellAddress address = new CellAddress(cell);
        return spreadsheet.isMarkedAsInvalidFormula(address.getColumn() + 1,
                address.getRow() + 1);
    }

    @Test
    void aFormulaThatDoesNotParse_isMarked() {
        typeInvalidFormula(1, "B2");

        assertTrue(isMarked(1, "B2"));
        assertFalse(isMarked(0, "B2"), "on another sheet");
    }

    @Test
    void theMarksOfASheet_stayOnIt_whenASheetBeforeItIsDeleted() {
        typeInvalidFormula(0, "B2");
        typeInvalidFormula(2, "C3");

        spreadsheet.deleteSheet(0);

        // Second and Third are the first and second sheets now.
        assertFalse(isMarked(0, "B2"), "the marks of the sheet deleted");
        assertTrue(isMarked(1, "C3"), "the marks of Third");
    }

    @Test
    void theMarksOfASheetDeleted_goWithIt_andThoseOfTheSheetsBeforeItStay() {
        typeInvalidFormula(0, "C3");
        typeInvalidFormula(1, "B2");

        spreadsheet.deleteSheet(1);

        assertTrue(isMarked(0, "C3"), "the marks of First");
        assertFalse(isMarked(0, "B2"), "First");
        assertFalse(isMarked(1, "B2"), "Third");
    }
}
