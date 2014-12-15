package com.vaadin.addon.spreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheetConditionalFormatting;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Class that handles all processing regarding Conditional Formatting rules.
 * <p>
 * Rules are parsed into CSS rules with individual class names. Classnames for
 * each cell can then be fetched from this class.
 * 
 * @author Thomas Mattsson / Vaadin Ltd.
 */
public class ConditionalFormatter {

    protected Spreadsheet spreadsheet;

    /**
     * Cache of styles for each cell. One cell may have several styles.
     */
    protected Map<String, Set<Integer>> cellToIndex = new HashMap<String, Set<Integer>>();

    protected FormulaEvaluator formulaEvaluator;
    protected ColorConverter colorConverter;

    public ConditionalFormatter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        formulaEvaluator = spreadsheet.getWorkbook().getCreationHelper()
                .createFormulaEvaluator();

        final Workbook workbook = spreadsheet.getWorkbook();
        if (workbook instanceof HSSFWorkbook) {
            colorConverter = new HSSFColorConverter((HSSFWorkbook) workbook);
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) workbook);
        }
    }

    /**
     * Each cell can have multiple matching rules, hence a list. Order doesn't
     * matter here, CSS applied in correct order on the client side.
     * 
     * @param cell
     * @return indexes of the rules that match this Cell (to be used in
     *         classnames)
     */
    public Set<Integer> getCellFormattingIndex(Cell cell) {
        Set<Integer> index = cellToIndex.get(SpreadsheetUtil.toKey(cell));
        return index;
    }

    /**
     * Creates the necessary CSS rules and runs evaluations on all affected
     * cells.
     */
    public void createConditionalFormatterRules() {
        cellToIndex.clear();
        spreadsheet.getState().conditionalFormattingStyles = new HashMap<Integer, String>();

        SheetConditionalFormatting cfs = spreadsheet.getActiveSheet()
                .getSheetConditionalFormatting();

        if (cfs instanceof HSSFSheetConditionalFormatting) {
            // disable formatting for HSSF, since formulas are read incorrectly
            // and we would return incorrect results.
            return;
        }

        for (int i = 0; i < cfs.getNumConditionalFormattings(); i++) {
            ConditionalFormatting cf = cfs.getConditionalFormattingAt(i);

            for (int ruleIndex = 0; ruleIndex < cf.getNumberOfRules(); ruleIndex++) {

                ConditionalFormattingRule rule = cf.getRule(ruleIndex);

                int cssIndex = i * 1000 + ruleIndex;

                // build style

                FontFormatting fontFormatting = rule.getFontFormatting();

                // FIXME: some of this code will override all old values on each
                // iteration. POI API will return the default value for nulls,
                // which is not what we want.

                StringBuilder css = new StringBuilder();

                if (fontFormatting != null) {
                    String fontColorCSS = colorConverter.getFontColorCSS(rule);
                    if (fontColorCSS != null) {
                        css.append("color:" + fontColorCSS);
                    }

                    // HSSF might return 255 for 'none'...
                    if (fontFormatting.getUnderlineType() != FontFormatting.U_NONE
                            && fontFormatting.getUnderlineType() != 255)
                        css.append("text-decoration: underline;");

                    if (fontFormatting.getFontHeight() != -1) {
                        // POI returns height in 1/20th points, convert
                        int fontHeight = fontFormatting.getFontHeight() / 20;
                        css.append("font-size:" + fontHeight + "pt;");
                    }

                    if (fontFormatting.isItalic())
                        css.append("font-style: italic;");
                    if (fontFormatting.isBold())
                        css.append("font-weight: bold;");
                }

                PatternFormatting patternFormatting = rule
                        .getPatternFormatting();
                if (patternFormatting != null) {
                    String colorCSS = colorConverter
                            .getBackgroundColorCSS(rule);

                    if (colorCSS != null) {
                        css.append("background-color:" + colorCSS);
                    }
                }

                spreadsheet.getState().conditionalFormattingStyles.put(
                        cssIndex, css.toString());

                // check actual cells
                runCellMatcher(cf, rule, cssIndex);
            }

        }
    }

    /**
     * Goes through the cells specified in the given formatting, and checks if
     * each rule matches. Style ids from resulting matches are put in
     * {@link #cellToIndex}.
     * 
     * @param cf
     *            {@link ConditionalFormatting} that specifies the affected
     *            cells
     * @param rule
     *            the rule to be evaluated
     * @param classNameIndex
     *            the index of the classname that was generated for this rule,
     *            to be added to {@link #cellToIndex}
     */
    protected void runCellMatcher(ConditionalFormatting cf,
            ConditionalFormattingRule rule, int classNameIndex) {

        for (CellRangeAddress cra : cf.getFormattingRanges()) {

            for (int row = cra.getFirstRow(); row <= cra.getLastRow(); row++) {
                for (int col = cra.getFirstColumn(); col <= cra.getLastColumn(); col++) {

                    Cell cell = spreadsheet.getCell(row, col);
                    if (matches(cell, rule)) {
                        Set<Integer> list = cellToIndex.get(SpreadsheetUtil
                                .toKey(cell));
                        if (list == null) {
                            list = new HashSet<Integer>();
                            cellToIndex.put(SpreadsheetUtil.toKey(cell), list);
                        }
                        list.add(classNameIndex);
                    }
                }
            }
        }
    }

    /**
     * Checks if the given cell value matches the given conditional formatting
     * rule.
     * 
     * @param cell
     * @param rule
     * @return Whether the given rule evaluates to <code>true</code> for the
     *         given cell.
     */
    protected boolean matches(Cell cell, ConditionalFormattingRule rule) {

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

        switch (rule.getConditionType()) {
        case ConditionalFormattingRule.CONDITION_TYPE_CELL_VALUE_IS:
            return matchesValue(cell, rule);
        case ConditionalFormattingRule.CONDITION_TYPE_FORMULA:
            return matchesFormula(rule);

        default:
            return matchesFormula(rule);
        }
    }

    /**
     * Checks if the given formula in the rule evaluates to <code>true</code>.
     * <p>
     * TODO does not support HSSF files for now, since HSSF does not read cell
     * references in the file correctly.
     * 
     * @param rule
     * @return whether the formula in the given rule is of boolean formula type
     *         and evaluates to <code>true</code>
     */
    protected boolean matchesFormula(ConditionalFormattingRule rule) {
        String booleanFormula = rule.getFormula1();

        if (booleanFormula == null || booleanFormula.isEmpty())
            return false;

        if (rule instanceof XSSFConditionalFormattingRule) {

            /*
             * Use a temporary cell (hopefully) far away of any actual content,
             * so that we don't override any values. Done like this because POI
             * has issues when changing cell type back and forth, so we can't
             * use the given cell because we might not be able to recover the
             * value after our formula calculation.
             */
            Cell cell = spreadsheet.createFormulaCell(Short.MAX_VALUE, 255,
                    booleanFormula);

            // Since we use the same cell for all calculations, we need to clear
            // it each time. For some reason we can't clear just one cell.
            formulaEvaluator.clearAllCachedResultValues();
            CellValue value = formulaEvaluator.evaluate(cell);
            boolean match = value.getBooleanValue();

            spreadsheet.deleteCell(Short.MAX_VALUE, 255);

            return match;

        } else {
            // TODO since HSSF formulas are read completely wrong, that
            // boolean formula above is useless.

            return false;

        }
    }

    /**
     * Checks if the given cell value matches a
     * {@link ConditionalFormattingRule} of <code>VALUE_IS</code> type. Covers
     * all cell types and comparison operations.
     * 
     * @param cell
     * @param rule
     * @return whether the given cells value matches the given
     *         <code>VALUE_IS</code> rule
     */
    protected boolean matchesValue(Cell cell, ConditionalFormattingRule rule) {

        // other than numerical types
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {

            switch (rule.getComparisonOperation()) {
            case ComparisonOperator.EQUAL:
                return cell.getStringCellValue().equals(rule.getFormula1());
            case ComparisonOperator.NOT_EQUAL:
                return !cell.getStringCellValue().equals(rule.getFormula1());
            }
        }
        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            // not sure if this is used, since no boolean option exists in
            // Excel..

            Boolean formulaVal = Boolean.parseBoolean(rule.getFormula1());

            switch (rule.getComparisonOperation()) {
            case ComparisonOperator.EQUAL:
                return cell.getBooleanCellValue() == formulaVal;
            case ComparisonOperator.NOT_EQUAL:
                return cell.getBooleanCellValue() != formulaVal;
            }
        }

        // numerical types
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

            double formula1Val = Double.valueOf(rule.getFormula1());

            switch (rule.getComparisonOperation()) {

            case ComparisonOperator.EQUAL:
                return cell.getNumericCellValue() == formula1Val;
            case ComparisonOperator.NOT_EQUAL:
                return cell.getNumericCellValue() != formula1Val;

            case ComparisonOperator.LT:
                return cell.getNumericCellValue() < formula1Val;
            case ComparisonOperator.LE:
                return cell.getNumericCellValue() <= formula1Val;
            case ComparisonOperator.GT:
                return cell.getNumericCellValue() > formula1Val;
            case ComparisonOperator.GE:
                return cell.getNumericCellValue() >= formula1Val;

            case ComparisonOperator.BETWEEN:
                boolean lt = cell.getNumericCellValue() >= formula1Val;
                boolean gt = cell.getNumericCellValue() <= Double.valueOf(rule
                        .getFormula2());
                return lt && gt;

            case ComparisonOperator.NOT_BETWEEN:
                lt = cell.getNumericCellValue() <= formula1Val;
                gt = cell.getNumericCellValue() >= Double.valueOf(rule
                        .getFormula2());
                return lt && gt;
            }
        }
        return false;
    }
}
