package com.vaadin.addon.spreadsheet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFFontFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;

import com.vaadin.addon.spreadsheet.SpreadsheetStyleFactory.BorderStyle;

/**
 * Class that handles all processing regarding Conditional Formatting rules.
 * <p>
 * Rules are parsed into CSS rules with individual class names. Classnames for
 * each cell can then be fetched from this class.
 * 
 * @author Thomas Mattsson / Vaadin Ltd.
 */
public class ConditionalFormatter {

    private static final Logger LOGGER = Logger
            .getLogger(ConditionalFormatter.class.getName());

    protected Spreadsheet spreadsheet;

    /**
     * Cache of styles for each cell. One cell may have several styles.
     */
    protected Map<String, Set<Integer>> cellToIndex = new HashMap<String, Set<Integer>>();

    protected Map<ConditionalFormatting, Integer> topBorders = new HashMap<ConditionalFormatting, Integer>();
    protected Map<ConditionalFormatting, Integer> leftBorders = new HashMap<ConditionalFormatting, Integer>();

    private int borderStyleIndex = 0;

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
     * Each cell can have multiple matching rules, hence a collection. Order
     * doesn't matter here, CSS is applied in correct order on the client side.
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

        // make sure old styles are cleared
        if (cellToIndex != null) {
            for (String key : cellToIndex.keySet()) {
                int col = SpreadsheetUtil.getColumnIndexFromKey(key) - 1;
                int row = SpreadsheetUtil.getRowFromKey(key) - 1;
                Cell cell = spreadsheet.getCell(row, col);
                spreadsheet.markCellAsUpdated(cell, false);
            }
        }

        cellToIndex.clear();
        topBorders.clear();
        leftBorders.clear();
        spreadsheet.getState().conditionalFormattingStyles = new HashMap<Integer, String>();
        borderStyleIndex = 1000000;

        SheetConditionalFormatting cfs = spreadsheet.getActiveSheet()
                .getSheetConditionalFormatting();

        if (cfs instanceof HSSFSheetConditionalFormatting) {
            // disable formatting for HSSF, since formulas are read incorrectly
            // and we would return incorrect results.
            return;
        }

        for (int i = 0; i < cfs.getNumConditionalFormattings(); i++) {
            ConditionalFormatting cf = cfs.getConditionalFormattingAt(i);

            // Excel stops processing certain rules after it finds the first
            // match, so use a marker
            boolean hasFontLines = false;
            boolean hasFontStyle = false;

            // rules are listen bottom up, but we want top down so that we can
            // stop when we need to. Rule indexes follow original order, because
            // that is the order CSS is applied on client side.
            for (int ruleIndex = cf.getNumberOfRules() - 1; ruleIndex >= 0; ruleIndex--) {

                ConditionalFormattingRule rule = cf.getRule(ruleIndex);

                // first formatting object gets 0-999, second 1000-1999...
                // should be enough.
                int cssIndex = i * 1000 + ruleIndex;

                // build style

                // TODO: some of this code will override all old values on each
                // iteration. POI API will return the default value for nulls,
                // which is not what we want.

                StringBuilder css = new StringBuilder();

                FontFormatting fontFormatting = rule.getFontFormatting();

                if (fontFormatting != null) {
                    String fontColorCSS = colorConverter.getFontColorCSS(rule);
                    if (fontColorCSS != null) {
                        css.append("color:" + fontColorCSS);
                    }

                    // we can't have both underline and line-through in the same
                    // DIV element, so use the first one that matches.

                    // HSSF might return 255 for 'none'...
                    if (!hasFontLines
                            && fontFormatting.getUnderlineType() != FontFormatting.U_NONE
                            && fontFormatting.getUnderlineType() != 255) {
                        css.append("text-decoration: underline;");
                        hasFontLines = true;
                    }
                    if (!hasFontLines && hasStrikeThrough(fontFormatting)) {
                        css.append("text-decoration: line-through;");
                        hasFontLines = true;
                    }

                    if (fontFormatting.getFontHeight() != -1) {
                        // POI returns height in 1/20th points, convert
                        int fontHeight = fontFormatting.getFontHeight() / 20;
                        css.append("font-size:" + fontHeight + "pt;");
                    }

                    // excel has a setting for bold italic, otherwise bold
                    // overrides
                    // italic and vice versa
                    if (!hasFontStyle && fontFormatting.isItalic()
                            && fontFormatting.isBold()) {
                        css.append("font-style: italic;");
                        css.append("font-weight: bold;");
                        hasFontStyle = true;
                    } else if (!hasFontStyle && fontFormatting.isItalic()) {
                        css.append("font-style: italic;");
                        css.append("font-weight: initial;");
                        hasFontStyle = true;
                    } else if (!hasFontStyle && fontFormatting.isBold()) {
                        css.append("font-style: normal;");
                        css.append("font-weight: bold;");
                        hasFontStyle = true;
                    }
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

                addBorderFormatting(cf, rule, css);

                spreadsheet.getState().conditionalFormattingStyles.put(
                        cssIndex, css.toString());

                // check actual cells
                runCellMatcher(cf, rule, cssIndex);

                // stop here if defined in rules
                if (stopHere(rule)) {
                    break;
                }
            }

        }
    }

