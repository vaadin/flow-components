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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * Drains the flush queue and pushes the resulting cell-data batch to the
 * client.
 *
 * <p>
 * The flush is sheet-wide: every cell is visited, and three things can happen
 * per cell:
 *
 * <ul>
 * <li>If conditional formatting applies, the cell is marked for update so that
 * conditional-format style indices reach the client.</li>
 * <li>If the cell is a formula and either was previously sent or is currently
 * marked, its re-evaluated value is captured into the batch (formula cells
 * always re-flush because POI may have produced a new value).</li>
 * <li>If the cell is non-formula and marked, its value is formatted and
 * captured into the batch.</li>
 * </ul>
 *
 * <p>
 * Before that, a cell whose text runs into a marked or removed cell is captured
 * too, as how far it runs depends on whether that cell holds a value.
 *
 * <p>
 * The {@link ClientFlushQueue}'s {@code removedCells} are appended to the batch
 * (each carries only {@code row} / {@code col} so the client knows to evict
 * that key). The marked / removed queues are then cleared. If any formula
 * values actually changed, a {@code FormulaValueChangeEvent} is fired via the
 * parent.
 */
final class ClientCacheFlusher implements Serializable {

    private final Spreadsheet spreadsheet;
    private final CellValueManager parent;
    private final ClientCellCache cache;
    private final ClientFlushQueue queue;

    ClientCacheFlusher(Spreadsheet spreadsheet, CellValueManager parent,
            ClientCellCache cache, ClientFlushQueue queue) {
        this.spreadsheet = spreadsheet;
        this.parent = parent;
        this.cache = cache;
        this.queue = queue;
    }

    void flush() {
        ArrayList<CellData> updatedCellData = new ArrayList<CellData>();
        Sheet sheet = spreadsheet.getActiveSheet();
        addSpillSources(sheet, updatedCellData);

        // it is unnecessary to worry about having custom components in the
        // cell because the client side handles it — it will not replace a
        // custom component with a cell value

        // update all cached formula cell values on client side because they
        // might have changed; also push every cell that is currently marked.
        Iterator<Row> rows = sheet.rowIterator();
        while (rows.hasNext()) {
            Row r = rows.next();
            Iterator<Cell> cells = r.cellIterator();
            while (cells.hasNext()) {
                Cell cell = cells.next();
                int rowIndex = cell.getRowIndex();
                int columnIndex = cell.getColumnIndex();
                String key = SpreadsheetUtil.toKey(columnIndex + 1,
                        rowIndex + 1);

                if (spreadsheet.getConditionalFormatter()
                        .getCellFormattingIndex(cell) != null) {
                    queue.markForUpdate(key);
                }

                if (cell.getCellType() == CellType.FORMULA) {
                    if (cache.containsFormula(key)
                            || queue.isMarkedForUpdate(key)) {
                        CellData cd = parent.createCellDataForCell(cell);
                        if (cd == null) {
                            // in case the formula cell value has changed to
                            // null or empty; this case is probably quite rare
                            // — formula pointing to a cell that was removed
                            // or had its value cleared
                            cd = new CellData();
                            cd.col = columnIndex + 1;
                            cd.row = rowIndex + 1;
                            cd.cellStyle = "" + cell.getCellStyle().getIndex();
                        }
                        cache.markFormulaSent(key);
                        updatedCellData.add(cd);
                    }
                } else if (queue.isMarkedForUpdate(key)) {
                    cache.markNormalSent(key);
                    updatedCellData.add(parent.createCellDataForCell(cell));
                }
            }
        }
        if (queue.hasChangedFormulaCells()) {
            parent.fireFormulaValueChangeEvent(
                    queue.drainChangedFormulaCells());
        }
        // empty cells have cell data with just col and row
        updatedCellData.addAll(queue.getRemovedCells());
        if (!updatedCellData.isEmpty()) {
            spreadsheet.getRpcProxy().cellsUpdated(updatedCellData);
            spreadsheet.getRpcProxy().refreshCellStyles();
        }
        queue.clear();
    }

    /**
     * Adds the cells whose text may run into a cell about to be sent.
     * <p>
     * Text too wide for its column runs on into the empty cells to its right
     * and stops at the first one holding a value. A cell that gets or loses a
     * value therefore changes how far the nearest cell to its left runs, and
     * the client has that from when the cell was sent. Text that fits its
     * column runs nowhere, so such a cell is not sent again; nor is one sent
     * below anyway, being marked or a formula the client has.
     */
    private void addSpillSources(Sheet sheet, List<CellData> updatedCellData) {
        Map<String, Cell> sources = new LinkedHashMap<>();
        for (String key : queue.getMarkedCells()) {
            addSpillSource(sheet, SpreadsheetUtil.getColumnIndexFromKey(key),
                    SpreadsheetUtil.getRowFromKey(key), sources);
        }
        for (CellData removed : queue.getRemovedCells()) {
            addSpillSource(sheet, removed.col, removed.row, sources);
        }
        for (Cell source : sources.values()) {
            CellData cellData = parent.createCellDataForCell(source);
            if (cellData != null && cellData.overflowWidth > 0) {
                updatedCellData.add(cellData);
            }
        }
    }

    /** Column and row are 1-based. */
    private void addSpillSource(Sheet sheet, int column, int row,
            Map<String, Cell> sources) {
        Cell source = CellDataBuilder.spillSourceOf(sheet.getRow(row - 1),
                column - 1);
        if (source != null) {
            String key = SpreadsheetUtil.toKey(source);
            if (cache.containsNormal(key) && !queue.isMarkedForUpdate(key)) {
                sources.put(key, source);
            }
        }
    }
}
