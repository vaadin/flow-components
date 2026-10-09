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
        // "C1+D2". The matcher routes through the column-delta branch
        // (oldCol != newCol AND oldRow == newRow).
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
        // Pin: a formula whose body has no cell references (matches nothing
        // under ROW_SHIFT_PATTERN) is written to the destination unchanged.
        // The matcher loop never executes, so the StringBuilder ends up
        // equal to the original formula. A regression that emitted "" or
        // null when the matcher found nothing would fail this assertion.
        Cell src = sheet.createRow(0).createCell(0);
        src.setCellFormula("2+3");
        Cell dst = sheet.createRow(1).createCell(0);

        shifter.shift(src, dst, false, null);

        assertEquals("2+3", dst.getCellFormula(),
                "Formulas without cell references pass through unchanged");
    }

    @Test
    void shift_formulaCell_postShiftRefIsInvalid_catchBranchRestoresOriginal() {
        // Pin the load-bearing catch (shiftFormula lines 217-221): if the
        // rewritten formula is invalid (POI rejects it on setCellFormula),
        // the catch resets the destination to the source's ORIGINAL
        // formula. We trigger this by row-shifting "A1" up to a position
        // where the rewrite produces "A0" — POI rejects row 0 in formulas
        // (rows are 1-based in Excel formula syntax) and throws
        // FormulaParseException.
        //
        // Verifies: removing the catch (or its fallback) would propagate
        // the exception OR leave the dst cell without the original
        // formula — either way the assertEquals on "A1" fails.
        Cell src = sheet.createRow(1).createCell(0); // POI row 1 = Excel 2
        src.setCellFormula("A1");
        Cell dst = sheet.createRow(0).createCell(0); // POI row 0 = Excel 1
        // rowDiff = 0 - 1 = -1; rewrite makes "A0", which POI rejects.

        shifter.shift(src, dst, false, null);

        assertEquals("A1", dst.getCellFormula(),
                "Catch fallback MUST set dst to the original source formula");
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