    private void addBorderFormatting(ConditionalFormatting cf,
            ConditionalFormattingRule rule, StringBuilder css) {

        if (!(rule instanceof XSSFConditionalFormattingRule)) {
            // HSSF not supported
            return;
        }

        XSSFBorderFormatting borderFormatting = (XSSFBorderFormatting) rule
                .getBorderFormatting();
        if (borderFormatting != null) {

            BorderStyle borderLeft = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderLeft());
            BorderStyle borderRight = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderRight());
            BorderStyle borderTop = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderTop());
            BorderStyle borderBottom = SpreadsheetStyleFactory.BORDER
                    .get(borderFormatting.getBorderBottom());

            if (borderRight != BorderStyle.NONE) {
                css.append("border-right:");
                css.append(borderRight.getBorderAttributeValue());
                css.append(colorConverter.getBorderColorCSS(BorderSide.RIGHT,
                        "border-right-color", borderFormatting));
            }
            if (borderBottom != BorderStyle.NONE) {
                css.append("border-bottom:");
                css.append(borderBottom.getBorderAttributeValue());
                css.append(colorConverter.getBorderColorCSS(BorderSide.BOTTOM,
                        "border-bottom-color", borderFormatting));
            }

            // top and left borders might be applied to another cell, so store
            // them with a different index
            if (borderTop != BorderStyle.NONE) {
                final StringBuilder sb2 = new StringBuilder("border-bottom:");
                sb2.append(borderTop.getBorderAttributeValue());
                sb2.append(colorConverter.getBorderColorCSS(BorderSide.TOP,
                        "border-bottom-color", borderFormatting));

                spreadsheet.getState().conditionalFormattingStyles.put(
                        borderStyleIndex, sb2.toString());
                topBorders.put(cf, borderStyleIndex++);
            }

            if (borderLeft != BorderStyle.NONE) {
                final StringBuilder sb2 = new StringBuilder("border-right:");
                sb2.append(borderLeft.getBorderAttributeValue());
                sb2.append(colorConverter.getBorderColorCSS(BorderSide.LEFT,
                        "border-right-color", borderFormatting));

                spreadsheet.getState().conditionalFormattingStyles.put(
                        borderStyleIndex, sb2.toString());
                leftBorders.put(cf, borderStyleIndex++);
            }
        }
    }

    /**
     * Checks if this rule has 'stop if true' defined.
     */
    private boolean stopHere(ConditionalFormattingRule rule) {
        if (rule instanceof XSSFConditionalFormattingRule) {

            // No POI API for this particular data, but it is present in XML.

            Method declaredMethod = null;
            try {
                declaredMethod = XSSFConditionalFormattingRule.class
                        .getDeclaredMethod("getCTCfRule");
                declaredMethod.setAccessible(true);
                CTCfRule ctRule = (CTCfRule) declaredMethod.invoke(rule);

                return ctRule.getStopIfTrue();

            } catch (NoSuchMethodException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (SecurityException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (IllegalAccessException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (IllegalArgumentException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (InvocationTargetException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } finally {
                if (declaredMethod != null) {
                    declaredMethod.setAccessible(false);
                }
            }
        }
        return false;
    }

    /**
     * Checks if this formatting has strikethrough enabled or not.
     */
    private boolean hasStrikeThrough(FontFormatting fontFormatting) {
        if (fontFormatting instanceof XSSFFontFormatting) {

            // No POI API for this particular data, but it is present in XML.

            Field field = null;
            try {
                field = XSSFFontFormatting.class.getDeclaredField("_font");
                field.setAccessible(true);
                CTFont font = (CTFont) field.get(fontFormatting);

                List<CTBooleanProperty> strikeList = font.getStrikeList();

                if (strikeList != null) {
                    for (CTBooleanProperty p : strikeList) {
                        if (p.getVal()) {
                            return true;
                        }
                    }
                }

            } catch (NoSuchFieldException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (SecurityException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (IllegalArgumentException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } catch (IllegalAccessException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Incompatible POI implementation, unable to parse conditional formatting rule",
                        e);
            } finally {
                if (field != null) {
                    field.setAccessible(false);
                }
            }
        }
        return false;
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
                    if (cell != null && matches(cell, rule)) {
                        Set<Integer> list = cellToIndex.get(SpreadsheetUtil
                                .toKey(cell));
                        if (list == null) {
                            list = new HashSet<Integer>();
                            cellToIndex.put(SpreadsheetUtil.toKey(cell), list);
                        }
                        list.add(classNameIndex);

                        // if the rule contains borders, we need to add styles
                        // to other cells too
                        if (leftBorders.containsKey(cf)) {
                            int ruleIndex = leftBorders.get(cf);

                            // left border for col 0 isn't rendered
                            if (col != 0) {
                                Cell cellToLeft = spreadsheet.getCell(row,
                                        col - 1);
                                if (cellToLeft == null) {
                                    cellToLeft = spreadsheet.createCell(row,
                                            col - 1, "");
                                }
                                list = cellToIndex.get(SpreadsheetUtil
                                        .toKey(cellToLeft));
                                if (list == null) {
                                    list = new HashSet<Integer>();
                                    cellToIndex.put(
                                            SpreadsheetUtil.toKey(cellToLeft),
                                            list);
                                }
                                list.add(ruleIndex);
                            }
                        }
                        if (topBorders.containsKey(cf)) {
                            int ruleIndex = topBorders.get(cf);

                            // top border for row 0 isn't rendered
                            if (row != 0) {
                                Cell cellOnTop = spreadsheet.getCell(row - 1,
                                        col);
                                if (cellOnTop == null) {
                                    cellOnTop = spreadsheet.createCell(row - 1,
                                            col, "");
                                }
                                list = cellToIndex.get(SpreadsheetUtil
                                        .toKey(cellOnTop));
                                if (list == null) {
                                    list = new HashSet<Integer>();
                                    cellToIndex.put(
                                            SpreadsheetUtil.toKey(cellOnTop),
                                            list);
                                }
                                list.add(ruleIndex);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the given cell value matches the given conditional formatting
     * rule.
     * 
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
     * @return whether the formula in the given rule is of boolean formula type
     *         and evaluates to <code>true</code>
     */
    protected boolean matchesFormula(ConditionalFormattingRule rule) {
        String booleanFormula = rule.getFormula1();

        if (booleanFormula == null || booleanFormula.isEmpty()) {
            return false;
        }

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
