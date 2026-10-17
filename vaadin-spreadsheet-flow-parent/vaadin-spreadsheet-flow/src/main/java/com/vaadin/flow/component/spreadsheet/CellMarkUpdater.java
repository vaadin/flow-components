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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Orchestrates the cell-state update lifecycle on the active sheet. Five
 * cohesive paths, all about "a cell changed — push the change to the client":
 *
 * <ul>
 * <li>{@link #markCellAsUpdated} — flag a single cell as changed; optionally
 * propagate the style change too.</li>
 * <li>{@link #markCellAsDeleted} — flag a cell as deleted; optionally propagate
 * style; trigger a refresh.</li>
 * <li>{@link #deleteCell} — locate the cell on the active sheet, remove it from
 * POI, propagate the delete-style + value-manager changes.</li>
 * <li>{@link #updateMarkedCells} — session-end flush: rebuild conditional
 * formatting rules, push value updates, refresh selection + comments, reload
 * visible custom components / editors.</li>
 * <li>{@link #refreshAllCellValues} — full sheet refresh: clear formula +
 * conditional-formatting evaluators, clear value cache, reload the whole
 * viewport range if it's been loaded once before, then re-select the active
 * cell to refresh its formula bar.</li>
 * </ul>
 *
 * <p>
 * Pure orchestrator. Holds a {@link Spreadsheet} reference for read / dispatch
 * only.
 */
final class CellMarkUpdater implements Serializable {

    private final Spreadsheet spreadsheet;

    CellMarkUpdater(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void markCellAsUpdated(Cell cell, boolean cellStyleUpdated) {
        spreadsheet.getCellValueManager().cellUpdated(cell);
        // Only when told: a style unchanged changes nothing, and going
        // through the cell's borders again costs time.
        if (cellStyleUpdated) {
            spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(cell,
                    true);
        }
    }

    void markCellAsDeleted(Cell cell, boolean cellStyleUpdated) {
        spreadsheet.getCellValueManager().cellDeleted(cell);
        if (cellStyleUpdated) {
            // Its style is not read: out of its row, an XSSF cell can no
            // longer be read.
            spreadsheet.getSpreadsheetStyleFactory()
                    .clearCellStyle(cell.getRowIndex(), cell.getColumnIndex());
        }
        // Not refreshCells(cell), which reads the cell: it is out of its row
        // already, where an XSSF cell can no longer be read.
        spreadsheet.updateMarkedCells();
    }

    void deleteCell(int row, int col) {
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        // A row with no cells is not in POI at all.
        final Row poiRow = activeSheet.getRow(row);
        final Cell cell = poiRow == null ? null : poiRow.getCell(col);
        if (cell != null) {
            poiRow.removeCell(cell);
            spreadsheet.getSpreadsheetStyleFactory().clearCellStyle(row, col);
            spreadsheet.getCellValueManager().cellDeleted(cell);
            // Not refreshCells(cell), which reads the cell: it is out of its
            // row, where an XSSF cell can no longer be read.
            spreadsheet.updateMarkedCells();
        }
    }

    void updateMarkedCells(boolean recreateEditors) {
        // update conditional formatting in case styling has changed. New
        // values are fetched in ValueManager (below).
        spreadsheet.getConditionalFormatter().createConditionalFormatterRules();
        // FIXME should be optimized, should not go through all links,
        // comments etc. always
        spreadsheet.getCellValueManager().updateMarkedCellValues();
        // if the selected cell is of type formula, there is a chance that
        // the formula has been changed.
        spreadsheet.getCellSelectionManager().reSelectSelectedCell();
        // update custom components, editors, and the comments, links and
        // overlays of the cells shown
        spreadsheet.reloadVisibleCellContents(recreateEditors);
    }

    void refreshAllCellValues() {
        spreadsheet.getFormulaEvaluator().clearAllCachedResultValues();
        spreadsheet.getCellValueManager().clearCachedContent();
        // The rules themselves, not just their cached results: a full
        // refresh follows things that add and remove formattings - loading a
        // workbook, undoing a row delete - and the cell data built below
        // asks this table which rules a cell matches. Leaving it stale sends
        // every one of those cells without its formatting.
        spreadsheet.getConditionalFormatter().createConditionalFormatterRules();

        // only reload if the cells have been loaded once previously
        if (spreadsheet.getFirstColumn() == -1) {
            // client will request cells soon, no need for reload now
            return;
        }
        spreadsheet.updateRowAndColumnRangeCellData(1, 1, spreadsheet.getRows(),
                spreadsheet.getColumns());
        // if the selected cell is of type formula, there is a chance that
        // the formula has been changed.
        spreadsheet.getCellSelectionManager().reSelectSelectedCell();
    }
}
