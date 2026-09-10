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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Orchestrates a POI row-shift on the active sheet and propagates the
 * server-side bookkeeping that POI doesn't know about: cached row heights,
 * hidden-row indexes, cell styles for shifted borders, the formula and
 * conditional-formatting evaluator caches, the value-manager's client-cache row
 * mapping, the selection address, merged regions intersected by removed rows,
 * popup buttons rooted in shifted rows, and the invalid-formula tracker's
 * per-cell key set.
 *
 * <p>
 * One concern, one place. Before extraction, all of this lived inline in
 * {@code Spreadsheet.shiftRows(int, int, int, boolean, boolean)} (~155 LOC)
 * plus a private {@code rowsMoved} helper and a private
 * {@code getLastNonBlankRow} scan. Public surface preserved: the orchestrator
 * is only called from the two public {@code shiftRows} overloads on
 * {@code Spreadsheet} which still exist as thin delegators.
 *
 * <p>
 * The orchestrator carries no state of its own; it holds a {@link Spreadsheet}
 * reference for read/write into the host's many collaborator handles
 * ({@code valueManager}, {@code styler}, {@code sheetOverlayManager},
 * {@code selectionManager}, {@code viewportBounds},
 * {@code popupButtonRowShifter}, {@code invalidFormulaTracker}). The behavior
 * is preserved verbatim from the pre-extraction code path.
 */
final class RowShiftOrchestrator implements Serializable {

    private final Spreadsheet spreadsheet;

    RowShiftOrchestrator(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void shift(int startRow, int endRow, int n, boolean copyRowHeight,
            boolean resetOriginalRowHeight) {
        Sheet sheet = spreadsheet.getActiveSheet();
        int lastNonBlankRow = getLastNonBlankRow(sheet);
        sheet.shiftRows(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
        // need to re-send the cell values to client
        // remove all cached cell data that is now empty
        spreadsheet.getFormulaEvaluator().clearAllCachedResultValues();
        spreadsheet.getConditionalFormattingEvaluator().clearAllCachedValues();
        int start = n < 0 ? Math.max(lastNonBlankRow, startRow) : startRow;
        int end = n < 0 ? endRow : startRow + n - 1;
        spreadsheet.getCellValueManager()
                .updateDeletedRowsInClientCache(start + 1, end + 1);
        int firstAffectedRow = n < 0 ? startRow + n : startRow;
        int lastAffectedRow = n < 0 ? endRow : endRow + n;
        if (copyRowHeight || resetOriginalRowHeight) {
            // might need to increase the size of the row heights array
            int oldLength = spreadsheet.getRowH().length;
            int neededLength = endRow + n + 1;
            float[] rowH = Arrays.copyOf(spreadsheet.getRowH(),
                    spreadsheet.getRowH().length);
            if (n > 0 && oldLength < neededLength) {
                rowH = Arrays.copyOf(rowH, neededLength);
            }
            for (int i = firstAffectedRow; i <= lastAffectedRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    if (row.getZeroHeight()) {
                        rowH[i] = 0f;
                    } else {
                        rowH[i] = row.getHeightInPoints();
                    }
                } else {
                    rowH[i] = sheet.getDefaultRowHeightInPoints();
                }
            }
            spreadsheet.setRowH(rowH);
        }

        if (spreadsheet.sheetOverlayManager.hasOverlays()) {
            spreadsheet.sheetOverlayManager.markReloadFromPoi();
        }
        // need to shift the cell styles, clear and update
        // need to go -1 and +1 because of shifted borders..
        final ArrayList<Cell> cellsToUpdate = new ArrayList<>();
        SpreadsheetStyleFactory styler = spreadsheet
                .getSpreadsheetStyleFactory();
        for (int r = (firstAffectedRow - 1); r <= (lastAffectedRow + 1); r++) {
            if (r < 0) {
                r = 0;
            }
            Row row = sheet.getRow(r);
            final Integer rowIndex = r + 1;
            ArrayList<Integer> hiddenRowIndexes = new ArrayList<>(
                    spreadsheet.getHiddenRowIndexes());
            if (row == null) {
                spreadsheet.getCellValueManager()
                        .updateDeletedRowsInClientCache(rowIndex, rowIndex);
                hiddenRowIndexes.remove(rowIndex);
                for (int c = 0; c < spreadsheet.getCols(); c++) {
                    styler.clearCellStyle(r, c);
                }
            } else {
                if (row.getZeroHeight()) {
                    hiddenRowIndexes.add(rowIndex);
                } else {
                    hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < spreadsheet.getCols(); c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        styler.clearCellStyle(r, c);
                        if (r <= lastNonBlankRow + n) {
                            // There might be a pre-shift value for this
                            // cell in client-side and should be overwritten
                            cell = row.createCell(c);
                            cellsToUpdate.add(cell);
                        }
                    } else {
                        cellsToUpdate.add(cell);
                    }
                }
            }
            spreadsheet.setHiddenRowIndexes(hiddenRowIndexes);
        }
        rowsMoved(firstAffectedRow, lastAffectedRow, n);

        for (Cell cell : cellsToUpdate) {
            styler.cellStyleUpdated(cell, false);
            spreadsheet.markCellAsUpdated(cell, false);
        }
        styler.loadCustomBorderStylesToState();

        // Shifting moves cells, and an editor is cached against a cell key —
        // one left in place would end up on the wrong cell.
        spreadsheet.updateMarkedCellsRecreatingEditors(); // deleted and formula
                                                          // cells and
        // style selectors
        spreadsheet.updateRowAndColumnRangeCellData(spreadsheet.getFirstRow(),
                spreadsheet.getFirstColumn(), spreadsheet.getLastRow(),
                spreadsheet.getLastColumn());
        spreadsheet.updateMergedRegions();

        CellReference selectedCellReference = spreadsheet
                .getCellSelectionManager().getSelectedCellReference();
        if (selectedCellReference != null) {
            if (selectedCellReference.getRow() >= firstAffectedRow
                    && selectedCellReference.getRow() <= lastAffectedRow) {
                spreadsheet.getCellSelectionManager().onSheetAddressChanged(
                        selectedCellReference.formatAsString(), false);
            }
        }
    }

