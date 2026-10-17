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
import java.util.Iterator;
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
 * The cells the client has, those to send it on the next update, and sending
 * them with {@link #flush}.
 * <p>
 * Cells are known by their keys ({@link SpreadsheetUtil#toKey}). Formula cells
 * the client has are kept apart from the other cells: they are sent again on
 * every update, as what they work out to may have changed although they did
 * not. Other cells are sent again only when they are marked for update.
 */
final class ClientCellCache implements Serializable {

    private final HashSet<String> sentCells = new HashSet<String>();
    private final HashSet<String> sentFormulaCells = new HashSet<String>();

    /** Cells whose values have changed and are to be sent again. */
    private final HashSet<String> markedCells = new HashSet<String>();
    /** Cells to take off the client: a row and column, and no value. */
    private final HashSet<CellData> removedCells = new HashSet<CellData>();
    /**
     * Formula cells that work out to something else than when they were last
     * sent, for the formula change event of the next update.
     */
    private HashSet<CellReference> changedFormulaCells = new HashSet<CellReference>();

    /**
     * Forgets the cells the client has and those to send it. A cell taken off
     * the client is sent at once, so none is waiting here then.
     */
    void clear() {
        sentCells.clear();
        sentFormulaCells.clear();
        markedCells.clear();
    }

    boolean containsNormal(String key) {
        return sentCells.contains(key);
    }

    boolean containsFormula(String key) {
        return sentFormulaCells.contains(key);
    }

    /** The client has the cell now: it is no longer to be taken off. */
    void markNormalSent(String key) {
        sentCells.add(key);
        removedCells.remove(removalOf(key));
    }

    /** The client has the formula cell now: it is no longer to be taken off. */
    void markFormulaSent(String key) {
        sentFormulaCells.add(key);
        removedCells.remove(removalOf(key));
    }

    /**
     * Drops {@code key} from whichever set holds it. Tries the plain-cell set
     * first; only falls through to the formula set if the plain set didn't have
     * it.
     */
    boolean dropSent(String key) {
        if (sentCells.remove(key)) {
            return true;
        }
        return sentFormulaCells.remove(key);
    }

    /**
     * Removes every key in either set that ends with {@code "row" + r} for any
     * {@code r} in {@code [startRow, endRow]}, and returns one {@link CellData}
     * removal entry per pruned key (with {@code row} set to the matched row
     * index and {@code col} extracted from the cell key). Plain cells come
     * before formula cells.
     */
    List<CellData> pruneRowsRange(int startRow, int endRow) {
        List<CellData> removals = new ArrayList<CellData>();
        for (int i = startRow; i <= endRow; i++) {
            String rowKey = "row" + i;
            for (Iterator<String> iterator = sentCells.iterator(); iterator
                    .hasNext();) {
                String key = iterator.next();
                if (key.endsWith(rowKey)) {
                    iterator.remove();
                    CellData cd = new CellData();
                    cd.col = SpreadsheetUtil.getColumnIndexFromKey(key);
                    cd.row = i;
                    removals.add(cd);
                }
            }
            for (Iterator<String> iterator = sentFormulaCells
                    .iterator(); iterator.hasNext();) {
                String key = iterator.next();
                if (key.endsWith(rowKey)) {
                    iterator.remove();
                    CellData cd = new CellData();
                    cd.col = SpreadsheetUtil.getColumnIndexFromKey(key);
                    cd.row = i;
                    removals.add(cd);
                }
            }
        }
        return removals;
    }

    /**
     * Removes every key in either set that starts with {@code "col<N> r"}. Used
     * by {@code clearCacheForColumn}.
     */
    void pruneColumn(int indexColumn) {
        final String columnKey = "col" + indexColumn + " r";
        for (Iterator<String> iterator = sentCells.iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (key.startsWith(columnKey)) {
                iterator.remove();
            }
        }
        for (Iterator<String> iterator = sentFormulaCells.iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (key.startsWith(columnKey)) {
                iterator.remove();
            }
        }
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

    /** The removal of the cell of the key. */
    private static CellData removalOf(String key) {
        CellData removal = new CellData();
        removal.col = SpreadsheetUtil.getColumnIndexFromKey(key);
        removal.row = SpreadsheetUtil.getRowFromKey(key);
        return removal;
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
        ArrayList<CellData> updatedCellData = new ArrayList<CellData>();
        Sheet sheet = spreadsheet.getActiveSheet();
        addSpillSources(sheet, manager, updatedCellData);
        // A cell with a component is not sent, as it is not when loaded.
        Set<String> componentCells = new HashSet<>(
                spreadsheet.getComponentIDtoCellKeysMap().values());

        // The formula cells the client has are sent again, as what they work
        // out to may have changed, and so are the cells marked: only those
        // are visited, rather than all the cells of the sheet. The client
        // takes them in any order; the order of the sheet keeps what goes
        // out the same from one run to the next, as it was.
        TreeMap<Long, String> visited = new TreeMap<>();
        for (String key : markedCells) {
            visited.put(sheetOrder(key), key);
        }
        for (String key : sentFormulaCells) {
            visited.put(sheetOrder(key), key);
        }
        for (String key : visited.values()) {
            Row row = sheet.getRow(SpreadsheetUtil.getRowFromKey(key) - 1);
            Cell cell = row == null ? null
                    : row.getCell(
                            SpreadsheetUtil.getColumnIndexFromKey(key) - 1);
            if (cell == null || componentCells.contains(key)) {
                continue;
            }
            if (cell.getCellType() == CellType.FORMULA) {
                if (containsFormula(key) || isMarkedForUpdate(key)) {
                    markFormulaSent(key);
                    updatedCellData.add(manager.createCellDataForCell(cell));
                }
            } else if (isMarkedForUpdate(key)) {
                markNormalSent(key);
                updatedCellData.add(manager.createCellDataForCell(cell));
            }
        }
        if (!changedFormulaCells.isEmpty()) {
            // A new set rather than a cleared one, so a listener can go
            // through the cells while new changes are recorded.
            Set<CellReference> changed = changedFormulaCells;
            changedFormulaCells = new HashSet<CellReference>();
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

    /** Orders the keys of cells by row, then column, as the sheet has them. */
    private static long sheetOrder(String key) {
        return ((long) SpreadsheetUtil.getRowFromKey(key) << 16)
                | SpreadsheetUtil.getColumnIndexFromKey(key);
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
            if (containsNormal(key) && !isMarkedForUpdate(key)) {
                sources.put(key, source);
            }
        }
    }
}
