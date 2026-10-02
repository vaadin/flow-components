/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;

/**
 * A full refresh keeps the conditional formats the cells match.
 *
 * <p>
 * The refresh rebuilds the formatting rules, which it has to - undoing a row
 * delete puts formats back that the table must learn about - and it runs on
 * every load, once the client reports its character widths. Rebuilt, the rules
 * matched nothing in a workbook whose formats are written as shared formulas,
 * and its cells lost their red.
 */
class ConditionalFormattingRefreshTest {

    @Test
    void aFullRefresh_keepsTheFormatsTheCellsMatch() {
        Spreadsheet spreadsheet = TestHelper.createSpreadsheet(
                "conditional_formatting_shared_formula.xlsx");
        ConditionalFormatter formatter = spreadsheet.getConditionalFormatter();
        Cell a2 = spreadsheet.getCell("A2");
        Set<Integer> onLoad = formatter.getCellFormattingIndex(a2);
        assertNotNull(onLoad, "A2 holds 10, which its format colours red");

        spreadsheet.refreshAllCellValues();

        assertEquals(onLoad, formatter.getCellFormattingIndex(a2));
    }

    @Test
    void cellsTheFormatNeedsAreCreatedBlank() {
        // B3 sits in a formatted range and holds nothing. The format needs
        // a cell there to hang on, and it used to make one holding an empty
        // string - which A2's rule, comparing against B3, then read as text.
        Spreadsheet spreadsheet = TestHelper.createSpreadsheet(
                "conditional_formatting_shared_formula.xlsx");

        Cell b3 = spreadsheet.getCell("B3");
        assertNotNull(b3, "the format made a cell to hang on");
        assertEquals(CellType.BLANK, b3.getCellType());
    }

    @Test
    void aFormulaReadingSuchACell_stillAddsUp() {
        Spreadsheet spreadsheet = TestHelper.createSpreadsheet(
                "conditional_formatting_shared_formula.xlsx");
        Cell j1 = spreadsheet.getActiveSheet().getRow(0).createCell(9);
        j1.setCellFormula("B3+1");

        // Text plus one is #VALUE!; nothing plus one is one.
        assertEquals(1d, spreadsheet.getFormulaEvaluator().evaluate(j1)
                .getNumberValue());
    }
}
