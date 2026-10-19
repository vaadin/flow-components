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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * The cells built for the client, those to send it on the next update, and
 * sending them with {@link #flush}.
 * <p>
 * Which cells were built, and as formulas, is kept in the
 * {@link ClientCellRecord} that {@link ClientCellMirror} keeps of what the
 * client has: one record of the cells, keyed by column and row. Here are only
 * the cells to send again and to take off. Formula cells built are sent again
 * on every update, as what they work out to may have changed although they did
 * not. Other cells are sent again only when they are marked for update.
 * <p>
 * Cells are named here by their keys ({@link SpreadsheetUtil#toKey}), or by
 * their column and row, 1-based.
 */
final class ClientCellCache implements Serializable {

    /**
     * Which cells were built, shared with the record of what the client has.
     */
    private final ClientCellRecord record;

    /** Cells whose values have changed and are to be sent again. */
    private final HashSet<String> markedCells = new HashSet<>();
    /** Cells to take off the client: a row and column, and no value. */
    private final HashSet<CellData> removedCells = new HashSet<>();
    /**
     * Formula cells that work out to something else than when they were last
     * sent, for the formula change event of the next update.
     */
    private HashSet<CellReference> changedFormulaCells = new HashSet<>();

    ClientCellCache(ClientCellRecord record) {
        this.record = record;
    }

    /**
     * Every cell is to be built again, and none is marked: the client keeps
     * what it has, and is sent again only what changed. A cell taken off the
     * client is sent at once, so none is waiting here then.
     */
    void clear() {
        record.unbuildAll();
        markedCells.clear();
    }

    /** Whether the cell was built, and not as a formula. */
    boolean containsNormal(String key) {
        return record.isBuiltPlain(keyOf(key));
    }

    /** Whether the cell was built as a formula. */
    boolean containsFormula(String key) {
        return record.isBuiltFormula(keyOf(key));
    }

    /**
     * Whether the cell, column and row 1-based, was built, plain or as a
     * formula.
     */
    boolean contains(int col, int row) {
        return record.isBuilt(ClientCellRecord.keyOf(col, row));
    }

    /** The cell was built for the client: it is no longer to be taken off. */
    void markNormalSent(String key) {
        markSent(SpreadsheetUtil.getColumnIndexFromKey(key),
                SpreadsheetUtil.getRowFromKey(key), false);
    }

    /**
     * The formula cell was built for the client: it is no longer to be taken
     * off.
     */
    void markFormulaSent(String key) {
        markSent(SpreadsheetUtil.getColumnIndexFromKey(key),
                SpreadsheetUtil.getRowFromKey(key), true);
    }

    /**
     * The cell, column and row 1-based, was built for the client, as a formula
     * or not - one or the other: it is no longer to be taken off.
     */
    void markSent(int col, int row, boolean formula) {
        record.build(ClientCellRecord.keyOf(col, row), formula);
        removedCells.remove(removalOf(col, row));
    }

    /** The cell is to be built again: whether it was built. */
    boolean dropSent(String key) {
        return record.unbuild(keyOf(key));
    }

    /**
     * The cells of the rows, 1-based and inclusive, are to be built again: a
     * removal for each that was built, in the order of the sheet.
     */
    List<CellData> pruneRowsRange(int startRow, int endRow) {
        List<CellData> removals = new ArrayList<>();
        for (long key : record.unbuildRows(startRow, endRow)) {
            removals.add(removalOf(ClientCellRecord.colOf(key),
                    ClientCellRecord.rowOf(key)));
        }
        return removals;
    }

    /** The cells of the column, 1-based, are to be built again. */
    void pruneColumn(int indexColumn) {
        record.unbuildColumn(indexColumn);
    }

    /**
     * Sends the cell again on the next update, unless it is taken off the
     * client after this: of a removal and an update of a cell, the last is the
     * one. A removal queued before is dropped when the cell is sent.
     */
    void markForUpdate(String key) {
        markedCells.add(key);
    }

    boolean isMarkedForUpdate(String key) {
        return markedCells.contains(key);
    }

    /**
     * Takes the cell off the client on the next update, unless it is marked for
     * update after this.
     */
    void enqueueRemoval(CellData cd) {
        removedCells.add(cd);
        markedCells.remove(SpreadsheetUtil.toKey(cd.col, cd.row));
    }

    /** The removal of the cell, column and row 1-based. */
    private static CellData removalOf(int col, int row) {
        CellData removal = new CellData();
        removal.col = col;
        removal.row = row;
        return removal;
    }

    /** The record's key of the cell of a key. */
    private static long keyOf(String key) {
        return ClientCellRecord.keyOf(
                SpreadsheetUtil.getColumnIndexFromKey(key),
                SpreadsheetUtil.getRowFromKey(key));
    }

    void addChangedFormulaCell(CellReference ref) {
        changedFormulaCells.add(ref);
    }

    /**
     * Sends the client the cells marked for update, the formula cells it has,
     * and the cells whose text may run into one of those, and takes the cells
     * queued for removal off it. Fires the formula change event first when a
     * formula works out to something else.
     */
    void flush(Spreadsheet spreadsheet, CellValueManager manager) {
        ArrayList<CellData> updatedCellData = new ArrayList<>();
        Sheet sheet = spreadsheet.getActiveSheet();
        addSpillSources(sheet, manager, updatedCellData);
        // A cell with a component is not sent, as it is not when loaded.
        Set<String> componentCells = new HashSet<>(
                spreadsheet.sheetModel().componentIDtoCellKeysMap.get()
                        .values());

        // The formula cells built are sent again, as what they work out to
        // may have changed, and so are the cells marked: only those are
        // visited, rather than all the cells of the sheet. The client takes
        // them in any order; the order of the sheet - the record's keys sort
        // that way - keeps what goes out the same from one run to the next.
        TreeMap<Long, String> visited = new TreeMap<>();
        for (String key : markedCells) {
            visited.put(keyOf(key), key);
        }
        for (long key : record.builtFormulas()) {
            visited.putIfAbsent(key, SpreadsheetUtil.toKey(
                    ClientCellRecord.colOf(key), ClientCellRecord.rowOf(key)));
        }
        for (Map.Entry<Long, String> entry : visited.entrySet()) {
            long at = entry.getKey();
            String key = entry.getValue();
            int col = ClientCellRecord.colOf(at);
            Row row = sheet.getRow(ClientCellRecord.rowOf(at) - 1);
            Cell cell = row == null ? null : row.getCell(col - 1);
            if (cell == null || componentCells.contains(key)) {
                continue;
            }
            if (cell.getCellType() == CellType.FORMULA) {
                if (record.isBuiltFormula(at) || isMarkedForUpdate(key)) {
                    markSent(col, cell.getRowIndex() + 1, true);
                    updatedCellData.add(manager.createCellDataForCell(cell));
                }
            } else if (isMarkedForUpdate(key)) {
                markSent(col, cell.getRowIndex() + 1, false);
                updatedCellData.add(manager.createCellDataForCell(cell));
            }
        }
        if (!changedFormulaCells.isEmpty()) {
            // A new set rather than a cleared one, so a listener can go
            // through the cells while new changes are recorded.
            Set<CellReference> changed = changedFormulaCells;
            changedFormulaCells = new HashSet<>();
            manager.fireFormulaValueChangeEvent(changed);
        }
        // empty cells have cell data with just col and row
        updatedCellData.addAll(removedCells);
        if (!updatedCellData.isEmpty()) {
            spreadsheet.getRpcProxy().cellsUpdated(updatedCellData);
            spreadsheet.getRpcProxy().refreshCellStyles();
        }
        markedCells.clear();
        removedCells.clear();
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
    private void addSpillSources(Sheet sheet, CellValueManager manager,
            List<CellData> updatedCellData) {
        Map<String, Cell> sources = new LinkedHashMap<>();
        for (String key : markedCells) {
            addSpillSource(sheet, SpreadsheetUtil.getColumnIndexFromKey(key),
                    SpreadsheetUtil.getRowFromKey(key), sources);
        }
        for (CellData removed : removedCells) {
            addSpillSource(sheet, removed.col, removed.row, sources);
        }
        for (Cell source : sources.values()) {
            CellData cellData = manager.createCellDataForCell(source);
            if (cellData.overflowWidth > 0) {
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
            if (record.isBuiltPlain(ClientCellRecord.keyOf(
                    source.getColumnIndex() + 1, source.getRowIndex() + 1))
                    && !isMarkedForUpdate(key)) {
                sources.put(key, source);
            }
        }
    }
}
