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
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellDeletionHandler;

/**
 * Decides whether the default deletion path runs for a delete-cells action, or
 * whether the application's
 * {@link com.vaadin.flow.component.spreadsheet.Spreadsheet.CellDeletionHandler}
 * has taken responsibility for the deletion.
 *
 * <p>
 * Three deletion shapes, three predicates: single selected cell
 * ({@link #passesDeletionCheck(CellReference)}), individually-selected cell
 * list ({@link #passesIndividualDeletionCheck(List)}), and cell-range list
 * ({@link #passesRangeDeletionCheck(List)}). Each returns {@code true} when the
 * default path should run — meaning either there is no custom handler, the
 * selection is empty, or the handler accepted the deletion.
 *
 * <p>
 * Reads the parent {@link CellValueManager}'s formatter, formula evaluator,
 * conditional-formatting evaluator, custom-handler setting, and workbook — the
 * deletion handler API forwards all five to the user code. Holding the parent
 * reference avoids a five-parameter constructor that would need to change every
 * time the handler signature evolves.
 */
final class CellDeletionGuard implements Serializable {

    private final CellValueManager parent;

    CellDeletionGuard(CellValueManager parent) {
        this.parent = parent;
    }

    /**
     * @return {@code true} if {@code cell} sits inside any of
     *         {@code cellRangeAddresses}, {@code false} otherwise.
     */
    boolean isInRange(CellReference cell,
            List<CellRangeAddress> cellRangeAddresses) {
        for (CellRangeAddress range : cellRangeAddresses) {
            if (range.isInRange(cell.getRow(), cell.getCol())) {
                return true;
            }
        }
        return false;
    }

    boolean passesDeletionCheck(CellReference selectedCellReference) {
        CellDeletionHandler handler = parent.getCustomCellDeletionHandler();
        if (selectedCellReference == null || handler == null) {
            return true;
        }
        Sheet activeSheet = activeSheet();
        int rowIndex = selectedCellReference.getRow();
        Row row = activeSheet.getRow(rowIndex);
        if (row != null) {
            short colIndex = selectedCellReference.getCol();
            Cell cell = row.getCell(colIndex);
            if (cell != null) {
                return handler.cellDeleted(cell, activeSheet, colIndex,
                        rowIndex, parent.getFormulaEvaluator(),
                        parent.getDataFormatter(),
                        parent.getConditionalFormattingEvaluator());
            }
        }
        return true;
    }

    boolean passesIndividualDeletionCheck(
            List<CellReference> individualSelectedCells) {
        CellDeletionHandler handler = parent.getCustomCellDeletionHandler();
        if (individualSelectedCells.isEmpty() || handler == null) {
            return true;
        }
        return handler.individualSelectedCellsDeleted(individualSelectedCells,
                activeSheet(), parent.getFormulaEvaluator(),
                parent.getDataFormatter(),
                parent.getConditionalFormattingEvaluator());
    }

    boolean passesRangeDeletionCheck(
            List<CellRangeAddress> cellRangeAddresses) {
        CellDeletionHandler handler = parent.getCustomCellDeletionHandler();
        if (cellRangeAddresses.isEmpty() || handler == null) {
            return true;
        }
        return handler.cellRangeDeleted(cellRangeAddresses, activeSheet(),
                parent.getFormulaEvaluator(), parent.getDataFormatter(),
                parent.getConditionalFormattingEvaluator());
    }

    private Sheet activeSheet() {
        Workbook workbook = parent.spreadsheet.getWorkbook();
        return workbook.getSheetAt(workbook.getActiveSheetIndex());
    }
}
