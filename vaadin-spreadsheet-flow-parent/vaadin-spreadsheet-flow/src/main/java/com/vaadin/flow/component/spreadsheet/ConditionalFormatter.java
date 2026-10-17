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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheetConditionalFormatting;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;

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
 * @since 23.1
 */
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

    protected ColorConverter colorConverter;

    private RuleBorderTracker borderTracker;
    private RuleEvaluator ruleEvaluator;

    /**
     * Constructs a new ConditionalFormatter targeting the given Spreadsheet.
     *
     * @param spreadsheet
     *            Target spreadsheet
     */
    public ConditionalFormatter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;

        final Workbook workbook = spreadsheet.getWorkbook();
        if (workbook instanceof HSSFWorkbook hssf) {
            colorConverter = new HSSFColorConverter(hssf);
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) workbook);
        }
        this.borderTracker = new RuleBorderTracker();
        this.ruleEvaluator = new RuleEvaluator(spreadsheet, this);
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
     * cells. The cells whose formats change are sent again.
     */
    public void createConditionalFormatterRules() {
        // The number formats of the rules come from POI's evaluator, which
        // keeps the rules and what they hold for: a value changed anywhere
        // can change that, and a rule removed would leave it one it can no
        // longer read.
        ConditionalFormattingEvaluator evaluator = spreadsheet
                .getConditionalFormattingEvaluator();
        evaluator.clearAllCachedFormats();
        evaluator.clearAllCachedValues();

        Map<String, Set<Integer>> before = cellToIndex;
        cellToIndex = new HashMap<>();
        borderTracker.clear();
        HashMap<Integer, String> conditionalFormattingStyles = new HashMap<>();

        SheetConditionalFormatting cfs = spreadsheet.getActiveSheet()
                .getSheetConditionalFormatting();

        if (cfs instanceof HSSFSheetConditionalFormatting) {
            // disable formatting for HSSF, since formulas are read incorrectly
            // and we would return incorrect results.
            return;
        }

        for (int i = 0; i < cfs.getNumConditionalFormattings(); i++) {
            ConditionalFormatting cf = cfs.getConditionalFormattingAt(i);

            List<XSSFConditionalFormattingRule> cfRuleList = POIReflectionTools
                    .getOrderedRuleList(cf);

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

                    // HSSF might return 255 for 'none'...
                    css.append(SpreadsheetStyleFactory.textDecoration(
                            fontFormatting.getUnderlineType() != Font.U_NONE
                                    && fontFormatting.getUnderlineType() != 255,
                            POIReflectionTools
                                    .hasStrikeThrough(fontFormatting)));

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

                cssIndex = borderTracker.addBorderFormatting(cf, rule, css,
                        cssIndex, conditionalFormattingStyles, colorConverter);

                conditionalFormattingStyles.put(cssIndex, css.toString());

                // check actual cells
                runCellMatcher(cf, rule, cssIndex);

                // stop here if defined in rules
                if (POIReflectionTools.stopHere(rule)) {
                    break;
                }
            }
        }

        sendCellsChanged(before);
        spreadsheet.setConditionalFormattingStyles(conditionalFormattingStyles);
    }

    /**
     * Sends again the cells whose formats changed: those that gained or lost a
     * format. Only those: sending the others again changes nothing on the
     * client, but building them again takes as long as the rest of an update on
     * a sheet with many formatted cells. Only sent: their values and styles are
     * as they were, and marked as updated, every formula reading them would be
     * worked out again.
     */
    private void sendCellsChanged(Map<String, Set<Integer>> before) {
        before.forEach((key, formats) -> {
            if (!formats.equals(cellToIndex.get(key))) {
                send(key);
            }
        });
        cellToIndex.keySet().forEach(key -> {
            if (!before.containsKey(key)) {
                send(key);
            }
        });
    }

    private void send(String key) {
        Cell cell = spreadsheet.getCell(SpreadsheetUtil.getRowFromKey(key) - 1,
                SpreadsheetUtil.getColumnIndexFromKey(key) - 1);
        if (cell != null) {
            spreadsheet.getCellValueManager().markCellForUpdate(cell);
        }
    }

    /**
     * Get the common {@link FormulaEvaluator} instance from {@link Spreadsheet}
     */
    protected FormulaEvaluator getFormulaEvaluator() {
        return spreadsheet.getFormulaEvaluator();
    }

    /**
     * Goes through the cells specified in the given formatting, and checks if
     * each rule matches. Style ids from resulting matches are recorded for each
     * cell, see {@link #getCellFormattingIndex(Cell)}.
     *
     * @param cf
     *            {@link ConditionalFormatting} that specifies the affected
     *            cells
     * @param rule
     *            The rule to be evaluated
     * @param classNameIndex
     *            The index of the class name that was generated for this rule,
     *            to be added to the style ids of the matching cells
     */
    protected void runCellMatcher(ConditionalFormatting cf,
            ConditionalFormattingRule rule, int classNameIndex) {
        final int firstColumn = cf.getFormattingRanges()[0].getFirstColumn();
        final int firstRow = cf.getFormattingRanges()[0].getFirstRow();
        // Only the cells the sheet shows: a rule over whole columns would
        // make a cell for each of their million rows, and grow the sheet to
        // show them all. Rows and columns added later are matched then.
        final int lastRowShown = spreadsheet.getRows() - 1;
        final int lastColumnShown = spreadsheet.getColumns() - 1;
        for (CellRangeAddress cra : cf.getFormattingRanges()) {
            int lastRow = Math.min(cra.getLastRow(), lastRowShown);
            int lastColumn = Math.min(cra.getLastColumn(), lastColumnShown);
            for (int row = cra.getFirstRow(); row <= lastRow; row++) {
                for (int col = cra.getFirstColumn(); col <= lastColumn; col++) {

                    Cell cell = spreadsheet.getCell(row, col);
                    if (cell == null) {
                        // A blank cell, to hang the format on. An empty
                        // string would be a value: a formula reading this
                        // cell would see text where it saw nothing, and a
                        // rule comparing against it would stop matching the
                        // next time the rules are built.
                        cell = spreadsheet.createCell(row, col, null);
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
                        if (borderTracker.hasLeftBorder(cf)) {
                            int ruleIndex = borderTracker.getLeftBorder(cf);

                            // left border for col 0 isn't rendered
                            if (col != 0) {
                                Cell cellToLeft = spreadsheet.getCell(row,
                                        col - 1);
                                if (cellToLeft == null) {
                                    cellToLeft = spreadsheet.createCell(row,
                                            col - 1, null);
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
                        if (borderTracker.hasTopBorder(cf)) {
                            int ruleIndex = borderTracker.getTopBorder(cf);

                            // top border for row 0 isn't rendered
                            if (row != 0) {
                                Cell cellOnTop = spreadsheet.getCell(row - 1,
                                        col);
                                if (cellOnTop == null) {
                                    cellOnTop = spreadsheet.createCell(row - 1,
                                            col, null);
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
     * rule. A {@code VALUE_IS} rule is checked with {@link #matchesValue}, any
     * other rule with {@link #matchesFormula}.
     *
     * @param cell
     *            Target cell
     * @param rule
     *            Conditional formatting rule to check against
     * @param deltaColumn
     *            delta (on column axis) between cell and the origin cell
     * @param deltaRow
     *            delta (on row axis) between cell and the origin cell
     * @return Whether the given rule evaluates to <code>true</code> for the
     *         given cell.
     */
    protected boolean matches(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {
        return ruleEvaluator.matches(cell, rule, deltaColumn, deltaRow);
    }

    /**
     * Checks if the formula in the given rule evaluates to <code>true</code>.
     * <p>
     * NOTE: Does not support HSSF files currently.
     *
     * @param cell
     *            Cell with conditional formatting
     * @param rule
     *            Conditional formatting rule based on formula
     * @param deltaColumn
     *            delta (on column axis) between cell and the origin cell
     * @param deltaRow
     *            delta (on row axis) between cell and the origin cell
     * @return Formula value, if the formula is of boolean formula type Formula
     *         value != 0, if the formula is of numeric formula type and false
     *         otherwise
     */
    protected boolean matchesFormula(Cell cell, ConditionalFormattingRule rule,
            int deltaColumn, int deltaRow) {
        return ruleEvaluator.matchesFormula(cell, rule, deltaColumn, deltaRow);
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
        return ruleEvaluator.matchesValue(cell, rule, deltaColumn, deltaRow);
    }

}
