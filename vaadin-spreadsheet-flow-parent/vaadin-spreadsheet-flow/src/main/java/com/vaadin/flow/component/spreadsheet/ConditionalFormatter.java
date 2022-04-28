package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheetConditionalFormatting;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFBorderFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFFontFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.SpreadsheetStyleFactory.BorderStyle;

/**
 * ConditionalFormatter is an utility class of Spreadsheet, which handles all
 * processing regarding Conditional Formatting rules.
 * <p>
 * Rules are parsed into CSS rules with individual class names. Class names for
 * each cell can then be fetched from this class.
 * <p>
 * For now, only XSSF formatting rules are supported because of bugs in POI.
 *
 * @author Thomas Mattsson / Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class ConditionalFormatter implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(ConditionalFormatter.class);

    /*
     * Slight hack. This style is used when a CF rule defines 'no border', in
     * which case the border should be empty. However, since we use cell DIV
     * borders for the grid structure, empty borders are in fact grey. So, if
     * one rule says red, and the next says no border, then we need to know what
     * 'no border' means in CSS. Of course, if the default CSS changes, this
     * needs to change too.
     */
    private static String BORDER_STYLE_DEFAULT = "1pt solid #d6d6d6;";

    private Spreadsheet spreadsheet;

    /**
     * Cache of styles for each cell. One cell may have several styles.
     */
    private Map<String, Set<Integer>> cellToIndex = new HashMap<String, Set<Integer>>();

    private Map<ConditionalFormatting, Integer> topBorders = new HashMap<ConditionalFormatting, Integer>();
    private Map<ConditionalFormatting, Integer> leftBorders = new HashMap<ConditionalFormatting, Integer>();

    protected ColorConverter colorConverter;

    /**
     * Constructs a new ConditionalFormatter targeting the given Spreadsheet.
     *
     * @param spreadsheet
     *            Target spreadsheet
     */
    public ConditionalFormatter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;

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
     *            Target cell
     * @return indexes of the rules that match this Cell (to be used in class
     *         names)
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
                if (cell != null) {
                    spreadsheet.markCellAsUpdated(cell, true);
                }
            }
        }

        cellToIndex.clear();
        topBorders.clear();
        leftBorders.clear();
        HashMap<Integer, String> _conditionalFormattingStyles = new HashMap<Integer, String>();

        SheetConditionalFormatting cfs = spreadsheet.getActiveSheet()
                .getSheetConditionalFormatting();

        if (cfs instanceof HSSFSheetConditionalFormatting) {
            // disable formatting for HSSF, since formulas are read incorrectly
            // and we would return incorrect results.
            return;
        }

        for (int i = 0; i < cfs.getNumConditionalFormattings(); i++) {
            ConditionalFormatting cf = cfs.getConditionalFormattingAt(i);

            List<XSSFConditionalFormattingRule> cfRuleList = getOrderedRuleList(
                    cf);

            // rules are listen bottom up, but we want top down so that we can
            // stop when we need to. Rule indexes follow original order, because
            // that is the order CSS is applied on client side.
            for (int ruleIndex = cf.getNumberOfRules()
                    - 1; ruleIndex >= 0; ruleIndex--) {

                ConditionalFormattingRule rule = cfRuleList.get(ruleIndex);

                // first formatting object gets 0-999, second 1000-1999...
                // should be enough.
                int cssIndex = i * 1000000 + ruleIndex * 1000;

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
                    if (fontFormatting.getUnderlineType() != Font.U_NONE
                            && fontFormatting.getUnderlineType() != 255) {
                        css.append("text-decoration: underline;");
                    }
                    if (hasStrikeThrough(fontFormatting)) {
                        css.append("text-decoration: line-through;");
                    }

                    if (fontFormatting.getFontHeight() != -1) {
                        // POI returns height in 1/20th points, convert
                        int fontHeight = fontFormatting.getFontHeight() / 20;
                        css.append("font-size:" + fontHeight + "pt;");
                    }

                    // excel has a setting for bold italic, otherwise bold
                    // overrides
                    // italic and vice versa
                    if (fontFormatting.isItalic() && fontFormatting.isBold()) {
                        css.append("font-style: italic;");
                        css.append("font-weight: bold;");
                    } else if (fontFormatting.isItalic()) {
                        css.append("font-style: italic;");
                        css.append("font-weight: initial;");
                    } else if (fontFormatting.isBold()) {
                        css.append("font-style: normal;");
                        css.append("font-weight: bold;");
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

                cssIndex = addBorderFormatting(cf, rule, css, cssIndex);

                _conditionalFormattingStyles.put(cssIndex, css.toString());

                // check actual cells
                runCellMatcher(cf, rule, cssIndex);

                // stop here if defined in rules
                if (stopHere(rule)) {
                    break;
                }
            }

        }
        spreadsheet
                .setConditionalFormattingStyles(_conditionalFormattingStyles);
    }

    /**
     * Get the common {@link FormulaEvaluator} instance from {@link Spreadsheet}
     */
    protected FormulaEvaluator getFormulaEvaluator() {
        return spreadsheet.getFormulaEvaluator();
    }

    /**
     * Excel uses a field called 'priority' to re-order rules. Just calling
     * {@link XSSFConditionalFormatting#getRule(int)} will result in wrong
     * order. So, instead, get the list and reorder it according to the priority
     * field.
     *
     * @return The list of conditional formatting rules in reverse order (same
     *         order Excel processes them).
     */
    private List<XSSFConditionalFormattingRule> getOrderedRuleList(
            ConditionalFormatting cf) {

        // get the list
        XSSFConditionalFormatting xcf = (XSSFConditionalFormatting) cf;
        List<XSSFConditionalFormattingRule> rules = new ArrayList<XSSFConditionalFormattingRule>();
        for (int i = 0; i < xcf.getNumberOfRules(); i++) {
            rules.add(xcf.getRule(i));
        }

        // reorder with hidden field
        Collections.sort(rules,
                new Comparator<XSSFConditionalFormattingRule>() {

                    @Override
                    public int compare(XSSFConditionalFormattingRule o1,
                            XSSFConditionalFormattingRule o2) {

                        CTCfRule object = (CTCfRule) getFieldValWithReflection(
                                o1, "_cfRule");
                        CTCfRule object2 = (CTCfRule) getFieldValWithReflection(
                                o2, "_cfRule");

                        if (object != null && object2 != null) {
                            // reverse order
                            return object2.getPriority() - object.getPriority();
                        }

                        return 0;
                    }
                });

        return rules;
    }

    /**
     * @return the new cssIndex
     */
    private int addBorderFormatting(ConditionalFormatting cf,
            ConditionalFormattingRule rule, StringBuilder css, int cssIndex) {

        if (!(rule instanceof XSSFConditionalFormattingRule)) {
            // HSSF not supported
            return cssIndex;
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

            // In Excel, we can set a border to 'none', which overrides previous
            // rules. Default is 'not set', in which case we add no CSS.
            boolean isLeftSet = isBorderSet(borderFormatting, BorderSide.LEFT);
            boolean isTopSet = isBorderSet(borderFormatting, BorderSide.TOP);
            boolean isRightSet = isBorderSet(borderFormatting,
                    BorderSide.RIGHT);
            boolean isBottomSet = isBorderSet(borderFormatting,
                    BorderSide.BOTTOM);

            if (isRightSet) {
                css.append("border-right:");
                if (borderRight != BorderStyle.NONE) {
                    css.append(borderRight.getBorderAttributeValue());
                    css.append(
                            colorConverter.getBorderColorCSS(BorderSide.RIGHT,
                                    "border-right-color", borderFormatting));
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }
            if (isBottomSet) {
                css.append("border-bottom:");
                if (borderBottom != BorderStyle.NONE) {
                    css.append(borderBottom.getBorderAttributeValue());
                    css.append(
                            colorConverter.getBorderColorCSS(BorderSide.BOTTOM,
                                    "border-bottom-color", borderFormatting));
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }

            // top and left borders might be applied to another cell, so store
            // them with a different index
            HashMap<Integer, String> _conditionalFormattingStyles = spreadsheet
                    .getConditionalFormattingStyles();
            if (isTopSet) {
                // bottom border for cell above
                final StringBuilder sb2 = new StringBuilder("border-bottom:");
                if (borderTop != BorderStyle.NONE) {
                    sb2.append(borderTop.getBorderAttributeValue());
                    sb2.append(colorConverter.getBorderColorCSS(BorderSide.TOP,
                            "border-bottom-color", borderFormatting));

                    _conditionalFormattingStyles.put(cssIndex, sb2.toString());
                    topBorders.put(cf, cssIndex++);
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }

            if (isLeftSet) {
                // right border for cell to the left
                final StringBuilder sb2 = new StringBuilder("border-right:");
                if (borderLeft != BorderStyle.NONE) {
                    sb2.append(borderLeft.getBorderAttributeValue());
                    sb2.append(colorConverter.getBorderColorCSS(BorderSide.LEFT,
                            "border-right-color", borderFormatting));

                    _conditionalFormattingStyles.put(cssIndex, sb2.toString());
                    leftBorders.put(cf, cssIndex++);
                } else {
                    css.append(BORDER_STYLE_DEFAULT);
                }
            }
            spreadsheet.setConditionalFormattingStyles(
                    _conditionalFormattingStyles);
        }

        return cssIndex;
    }

    /**
     * Checks if this rule has 'stop if true' defined.
     */
    private boolean stopHere(ConditionalFormattingRule rule) {
        if (rule instanceof XSSFConditionalFormattingRule) {

            // No POI API for this particular data, but it is present in XML.
            CTCfRule ctRule = (CTCfRule) getFieldValWithReflection(rule,
                    "_cfRule");
            if (ctRule != null) {
                return ctRule.getStopIfTrue();
            }
        }
        return false;
    }

    /**
     * Helper for the very common case of having to get underlying XML data.
     */
    private Object getFieldValWithReflection(Object owner, String fieldName) {
        Field f = null;
        Object val = null;
        try {
            f = owner.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);

            val = f.get(owner);
            return val;

        } catch (NoSuchFieldException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } catch (SecurityException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } catch (IllegalAccessException e) {
            LOGGER.error(
                    "Incompatible POI implementation, unable to parse conditional formatting rule",
                    e);
        } finally {
            if (f != null) {
                f.setAccessible(false);
            }
        }

        return null;
    }

    /**
     * @param i
     *            0 - left, 1 - top, 2 - right, 3 - bottom
     */
    private boolean isBorderSet(XSSFBorderFormatting borderFormatting,
            BorderSide b) {

        CTBorder ctBorder = (CTBorder) getFieldValWithReflection(
                borderFormatting, "_border");

        if (ctBorder == null) {
            return false;
        }

        switch (b) {
        case LEFT:
            return ctBorder.isSetLeft();
        case TOP:
            return ctBorder.isSetTop();
        case RIGHT:
            return ctBorder.isSetRight();
        case BOTTOM:
            return ctBorder.isSetBottom();
        case DIAGONAL:
            return ctBorder.isSetDiagonal();
        case HORIZONTAL:
            return ctBorder.isSetHorizontal();
        case VERTICAL:
            return ctBorder.isSetVertical();
        default:
            break;
        }

        return false;
    }

    /**
     * Checks if this formatting has strike-through enabled or not.
     */
    private boolean hasStrikeThrough(FontFormatting fontFormatting) {
        if (fontFormatting instanceof XSSFFontFormatting) {

            // No POI API for this particular data, but it is present in XML.

            CTFont font = (CTFont) getFieldValWithReflection(fontFormatting,
                    "_font");

            if (font == null) {
                return false;
            }

            List<CTBooleanProperty> strikeList = font.getStrikeList();

            if (strikeList != null) {
                for (CTBooleanProperty p : strikeList) {
                    if (p.getVal()) {
                        return true;
                    }
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
     *            The rule to be evaluated
     * @param classNameIndex
     *            The index of the class name that was generated for this rule,
     *            to be added to {@link #cellToIndex}
     */
    protected void runCellMatcher(ConditionalFormatting cf,
            ConditionalFormattingRule rule, int classNameIndex) {
        final int firstColumn = cf.getFormattingRanges()[0].getFirstColumn();
        final int firstRow = cf.getFormattingRanges()[0].getFirstRow();
        for (CellRangeAddress cra : cf.getFormattingRanges()) {

            for (int row = cra.getFirstRow(); row <= cra.getLastRow(); row++) {
                for (int col = cra.getFirstColumn(); col <= cra
                        .getLastColumn(); col++) {

                    Cell cell = spreadsheet.getCell(row, col);
                    if (cell == null) {
                        cell = spreadsheet.createCell(row, col, "");
                    }
                    if (matches(cell, rule, col - firstColumn,
                            row - firstRow)) {
                        Set<Integer> list = cellToIndex
                                .get(SpreadsheetUtil.toKey(cell));
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
                                list = cellToIndex
                                        .get(SpreadsheetUtil.toKey(cellToLeft));
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
                                list = cellToIndex
                                        .get(SpreadsheetUtil.toKey(cellOnTop));
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
     * @param cell
     *            Target cell
     * @param rule
     *            Conditional formatting rule to check against
     * @return Whether the given rule evaluates to <code>true</code> for the
     *         given cell.
     */
    protected boolean matches(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {
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
                return matchesValue(cell, rule, deltaColumn, deltaRow);
            } else {
                return matchesFormula(cell, rule, deltaColumn, deltaRow);
            }
        } catch (NotImplementedException e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        }

    }

    /**
     * Checks if the formula in the given rule evaluates to <code>true</code>.
     * <p>
     *
     * NOTE: Does not support HSSF files currently.
     *
     * @param cell
     *            Cell with conditional formatting
     * @param rule
     *            Conditional formatting rule based on formula
     * @return Formula value, if the formula is of boolean formula type Formula
     *         value != 0, if the formula is of numeric formula type and false
     *         otherwise
     */
    protected boolean matchesFormula(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {
        if (!(rule instanceof XSSFConditionalFormattingRule)) {
            // TODO Does not support HSSF files for now, since HSSF does not
            // read cell references in the file correctly.Since HSSF formulas
            // are read completely wrong, that boolean formula above is useless.
            return false;
        }
        String booleanFormula = rule.getFormula1();

        if (booleanFormula == null || booleanFormula.isEmpty()) {
            return false;
        }

        ValueEval eval = getValueEvalFromFormula(booleanFormula, cell,
                deltaColumn, deltaRow);

        if (eval instanceof ErrorEval) {
            LOGGER.trace(((ErrorEval) eval).getErrorString(), eval);
        }

        if (eval instanceof BoolEval) {
            return ((BoolEval) eval).getBooleanValue();
        } else {
            if (eval instanceof NumericValueEval) {
                return ((NumberEval) eval).getNumberValue() != 0;
            } else {
                return false;
            }
        }
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
            if (ptg instanceof RefPtgBase) {
                RefPtgBase ref = (RefPtgBase) ptg;
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

    /**
     * Checks if the given cell value matches a
     * {@link ConditionalFormattingRule} of <code>VALUE_IS</code> type. Covers
     * all cell types and comparison operations.
     *
     * @param cell
     *            Target cell
     * @param rule
     *            Conditional formatting rule to match against.
     * @param deltaColumn
     *            delta (on column axis) between cell and the origin cell
     * @param deltaRow
     *            delta (on row axis) between cell and the origin cell
     * @return True if the given cells value matches the given
     *         <code>VALUE_IS</code> rule, false otherwise
     */
    protected boolean matchesValue(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {

        boolean isFormulaType = cell.getCellType() == CellType.FORMULA;

        if (isFormulaType) {
            // make sure we have the latest value for formula cells
            getFormulaEvaluator().evaluateFormulaCell(cell);
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

        if (eval instanceof ErrorEval) {
            LOGGER.trace(((ErrorEval) eval).getErrorString(), eval);
            return false;
        }

        if (!hasCoherentType(eval, cell.getCellType(), isFormulaStringType,
                isFormulaBooleanType, isFormulaNumericType)) {
            // Comparison between different types (e.g. Bool vs String)
            return (comparisonOperation == ComparisonOperator.NOT_EQUAL);
        }

        // other than numerical types
        if (cell.getCellType() == CellType.STRING || isFormulaStringType) {

            String formulaValue = ((StringEval) eval).getStringValue();
            String stringValue = cell.getStringCellValue();

            // Excel string comparison ignores case
            switch (comparisonOperation) {
            case ComparisonOperator.EQUAL:
                return stringValue.equalsIgnoreCase(formulaValue);
            case ComparisonOperator.NOT_EQUAL:
                return !stringValue.equalsIgnoreCase(formulaValue);
            }
        }
        if (cell.getCellType() == CellType.BOOLEAN || isFormulaBooleanType) {
            // not sure if this is used, since no boolean option exists in
            // Excel..

            boolean formulaVal = ((BoolEval) eval).getBooleanValue();

            switch (comparisonOperation) {
            case ComparisonOperator.EQUAL:
                return cell.getBooleanCellValue() == formulaVal;
            case ComparisonOperator.NOT_EQUAL:
                return cell.getBooleanCellValue() != formulaVal;
            }
        }

        // numerical types
        if (cell.getCellType() == CellType.NUMERIC || isFormulaNumericType) {

            double formula1Val = ((NumericValueEval) eval).getNumberValue();

            switch (comparisonOperation) {

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
                boolean gt = cell.getNumericCellValue() <= Double
                        .valueOf(rule.getFormula2());
                return lt && gt;

            case ComparisonOperator.NOT_BETWEEN:
                lt = cell.getNumericCellValue() <= formula1Val;
                gt = cell.getNumericCellValue() >= Double
                        .valueOf(rule.getFormula2());
                return lt && gt;
            }
        }

        return false;
    }

    /**
     * @param eval
     *            Value of a formula
     * @param cellType
     *            Type of a cell
     * @param isFormulaStringType
     *            true if eval is a formula of type String, false otherwise
     * @param isFormulaBooleanType
     *            true if eval is a formula of type Boolean, false otherwise
     * @param isFormulaNumericType
     *            true if eval is a formula of type Numeric, false otherwise
     * @return true if eval is coherent with cellType, false otherwise
     */
    private boolean hasCoherentType(ValueEval eval, CellType cellType,
            boolean isFormulaStringType, boolean isFormulaBooleanType,
            boolean isFormulaNumericType) {
        switch (cellType) {
        case STRING:
            return eval instanceof StringEval;
        case BOOLEAN:
            return eval instanceof BoolEval;
        case NUMERIC:
            return eval instanceof NumericValueEval || isFormulaNumericType;
        case FORMULA:
            return isCoherentTypeFormula(eval, isFormulaStringType,
                    isFormulaBooleanType, isFormulaNumericType);
        default:
            return false;
        }
    }

    private boolean isCoherentTypeFormula(ValueEval eval,
            boolean isFormulaStringType, boolean isFormulaBooleanType,
            boolean isFormulaNumericType) {
        boolean coherentString = eval instanceof StringEval
                && isFormulaStringType;
        boolean coherentBoolean = eval instanceof BoolEval
                && isFormulaBooleanType;
        boolean coherentNumeric = eval instanceof NumericValueEval
                && isFormulaNumericType;
        return coherentString || coherentBoolean || coherentNumeric;
    }
}
