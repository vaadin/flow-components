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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * SpreadsheetStyleFactory is an utility class for the Spreadsheet component.
 * This class handles converting Apache POI CellStyles to CSS styles.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SpreadsheetStyleFactory implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpreadsheetStyleFactory.class);

    /**
     * Styling for cell borders
     *
     * @author Vaadin Ltd.
     */
    public enum BorderStyle {
        SOLID_THIN("solid", 1, 1), DOTTED_THIN("dotted", 1, 1), DASHED_THIN(
                "dashed", 1, 1), SOLID_MEDIUM("solid", 2, 2), DASHED_MEDIUM(
                        "dashed", 2, 2), SOLID_THICK("solid", 3,
                                4), DOUBLE("double", 3, 4), NONE("none", 0, 0);

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

    /** CellStyle index to selector + style map */
    private final HashMap<Integer, String> shiftedBorderTopStyles = new HashMap<Integer, String>();
    /** CellStyle index to selector + style map */
    private final HashMap<Integer, String> shiftedBorderLeftStyles = new HashMap<Integer, String>();
    /** */
    private final HashMap<String, String> mergedCellBorders = new HashMap<String, String>();

    /**
     * Temp structure for shiftedBorderTopStyles that keeps track of the
     * association between: StyleId to ColumnId to Rows
     */
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> shiftedBorderTopStylesMap = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();

    /**
     * Temp structure for shiftedBorderLeftStyles that keeps track of the
     * association between: StyleId to ColumnId to Rows
     */
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> shiftedBorderLeftStylesMap = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();

    private ColorConverter colorConverter;

    private Spreadsheet spreadsheet;

    private Font defaultFont;

    private HorizontalAlignment defaultTextAlign;

    private short defaultFontHeightInPoints;

    private String defaultFontFamily;

    /**
     * Constructs a new SpreadsheetStyleFactory for the given Spreadsheet
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public SpreadsheetStyleFactory(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        setupColorMap();
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
        shiftedBorderLeftStyles.clear();
        shiftedBorderTopStyles.clear();
        mergedCellBorders.clear();

        // get default text alignments
        CellStyle cellStyle = workbook.getCellStyleAt((short) 0);
        defaultTextAlign = cellStyle.getAlignment();
        // defaultVerticalAlign = cellStyle.getVerticalAlignment();

        // create default style (cell style 0)
        StringBuilder sb = new StringBuilder();
        borderStyles(sb, cellStyle);
        defaultFontStyle(cellStyle, sb);
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
        hlink_font.setFontName(defaultFont.getFontName());
        hlink_font.setFontHeightInPoints(defaultFontHeightInPoints);
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
        // remove/modify all possible old custom styles that the cell had (can
        // be found from state)
        ArrayList<String> add = new ArrayList<String>();
        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        Iterator<String> iterator = _shiftedCellBorderStyles.iterator();
        while (iterator.hasNext()) {
            String style = iterator.next();
            // only cell with this style -> remove
            if (style.startsWith(cssSelector + "{")) {
                iterator.remove();
            } else if (style.contains(cssSelector)) { // shifted borders
                iterator.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace(cssSelector + "{", "{");
                }
                if (!style.startsWith("{")) {
                    add.add(style);
                }
            }
        }
        for (String s : add) {
            _shiftedCellBorderStyles.add(s);
        }

        HashMap<Integer, String> add2 = new HashMap<Integer, String>();
        Iterator<Entry<Integer, String>> iterator2 = shiftedBorderLeftStyles
                .entrySet().iterator();
        while (iterator2.hasNext()) {
            Entry<Integer, String> entry = iterator2.next();
            String style = entry.getValue();
            if (style.contains(cssSelector)) { // shifted borders
                iterator2.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the only
                    style = style.replace(cssSelector + "{", "{");
                }
                add2.put(entry.getKey(), style);
            }
        }
        shiftedBorderLeftStyles.putAll(add2);
        add2.clear();

        iterator2 = shiftedBorderTopStyles.entrySet().iterator();
        while (iterator2.hasNext()) {
            Entry<Integer, String> entry = iterator2.next();
            String style = entry.getValue();
            if (style.contains(cssSelector)) { // shifted borders
                iterator2.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the only
                    style = style.replace(cssSelector + "{", "{");
                }
                add2.put(entry.getKey(), style);
            }
        }
        shiftedBorderTopStyles.putAll(add2);
        add2.clear();

        if (mergedCellBorders.containsKey(cssSelector)) {
            String rules = mergedCellBorders.remove(cssSelector);
            _shiftedCellBorderStyles
                    .remove(buildMergedCellBorderCSS(cssSelector, rules));
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
        // remove/modify all possible old custom styles that the cell had (can
        // be found from state)
        ArrayList<String> add = new ArrayList<String>();
        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        Iterator<String> iterator = _shiftedCellBorderStyles.iterator();
        while (iterator.hasNext()) {
            String style = iterator.next();
            // only cell with this style -> remove
            if (style.startsWith(cssSelector + "{")) {
                iterator.remove();
            } else if (style.contains(cssSelector)) { // shifted borders
                iterator.remove();
                int index = style.indexOf(cssSelector);
                if (index > 0) { // doesn't start with the selector
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the last
                    style = style.replace("," + cssSelector + "{", "{");
                } else {
                    style = style.replace(cssSelector + ",", "");
                    // in case it is the only
                    style = style.replace(cssSelector + "{", "{");
                }
                if (!style.startsWith(",") && !style.startsWith("{")) {
                    add.add(style);
                }
            }
        }

        for (String s : add) {
            _shiftedCellBorderStyles.add(s);
        }
        // remove the cell's new custom styles from state (will be added again
        // as this cell is styled)
        if (shiftedBorderLeftStyles.containsKey(key)) {
            final String style = shiftedBorderLeftStyles.get(key);
            _shiftedCellBorderStyles.remove(style);
        }
        if (shiftedBorderTopStyles.containsKey(key)) {
            final String style = shiftedBorderTopStyles.get(key);
            _shiftedCellBorderStyles.remove(style);
        }
        if (mergedCellBorders.containsKey(cssSelector)) {
            final String style = buildMergedCellBorderCSS(cssSelector,
                    mergedCellBorders.remove(cssSelector));
            _shiftedCellBorderStyles.remove(style);
        }

        // TODO May need optimizing since the client side might already have
        // this cell style
        CellStyle cellStyle = cell.getCellStyle();
        addCellStyleCSS(cellStyle);

        shiftedBorderTopStylesMap.clear();
        shiftedBorderLeftStylesMap.clear();
        // custom styles
        doCellCustomStyling(cell);
        updateStyleMap(shiftedBorderLeftStylesMap, shiftedBorderLeftStyles);
        updateStyleMap(shiftedBorderTopStylesMap, shiftedBorderTopStyles);
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
                _shiftedCellBorderStyles.add(buildMergedCellBorderCSS(
                        cssSelector, mergedCellBorders.get(cssSelector)));
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
        for (String value : shiftedBorderLeftStyles.values()) {
            if (value.startsWith(".col")) {
                _shiftedCellBorderStyles.add(value);
            }
        }
        for (String value : shiftedBorderTopStyles.values()) {
            if (value.startsWith(".col")) {
                _shiftedCellBorderStyles.add(value);
            }
        }
        for (Entry<String, String> entry : mergedCellBorders.entrySet()) {
            _shiftedCellBorderStyles.add(
                    buildMergedCellBorderCSS(entry.getKey(), entry.getValue()));

        }
        spreadsheet.setShiftedCellBorderStyles(_shiftedCellBorderStyles);
    }

    /**
     * Reloads all styles for the currently active sheet.
     */
    public void reloadActiveSheetCellStyles() {
        // need to remove the cell identifiers (css selectors from the shifted
        // border style rules
        for (Entry<Integer, String> entry : shiftedBorderLeftStyles
                .entrySet()) {
            String value = entry.getValue();
            if (value.startsWith(".col")) {
                shiftedBorderLeftStyles.put(entry.getKey(),
                        value.substring(value.indexOf("{")));
            }
        }
        for (Entry<Integer, String> entry : shiftedBorderTopStyles.entrySet()) {
            String value = entry.getValue();
            if (value.startsWith(".col")) {
                shiftedBorderTopStyles.put(entry.getKey(),
                        value.substring(value.indexOf("{")));
            }
        }
        mergedCellBorders.clear();

        ArrayList<String> _shiftedCellBorderStyles = spreadsheet
                .getShiftedCellBorderStyles();
        if (_shiftedCellBorderStyles == null) {
            _shiftedCellBorderStyles = new ArrayList<String>();
        } else {
            _shiftedCellBorderStyles.clear();
        }

        shiftedBorderTopStylesMap.clear();
        shiftedBorderLeftStylesMap.clear();

        for (Row row : spreadsheet.getActiveSheet()) {
            for (Cell cell : row) {
                doCellCustomStyling(cell);
            }
        }
        updateStyleMap(shiftedBorderLeftStylesMap, shiftedBorderLeftStyles);
        updateStyleMap(shiftedBorderTopStylesMap, shiftedBorderTopStyles);

        for (String value : shiftedBorderLeftStyles.values()) {
            if (value.startsWith(".col")) {
                _shiftedCellBorderStyles.add(value);
            }
        }
        for (String value : shiftedBorderTopStyles.values()) {
            if (value.startsWith(".col")) {
                _shiftedCellBorderStyles.add(value);
            }
        }
        for (Entry<String, String> entry : mergedCellBorders.entrySet()) {
            _shiftedCellBorderStyles.add(
                    buildMergedCellBorderCSS(entry.getKey(), entry.getValue()));

        }

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

    private void addCellStyleCSS(CellStyle cellStyle) {

        if (cellStyle.getIndex() == 0) {
            // default cell style, do not change.
            return;
        }

        StringBuilder sb = new StringBuilder();

        fontStyle(sb, cellStyle);
        colorConverter.colorStyles(cellStyle, sb);
        borderStyles(sb, cellStyle);
        if (cellStyle.getAlignment() != defaultTextAlign) {
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

        HashMap<Integer, String> _cellStyleToCSSStyle = spreadsheet
                .getCellStyleToCSSStyle();
        _cellStyleToCSSStyle.put((int) cellStyle.getIndex(), sb.toString());
        spreadsheet.setCellStyleToCSSStyle(_cellStyleToCSSStyle);
    }

    private String buildMergedCellBorderCSS(String selector, String rules) {
        if (selector.endsWith(",")) {
            selector = selector.substring(0, selector.length() - 1);
        }
        if (selector.length() < 1) {
            selector = ".notusedselector";
        }
        StringBuilder sb = new StringBuilder(selector);
        sb.append("{");
        sb.append(rules);
        sb.append("}");
        return sb.toString();
    }

    private void doCellCustomStyling(final Cell cell) {
        CellStyle cellStyle = cell.getCellStyle();
        final Integer key = (int) cellStyle.getIndex();
        if (key == 0) { // default style
            return;
        }

        // merged regions have their borders in edge cells that are "invisible"
        // inside the region -> right and bottom cells need to be transfered to
        // the actual merged cell
        final int columnIndex = cell.getColumnIndex();
        final int rowIndex = cell.getRowIndex();

        if (spreadsheet.isColumnHidden(columnIndex)
                || spreadsheet.isRowHidden(rowIndex)) {
            return;
        }

        MergedRegion region = spreadsheet.mergedRegionContainer
                .getMergedRegion((columnIndex + 1), (rowIndex + 1));
        if (region != null) {
            final String borderRight = getBorderRightStyle(cellStyle);
            final String borderBottom = getBorderBottomStyle(cellStyle);
            if ((borderRight != null && !borderRight.isEmpty())
                    || (borderBottom != null && !borderBottom.isEmpty())) {
                StringBuilder sb = new StringBuilder(".col");
                sb.append(region.col1);
                sb.append(".row");
                sb.append(region.row1);
                final String cssKey = sb.toString();
                final String currentBorders = mergedCellBorders.get(cssKey);
                StringBuilder style;
                if (currentBorders != null && !currentBorders.isEmpty()) {
                    style = new StringBuilder(currentBorders);
                } else {
                    style = new StringBuilder();
                }
                if (borderRight != null && !borderRight.isEmpty()
                        && (currentBorders == null
                                || !currentBorders.contains("border-right"))) {
                    style.append(borderRight);
                }
                if (borderBottom != null && !borderBottom.isEmpty()
                        && (currentBorders == null
                                || !currentBorders.contains("border-bottom"))) {
                    style.append(borderBottom);
                }
                final String newBorders = style.toString();
                if (!newBorders.isEmpty()) {
                    mergedCellBorders.put(cssKey, newBorders);
                }
            }

        }

        // only take transfered borders into account on the (possible) merged
        // regions edges
        if (region == null || region.col1 == (columnIndex + 1)
                || region.col2 == (columnIndex + 1)
                || region.row1 == (rowIndex + 1)
                || region.row2 == (rowIndex + 1)) {

            if (shiftedBorderLeftStyles.containsKey(key)) {
                // need to add the border right style to previous cell on
                // left, which might be a merged cell
                if (columnIndex > 0) {
                    int row, col;

                    int upperVisibleColumnIndex = columnIndex;
                    while (spreadsheet
                            .isColumnHidden(upperVisibleColumnIndex - 1)) {
                        upperVisibleColumnIndex--;
                    }

                    MergedRegion previousRegion = spreadsheet.mergedRegionContainer
                            .getMergedRegion(upperVisibleColumnIndex,
                                    rowIndex + 1);

                    if (previousRegion != null) {
                        col = previousRegion.col1;
                        row = previousRegion.row1;
                    } else {
                        col = upperVisibleColumnIndex;
                        row = rowIndex + 1;
                    }
                    insertMapEntryIfNeeded(shiftedBorderLeftStylesMap, key, row,
                            col);
                }
            }
            if (shiftedBorderTopStyles.containsKey(key)) {
                // need to add the border bottom style to cell on previous
                // row, which might be a merged cell
                if (rowIndex > 0) {
                    int row, col;

                    int prevVisibleRowIndex = rowIndex;
                    while (spreadsheet.isRowHidden(prevVisibleRowIndex - 1)) {
                        prevVisibleRowIndex--;
                    }

                    MergedRegion previousRegion = spreadsheet.mergedRegionContainer
                            .getMergedRegion(columnIndex + 1,
                                    prevVisibleRowIndex);

                    if (previousRegion != null) {
                        col = previousRegion.col1;
                        row = previousRegion.row1;
                    } else {
                        col = columnIndex + 1;
                        row = prevVisibleRowIndex;
                    }
                    insertMapEntryIfNeeded(shiftedBorderTopStylesMap, key, row,
                            col);

                }
            }

        }
    }

    private void insertMapEntryIfNeeded(
            HashMap<Integer, HashMap<Integer, HashSet<Integer>>> shiftedBorderStylesMap,
            int key, int row, int col) {
        HashMap<Integer, HashSet<Integer>> colToRowMap = shiftedBorderStylesMap
                .get(key);
        if (colToRowMap == null) {
            colToRowMap = new HashMap<Integer, HashSet<Integer>>();
            shiftedBorderStylesMap.put(key, colToRowMap);
        }
        HashSet<Integer> rows = colToRowMap.get(col);
        if (rows == null) {
            rows = new HashSet<Integer>();
            colToRowMap.put(col, rows);
        }
        rows.add(row);
    }

    private void updateStyleMap(
            HashMap<Integer, HashMap<Integer, HashSet<Integer>>> shiftedBorderStylesMap,
            HashMap<Integer, String> shiftedBorderStyles) {
        for (Entry<Integer, HashMap<Integer, HashSet<Integer>>> currentEntry : shiftedBorderStylesMap
                .entrySet()) {
            Integer key = currentEntry.getKey();
            HashMap<Integer, HashSet<Integer>> colToRows = currentEntry
                    .getValue();

            if (colToRows != null) {
                String value = shiftedBorderStyles.get(key);
                StringBuilder sb = new StringBuilder();
                boolean somethingAdded = false;
                for (Entry<Integer, HashSet<Integer>> colToRowEntry : colToRows
                        .entrySet()) {
                    Integer col = colToRowEntry.getKey();
                    for (Integer row : colToRowEntry.getValue()) {
                        if (somethingAdded) {
                            sb.append(",");
                        } else {
                            somethingAdded = true;
                        }
                        sb.append(".col").append(col);
                        sb.append(".row").append(row);
                    }

                }
                sb.append(value);

                shiftedBorderStyles.put(key, sb.toString());
            }
        }
    }

    private void defaultFontStyle(CellStyle cellStyle, StringBuilder sb) {
        if (cellStyle.getIndex() == 0) {
            defaultFont = spreadsheet.getWorkbook()
                    .getFontAt(cellStyle.getFontIndexAsInt());
            defaultFontFamily = styleFontFamily(defaultFont);
            sb.append(defaultFontFamily);
            if (defaultFont.getBold()) {
                sb.append("font-weight:bold;");
            }
            if (defaultFont.getItalic()) {
                sb.append("font-style:italic;");
            }
            defaultFontHeightInPoints = defaultFont.getFontHeightInPoints();
            sb.append("font-size:");
            sb.append(defaultFontHeightInPoints);
            sb.append("pt;");
            if (defaultFont.getUnderline() != Font.U_NONE) {
                sb.append("text-decoration:underline;");
            } else if (defaultFont.getStrikeout()) {
                sb.append("text-decoration:overline;");
            }
        }
    }

    private void fontStyle(StringBuilder sb, CellStyle cellStyle) {
        try {
            Font font = spreadsheet.getWorkbook()
                    .getFontAt(cellStyle.getFontIndexAsInt());
            if (font.getIndexAsInt() == defaultFont.getIndexAsInt()) {
                // uses default font, no need to add styles
                return;
            }
            String fontFamily = styleFontFamily(font);
            if (!fontFamily.equals(defaultFontFamily)) {
                sb.append(fontFamily);
            }
            if (font.getBold()) {
                sb.append("font-weight:bold;");
            }
            if (font.getItalic()) {
                sb.append("font-style:italic;");
            }
            final int fontheight = font.getFontHeightInPoints();
            if (fontheight != defaultFontHeightInPoints) {
                sb.append("font-size:");
                sb.append(fontheight);
                sb.append("pt;");
            }
            if (font.getUnderline() != Font.U_NONE) {
                sb.append("text-decoration:underline;");
            } else if (font.getStrikeout()) {
                sb.append("text-decoration:overline;");
            }
        } catch (IndexOutOfBoundsException ioobe) {
            // somehow workbook doesn't have all the fonts the cells have???
            LOGGER.warn(
                    "Font missing, " + cellStyle.getFontIndexAsInt() + " / "
                            + cellStyle.getClass() + ", " + ioobe.getMessage(),
                    ioobe);
        }
    }

    private String styleFontFamily(Font font) {
        StringBuilder sb = new StringBuilder();
        sb.append("font-family:");
        String fontName = font.getFontName();
        if (fontName.contains(" ")) {
            sb.append("\"");
            sb.append(fontName);
            sb.append("\",");
        } else {
            sb.append(fontName);
            sb.append(",");
        }
        if (font instanceof XSSFFont) {
            FontFamily family = FontFamily
                    .valueOf(((XSSFFont) font).getFamily());
            switch (family) {
            case ROMAN:
                sb.append("roman,");
                break;
            case SWISS:
                sb.append("swiss,");
                break;
            case MODERN:
                sb.append("modern,");
                break;
            case SCRIPT:
                sb.append("script,");
                break;
            case DECORATIVE:
                sb.append("decorative,");
                break;
            case NOT_APPLICABLE:
                break;
            default:
                break;
            }
        }
        sb.append("Helvetica,arial;");
        return sb.toString();
    }

    private String getBorderRightStyle(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();
        BorderStyle borderRight = BORDER.get(cellStyle.getBorderRight());
        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = ((XSSFCellStyle) cellStyle).getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();
            if (borderRight == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetRight()
                        ? ct.getRight().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderRight = BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }
            }
        }
        if (borderRight != BorderStyle.NONE) {
            sb.append("border-right:");
            sb.append(borderRight.getBorderAttributeValue());
            sb.append(colorConverter.getBorderColorCSS(BorderSide.RIGHT,
                    "border-right-color", cellStyle));
        }
        return sb.toString();
    }

    private String getBorderBottomStyle(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();
        BorderStyle borderBottom = BORDER.get(cellStyle.getBorderBottom());
        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = ((XSSFCellStyle) cellStyle).getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();
            if (borderBottom == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetBottom()
                        ? ct.getBottom().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderBottom = BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }
            }
        }
        if (borderBottom != BorderStyle.NONE) {
            sb.append("border-bottom:");
            sb.append(borderBottom.getBorderAttributeValue());
            sb.append(colorConverter.getBorderColorCSS(BorderSide.BOTTOM,
                    "border-bottom-color", cellStyle));
        }
        return sb.toString();
    }

    private void borderStyles(StringBuilder sb, CellStyle cellStyle) {

        BorderStyle borderLeft = BORDER.get(cellStyle.getBorderLeft());
        BorderStyle borderRight = BORDER.get(cellStyle.getBorderRight());
        BorderStyle borderTop = BORDER.get(cellStyle.getBorderTop());
        BorderStyle borderBottom = BORDER.get(cellStyle.getBorderBottom());

        if (cellStyle instanceof XSSFCellStyle
                && !((XSSFCellStyle) cellStyle).getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = ((XSSFCellStyle) cellStyle).getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();

            if (borderLeft == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetLeft()
                        ? ct.getLeft().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderLeft = BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }
            }
            if (borderRight == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetRight()
                        ? ct.getRight().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderRight = BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }
            }
            if (borderBottom == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetBottom()
                        ? ct.getBottom().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderBottom = BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }
            }
            if (borderTop == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderTop = BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }

            }
        }

        if (borderRight != BorderStyle.NONE) {
            sb.append("border-right:");
            sb.append(borderRight.getBorderAttributeValue());
            sb.append(colorConverter.getBorderColorCSS(BorderSide.RIGHT,
                    "border-right-color", cellStyle));
        }
        if (borderBottom != BorderStyle.NONE) {
            sb.append("border-bottom:");
            sb.append(borderBottom.getBorderAttributeValue());
            sb.append(colorConverter.getBorderColorCSS(BorderSide.BOTTOM,
                    "border-bottom-color", cellStyle));
        }

        // the top and right borders are transferred to previous cells
        if (borderTop != BorderStyle.NONE || borderLeft != BorderStyle.NONE) {
            if (borderTop != BorderStyle.NONE) {
                final StringBuilder sb2 = new StringBuilder("{border-bottom:");
                sb2.append(borderTop.getBorderAttributeValue());
                sb2.append(colorConverter.getBorderColorCSS(BorderSide.TOP,
                        "border-bottom-color", cellStyle));
                sb2.append("}");
                shiftedBorderTopStyles.put((int) cellStyle.getIndex(),
                        sb2.toString());
            }
            if (borderLeft != BorderStyle.NONE) {
                final StringBuilder sb2 = new StringBuilder("{border-right:");
                sb2.append(borderLeft.getBorderAttributeValue());
                sb2.append(colorConverter.getBorderColorCSS(BorderSide.LEFT,
                        "border-right-color", cellStyle));
                sb2.append("}");
                shiftedBorderLeftStyles.put((int) cellStyle.getIndex(),
                        sb2.toString());
            }
        }
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
        if (workbook instanceof HSSFWorkbook) {
            colorConverter = new HSSFColorConverter((HSSFWorkbook) workbook);
        } else {
            colorConverter = new XSSFColorConverter((XSSFWorkbook) workbook);
        }
    }
}