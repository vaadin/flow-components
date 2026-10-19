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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Matching cells against conditional-formatting rules with
 * {@link RuleEvaluator}.
 *
 * <p>
 * Uses a real {@link XSSFWorkbook} so POI's formula machinery (FormulaParser /
 * WorkbookEvaluator / OperationEvaluationContext) has actual cells/sheets to
 * read. Mocks {@link Spreadsheet} but routes {@code getFormulaEvaluator},
 * {@code getActiveSheetIndex}, and the workbook through the real workbook so
 * {@code WorkbookEvaluatorUtil}'s static calls succeed.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>CELL_VALUE_IS dispatch: numeric ops (=, !=, &lt;, &lt;=, &gt;, &gt;=,
 * BETWEEN, NOT_BETWEEN), string EQUAL (case-insensitive), boolean EQUAL,
 * type-mismatch → only NOT_EQUAL is true.</li>
 * <li>Formula-based rule: BoolEval(true) → match; NumberEval(non-zero) → match;
 * NumberEval(0) → no match.</li>
 * <li>HSSF skip: non-XSSF rule via the formula path returns false.</li>
 * <li>Empty/null formula returns false.</li>
 * <li>NotImplementedException → false (soft-fail).</li>
 * </ul>
 */
class RuleEvaluatorTest {

    @Mock
    private Spreadsheet spreadsheet;

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private RuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        lenient().when(spreadsheet.getWorkbook()).thenReturn(workbook);
        lenient().when(spreadsheet.getActiveSheetIndex()).thenReturn(0);
        lenient().when(spreadsheet.getFormulaEvaluator()).thenReturn(
                workbook.getCreationHelper().createFormulaEvaluator());
        evaluator = new RuleEvaluator(spreadsheet,
                new ConditionalFormatter(spreadsheet));
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    // ----- helpers ----------------------------------------------------------

    private ConditionalFormattingRule cellValueRule(byte op, String formula1) {
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        XSSFConditionalFormattingRule rule = (XSSFConditionalFormattingRule) scf
                .createConditionalFormattingRule(op, formula1);
        scf.addConditionalFormatting(
                new CellRangeAddress[] { new CellRangeAddress(0, 5, 0, 5) },
                rule);
        return rule;
    }

    private ConditionalFormattingRule betweenRule(byte op, String formula1,
            String formula2) {
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        XSSFConditionalFormattingRule rule = (XSSFConditionalFormattingRule) scf
                .createConditionalFormattingRule(op, formula1, formula2);
        scf.addConditionalFormatting(
                new CellRangeAddress[] { new CellRangeAddress(0, 5, 0, 5) },
                rule);
        return rule;
    }

    private ConditionalFormattingRule formulaRule(String booleanFormula) {
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        XSSFConditionalFormattingRule rule = (XSSFConditionalFormattingRule) scf
                .createConditionalFormattingRule(booleanFormula);
        scf.addConditionalFormatting(
                new CellRangeAddress[] { new CellRangeAddress(0, 5, 0, 5) },
                rule);
        return rule;
    }

