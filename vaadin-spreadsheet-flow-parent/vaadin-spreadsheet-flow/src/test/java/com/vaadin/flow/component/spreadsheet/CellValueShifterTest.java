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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Writing a filled cell during autofill with {@link CellValueShifter}.
 *
 * <p>
 * Pins the type-dispatch behavior of the Excel-autofill cell writer. Each
 * cell-type branch is tested with and without a sequence increment where
 * applicable. {@code FORMULA} is exercised separately because it requires
 * mocking the formula-evaluator chain.
 *
 * <p>
 * Uses an in-memory {@link XSSFWorkbook} fixture for the cells themselves (real
 * POI types are part of the contract — POI's {@code setBlank} and
 * {@code setCellValue} side effects are exactly what we want to pin). Mocks the
 * parent {@link Spreadsheet} because the shifter notifies it via
 * {@code getSpreadsheetStyleFactory().cellStyleUpdated} and
 * {@code getCellValueManager().cellUpdated} as side effects.
 */
class CellValueShifterTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SpreadsheetStyleFactory styleFactory;
    @Mock
    private CellValueManager valueManager;
    @Mock
    private FormulaEvaluator formulaEvaluator;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private CellValueShifter shifter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        when(spreadsheet.getSpreadsheetStyleFactory()).thenReturn(styleFactory);
        when(spreadsheet.getCellValueManager()).thenReturn(valueManager);
        lenient().when(valueManager.getFormulaEvaluator())
                .thenReturn(formulaEvaluator);
        shifter = new CellValueShifter(spreadsheet);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void shift_numericCell_withoutIncrement_copiesValue() {
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(42.0);
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, null);

        assertEquals(CellType.NUMERIC, dst.getCellType());
        assertEquals(42.0, dst.getNumericCellValue(), 0.0);
    }

    @Test
    void shift_numericCell_withIncrement_alongRow_appliesScaledIncrement() {
        // Pin: row-axis shift uses column delta, increment * (newCol - oldCol)
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(10.0);
        Cell dst = row.createCell(3); // 3 columns to the right
        Double increment = 2.0;

        shifter.shift(src, dst, false, increment);

        // 10 + 2 * (3 - 0) = 16
        assertEquals(16.0, dst.getNumericCellValue(), 0.0);
    }

    @Test
    void shift_numericCell_withIncrement_alongColumn_appliesScaledIncrement() {
        // Pin: column-axis shift uses row delta (when source and destination
        // share the column).
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellValue(10.0);
        Cell dst = sheet.createRow(4).createCell(0); // 4 rows below
        Double increment = 1.5;

        shifter.shift(src, dst, false, increment);

        // 10 + 1.5 * (4 - 0) = 16.0
        assertEquals(16.0, dst.getNumericCellValue(), 0.0);
    }

    @Test
    void shift_stringCell_withIncrement_incrementsTrailingDigits() {
        // Pin: STRING cells whose value ends in digits get the digits
        // incremented along the shift axis. "A1" + col-shift 2 → "A3".
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("Item1");
        Cell dst = row.createCell(2); // 2 columns right

        shifter.shift(src, dst, false, 1.0);

        // "Item" + abs(1 + 1*2) = "Item3"
        assertEquals("Item3", dst.getStringCellValue());
    }

    @Test
    void shift_stringCell_noTrailingDigits_withIncrement_copiesUnchanged() {
        // Pin: a STRING cell without trailing digits ignores the increment
        // and just gets copied. "foo" stays "foo" no matter the shift.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("foo");
        Cell dst = row.createCell(2);

        shifter.shift(src, dst, false, 1.0);

        assertEquals("foo", dst.getStringCellValue());
    }

    @Test
    void shift_stringCell_withoutIncrement_copiesValue() {
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("hello");
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, null);

        assertEquals("hello", dst.getStringCellValue());
    }

    @Test
    void shift_booleanCell_copiesValue_ignoringIncrement() {
        // Pin: BOOLEAN cells copy as-is; the increment parameter is ignored.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(true);
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, 5.0);

        assertEquals(CellType.BOOLEAN, dst.getCellType());
        assertEquals(true, dst.getBooleanCellValue());
    }

    @Test
    void shift_blankCell_writesBlank() {
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        // src is default BLANK
        Cell dst = row.createCell(1);
        dst.setCellValue("non-blank initially");

        shifter.shift(src, dst, false, null);

        assertEquals(CellType.BLANK, dst.getCellType());
    }

    @Test
    void shift_notifiesStyleFactoryAndCellValueManager() {
        // Pin the two parent-side notifications: style-factory's
        // cellStyleUpdated(newCell, true) and value-manager's
        // cellUpdated(newCell) — both fire on every shift, regardless of
        // type.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue(1.0);
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, false, null);

        verify(styleFactory).cellStyleUpdated(dst, true);
        verify(valueManager).cellUpdated(dst);
    }

    @Test
    void shift_withRemoveShifted_clearsSourceAndNotifiesDeletion() {
        // Pin: removeShifted=true clears the source cell's value and
        // notifies the value-manager via cellDeleted. The destination still
        // gets its normal cellUpdated notification.
        Row row = sheet.createRow(0);
        Cell src = row.createCell(0);
        src.setCellValue("payload");
        Cell dst = row.createCell(1);

        shifter.shift(src, dst, true, null);

        // Source is cleared.
        assertEquals("", src.getStringCellValue());
        verify(valueManager).cellDeleted(src);
        verify(valueManager).cellUpdated(dst);
    }

    // ----- FORMULA branch ---------------------------------------------------

    @Test
    void shift_formulaCell_rowShift_rewritesRelativeRowReferences() {
        // Pin the row-shift rewrite: a same-column move down by 2 rows
        // increments row numbers in cell references. "A1+B2" shifted from
        // row 0 to row 2 becomes "A3+B4". The shifter MUST follow this
        // contract because Excel autofill expects relative references to
        // move with the cell.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("A1+B2");
        Cell dst = sheet.createRow(2).createCell(0); // same column, 2 rows down

        shifter.shift(src, dst, false, null);

        assertEquals(CellType.FORMULA, dst.getCellType());
        assertEquals("A3+B4", dst.getCellFormula());
        // Pin: shifter notifies the formula evaluator after the rewrite so
        // the cached evaluation is invalidated.
        verify(formulaEvaluator).notifySetFormula(dst);
    }

    @Test
    void shift_formulaCell_rowShift_preservesAbsoluteRowReferences() {
        // Pin: the "$"-prefixed absolute references are left untouched on a
        // row shift. "A$1+B2" shifted down by 1 row stays "A$1+B3" —
        // dollar locks the row, the un-dollared B2 still advances.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("A$1+B2");
        Cell dst = sheet.createRow(1).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("A$1+B3", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_columnShift_rewritesRelativeColumnReferences() {
        // Pin column-shift rewrite: a same-row move 2 columns right
        // increments column letters. "A1+B2" shifted to column 2 becomes
        // "C1+D2".
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("A1+B2");
        Cell dst = sheet.getRow(0).createCell(2); // same row, 2 cols right

        shifter.shift(src, dst, false, null);

        assertEquals("C1+D2", dst.getCellFormula());
        verify(formulaEvaluator).notifySetFormula(dst);
    }

    @Test
    void shift_formulaCell_columnShift_preservesAbsoluteColumnReferences() {
        // Pin: "$A1" — locked column, relative row — survives a column
        // shift unchanged. The dollared column doesn't advance even though
        // we moved 2 columns to the right.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("$A1+B2");
        Cell dst = sheet.getRow(0).createCell(2);

        shifter.shift(src, dst, false, null);

        assertEquals("$A1+D2", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_noCellReferences_passesThroughUnchanged() {
        // Pin: a formula whose body has no cell references is written to the
        // destination unchanged. A regression that emitted "" or null for a
        // formula with nothing to move would fail this assertion.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("2+3");
        Cell dst = sheet.createRow(1).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("2+3", dst.getCellFormula(),
                "Formulas without cell references pass through unchanged");
    }

    @Test
    void shift_formulaCell_referenceMovedOffTheSheet_becomesARefError() {
        // As in Excel: "A1" filled from row 2 up to row 1 would point above
        // the first row. Copied unchanged, it would make the cell in A1 refer
        // to itself.
        Cell src = sheet.createRow(1).createCell(0);
        src.setCellFormula("A1");
        Cell dst = sheet.createRow(0).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("#REF!", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_rowShift_keepsTheSheetOfASheetQualifiedReference() {
        // The digits of a sheet's name are not a row: "Sheet1!A1" used to
        // become "Sheet2!A2", another sheet.
        workbook.createSheet("Sheet1");
        workbook.createSheet("Sheet2");
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("Sheet1!A1*2");
        Cell dst = sheet.createRow(1).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("Sheet1!A2*2", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_columnShift_keepsTheSheetOfASheetQualifiedReference() {
        workbook.createSheet("Sheet1");
        workbook.createSheet("Sheet2");
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("Sheet1!A1*2");
        Cell dst = sheet.getRow(0).createCell(1);

        shifter.shift(src, dst, false, null);

        assertEquals("Sheet1!B1*2", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_rowShift_leavesAFunctionNameWithDigitsAlone() {
        // With "LOG10" read as a reference, "LOG10(A1)" would become
        // "LOG11(A2)", which does not parse, and the formula would be copied
        // with its reference not moved.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("LOG10(A1)");
        Cell dst = sheet.createRow(1).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("LOG10(A2)", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_rowShift_leavesTextAlone() {
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("\"Q1\"&A1");
        Cell dst = sheet.createRow(1).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("\"Q1\"&A2", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_rowShift_movesTheRelativeEndsOfARange() {
        Cell src = sheet.createRow(0).createCell(3);
        src.setCellFormula("SUM(A1:A3)+SUM($A$1:A3)");
        Cell dst = sheet.createRow(1).createCell(3);

        shifter.shift(src, dst, false, null);

        assertEquals("SUM(A2:A4)+SUM($A$1:A4)", dst.getCellFormula());
    }

    @Test
    void shift_formulaCell_ofAnOldWorkbook_rowShift_keepsTheSheetOfAReference()
            throws Exception {
        try (HSSFWorkbook old = new HSSFWorkbook()) {
            Sheet first = old.createSheet("Data");
            old.createSheet("Sheet1");
            Cell src = first.createRow(0).createCell(0);
            src.setCellFormula("Sheet1!A1*2");
            Cell dst = first.createRow(1).createCell(0);

            shifter.shift(src, dst, false, null);

            assertEquals("Sheet1!A2*2", dst.getCellFormula());
        }
    }

    // ----- ERROR branch -----------------------------------------------------

    @Test
    void shift_errorCell_copiesErrorCodeUnchanged_ignoringIncrement() {
        // Pin: ERROR cells copy via setCellErrorValue with the source's
        // error byte; the increment parameter is ignored (errors don't
        // arithmetic-shift). Round-trip via FormulaError.DIV0 so the
        // assertion is over a known constant.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellErrorValue(FormulaError.DIV0.getCode());
        Cell dst = sheet.getRow(0).createCell(1);

        shifter.shift(src, dst, false, 99.0);

        assertEquals(CellType.ERROR, dst.getCellType());
        assertEquals(FormulaError.DIV0.getCode(), dst.getErrorCellValue());
        // Smoke: error code resolves to the expected text through ErrorEval.
        assertEquals("#DIV/0!", ErrorEval.getText(dst.getErrorCellValue()));
    }
}
