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
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

import com.vaadin.flow.component.spreadsheet.SpreadsheetStyleFactory.BorderStyle;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Builds and maintains the per-cell-style CSS for borders, including the
 * "transferred border" caches that route top / left borders to the bottom /
 * right of the previous cell (Excel's drawing convention is to render those
 * borders on the adjacent cell, not the source cell).
 *
 * <p>
 * Owns five maps:
 *
 * <ul>
 * <li>{@code shiftedBorderTopStyles}, {@code shiftedBorderLeftStyles} —
 * cell-style index → ready-to-emit CSS rule for the cells that should pick up
 * the transferred bottom / right border.</li>
 * <li>{@code shiftedBorderTopStylesMap}, {@code shiftedBorderLeftStylesMap} —
 * temp accumulators used during a styling pass to collect the row / column
 * index lists that need the transferred border, before being flattened into CSS
 * selectors via {@link #flattenAccumulators()}.</li>
 * <li>{@code mergedCellBorders} — CSS selector (the merged region's top-left
 * cell) → border CSS rules that need to apply to it (right / bottom borders of
 * cells inside the merged region that would otherwise be invisible inside the
 * region).</li>
 * </ul>
 *
 * <p>
 * The bulk of the CSS-composition logic lives here; the parent
 * {@link SpreadsheetStyleFactory} orchestrates the higher-level methods
 * ({@code clearCellStyle}, {@code cellStyleUpdated},
 * {@code reloadActiveSheetCellStyles}, {@code loadCustomBorderStylesToState})
 * but delegates the cache-touching work to this class.
 */
final class CellBorderStyleBuilder implements Serializable {

    private final Spreadsheet spreadsheet;
    private final ColorConverter colorConverter;

    private final HashMap<Integer, String> shiftedBorderTopStyles = new HashMap<Integer, String>();
    private final HashMap<Integer, String> shiftedBorderLeftStyles = new HashMap<Integer, String>();
    private final HashMap<String, String> mergedCellBorders = new HashMap<String, String>();
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> shiftedBorderTopStylesMap = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
    private final HashMap<Integer, HashMap<Integer, HashSet<Integer>>> shiftedBorderLeftStylesMap = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();

    CellBorderStyleBuilder(Spreadsheet spreadsheet,
            ColorConverter colorConverter) {
        this.spreadsheet = spreadsheet;
        this.colorConverter = colorConverter;
    }

    HashMap<Integer, String> getShiftedBorderTopStyles() {
        return shiftedBorderTopStyles;
    }

    HashMap<Integer, String> getShiftedBorderLeftStyles() {
        return shiftedBorderLeftStyles;
    }

    HashMap<String, String> getMergedCellBorders() {
        return mergedCellBorders;
    }

    /**
     * Resets all five caches. Called by {@code reloadWorkbookStyles} before a
     * fresh styling pass.
     */
    void resetAll() {
        shiftedBorderTopStyles.clear();
        shiftedBorderLeftStyles.clear();
        mergedCellBorders.clear();
        shiftedBorderTopStylesMap.clear();
        shiftedBorderLeftStylesMap.clear();
    }

    /**
     * Resets only the two temp accumulators. Called at the start of an
     * incremental cell-styling pass (a single cell update or a sheet-wide
     * reload) — the final {@code shiftedBorder*Styles} maps are preserved
     * across passes so the next call can find them.
     */
    void resetAccumulators() {
        shiftedBorderTopStylesMap.clear();
        shiftedBorderLeftStylesMap.clear();
    }

    /**
     * Appends border-related CSS to {@code sb} for {@code cellStyle}. As a side
     * effect, the top and left borders are routed into
     * {@code shiftedBorderTopStyles} / {@code shiftedBorderLeftStyles} keyed by
     * the cell-style index, so a later pass can render them on the previous row
     * / column.
     */
    void appendBorderStyles(StringBuilder sb, CellStyle cellStyle) {
        BorderStyle borderLeft = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderLeft());
        BorderStyle borderRight = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderRight());
        BorderStyle borderTop = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderTop());
        BorderStyle borderBottom = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderBottom());

        if (cellStyle instanceof XSSFCellStyle xssfCellStyle
                && !xssfCellStyle.getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = xssfCellStyle.getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();

            if (borderLeft == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetLeft()
                        ? ct.getLeft().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderLeft = SpreadsheetStyleFactory.BORDER
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
                    borderRight = SpreadsheetStyleFactory.BORDER
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
                    borderBottom = SpreadsheetStyleFactory.BORDER
                            .get(org.apache.poi.ss.usermodel.BorderStyle
                                    .valueOf(key));
                }
            }
            if (borderTop == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderTop = SpreadsheetStyleFactory.BORDER
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

    /**
     * Per-cell custom-styling pass: handles merged-region border transfer, and
     * accumulates per-style row / column index lists in the temp maps for later
     * flattening.
     */
    void processCustomStyling(Cell cell) {
        CellStyle cellStyle = cell.getCellStyle();
        final Integer key = (int) cellStyle.getIndex();
        if (key == 0) { // default style
            return;
        }

        // merged regions have their borders in edge cells that are "invisible"
        // inside the region -> right and bottom cells need to be transferred to
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

        // only take transferred borders into account on the (possible) merged
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

    /**
     * After {@link #processCustomStyling} has accumulated the temp maps,
     * flattens them into the final {@code shiftedBorderTopStyles} /
     * {@code shiftedBorderLeftStyles} CSS selectors.
     */
    void flattenAccumulators() {
        updateStyleMap(shiftedBorderLeftStylesMap, shiftedBorderLeftStyles);
        updateStyleMap(shiftedBorderTopStylesMap, shiftedBorderTopStyles);
    }

    /**
     * Drops the per-cell selector entries from a list of existing shifted-cell
     * border CSS rules, used by {@code clearCellStyle} and
     * {@code cellStyleUpdated}. Each entry in {@code shiftedCellBorderStyles}
     * is a CSS rule string of the form {@code "<sel>{...}"} or
     * {@code "<sel>,<sel>,...{...}"}; if the cell's selector appears in any
     * entry, that selector is removed from the entry, and entries containing
     * only the cell's selector are removed entirely.
     */
    void removeCellFromShiftedStyles(ArrayList<String> shiftedCellBorderStyles,
            String cssSelector) {
        ArrayList<String> add = new ArrayList<String>();
        Iterator<String> iterator = shiftedCellBorderStyles.iterator();
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
                if (!style.startsWith(",") && !style.startsWith("{")) {
                    add.add(style);
                }
            }
        }
        shiftedCellBorderStyles.addAll(add);
    }

    /**
     * Drops the per-cell selector entries from {@code shiftedBorderLeftStyles}
     * and {@code shiftedBorderTopStyles}, used by {@code clearCellStyle}. The
     * map values' format is {@code "<selectors>{<rules>}"} — same selector
     * splitting rules as {@link #removeCellFromShiftedStyles}.
     */
    void removeCellFromInternalShiftedMaps(String cssSelector) {
        removeFromMap(shiftedBorderLeftStyles, cssSelector);
        removeFromMap(shiftedBorderTopStyles, cssSelector);
    }

    private static void removeFromMap(HashMap<Integer, String> map,
            String cssSelector) {
        HashMap<Integer, String> add = new HashMap<Integer, String>();
        Iterator<Entry<Integer, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, String> entry = iterator.next();
            String style = entry.getValue();
            if (style.contains(cssSelector)) { // shifted borders
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
                add.put(entry.getKey(), style);
            }
        }
        map.putAll(add);
    }

    /**
     * Strips a leading {@code .col<n>.row<m>} (or comma-separated list of them)
     * from each map value, leaving just the CSS rule body. Used by
     * {@code reloadActiveSheetCellStyles} as the first step of a sheet reload —
     * the per-cell selector lists are about to be rebuilt from scratch by
     * {@link #processCustomStyling}, so any old ones are stale.
     */
    void stripLeadingSelectorsFromShiftedMaps() {
        stripLeadingSelectors(shiftedBorderLeftStyles);
        stripLeadingSelectors(shiftedBorderTopStyles);
    }

    private static void stripLeadingSelectors(HashMap<Integer, String> map) {
        for (Entry<Integer, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (value.startsWith(".col")) {
                map.put(entry.getKey(), value.substring(value.indexOf("{")));
            }
        }
    }

    /**
     * Wraps {@code rules} in a CSS block with {@code selector} as the selector.
     * Handles a few edge cases: trailing-comma stripping, empty selector
     * replaced with {@code .notusedselector}.
     */
    String buildMergedCellBorderCss(String selector, String rules) {
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

    /**
     * Appends the contents of the three border-related caches into
     * {@code outShiftedCellBorderStyles} as final CSS rule strings. Only
     * entries whose value starts with {@code .col} (i.e. already-flattened CSS
     * selectors) are emitted; the rule-body-only entries (which start with
     * {@code "{"}) are kept in the cache for the next flatten pass.
     */
    void appendShiftedStylesToOutput(
            ArrayList<String> outShiftedCellBorderStyles) {
        for (String value : shiftedBorderLeftStyles.values()) {
            if (value.startsWith(".col")) {
                outShiftedCellBorderStyles.add(value);
            }
        }
        for (String value : shiftedBorderTopStyles.values()) {
            if (value.startsWith(".col")) {
                outShiftedCellBorderStyles.add(value);
            }
        }
        for (Entry<String, String> entry : mergedCellBorders.entrySet()) {
            outShiftedCellBorderStyles.add(
                    buildMergedCellBorderCss(entry.getKey(), entry.getValue()));
        }
    }

    private String getBorderRightStyle(CellStyle cellStyle) {
        StringBuilder sb = new StringBuilder();
        BorderStyle borderRight = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderRight());
        if (cellStyle instanceof XSSFCellStyle xssfCellStyle
                && !xssfCellStyle.getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = xssfCellStyle.getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();
            if (borderRight == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetRight()
                        ? ct.getRight().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderRight = SpreadsheetStyleFactory.BORDER
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
        BorderStyle borderBottom = SpreadsheetStyleFactory.BORDER
                .get(cellStyle.getBorderBottom());
        if (cellStyle instanceof XSSFCellStyle xssfCellStyle
                && !xssfCellStyle.getCoreXf().getApplyBorder()) {
            // borders from theme are not working in POI 3.9
            final CTXf _cellXf = xssfCellStyle.getCoreXf();
            int idx = (int) _cellXf.getBorderId();
            CTBorder ct = ((XSSFWorkbook) spreadsheet.getWorkbook())
                    .getStylesSource().getBorderAt(idx).getCTBorder();
            if (borderBottom == BorderStyle.NONE) {
                STBorderStyle.Enum ptrn = ct.isSetBottom()
                        ? ct.getBottom().getStyle()
                        : null;
                if (ptrn != null) {
                    Short key = (short) (ptrn.intValue() - 1);
                    borderBottom = SpreadsheetStyleFactory.BORDER
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

    private static void insertMapEntryIfNeeded(
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

    private static void updateStyleMap(
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

}
