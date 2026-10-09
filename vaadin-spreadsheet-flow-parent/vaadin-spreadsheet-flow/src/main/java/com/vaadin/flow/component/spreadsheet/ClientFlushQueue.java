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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * Buffers the cells that need to be pushed (or unpushed) on the next call to
 * {@code updateMarkedCells} on the parent.
 *
 * <p>
 * Two parallel queues:
 *
 * <ul>
 * <li>{@code markedCells} — cell keys whose values have changed and need to be
 * re-sent.</li>
 * <li>{@code removedCells} — {@link CellData} entries for cells that should
 * disappear from the client cache.</li>
 * </ul>
 *
 * <p>
 * Also carries {@code changedFormulaCells} — formula cells whose re-evaluated
 * value differs from the previously-cached value. Held here (rather than on
 * {@link ClientCellCache}) because the flusher reads it to fire the
 * formula-change event and then clears it; it's flush-tied state, not
 * cache-tied state.
 *
 * <p>
 * Pure data buffer: no RPC, no element-property writes. Drained by
 * {@link ClientCacheFlusher}.
 */
final class ClientFlushQueue implements Serializable {

    private final HashSet<String> markedCells = new HashSet<String>();
    private final HashSet<CellData> removedCells = new HashSet<CellData>();
    private HashSet<CellReference> changedFormulaCells = new HashSet<CellReference>();

    ClientFlushQueue() {
    }

    void clear() {
        markedCells.clear();
        removedCells.clear();
    }

    void markForUpdate(String key) {
        markedCells.add(key);
    }

    boolean isMarkedForUpdate(String key) {
        return markedCells.contains(key);
    }

    /**
     * The keys of the cells currently marked for update. A read-only view, so
     * copy it before marking more cells.
     */
    Set<String> getMarkedCells() {
        return Collections.unmodifiableSet(markedCells);
    }

    void enqueueRemoval(CellData cd) {
        removedCells.add(cd);
    }

    void addChangedFormulaCell(CellReference ref) {
        changedFormulaCells.add(ref);
    }

    /**
     * Snapshot of cells currently queued for removal. The flusher drains this
     * after sending; callers must not mutate the returned set.
     */
    Set<CellData> getRemovedCells() {
        return removedCells;
    }

    /**
     * Returns the changed-formula-cells set and resets the field to a fresh
     * empty set, rather than clearing it, so a listener can iterate the
     * returned set while new changes are recorded.
     */
    Set<CellReference> drainChangedFormulaCells() {
        Set<CellReference> snapshot = changedFormulaCells;
        changedFormulaCells = new HashSet<CellReference>();
        return snapshot;
    }

    boolean hasChangedFormulaCells() {
        return !changedFormulaCells.isEmpty();
    }
}