    private Cell numericCell(int rowIdx, int colIdx, double value) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        Cell cell = row.createCell(colIdx, CellType.NUMERIC);
        cell.setCellValue(value);
        return cell;
    }

    private Cell booleanCell(int rowIdx, int colIdx, boolean value) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        Cell cell = row.createCell(colIdx, CellType.BOOLEAN);
        cell.setCellValue(value);
        return cell;
    }

    private Cell stringCell(int rowIdx, int colIdx, String value) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
            row = sheet.createRow(rowIdx);
        }
        Cell cell = row.createCell(colIdx, CellType.STRING);
        cell.setCellValue(value);
        return cell;
    }

    // ----- CELL_VALUE_IS — numeric ------------------------------------------

    @Test
    void matchesValue_numericEqual_matches() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.EQUAL,
                "5");
        Cell cell = numericCell(0, 0, 5d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericNotEqual_matches() {
        ConditionalFormattingRule rule = cellValueRule(
                ComparisonOperator.NOT_EQUAL, "5");
        Cell cell = numericCell(0, 0, 7d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericLT_matches() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.LT,
                "10");
        Cell cell = numericCell(0, 0, 5d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericLT_doesNotMatchTheBound() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.LT,
                "10");
        Cell cell = numericCell(0, 0, 10d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericGT_doesNotMatchTheBound() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.GT,
                "10");
        Cell cell = numericCell(0, 0, 10d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericGE_matchesTheBound() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.GE,
                "10");
        Cell cell = numericCell(0, 0, 10d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericLE_matchesEqualBoundary() {
        // Pin inclusive bound: LE includes the equal case.
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.LE,
                "10");
        Cell cell = numericCell(0, 0, 10d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericGT_matches() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.GT,
                "10");
        Cell cell = numericCell(0, 0, 15d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericGE_doesNotMatchBelow() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.GE,
                "10");
        Cell cell = numericCell(0, 0, 5d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericBETWEEN_matchesInRange() {
        ConditionalFormattingRule rule = betweenRule(ComparisonOperator.BETWEEN,
                "1", "10");
        Cell cell = numericCell(0, 0, 5d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_numericBETWEEN_boundariesAreInclusive() {
        // Pin: BETWEEN's boundaries are inclusive on both ends — matches
        // Excel's semantic.
        ConditionalFormattingRule rule = betweenRule(ComparisonOperator.BETWEEN,
                "1", "10");

        assertTrue(evaluator.matches(numericCell(0, 0, 1d), rule, 0, 0));
        assertTrue(evaluator.matches(numericCell(1, 0, 10d), rule, 0, 0));
    }

    @Test
    void matchesValue_numericBETWEEN_outsideRange_doesNotMatch() {
        ConditionalFormattingRule rule = betweenRule(ComparisonOperator.BETWEEN,
                "1", "10");

        assertFalse(evaluator.matches(numericCell(0, 0, 0d), rule, 0, 0));
        assertFalse(evaluator.matches(numericCell(1, 0, 11d), rule, 0, 0));
    }

    @Test
    void matchesValue_numericNOT_BETWEEN_matchesOutsideRange() {
        ConditionalFormattingRule rule = betweenRule(
                ComparisonOperator.NOT_BETWEEN, "10", "20");

        assertTrue(evaluator.matches(numericCell(0, 0, 5d), rule, 0, 0));
        assertTrue(evaluator.matches(numericCell(1, 0, 50d), rule, 0, 0));
    }

    @Test
    void matchesValue_numericNOT_BETWEEN_insideRangeOrOnABoundary_doesNotMatch() {
        ConditionalFormattingRule rule = betweenRule(
                ComparisonOperator.NOT_BETWEEN, "10", "20");

        assertFalse(evaluator.matches(numericCell(0, 0, 15d), rule, 0, 0));
        assertFalse(evaluator.matches(numericCell(1, 0, 10d), rule, 0, 0));
        assertFalse(evaluator.matches(numericCell(2, 0, 20d), rule, 0, 0));
    }

    @Test
    void matchesValue_numericBETWEEN_withACellReferenceAsTheSecondBound() {
        // The second bound used to be read as a number, so a reference threw
        // while the sheet loaded.
        numericCell(0, 2, 20d); // C1
        ConditionalFormattingRule rule = betweenRule(ComparisonOperator.BETWEEN,
                "10", "$C$1");

        assertTrue(evaluator.matches(numericCell(1, 0, 15d), rule, 0, 0));
        assertFalse(evaluator.matches(numericCell(2, 0, 25d), rule, 0, 0));
    }

    @Test
    void matchesValue_numericNOT_BETWEEN_withACellReferenceAsTheSecondBound() {
        numericCell(0, 2, 20d); // C1
        ConditionalFormattingRule rule = betweenRule(
                ComparisonOperator.NOT_BETWEEN, "10", "$C$1");

        assertTrue(evaluator.matches(numericCell(1, 0, 25d), rule, 0, 0));
        assertFalse(evaluator.matches(numericCell(2, 0, 15d), rule, 0, 0));
    }

    @Test
    void matchesValue_numericBETWEEN_withASecondBoundThatIsNoNumber_doesNotMatch() {
        stringCell(0, 2, "twenty"); // C1
        ConditionalFormattingRule rule = betweenRule(ComparisonOperator.BETWEEN,
                "10", "$C$1");

        assertFalse(evaluator.matches(numericCell(1, 0, 15d), rule, 0, 0));
    }

    // ----- CELL_VALUE_IS — string -------------------------------------------

    @Test
    void matchesValue_stringEqual_caseInsensitive() {
        // Pin: Excel string comparison ignores case. "HELLO" == "hello".
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.EQUAL,
                "\"hello\"");
        Cell cell = stringCell(0, 0, "HELLO");

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesValue_stringNotEqual_caseInsensitive() {
        ConditionalFormattingRule rule = cellValueRule(
                ComparisonOperator.NOT_EQUAL, "\"hello\"");

        assertFalse(evaluator.matches(stringCell(0, 0, "HELLO"), rule, 0, 0));
        assertTrue(evaluator.matches(stringCell(1, 0, "world"), rule, 0, 0));
    }

    @Test
    void matchesValue_stringOrdered_doesNotMatch() {
        // Text is compared only for being equal or not.
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.LT,
                "\"m\"");

        assertFalse(evaluator.matches(stringCell(0, 0, "a"), rule, 0, 0));
    }

    // ----- CELL_VALUE_IS — boolean ------------------------------------------

    @Test
    void matchesValue_booleanEqual_matchesTheSameValueOnly() {
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.EQUAL,
                "TRUE");

        assertTrue(evaluator.matches(booleanCell(0, 0, true), rule, 0, 0));
        assertFalse(evaluator.matches(booleanCell(1, 0, false), rule, 0, 0));
    }

    @Test
    void matchesValue_booleanNotEqual_matchesTheOtherValueOnly() {
        ConditionalFormattingRule rule = cellValueRule(
                ComparisonOperator.NOT_EQUAL, "TRUE");

        assertFalse(evaluator.matches(booleanCell(0, 0, true), rule, 0, 0));
        assertTrue(evaluator.matches(booleanCell(1, 0, false), rule, 0, 0));
    }

    @Test
    void matchesValue_booleanOrdered_doesNotMatch() {
        // A truth value is compared only for being equal or not.
        ConditionalFormattingRule rule = cellValueRule(ComparisonOperator.GT,
                "FALSE");

        assertFalse(evaluator.matches(booleanCell(0, 0, true), rule, 0, 0));
    }

    // ----- CELL_VALUE_IS — no operator --------------------------------------

    @Test
    void matchesValue_withoutAnOperator_doesNotMatch() {
        // As POI reads a cell-value rule whose file gives no operator
        ConditionalFormattingRule rule = mock(ConditionalFormattingRule.class);
        when(rule.getConditionType()).thenReturn(ConditionType.CELL_VALUE_IS);
        when(rule.getComparisonOperation())
                .thenReturn(ComparisonOperator.NO_COMPARISON);
        when(rule.getFormula1()).thenReturn("5");

        assertFalse(evaluator.matches(numericCell(0, 0, 5d), rule, 0, 0));
    }

    // ----- CELL_VALUE_IS — type mismatch ------------------------------------

    @Test
    void matchesValue_typeMismatch_onlyNotEqualReturnsTrue() {
        // Pin: when the comparison eval-type doesn't match the cell type
        // (e.g. comparing a number cell against a string formula), only
        // the NOT_EQUAL operator returns true — all others return false.
        ConditionalFormattingRule notEqualRule = cellValueRule(
                ComparisonOperator.NOT_EQUAL, "\"hello\"");
        ConditionalFormattingRule equalRule = cellValueRule(
                ComparisonOperator.EQUAL, "\"hello\"");
        Cell numCell = numericCell(0, 0, 5d);

        assertTrue(evaluator.matches(numCell, notEqualRule, 0, 0));
        assertFalse(evaluator.matches(numericCell(1, 0, 5d), equalRule, 0, 0));
    }

    // ----- Formula-based rule -----------------------------------------------

    @Test
    void matchesFormula_boolFormulaTrue_matches() {
        // A simple TRUE formula always matches.
        ConditionalFormattingRule rule = formulaRule("TRUE()");
        Cell cell = numericCell(0, 0, 1d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesFormula_boolFormulaFalse_doesNotMatch() {
        ConditionalFormattingRule rule = formulaRule("FALSE()");
        Cell cell = numericCell(0, 0, 1d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesFormula_numberFormulaNonZero_matches() {
        // Pin: numeric formula results that are non-zero are treated as
        // truthy. 1+1=2 → matches.
        ConditionalFormattingRule rule = formulaRule("1+1");
        Cell cell = numericCell(0, 0, 0d);

        assertTrue(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesFormula_numberFormulaZero_doesNotMatch() {
        // Pin: numeric 0 is falsy.
        ConditionalFormattingRule rule = formulaRule("0");
        Cell cell = numericCell(0, 0, 1d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }

    @Test
    void matchesFormula_stringFormula_doesNotMatch() {
        // Pin: a formula that evaluates to a string returns false (only
        // Bool / non-zero Number match).
        ConditionalFormattingRule rule = formulaRule("\"hello\"");
        Cell cell = numericCell(0, 0, 1d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }

    // ----- Edge cases -------------------------------------------------------

    @Test
    void matches_softFailsOnNotImplementedException() {
        // Pin: a rule using POI-unsupported functions throws
        // NotImplementedException; the evaluator catches it and returns
        // false (no crash).
        ConditionalFormattingRule rule = formulaRule("WEBSERVICE(\"x\")");
        Cell cell = numericCell(0, 0, 1d);

        assertFalse(evaluator.matches(cell, rule, 0, 0));
    }
}