    /**
     * Called after POI's shiftRows completes. Updates server-side state that
     * POI does not know about: merged regions that intersect the deleted-row
     * range (only relevant for upward shifts), popup-button row anchors, and
     * invalid-formula key mapping.
     *
     * <p>
     * If {@code n} is negative, rows moved UP and the original rows in
     * {@code [first + n, first]} are gone — merged regions rooted there must be
     * removed. POI handles the others automatically.
     */
    private void rowsMoved(int first, int last, int n) {
        // Merged regions
        if (n < 0) {
            // Remove merged cells from deleted rows. POI will handle the
            // other updated values.
            for (int row = (first + n); row <= first; ++row) {
                Sheet sheet = spreadsheet.getActiveSheet();
                for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                    CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                    if (mergedRegion.getFirstRow() == row) {
                        spreadsheet.removeMergedRegion(i);
                    }
                }
            }
        }

        spreadsheet.popupButtonRowShifter.shift(first, last, n);

        spreadsheet.invalidFormulaTracker.shiftActiveSheetRows(first, last, n);
    }

    /**
     * Scan from the sheet's last row down to row 0 to find the highest row
     * index containing at least one non-blank cell. Returns 0 if no non-blank
     * cell exists (matches POI's empty-sheet convention).
     */
    private int getLastNonBlankRow(Sheet sheet) {
        for (int r = sheet.getLastRowNum(); r >= 0; r--) {
            Row row = sheet.getRow(r);
            if (row != null) {
                for (short c = row.getFirstCellNum(); c < row
                        .getLastCellNum(); c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        return r;
                    }
                }
            }
        }
        return 0;
    }
}
