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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand.Type;

/**
 * Sets column widths and column visibility on the active sheet. Owns the
 * entanglement between {@code width == 0} and "column is hidden" — setting
 * width to zero hides the column, and explicitly hiding clears its width
 * mirror.
 */
final class ColumnSizeOperations implements Serializable {

    private final Spreadsheet spreadsheet;

    ColumnSizeOperations(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void setWidth(int index, int width) {
        applyWidth(index, width);
        if (width == 0) {
            spreadsheet.reloadSheetStyles(false, true);
        }
    }

    void setWidths(Map<Integer, Integer> columnIndexToWidth) {
        AtomicBoolean isAnyHidden = new AtomicBoolean();
        columnIndexToWidth.forEach((key, value) -> {
            isAnyHidden.set(value == 0);
            applyWidth(key, value);
        });
        if (isAnyHidden.get()) {
            spreadsheet.reloadSheetStyles(false, true);
        }
    }

    void setHidden(int columnIndex, boolean hidden) {
        applyHidden(columnIndex, hidden);
        spreadsheet.reloadSheetStyles(false, true);
    }

    void setColumnsHidden(Map<Integer, Boolean> columnIndexToHidden) {
        columnIndexToHidden.forEach(this::applyHidden);
        spreadsheet.reloadSheetStyles(false, true);
    }

    void setDefaultWidthPixels(int widthPX) {
        spreadsheet.setDefColW(widthPX);
    }

    void onClientResized(Map<Integer, Integer> newColumnSizes, int row1,
            int col1, int row2, int col2) {
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                Type.COLUMN);
        command.captureValues(newColumnSizes.keySet()
                .toArray(new Integer[newColumnSizes.size()]));
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        setWidths(newColumnSizes.entrySet().stream().collect(Collectors
                .toMap(entry -> entry.getKey() - 1, Map.Entry::getValue)));
        if (spreadsheet.sheetOverlayManager.hasOverlays()) {
            spreadsheet.sheetOverlayManager.markReloadFromPoi();
        }
        spreadsheet.loadCells(row1, col1, row2, col2);
    }

    private void applyWidth(int index, int width) {
        if (width == 0) {
            applyHidden(index, true);
            return;
        }
        // No wider than POI holds, so the sheet and the workbook agree.
        width = Math.min(width, SpreadsheetUtil.MAX_COLUMN_WIDTH_PX);
        ArrayList<Integer> hiddenColumnIndexes = new ArrayList<>(
                spreadsheet.getHiddenColumnIndexes());
        int[] colW = widthsCovering(spreadsheet, index);
        if (hiddenColumnIndexes.contains(Integer.valueOf(index + 1))) {
            hiddenColumnIndexes.remove(Integer.valueOf(index + 1));
        }
        if (spreadsheet.getActiveSheet().isColumnHidden(index)) {
            spreadsheet.getActiveSheet().setColumnHidden(index, false);
        }
        colW[index] = width;
        spreadsheet.setColW(colW);
        spreadsheet.setHiddenColumnIndexes(hiddenColumnIndexes);
        spreadsheet.getActiveSheet().setColumnWidth(index,
                SpreadsheetUtil.pixel2WidthUnits(width));

        if (spreadsheet.getActiveSheet() instanceof XSSFSheet) {
            ((XSSFSheet) spreadsheet.getActiveSheet()).getColumnHelper()
                    .cleanColumns();
        }
        spreadsheet.getCellValueManager().clearCacheForColumn(index + 1);
        spreadsheet.getCellValueManager().loadCellData(
                spreadsheet.getFirstRow(), index + 1, spreadsheet.getLastRow(),
                index + 1);
        spreadsheet.getCellValueManager().resendTextRunningInto(index);
    }

    private void applyHidden(int columnIndex, boolean hidden) {
        spreadsheet.getActiveSheet().setColumnHidden(columnIndex, hidden);
        ArrayList<Integer> hiddenColumnIndexes = new ArrayList<>(
                spreadsheet.getHiddenColumnIndexes());
        int[] colW = widthsCovering(spreadsheet, columnIndex);
        if (hidden && !hiddenColumnIndexes.contains(columnIndex + 1)) {
            hiddenColumnIndexes.add(columnIndex + 1);
            colW[columnIndex] = 0;
        } else if (!hidden && hiddenColumnIndexes.contains(columnIndex + 1)) {
            hiddenColumnIndexes
                    .remove(hiddenColumnIndexes.indexOf(columnIndex + 1));
            colW[columnIndex] = (int) spreadsheet.getActiveSheet()
                    .getColumnWidthInPixels(columnIndex);
            spreadsheet.getCellValueManager()
                    .clearCacheForColumn(columnIndex + 1);
            spreadsheet.getCellValueManager().loadCellData(
                    spreadsheet.getFirstRow(), columnIndex + 1,
                    spreadsheet.getLastRow(), columnIndex + 1);
        }
        spreadsheet.setHiddenColumnIndexes(hiddenColumnIndexes);
        spreadsheet.setColW(colW);
        spreadsheet.getCellValueManager().resendTextRunningInto(columnIndex);
    }

    /**
     * A copy of the column widths long enough to hold the column at
     * {@code index}. The widths stop at the columns the sheet had when it was
     * loaded, and {@link Spreadsheet#setMaxColumns(int)} can add more: those
     * take their widths from POI, as loading gives them.
     */
    static int[] widthsCovering(Spreadsheet spreadsheet, int index) {
        int[] current = spreadsheet.getColW();
        int[] colW = Arrays.copyOf(current,
                Math.max(current.length, index + 1));
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int column = current.length; column < colW.length; column++) {
            colW[column] = sheet.isColumnHidden(column) ? 0
                    : (int) sheet.getColumnWidthInPixels(column);
        }
        return colW;
    }
}
