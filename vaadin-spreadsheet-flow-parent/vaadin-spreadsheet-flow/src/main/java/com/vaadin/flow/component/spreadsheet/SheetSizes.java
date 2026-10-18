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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand.Type;

/**
 * The heights of the rows and the widths of the columns of the sheet shown, and
 * which of them are hidden: read from the sheet when it is shown, changed by
 * the application and the user, written to the sheet, and given to the client.
 * <p>
 * Rows and columns count from 0 here, as in POI. The client counts from 1: the
 * hidden rows and columns it is given, and the sizes it reports, are counted
 * anew where they cross.
 * <p>
 * Every change ends in {@link #refresh}, the one place that brings up to date
 * what the sizes affect.
 */
final class SheetSizes implements Serializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(SheetSizes.class);

    /**
     * The widest column POI holds, 255 characters, in the whole pixels POI
     * reads it as.
     */
    static final int MAX_COLUMN_WIDTH_PX = (int) (255
            * Units.DEFAULT_CHARACTER_WIDTH);

    /** The units POI holds a column width in: 1/256 of a character. */
    private static final int UNITS_PER_CHARACTER = 256;

    /** The width of a filter button. */
    private static final int FILTER_BUTTON_PIXEL_WIDTH = 14;

    /** The space between a filter button and the text of its cell. */
    private static final int FILTER_BUTTON_PIXEL_PADDING = 2;

    private final Spreadsheet spreadsheet;

    private float defRowH;
    private int defColW;
    private float[] rowH;
    private int[] colW;
    private final TreeSet<Integer> hiddenRows = new TreeSet<>();
    private final TreeSet<Integer> hiddenColumns = new TreeSet<>();

    SheetSizes(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    // ----- what the sizes are -------------------------------------------

    float defaultRowHeight() {
        return defRowH;
    }

    int defaultColumnWidth() {
        return defColW;
    }

    float[] rowHeights() {
        return rowH;
    }

    int[] columnWidths() {
        return colW;
    }

    /** The rows hidden, counted from 1, as the client counts them. */
    ArrayList<Integer> hiddenRowsFromOne() {
        return fromOne(hiddenRows);
    }

    /** The columns hidden, counted from 1, as the client counts them. */
    ArrayList<Integer> hiddenColumnsFromOne() {
        return fromOne(hiddenColumns);
    }

    /**
     * Shows the rows at the given heights: the loader of the components grows
     * the rows that hold one.
     */
    void setRowHeights(float[] heights) {
        rowH = heights;
        spreadsheet.getElement().setProperty("rowH",
                Serializer.serialize(heights == null ? null : runs(heights)));
    }

    /**
     * A copy of the row heights long enough to hold the row: the heights stop
     * at the rows the sheet had when it was loaded, and
     * {@link Spreadsheet#setMaxRows(int)} can add more, which take the heights
     * loading gives them.
     */
    float[] heightsCovering(int row) {
        float[] heights = Arrays.copyOf(rowH, Math.max(rowH.length, row + 1));
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int r = rowH.length; r < heights.length; r++) {
            heights[r] = rowHeightShown(sheet, r);
        }
        return heights;
    }

    // ----- loading a sheet ----------------------------------------------

    /**
     * The default height of the rows of the sheet shown, which a sheet without
     * one is given.
     */
    void loadDefaultRowHeight(Sheet sheet) {
        float height = sheet.getDefaultRowHeightInPoints();
        if (height <= 0) {
            height = SpreadsheetFactory.DEFAULT_ROW_HEIGHT_POINTS;
            sheet.setDefaultRowHeightInPoints(height);
        }
        setDefaultRowHeightShown(height);
    }

    /**
     * The width of the columns without a width of their own: that of the
     * component, unless the application set another, whatever the default width
     * of the sheet, which is left as it is.
     */
    void loadDefaultColumnWidth() {
        if (defColW <= 0) {
            setDefaultColumnWidthShown(defaultColumnWidthInPixels());
        }
    }

    /**
     * Loads the size of the sheet: how many rows and columns it has, at least
     * the default counts, and their heights and widths.
     */
    void load(Sheet sheet) {
        int rows = Math.max(sheet.getLastRowNum() + 1,
                spreadsheet.getDefaultRowCount());
        int cols = spreadsheet.getDefaultColumnCount();
        for (Row row : sheet) {
            cols = Math.max(cols, row.getLastCellNum());
        }
        spreadsheet.setRows(rows);
        spreadsheet.setCols(cols);
        readAll(sheet, rows, cols);
    }

    /**
     * Reads the rows of the sheet from the first to the last again: their
     * heights, and which of them are hidden. A shift or a delete moves and
     * removes rows in POI behind them.
     */
    void readRows(int first, int last) {
        Sheet sheet = spreadsheet.getActiveSheet();
        float[] heights = heightsCovering(last);
        for (int row = first; row <= last; row++) {
            heights[row] = readRow(sheet, row);
        }
        setRowHeights(heights);
        publishHiddenRows();
    }

    /**
     * The sheet shows more rows or columns than it has sizes for: the sizes of
     * those added are read, as loading the sheet reads them. Rows and columns
     * no longer shown keep their sizes, which are not sent again.
     */
    void grown() {
        Sheet sheet = spreadsheet.getActiveSheet();
        int rows = spreadsheet.getRows();
        if (rowH.length < rows) {
            float[] heights = Arrays.copyOf(rowH, rows);
            for (int row = rowH.length; row < rows; row++) {
                heights[row] = readRow(sheet, row);
            }
            setRowHeights(heights);
            publishHiddenRows();
        }
        int cols = spreadsheet.getColumns();
        if (colW.length < cols) {
            int[] widths = Arrays.copyOf(colW, cols);
            for (int column = colW.length; column < cols; column++) {
                widths[column] = readColumn(sheet, column);
            }
            setColumnWidths(widths);
            publishHiddenColumns();
        }
    }

    /**
     * A group of rows or columns was collapsed or expanded in POI: the rows and
     * columns the client has are read again, and what they affect is brought up
     * to date.
     */
    void groupToggled() {
        readAll(spreadsheet.getActiveSheet(), rowH.length, colW.length);
        refresh(true, true);
    }

    private void readAll(Sheet sheet, int rows, int cols) {
        hiddenRows.clear();
        float[] heights = new float[rows];
        for (int row = 0; row < rows; row++) {
            heights[row] = readRow(sheet, row);
        }
        hiddenColumns.clear();
        int[] widths = new int[cols];
        for (int column = 0; column < cols; column++) {
            widths[column] = readColumn(sheet, column);
        }
        setRowHeights(heights);
        publishHiddenRows();
        setColumnWidths(widths);
        publishHiddenColumns();
    }

    /** The height of the row as the sheet holds it, its being hidden noted. */
    private float readRow(Sheet sheet, int row) {
        Row poiRow = sheet.getRow(row);
        if (poiRow != null && poiRow.getZeroHeight()) {
            hiddenRows.add(row);
        } else {
            hiddenRows.remove(row);
        }
        return rowHeightShown(sheet, row);
    }

    /**
     * The width of the column as the sheet holds it, its being hidden noted:
     * the columns are read after the hidden ones are forgotten, or when only
     * their default width changed.
     */
    private int readColumn(Sheet sheet, int column) {
        if (sheet.isColumnHidden(column)) {
            hiddenColumns.add(column);
        }
        return columnWidthShown(sheet, column, defColW);
    }

    // ----- rows ---------------------------------------------------------

    void setRowHeight(int row, float height) {
        refresh(true, applyHeight(row, height));
    }

    void setRowHidden(int row, boolean hidden) {
        applyRowHidden(row, hidden);
        refresh(true, true);
    }

    /**
     * Hides or shows the rows, as a filter does, before any refresh. The client
     * is sent the heights and the hidden rows once for all of them: a filter
     * passes every row of its table.
     */
    void setRowsHidden(Map<Integer, Boolean> rowsHidden) {
        if (!rowsHidden.isEmpty()) {
            float[] heights = heightsCovering(
                    Collections.max(rowsHidden.keySet()));
            rowsHidden.forEach(
                    (row, hidden) -> markRowHidden(heights, row, hidden));
            publishHiddenRows();
            setRowHeights(heights);
        }
        refresh(true, true);
    }

    void setDefaultRowHeight(float height) {
        Sheet sheet = spreadsheet.getActiveSheet();
        sheet.setDefaultRowHeightInPoints(height);
        setDefaultRowHeightShown(height);
        // The rows without a height of their own are shown at it.
        readRows(0, rowH.length - 1);
        refresh(true, false);
    }

    /** The user resized rows, which the client counts from 1. */
    void onClientRowsResized(Map<Integer, Float> heightsFromOne) {
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                Type.ROW);
        command.captureValues(heightsFromOne.keySet().toArray(new Integer[0]));
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        boolean visibilityChanged = false;
        for (Map.Entry<Integer, Float> resized : heightsFromOne.entrySet()) {
            visibilityChanged |= applyHeight(resized.getKey() - 1,
                    resized.getValue());
        }
        refresh(true, visibilityChanged);
    }

    /** Whether the row is hidden by the height, or shown by it. */
    private boolean applyHeight(int row, float height) {
        if (height == 0f) {
            applyRowHidden(row, true);
            return true;
        }
        boolean shown = hiddenRows.remove(row);
        Row poiRow = poiRow(row);
        // A row given a height is shown.
        poiRow.setZeroHeight(false);
        poiRow.setHeightInPoints(height);
        float[] heights = heightsCovering(row);
        heights[row] = height;
        publishHiddenRows();
        setRowHeights(heights);
        return shown;
    }

    private void applyRowHidden(int row, boolean hidden) {
        float[] heights = heightsCovering(row);
        markRowHidden(heights, row, hidden);
        publishHiddenRows();
        setRowHeights(heights);
    }

    /**
     * Hides or shows the row in the sheet and in the heights given, which cover
     * it, sending the client nothing yet.
     */
    private void markRowHidden(float[] heights, int row, boolean hidden) {
        Row poiRow = poiRow(row);
        poiRow.setZeroHeight(hidden);
        if (hidden) {
            hiddenRows.add(row);
        } else {
            hiddenRows.remove(row);
        }
        heights[row] = hidden ? 0f : poiRow.getHeightInPoints();
    }

    private Row poiRow(int row) {
        Sheet sheet = spreadsheet.getActiveSheet();
        Row poiRow = sheet.getRow(row);
        return poiRow == null ? sheet.createRow(row) : poiRow;
    }

    // ----- columns ------------------------------------------------------

    void setColumnWidth(int column, int width) {
        refresh(false, applyWidth(column, width));
    }

    void setColumnHidden(int column, boolean hidden) {
        applyColumnHidden(column, hidden);
        refresh(false, true);
    }

    /** Hides or shows the columns, as a group does, before any refresh. */
    void setColumnsHidden(Map<Integer, Boolean> columnsHidden) {
        columnsHidden.forEach(this::applyColumnHidden);
        refresh(false, true);
    }

    void setDefaultColumnWidth(int width) {
        int[] before = colW;
        setDefaultColumnWidthShown(width);
        // The columns without a width of their own are shown at it.
        Sheet sheet = spreadsheet.getActiveSheet();
        int[] widths = new int[before.length];
        for (int column = 0; column < widths.length; column++) {
            widths[column] = readColumn(sheet, column);
        }
        setColumnWidths(widths);
        for (int column = 0; column < widths.length; column++) {
            if (widths[column] != before[column]) {
                cellsResized(column);
            }
        }
        refresh(false, false);
    }

    /** The user resized columns, which the client counts from 1. */
    void onClientColumnsResized(Map<Integer, Integer> widthsFromOne) {
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                Type.COLUMN);
        command.captureValues(widthsFromOne.keySet().toArray(new Integer[0]));
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        boolean visibilityChanged = false;
        for (Map.Entry<Integer, Integer> resized : widthsFromOne.entrySet()) {
            visibilityChanged |= applyWidth(resized.getKey() - 1,
                    resized.getValue());
        }
        refresh(false, visibilityChanged);
    }

    /** Whether the column is hidden by the width, or shown by it. */
    private boolean applyWidth(int column, int width) {
        if (width == 0) {
            applyColumnHidden(column, true);
            return true;
        }
        // No wider than POI holds, so the sheet and the workbook agree.
        width = Math.min(width, MAX_COLUMN_WIDTH_PX);
        Sheet sheet = spreadsheet.getActiveSheet();
        boolean shown = hiddenColumns.remove(column);
        if (sheet.isColumnHidden(column)) {
            sheet.setColumnHidden(column, false);
        }
        int[] widths = widthsCovering(column);
        widths[column] = width;
        setColumnWidths(widths);
        publishHiddenColumns();
        sheet.setColumnWidth(column, columnWidthUnits(sheet, width));
        if (sheet instanceof XSSFSheet xssfSheet) {
            // POI leaves the columns of a range it splits out of order, which
            // Excel does not read.
            xssfSheet.getColumnHelper().cleanColumns();
        }
        cellsResized(column);
        return shown;
    }

    private void applyColumnHidden(int column, boolean hidden) {
        Sheet sheet = spreadsheet.getActiveSheet();
        int units = sheet.getColumnWidth(column);
        sheet.setColumnHidden(column, hidden);
        if (sheet.getColumnWidth(column) != units) {
            // HSSF gives a column it hides a width of its own, when it has
            // none: the column keeps the width it has.
            sheet.setColumnWidth(column, units);
        }
        int[] widths = widthsCovering(column);
        boolean changed = false;
        if (hidden && hiddenColumns.add(column)) {
            widths[column] = 0;
            changed = true;
        } else if (!hidden && hiddenColumns.remove(column)) {
            widths[column] = columnWidthShown(sheet, column, defColW);
            changed = true;
        }
        publishHiddenColumns();
        setColumnWidths(widths);
        if (changed) {
            // Its cells are built again at the width they have now: none,
            // when they are not measured, or the width it is shown at.
            cellsResized(column);
        } else {
            spreadsheet.getCellValueManager().resendTextRunningInto(column);
        }
    }

    /**
     * The cells of a column that changed width, and the text running into it,
     * are sent again: how far text runs over depends on the width.
     */
    private void cellsResized(int column) {
        spreadsheet.getCellValueManager().clearCacheForColumn(column + 1);
        spreadsheet.getCellValueManager().loadCellData(
                spreadsheet.getFirstRow(), column + 1, spreadsheet.getLastRow(),
                column + 1);
        spreadsheet.getCellValueManager().resendTextRunningInto(column);
    }

    /**
     * A copy of the column widths long enough to hold the column: the widths
     * stop at the columns the sheet had when it was loaded, and
     * {@link Spreadsheet#setMaxColumns(int)} can add more, which take the
     * widths loading gives them.
     */
    private int[] widthsCovering(int column) {
        int[] widths = Arrays.copyOf(colW, Math.max(colW.length, column + 1));
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int c = colW.length; c < widths.length; c++) {
            widths[c] = columnWidthShown(sheet, c, defColW);
        }
        return widths;
    }

    // ----- autofit ------------------------------------------------------

    /**
     * The user autofitted a column, which the history can undo, through the
     * component, so an application that overrides
     * {@link Spreadsheet#autofitColumn(int)} sees it too.
     */
    void onClientAutofit(int column) {
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                Type.COLUMN);
        command.captureValues(new Integer[] { column + 1 });
        spreadsheet.autofitColumn(column);
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
    }

    /**
     * Fits the column to its text, with room for the button of a filter in it,
     * and keeps the width it was fitted to, for a filter put over it to fit it
     * again.
     */
    void autofit(int column) {
        Sheet sheet = spreadsheet.getActiveSheet();
        try {
            sheet.autoSizeColumn(column);
        } catch (NullPointerException e) {
            // POI's measuring of text can fail where fonts are missing: the
            // column keeps its width.
            LOGGER.warn("Could not autofit column {}", column + 1, e);
            return;
        }
        int width = withRoomForFilterButton(column,
                (int) sheet.getColumnWidthInPixels(column));
        spreadsheet.workbookScope().autofittedWidths.put(
                new CellReference(sheet.getSheetName(), 0, column, true, true),
                width);
        // Through the component, so an override applies.
        spreadsheet.setColumnWidth(column, width);
    }

    /**
     * A filter was put over the table: its columns that were autofitted, and
     * not resized since, are fitted again, with room for its buttons.
     */
    void refreshAutofittedColumnsInTable(SpreadsheetFilterTable table) {
        Sheet sheet = table.getSheet();
        CellRangeAddress region = table.getFullTableRegion();
        for (int column = region.getFirstColumn(); column <= region
                .getLastColumn(); column++) {
            Integer autofitted = spreadsheet.workbookScope().autofittedWidths
                    .get(new CellReference(sheet.getSheetName(), 0, column,
                            true, true));
            if (autofitted != null && autofitted == (int) sheet
                    .getColumnWidthInPixels(column)) {
                spreadsheet.autofitColumn(column);
            }
        }
    }

    /**
     * The width, with room for the button of a filter in the column, which is
     * in the header row of its table, so the button does not cover the text.
     */
    private int withRoomForFilterButton(int column, int width) {
        for (SpreadsheetTable table : spreadsheet.getTablesForActiveSheet()) {
            if (table instanceof SpreadsheetFilterTable
                    && table.getPopupButton(column) != null) {
                return width + FILTER_BUTTON_PIXEL_WIDTH
                        + FILTER_BUTTON_PIXEL_PADDING;
            }
        }
        return width;
    }

    // ----- the one refresh ----------------------------------------------

    /**
     * Brings up to date what the sizes affect: the rows with components, when
     * rows changed height, and the overlays over the rows and columns; when
     * rows or columns were hidden or shown, the comments, left out of hidden
     * ones, the borders drawn past them, and for rows the formulas, which can
     * leave hidden rows out.
     */
    private void refresh(boolean rowsChanged, boolean visibilityChanged) {
        if (rowsChanged) {
            // A row with a component given a height is grown for it again.
            spreadsheet.components().regrowRows();
        }
        // The overlays are sized from the rows and columns under them.
        spreadsheet.overlays().loadOrUpdate();
        if (visibilityChanged) {
            spreadsheet.cellCommentLoader.load();
        }
        if (rowsChanged && visibilityChanged) {
            // Formulas such as SUBTOTAL(109, ...) leave hidden rows out: the
            // values, and the formatting that follows them, are worked out
            // again.
            spreadsheet.getFormulaEvaluator().clearAllCachedResultValues();
            spreadsheet.updateMarkedCells();
        }
        if (visibilityChanged) {
            // A border on the top or the left of a cell is drawn on the cell
            // before it, past the hidden rows and columns.
            spreadsheet.getSpreadsheetStyleFactory()
                    .reloadActiveSheetCellStyles();
        }
    }

    // ----- the client's copy --------------------------------------------

    private void setColumnWidths(int[] widths) {
        colW = widths;
        spreadsheet.getElement().setProperty("colW",
                Serializer.serialize(widths == null ? null : runs(widths)));
    }

    private void setDefaultRowHeightShown(float height) {
        defRowH = height;
        spreadsheet.getElement().setProperty("defRowH", height);
    }

    private void setDefaultColumnWidthShown(int width) {
        defColW = width;
        spreadsheet.getElement().setProperty("defColW", width);
    }

    private void publishHiddenRows() {
        spreadsheet.getElement().setProperty("hiddenRowIndexes",
                Serializer.serialize(hiddenRowsFromOne()));
    }

    private void publishHiddenColumns() {
        spreadsheet.getElement().setProperty("hiddenColumnIndexes",
                Serializer.serialize(hiddenColumnsFromOne()));
    }

    private static ArrayList<Integer> fromOne(TreeSet<Integer> fromZero) {
        ArrayList<Integer> fromOne = new ArrayList<>(fromZero.size());
        for (int index : fromZero) {
            fromOne.add(index + 1);
        }
        return fromOne;
    }

    /**
     * Row heights and column widths as they go to the client: runs of equal
     * sizes, {@code {"runs": [size, count, size, count, ...]}}. Nearly all are
     * the same: sent one by one, the heights of the 1,048,576 rows of the
     * largest sheet came to 5.6 MB, and went again whenever one changed.
     */
    static Map<String, List<Number>> runs(float[] sizes) {
        List<Number> runs = new ArrayList<>();
        int i = 0;
        while (i < sizes.length) {
            float size = sizes[i];
            int start = i;
            while (i < sizes.length && Float.compare(sizes[i], size) == 0) {
                i++;
            }
            runs.add(size);
            runs.add(i - start);
        }
        return Map.of("runs", runs);
    }

    /** As {@link #runs(float[])}, for whole pixels. */
    static Map<String, List<Number>> runs(int[] sizes) {
        List<Number> runs = new ArrayList<>();
        int i = 0;
        while (i < sizes.length) {
            int size = sizes[i];
            int start = i;
            while (i < sizes.length && sizes[i] == size) {
                i++;
            }
            runs.add(size);
            runs.add(i - start);
        }
        return Map.of("runs", runs);
    }

    // ----- the rules ----------------------------------------------------

    /** The default width of the component, 10 characters, in pixels. */
    static int defaultColumnWidthInPixels() {
        return (int) (SpreadsheetFactory.DEFAULT_COL_WIDTH_UNITS
                * Units.DEFAULT_CHARACTER_WIDTH);
    }

    /**
     * The height a row is shown at, in points: none when it is hidden, its own
     * height in the sheet, or the default height of the sheet when the sheet
     * has no such row.
     */
    static float rowHeightShown(Sheet sheet, int row) {
        Row poiRow = sheet.getRow(row);
        if (poiRow == null) {
            return sheet.getDefaultRowHeightInPoints();
        }
        return poiRow.getZeroHeight() ? 0f : poiRow.getHeightInPoints();
    }

    /**
     * The width a column is shown at, in pixels: none when it is hidden, its
     * own width in the sheet, or the default width given when it has none of
     * its own, whatever the default width of the sheet.
     */
    static int columnWidthShown(Sheet sheet, int column, int defaultWidth) {
        if (sheet.isColumnHidden(column)) {
            return 0;
        }
        return hasOwnWidth(sheet, column)
                ? (int) sheet.getColumnWidthInPixels(column)
                : defaultWidth;
    }

    private static boolean hasOwnWidth(Sheet sheet, int column) {
        if (sheet instanceof XSSFSheet xssfSheet) {
            CTCol col = xssfSheet.getColumnHelper().getColumn(column, false);
            return col != null && col.isSetWidth();
        }
        // HSSF reads a column of just the default width as one without a
        // width of its own, which is all it tells them apart by.
        return sheet.getColumnWidth(column) != sheet.getDefaultColumnWidth()
                * UNITS_PER_CHARACTER;
    }

    /**
     * Converts a column width in pixels to the units POI holds it in, so that
     * POI reads the width back as the same pixels: the client is given the
     * width a fresh load reads.
     */
    static int columnWidthUnits(Sheet sheet, int pixels) {
        // POI reads the units as a fraction of a pixel, which is cut to whole
        // pixels: the fewest units that reach the pixels.
        int units = (int) Math.ceil(
                pixels * UNITS_PER_CHARACTER / Units.DEFAULT_CHARACTER_WIDTH);
        // HSSF reads just the default width of the sheet as a column of the
        // default width, which is drawn wider. A unit more is read as the same
        // pixels, on any sheet.
        if (units == sheet.getDefaultColumnWidth() * UNITS_PER_CHARACTER) {
            units++;
        }
        return units;
    }
}
