/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.WorkbookEvaluatorUtil;
import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides whether a given cell value matches a conditional-formatting rule.
 *
 * <p>
 * Two evaluation paths, dispatched on rule type:
 *
 * <ul>
 * <li>{@code CELL_VALUE_IS} — compares the cell's value to the rule's
 * formula-as-value with an operator (EQUAL / LT / GT / BETWEEN / etc.). Handles
 * STRING / BOOLEAN / NUMERIC cells and formula cells with cached typed
 * results.</li>
 * <li>Other condition types (formula-based) — evaluates the rule's formula as a
 * boolean / numeric expression and returns whether it evaluates to {@code true}
 * (or non-zero).</li>
 * </ul>
 *
 * <p>
 * Pure logic with one external dependency: {@link Spreadsheet} (for the formula
 * evaluator and active sheet index). No mutation of cell or workbook state.
 */
final class RuleEvaluator implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RuleEvaluator.class);

    private final Spreadsheet spreadsheet;

    /**
     * The formatter whose overridable methods the rules are matched through: a
     * subclass that overrides {@code matchesValue}, {@code matchesFormula} or
     * {@code getFormulaEvaluator} sees its override applied.
     */
    private final ConditionalFormatter formatter;

    RuleEvaluator(Spreadsheet spreadsheet, ConditionalFormatter formatter) {
        this.spreadsheet = spreadsheet;
        this.formatter = formatter;
    }

    boolean matches(Cell cell, ConditionalFormattingRule rule, int deltaColumn,
            int deltaRow) {
        /*
         * Formula type is the default for most rules in modern excel files.
         *
         * There are a couple of issues with this.
         *
         * 1. the condition type seems to be '0' in all xlsx files, which is an
         * illegal value according to the API. The formula is still correct, and
         * can be accessed.
         *
         * 2. in xls-files the type is correct, but the formula is not: it
         * references the wrong cell.
         *
         * 3. the formula is a String. POIs FormulaEvaluation only takes Cell
         * arguments. So, to use it, we need to copy the formula to an existing
         * cell temporarily, and run the eval.
         */
        try {
            if (rule.getConditionType().equals(ConditionType.CELL_VALUE_IS)) {
                return formatter.matchesValue(cell, rule, deltaColumn,
                        deltaRow);
            } else {
                return formatter.matchesFormula(cell, rule, deltaColumn,
                        deltaRow);
            }
        } catch (NotImplementedException e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        }
    }

    boolean matchesFormula(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {
        if (!(rule instanceof XSSFConditionalFormattingRule)) {
            // TODO Does not support HSSF files for now, since HSSF does not
            // read cell references in the file correctly. Since HSSF formulas
            // are read completely wrong, that boolean formula above is useless.
            return false;
        }
        String booleanFormula = rule.getFormula1();

        if (booleanFormula == null || booleanFormula.isEmpty()) {
            return false;
        }

        ValueEval eval = getValueEvalFromFormula(booleanFormula, cell,
                deltaColumn, deltaRow);

        if (eval instanceof ErrorEval err) {
            LOGGER.trace(err.getErrorString(), eval);
        }

        if (eval instanceof BoolEval bool) {
            return bool.getBooleanValue();
        }
        if (eval instanceof NumberEval num) {
            return num.getNumberValue() != 0;
        }
        return false;
    }

    boolean matchesValue(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {

        boolean isFormulaType = cell.getCellType() == CellType.FORMULA;

        if (isFormulaType) {
            // make sure we have the latest value for formula cells
            formatter.getFormulaEvaluator().evaluateFormulaCell(cell);
        }

        boolean isFormulaStringType = isFormulaType
                && cell.getCachedFormulaResultType() == CellType.STRING;
        boolean isFormulaBooleanType = isFormulaType
                && cell.getCachedFormulaResultType() == CellType.BOOLEAN;
        boolean isFormulaNumericType = isFormulaType
                && cell.getCachedFormulaResultType() == CellType.NUMERIC;

        String formula = rule.getFormula1();
        byte comparisonOperation = rule.getComparisonOperation();
        ValueEval eval = getValueEvalFromFormula(formula, cell, deltaColumn,
                deltaRow);

        if (eval instanceof ErrorEval err) {
            LOGGER.trace(err.getErrorString(), eval);
            return false;
        }

        if (!hasCoherentType(eval, cell.getCellType(), isFormulaStringType,
                isFormulaBooleanType, isFormulaNumericType)) {
            // Comparison between different types (e.g. Bool vs String)
            return (comparisonOperation == ComparisonOperator.NOT_EQUAL);
        }

        // other than numerical types
        if (eval instanceof StringEval str
                && (cell.getCellType() == CellType.STRING
                        || isFormulaStringType)) {

            String formulaValue = str.getStringValue();
            String stringValue = cell.getStringCellValue();

            // Excel string comparison ignores case
            return switch (comparisonOperation) {
            case ComparisonOperator.EQUAL ->
                stringValue.equalsIgnoreCase(formulaValue);
            case ComparisonOperator.NOT_EQUAL ->
                !stringValue.equalsIgnoreCase(formulaValue);
            default -> false;
            };
        }
        if (eval instanceof BoolEval bool
                && (cell.getCellType() == CellType.BOOLEAN
                        || isFormulaBooleanType)) {
            // not sure if this is used, since no boolean option exists in
            // Excel..

            boolean formulaVal = bool.getBooleanValue();

            return switch (comparisonOperation) {
            case ComparisonOperator.EQUAL ->
                cell.getBooleanCellValue() == formulaVal;
            case ComparisonOperator.NOT_EQUAL ->
                cell.getBooleanCellValue() != formulaVal;
            default -> false;
            };
        }

        // numerical types
        if (cell.getCellType() == CellType.NUMERIC || isFormulaNumericType) {

            double formula1Val = ((NumericValueEval) eval).getNumberValue();
            double value = cell.getNumericCellValue();

            return switch (comparisonOperation) {
            case ComparisonOperator.EQUAL -> value == formula1Val;
            case ComparisonOperator.NOT_EQUAL -> value != formula1Val;
            case ComparisonOperator.LT -> value < formula1Val;
            case ComparisonOperator.LE -> value <= formula1Val;
            case ComparisonOperator.GT -> value > formula1Val;
            case ComparisonOperator.GE -> value >= formula1Val;
            case ComparisonOperator.BETWEEN, ComparisonOperator.NOT_BETWEEN -> {
                Double formula2Val = secondBound(rule, cell, deltaColumn,
                        deltaRow);
                if (formula2Val == null) {
                    yield false;
                }
                boolean between = value >= formula1Val && value <= formula2Val;
                yield comparisonOperation == ComparisonOperator.BETWEEN
                        ? between
                        : !between;
            }
            default -> false;
            };
        }

        return false;
    }

    /**
     * The second bound of a "between" rule, evaluated as the first one is, so
     * it can be a cell reference or a formula; null when it is not a number,
     * and the rule then matches nothing.
     */
    private Double secondBound(ConditionalFormattingRule rule, Cell cell,
            int deltaColumn, int deltaRow) {
        String formula2 = rule.getFormula2();
        if (formula2 == null || formula2.isEmpty()) {
            return null;
        }
        ValueEval eval = getValueEvalFromFormula(formula2, cell, deltaColumn,
                deltaRow);
        return eval instanceof NumericValueEval number ? number.getNumberValue()
                : null;
    }

    private ValueEval getValueEvalFromFormula(String formula, Cell cell,
            int deltaColumn, int deltaRow) {
        // Parse formula and use deltas to get relative cell references to work
        // (#18702)
        Ptg[] ptgs = FormulaParser.parse(formula,
                WorkbookEvaluatorUtil.getEvaluationWorkbook(spreadsheet),
                FormulaType.CELL, spreadsheet.getActiveSheetIndex());

        for (Ptg ptg : ptgs) {
            // base class for cell reference "things"
            if (ptg instanceof RefPtgBase ref) {
                // re-calculate cell references
                if (ref.isColRelative()) {
                    ref.setColumn(ref.getColumn() + deltaColumn);
                }
                if (ref.isRowRelative()) {
                    ref.setRow(ref.getRow() + deltaRow);
                }
            }
        }
        return WorkbookEvaluatorUtil.evaluate(spreadsheet, ptgs, cell);
    }

    private boolean hasCoherentType(ValueEval eval, CellType cellType,
            boolean isFormulaStringType, boolean isFormulaBooleanType,
            boolean isFormulaNumericType) {
        return switch (cellType) {
        case STRING -> eval instanceof StringEval;
        case BOOLEAN -> eval instanceof BoolEval;
        case NUMERIC -> eval instanceof NumericValueEval;
        case FORMULA -> isCoherentTypeFormula(eval, isFormulaStringType,
                isFormulaBooleanType, isFormulaNumericType);
        default -> true;
        };
    }

    private boolean isCoherentTypeFormula(ValueEval eval,
            boolean isFormulaStringType, boolean isFormulaBooleanType,
            boolean isFormulaNumericType) {
        if (isFormulaStringType) {
            return eval instanceof StringEval;
        }
        if (isFormulaBooleanType) {
            return eval instanceof BoolEval;
        }
        if (isFormulaNumericType) {
            return eval instanceof NumericValueEval;
        }
        return true;
    }

}
