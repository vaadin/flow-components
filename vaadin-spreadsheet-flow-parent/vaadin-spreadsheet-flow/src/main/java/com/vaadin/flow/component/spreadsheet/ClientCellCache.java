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
import java.util.List;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * Tracks which cells the server has already pushed to the client and is
 * therefore safe to assume cached there.
 *
 * <p>
 * Two parallel sets — one for plain cells, one for formula cells — keyed by the
 * cell key string ({@link SpreadsheetUtil#toKey}). Formula cells are tracked
 * separately because they get re-pushed every flush (formula re-evaluation may
 * change the displayed value even though the formula text is unchanged),
 * whereas plain cells only get pushed when their value is marked dirty.
 *
 * <p>
 * Prune operations match the {@link SpreadsheetUtil} cell-key format
 * ({@code "col<N> row<M>"}); they're how the cache participates in row-range
 * deletions and column-clear events. The cache surface up to this point only
 * stores and queries strings — it does not invoke RPC or talk to the client;
 * that's {@link ClientCacheFlusher}'s job.
 */
final class ClientCellCache implements Serializable {

    private final HashSet<String> sentCells = new HashSet<String>();
    private final HashSet<String> sentFormulaCells = new HashSet<String>();

    ClientCellCache() {
    }

    void clear() {
        sentCells.clear();
        sentFormulaCells.clear();
    }

    boolean containsNormal(String key) {
        return sentCells.contains(key);
    }

    boolean containsFormula(String key) {
        return sentFormulaCells.contains(key);
    }

    void markNormalSent(String key) {
        sentCells.add(key);
    }

    void markFormulaSent(String key) {
        sentFormulaCells.add(key);
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
}
