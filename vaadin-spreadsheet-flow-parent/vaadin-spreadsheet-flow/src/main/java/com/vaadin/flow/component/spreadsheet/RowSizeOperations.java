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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand.Type;

/**
 * Sets row heights and row visibility on the active sheet. Owns the
 * entanglement between {@code height == 0} and "row is hidden" — a zero-height
 * set hides the row, and explicitly hiding clears its height mirror.
 *
 * <p>
 * The public methods on {@link Spreadsheet} that touch row dimensions delegate
 * here.
 */
final class RowSizeOperations implements Serializable {

    private final Spreadsheet spreadsheet;

    RowSizeOperations(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void setHeight(int index, float height) {
        spreadsheet.reloadSheetStyles(true, applyHeight(index, height));
    }

    void setHeights(Map<Integer, Float> rowIndexToHeight) {
        AtomicBoolean isAnyHidden = new AtomicBoolean();
        rowIndexToHeight.forEach((key, value) -> {
            applyHeight(key, value);
            if (value == 0.0F) {
                isAnyHidden.set(true);
            }
        });
        spreadsheet.reloadSheetStyles(true, isAnyHidden.get());
    }

    void setHidden(int rowIndex, boolean hidden) {
        applyHidden(rowIndex, hidden);
        spreadsheet.reloadSheetStyles(true, true);
    }

    void setRowsHidden(Map<Integer, Boolean> rowIndexToHidden) {
        rowIndexToHidden.forEach(this::applyHidden);
        spreadsheet.reloadSheetStyles(true, true);
    }

    void setDefaultHeightPoints(float heightPT) {
        spreadsheet.getActiveSheet().setDefaultRowHeightInPoints(heightPT);
        spreadsheet.setDefRowH(heightPT);
    }

    void onClientResized(Map<Integer, Float> newRowSizes, int row1, int col1,
            int row2, int col2) {
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                Type.ROW);
        command.captureValues(
                newRowSizes.keySet().toArray(new Integer[newRowSizes.size()]));
        spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
        setHeights(newRowSizes.entrySet().stream().collect(Collectors
                .toMap(entry -> entry.getKey() - 1, Map.Entry::getValue)));
        if (spreadsheet.overlays().hasOverlays()) {
            spreadsheet.overlays().markReloadFromPoi();
        }
        spreadsheet.loadCells(row1, col1, row2, col2);
    }

    /** Whether the row is hidden by the height, or shown by it. */
    private boolean applyHeight(int index, float height) {
        if (height == 0.0F) {
            applyHidden(index, true);
            return true;
        }
        ArrayList<Integer> hiddenRowIndexes = new ArrayList<>(
                spreadsheet.getHiddenRowIndexes());
        boolean shown = hiddenRowIndexes.remove(Integer.valueOf(index + 1));
        Row row = spreadsheet.getActiveSheet().getRow(index);
        if (row == null) {
            row = spreadsheet.getActiveSheet().createRow(index);
        }
        // A row given a height is shown.
        row.setZeroHeight(false);
        row.setHeightInPoints(height);
        float[] rowH = heightsCovering(spreadsheet, index);
        rowH[index] = height;
        spreadsheet.setHiddenRowIndexes(hiddenRowIndexes);
        spreadsheet.setRowH(rowH);
        return shown;
    }

    /**
     * A copy of the row heights long enough to hold the row at {@code index}.
     * The heights stop at the rows the sheet had when it was loaded, and
     * {@link Spreadsheet#setMaxRows(int)} can add more: those take their
     * heights from POI, as loading gives them.
     */
    static float[] heightsCovering(Spreadsheet spreadsheet, int index) {
        float[] current = spreadsheet.getRowH();
        float[] rowH = Arrays.copyOf(current,
                Math.max(current.length, index + 1));
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int r = current.length; r < rowH.length; r++) {
            Row row = sheet.getRow(r);
            rowH[r] = row == null ? sheet.getDefaultRowHeightInPoints()
                    : row.getZeroHeight() ? 0f : row.getHeightInPoints();
        }
        return rowH;
    }

    private void applyHidden(int rowIndex, boolean hidden) {
        Sheet activeSheet = spreadsheet.getActiveSheet();
        Row row = activeSheet.getRow(rowIndex);
        if (row == null) {
            row = activeSheet.createRow(rowIndex);
        }
        row.setZeroHeight(hidden);
        ArrayList<Integer> hiddenRowIndexes = new ArrayList<>(
                spreadsheet.getHiddenRowIndexes());
        hiddenRowIndexes.remove(Integer.valueOf(rowIndex + 1));
        if (hidden) {
            hiddenRowIndexes.add(rowIndex + 1);
        }
        float[] rowH = heightsCovering(spreadsheet, rowIndex);
        rowH[rowIndex] = hidden ? 0f : row.getHeightInPoints();
        spreadsheet.setHiddenRowIndexes(hiddenRowIndexes);
        spreadsheet.setRowH(rowH);
    }
}
