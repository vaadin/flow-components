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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpreadsheetStyleFactory is an utility class for the Spreadsheet component.
 * This class handles converting Apache POI CellStyles to CSS styles.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 */
public class SpreadsheetStyleFactory implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpreadsheetStyleFactory.class);

    /**
     * Styling for cell borders
     *
     * @author Vaadin Ltd.
     */
    public enum BorderStyle {
        SOLID_THIN("solid", 1, 1),
        DOTTED_THIN("dotted", 1, 1),
        DASHED_THIN("dashed", 1, 1),
        SOLID_MEDIUM("solid", 2, 2),
        DASHED_MEDIUM("dashed", 2, 2),
        SOLID_THICK("solid", 3, 4),
        DOUBLE("double", 3, 4),
        NONE("none", 0, 0);

        private final int size;
        private final String borderStyle;

        BorderStyle(String borderStyle, int size, int adjustment) {
            this.borderStyle = borderStyle;
            this.size = size;
        }

        /**
         * Returns the CSS name of this border style
         *
         * @return CSS name of border style
         */
        public String getValue() {
            return borderStyle;
        }

        /**
         * Returns the thickness of this border
         *
         * @return Border thickness in PT
         */
        public int getSize() {
            return size;
        }

        /**
         * Returns the complete border attribute value for CSS
         *
         * @return Complete border attribute value
         */
        public String getBorderAttributeValue() {
            return borderStyle + " " + size + "pt;";
        }

    }

    private static final Map<HorizontalAlignment, String> ALIGN = mapFor(
            HorizontalAlignment.LEFT, "left", HorizontalAlignment.CENTER,
            "center", HorizontalAlignment.RIGHT, "right",
            HorizontalAlignment.FILL, "left", HorizontalAlignment.JUSTIFY,
            "left", HorizontalAlignment.CENTER_SELECTION, "center");

    private static final Map<VerticalAlignment, String> VERTICAL_ALIGN = mapFor(
            VerticalAlignment.BOTTOM, "flex-end", VerticalAlignment.CENTER,
            "center", VerticalAlignment.TOP, "flex-start");

    static final Map<org.apache.poi.ss.usermodel.BorderStyle, BorderStyle> BORDER = mapFor(
            org.apache.poi.ss.usermodel.BorderStyle.DASH_DOT,
            BorderStyle.DASHED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DASH_DOT_DOT,
            BorderStyle.DASHED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DASHED,
            BorderStyle.DASHED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DOTTED,
            BorderStyle.DOTTED_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.DOUBLE, BorderStyle.DOUBLE,
            org.apache.poi.ss.usermodel.BorderStyle.HAIR,
            BorderStyle.SOLID_THIN,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM,
            BorderStyle.SOLID_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASH_DOT,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASH_DOT_DOT,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASHED,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.NONE, BorderStyle.NONE,
            null, BorderStyle.NONE,
            org.apache.poi.ss.usermodel.BorderStyle.SLANTED_DASH_DOT,
            BorderStyle.DASHED_MEDIUM,
            org.apache.poi.ss.usermodel.BorderStyle.THICK,
            BorderStyle.SOLID_THICK,
            org.apache.poi.ss.usermodel.BorderStyle.THIN,
            BorderStyle.SOLID_THIN);

    private ColorConverter colorConverter;

    private Spreadsheet spreadsheet;

    private DefaultStyleBaseline defaultStyleBaseline;
    private FontStyleComposer fontStyleComposer;
    private CellBorderStyleBuilder borderStyleBuilder;

    /**
     * Constructs a new SpreadsheetStyleFactory for the given Spreadsheet
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public SpreadsheetStyleFactory(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        setupColorMap();
        this.defaultStyleBaseline = new DefaultStyleBaseline(spreadsheet);
        this.fontStyleComposer = new FontStyleComposer(spreadsheet,
                defaultStyleBaseline);
        this.borderStyleBuilder = new CellBorderStyleBuilder(spreadsheet,
                colorConverter);
    }

    /**
     * Reloads all sheet and cell styles from the current Workbook.
     */
    public void reloadWorkbookStyles() {
        final Workbook workbook = spreadsheet.getWorkbook();
        spreadsheet.cellStyleToCSSStyle = spreadsheet.getCellStyleToCSSStyle();
        if (spreadsheet.cellStyleToCSSStyle == null) {
            spreadsheet.cellStyleToCSSStyle = new HashMap<Integer, String>(
                    workbook.getNumCellStyles());
        } else {
            spreadsheet.cellStyleToCSSStyle.clear();
        }
        borderStyleBuilder.resetAll();

        // get default text alignments
        CellStyle cellStyle = workbook.getCellStyleAt((short) 0);
        defaultStyleBaseline.setDefaultTextAlign(cellStyle.getAlignment());
        // defaultVerticalAlign = cellStyle.getVerticalAlignment();

        // create default style (cell style 0)
        StringBuilder sb = new StringBuilder();
        borderStyleBuilder.appendBorderStyles(sb, cellStyle);
        defaultStyleBaseline.captureDefaultStyle(cellStyle, sb);
        colorConverter.defaultColorStyles(cellStyle, sb);
        spreadsheet.cellStyleToCSSStyle.put((int) cellStyle.getIndex(),
                sb.toString());

        // 0 is default style, create all styles indexed from 1 and upwards
        for (short i = 1; i < workbook.getNumCellStyles(); i++) {
            cellStyle = workbook.getCellStyleAt(i);
            addCellStyleCSS(cellStyle);
        }
        spreadsheet.setCellStyleToCSSStyle(spreadsheet.cellStyleToCSSStyle);
        reloadActiveSheetColumnRowStyles();
    }

    public void reloadActiveSheetColumnRowStyles() {
        final Workbook workbook = spreadsheet.getWorkbook();

        HashMap<Integer, Integer> _rowIndexToStyleIndex = spreadsheet
                .getRowIndexToStyleIndex();
        if (_rowIndexToStyleIndex == null) {
            _rowIndexToStyleIndex = new HashMap<Integer, Integer>(
                    workbook.getNumCellStyles());
        } else {
            _rowIndexToStyleIndex.clear();
        }
        HashMap<Integer, Integer> _columnIndexToStyleIndex = spreadsheet
                .getColumnIndexToStyleIndex();
        if (_columnIndexToStyleIndex == null) {
            _columnIndexToStyleIndex = new HashMap<Integer, Integer>(
                    workbook.getNumCellStyles());
        } else {
            _columnIndexToStyleIndex.clear();
        }
        Set<Integer> _lockedColumnIndexes = spreadsheet
                .getLockedColumnIndexes();
        if (_lockedColumnIndexes == null) {
            _lockedColumnIndexes = new HashSet<Integer>();
        } else {
            _lockedColumnIndexes.clear();
        }
        Set<Integer> _lockedRowIndexes = spreadsheet.getLockedRowIndexes();
        if (_lockedRowIndexes == null) {
            _lockedRowIndexes = new HashSet<Integer>();
        } else {
            _lockedRowIndexes.clear();
        }

        Sheet activeSheet = spreadsheet.getActiveSheet();
        for (int i = 0; i < spreadsheet.getRows(); i++) {
            Row row = activeSheet.getRow(i);
            if (row != null && row.getRowStyle() != null) {
                int styleIndex = row.getRowStyle().getIndex();
                _rowIndexToStyleIndex.put(i + 1, styleIndex);
                if (row.getRowStyle().getLocked()) {
                    _lockedRowIndexes.add(i + 1);
                }
            } else {
                if (spreadsheet.isActiveSheetProtected()) {
                    _lockedRowIndexes.add(i + 1);
                }
            }
        }

        for (int i = 0; i < spreadsheet.getColumns(); i++) {
            if (activeSheet.getColumnStyle(i) != null) {
                int styleIndex = activeSheet.getColumnStyle(i).getIndex();
                _columnIndexToStyleIndex.put(i + 1, styleIndex);
                if (activeSheet.getColumnStyle(i).getLocked()) {
                    _lockedColumnIndexes.add(i + 1);
                }
            }
        }

        spreadsheet.setLockedRowIndexes(_lockedRowIndexes);
        spreadsheet.setLockedColumnIndexes(_lockedColumnIndexes);
        spreadsheet.setColumnIndexToStyleIndex(_columnIndexToStyleIndex);
        spreadsheet.setRowIndexToStyleIndex(_rowIndexToStyleIndex);
    }

    /**
     * Creates a CellStyle to be used with hyperlinks
     *
     * @return A new hyperlink CellStyle
     */
    public CellStyle createHyperlinkCellStyle() {
        Workbook wb = spreadsheet.getWorkbook();
        CellStyle hlink_style = wb.createCellStyle();
        Font hlink_font = wb.createFont();
        hlink_font.setFontName(
                defaultStyleBaseline.getDefaultFont().getFontName());
        hlink_font.setFontHeightInPoints(
                defaultStyleBaseline.getDefaultFontHeightInPoints());
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);
        return hlink_style;
    }

    /**
     * Clears all styles for the given cell. Should be used when i.e. a cell has
     * been shifted (the old location is cleared of all styles).
     *
     * @param oldRowIndex
     *            0-based
     * @param oldColumnIndex
     *            0-based
     */
    public void clearCellStyle(int oldRowIndex, int oldColumnIndex) {
        final String cssSelector = ".col" + (oldColumnIndex + 1) + ".row"
                + (oldRowIndex + 1);
        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        borderStyleBuilder.removeCellFromShiftedStyles(_shiftedCellBorderStyles,
                cssSelector);
        borderStyleBuilder.removeCellFromInternalShiftedMaps(cssSelector);
        HashMap<String, String> mergedCellBorders = borderStyleBuilder
                .getMergedCellBorders();
        if (mergedCellBorders.containsKey(cssSelector)) {
            String rules = mergedCellBorders.remove(cssSelector);
            _shiftedCellBorderStyles.remove(borderStyleBuilder
                    .buildMergedCellBorderCss(cssSelector, rules));
        }
        spreadsheet.setShiftedCellBorderStyles(_shiftedCellBorderStyles);
    }

    /**
     * This should be called when a Cell's styling has been changed. This will
     * tell the Spreadsheet to send the change to the client side.
     *
     * @param cell
     *            Target cell
     * @param updateCustomBorders
     *            true to also update custom borders
     */
    public void cellStyleUpdated(Cell cell, boolean updateCustomBorders) {
        final String cssSelector = ".col" + (cell.getColumnIndex() + 1) + ".row"
                + (cell.getRowIndex() + 1);
        final Integer key = (int) cell.getCellStyle().getIndex();
        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        borderStyleBuilder.removeCellFromShiftedStyles(_shiftedCellBorderStyles,
                cssSelector);

        // remove the cell's new custom styles from state (will be added again
        // as this cell is styled)
        HashMap<Integer, String> shiftedBorderLeftStyles = borderStyleBuilder
                .getShiftedBorderLeftStyles();
        HashMap<Integer, String> shiftedBorderTopStyles = borderStyleBuilder
                .getShiftedBorderTopStyles();
        HashMap<String, String> mergedCellBorders = borderStyleBuilder
                .getMergedCellBorders();
        if (shiftedBorderLeftStyles.containsKey(key)) {
            final String style = shiftedBorderLeftStyles.get(key);
            _shiftedCellBorderStyles.remove(style);
        }
        if (shiftedBorderTopStyles.containsKey(key)) {
            final String style = shiftedBorderTopStyles.get(key);
            _shiftedCellBorderStyles.remove(style);
        }
        if (mergedCellBorders.containsKey(cssSelector)) {
            final String style = borderStyleBuilder.buildMergedCellBorderCss(
                    cssSelector, mergedCellBorders.remove(cssSelector));
            _shiftedCellBorderStyles.remove(style);
        }

        // Most cells are given a style the client has already: only a style
        // whose CSS is new or has changed goes out again.
        CellStyle cellStyle = cell.getCellStyle();
        if (addCellStyleCSS(cellStyle)) {
            spreadsheet.setCellStyleToCSSStyle(
                    spreadsheet.getCellStyleToCSSStyle());
        }

        borderStyleBuilder.resetAccumulators();
        // custom styles
        borderStyleBuilder.processCustomStyling(cell);
        borderStyleBuilder.flattenAccumulators();
        if (updateCustomBorders) {
            if (shiftedBorderLeftStyles.containsKey(key)) {
                final String style = shiftedBorderLeftStyles.get(key);
                _shiftedCellBorderStyles.add(style);
            }
            if (shiftedBorderTopStyles.containsKey(key)) {
                final String style = shiftedBorderTopStyles.get(key);
                _shiftedCellBorderStyles.add(style);
            }
            if (mergedCellBorders.containsKey(cssSelector)) {
                _shiftedCellBorderStyles.add(
                        borderStyleBuilder.buildMergedCellBorderCss(cssSelector,
                                mergedCellBorders.get(cssSelector)));
            }
        }
        spreadsheet.setShiftedCellBorderStyles(_shiftedCellBorderStyles);
    }

    /**
     * Sets the custom border styles to shared state for sending them to the
     * client side.
     */
    public void loadCustomBorderStylesToState() {
        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        if (_shiftedCellBorderStyles != null) {
            _shiftedCellBorderStyles.clear();
        } else {
            _shiftedCellBorderStyles = new ArrayList<String>();
        }
        borderStyleBuilder
                .appendShiftedStylesToOutput(_shiftedCellBorderStyles);
        spreadsheet.setShiftedCellBorderStyles(_shiftedCellBorderStyles);
    }

    /**
     * Reloads all styles for the currently active sheet.
     */
    public void reloadActiveSheetCellStyles() {
        // need to remove the cell identifiers (css selectors from the shifted
        // border style rules
        borderStyleBuilder.stripLeadingSelectorsFromShiftedMaps();
        borderStyleBuilder.getMergedCellBorders().clear();

        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        if (_shiftedCellBorderStyles == null) {
            _shiftedCellBorderStyles = new ArrayList<String>();
        } else {
            _shiftedCellBorderStyles.clear();
        }

        borderStyleBuilder.resetAccumulators();

        for (Row row : spreadsheet.getActiveSheet()) {
            for (Cell cell : row) {
                borderStyleBuilder.processCustomStyling(cell);
            }
        }
        borderStyleBuilder.flattenAccumulators();

        borderStyleBuilder
                .appendShiftedStylesToOutput(_shiftedCellBorderStyles);

        // conditional formatting
        spreadsheet.getConditionalFormatter().createConditionalFormatterRules();

        spreadsheet.setShiftedCellBorderStyles(_shiftedCellBorderStyles);
    }

    @SuppressWarnings({ "unchecked" })
    private static <K, V> Map<K, V> mapFor(Object... mapping) {
        Map<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < mapping.length; i += 2) {
            map.put((K) mapping[i], (V) mapping[i + 1]);
        }
        return map;
    }

    /**
     * Puts the style's CSS in the style map. Leaves sending the map to the
     * caller: each send serializes the whole map, and a workbook's styles are
     * added one by one - 20,000 of them took the server half a minute when each
     * was sent on its own.
     *
     * @return whether the map changed: the style is new, or its CSS is
     */
    private boolean addCellStyleCSS(CellStyle cellStyle) {

        if (cellStyle.getIndex() == 0) {
            // default cell style, do not change.
            return false;
        }

        StringBuilder sb = new StringBuilder();

        fontStyleComposer.appendFontStyle(sb, cellStyle);
        colorConverter.colorStyles(cellStyle, sb);
        borderStyleBuilder.appendBorderStyles(sb, cellStyle);
        if (cellStyle.getAlignment() != defaultStyleBaseline
                .getDefaultTextAlign()) {
            styleOut(sb, "text-align", cellStyle.getAlignment(), ALIGN);
            // TODO For correct overflow, rtl should be used for right align
            // if (cellStyle.getAlignment() == ALIGN_RIGHT) {
            // sb.append("direction:rtl;");
            // }
        }

        // excel default is bottom, so that is what we have in the CSS base
        // files.
        // TODO This only works on modern (10+) IE.
        styleOut(sb, "justify-content", cellStyle.getVerticalAlignment(),
                VERTICAL_ALIGN);

        if (cellStyle.getWrapText()) { // default is to overflow
            sb.append(
                    "overflow:hidden;white-space:normal;word-wrap:break-word;");
        }

        if (cellStyle.getIndention() > 0) {
            sb.append("padding-left: " + cellStyle.getIndention() + "em;");
        }

        String css = sb.toString();
        return !css.equals(spreadsheet.getCellStyleToCSSStyle()
                .put((int) cellStyle.getIndex(), css));
    }

    private <K> void styleOut(StringBuilder sb, String attr, K key,
            Map<K, String> mapping) {
        String value = mapping.get(key);
        if (value != null) {
            sb.append(attr);
            sb.append(":");
            sb.append(value);
            sb.append(";");
        }
    }

    private void setupColorMap() {
        final Workbook workbook = spreadsheet.getWorkbook();
        if (workbook instanceof HSSFWorkbook hssf) {
            colorConverter = new HSSFColorConverter(hssf);
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) workbook);
        }
    }
}
