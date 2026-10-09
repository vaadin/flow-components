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
        applyHeight(index, height);
        boolean isRowHidden = height == 0.0F;
        spreadsheet.reloadSheetStyles(true, isRowHidden);
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
        if (spreadsheet.sheetOverlayManager.hasOverlays()) {
            spreadsheet.sheetOverlayManager.markReloadFromPoi();
        }
        spreadsheet.loadCells(row1, col1, row2, col2);
    }

    private void applyHeight(int index, float height) {
        if (height == 0.0F) {
            applyHidden(index, true);
            return;
        }
        Row row = spreadsheet.getActiveSheet().getRow(index);
        ArrayList<Integer> hiddenRowIndexes = new ArrayList<>(
                spreadsheet.getHiddenRowIndexes());
        if (hiddenRowIndexes.contains(Integer.valueOf(index + 1))) {
            hiddenRowIndexes.remove(Integer.valueOf(index + 1));
            if (row != null && row.getZeroHeight()) {
                row.setZeroHeight(false);
            }
        }
        if (row == null) {
            row = spreadsheet.getActiveSheet().createRow(index);
        }
        row.setHeightInPoints(height);
        spreadsheet.setHiddenRowIndexes(hiddenRowIndexes);
    }

    private void applyHidden(int rowIndex, boolean hidden) {
        Sheet activeSheet = spreadsheet.getActiveSheet();
        Row row = activeSheet.getRow(rowIndex);
        if (row == null) {
            row = activeSheet.createRow(rowIndex);
        }
        row.setZeroHeight(hidden);
    }
}
