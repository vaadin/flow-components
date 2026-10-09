/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * The cells {@link ClientFlushQueue} queues for sending to the client.
 *
 * <p>
 * Three independent queues:
 *
 * <ul>
 * <li>{@code markedCells} — keys queued for re-push.</li>
 * <li>{@code removedCells} — {@link CellData} entries queued for client
 * eviction.</li>
 * <li>{@code changedFormulaCells} — formula cells whose re-evaluated value
 * differs; drained-then-replaced (not cleared in place).</li>
 * </ul>
 */
class ClientFlushQueueTest {

    private ClientFlushQueue queue;

    @BeforeEach
    void setUp() {
        queue = new ClientFlushQueue();
    }

    @Test
    void freshQueue_isEmpty() {
        assertFalse(queue.isMarkedForUpdate("col1 row1"));
        assertEquals(0, queue.getRemovedCells().size());
        assertFalse(queue.hasChangedFormulaCells());
    }

    @Test
    void markForUpdate_setsIsMarkedForUpdate() {
        queue.markForUpdate("col5 row2");
        assertTrue(queue.isMarkedForUpdate("col5 row2"));
    }

    @Test
    void markForUpdate_isIdempotent() {
        queue.markForUpdate("col5 row2");
        queue.markForUpdate("col5 row2");
        // Still just one entry; HashSet semantics. Pinned via the read-side
        // check returning true.
        assertTrue(queue.isMarkedForUpdate("col5 row2"));
    }

    @Test
    void enqueueRemoval_addsToRemovedCellsSnapshot() {
        CellData cd = new CellData();
        cd.col = 3;
        cd.row = 7;
        queue.enqueueRemoval(cd);

        Set<CellData> removed = queue.getRemovedCells();
        assertEquals(1, removed.size());
        assertTrue(removed.contains(cd));
    }

    @Test
    void clear_drainsMarkedAndRemoved_butLeavesChangedFormulaCells() {
        // Pin the asymmetry: clear() handles the two flush-side queues but NOT
        // changedFormulaCells (which is drained separately via the
        // drainChangedFormulaCells() snapshot-and-replace operation).
        queue.markForUpdate("col1 row1");
        CellData cd = new CellData();
        cd.col = 1;
        cd.row = 1;
        queue.enqueueRemoval(cd);
        queue.addChangedFormulaCell(new CellReference("A1"));

        queue.clear();

        assertFalse(queue.isMarkedForUpdate("col1 row1"));
        assertEquals(0, queue.getRemovedCells().size());
        // changedFormulaCells preserved — clear() doesn't touch it.
        assertTrue(queue.hasChangedFormulaCells());
    }

    @Test
    void addChangedFormulaCell_setsHasChangedFormulaCells() {
        assertFalse(queue.hasChangedFormulaCells());
        queue.addChangedFormulaCell(new CellReference("B5"));
        assertTrue(queue.hasChangedFormulaCells());
    }

    @Test
    void drainChangedFormulaCells_returnsSnapshotAndResetsToEmpty() {
        // Pin the drain-and-replace semantic: the snapshot the caller gets has
        // the prior contents; the field is reassigned to a fresh empty set so
        // any subsequent additions go into the new set, not the snapshot.
        CellReference a1 = new CellReference("A1");
        CellReference b2 = new CellReference("B2");
        queue.addChangedFormulaCell(a1);
        queue.addChangedFormulaCell(b2);

        Set<CellReference> snapshot = queue.drainChangedFormulaCells();

        assertEquals(2, snapshot.size());
        assertTrue(snapshot.contains(a1));
        assertTrue(snapshot.contains(b2));
        // After drain, queue is empty.
        assertFalse(queue.hasChangedFormulaCells());

        // Adding a new entry after drain does NOT pollute the returned
        // snapshot (the snapshot is the prior set, not the new one).
        queue.addChangedFormulaCell(new CellReference("C3"));
        assertEquals(2, snapshot.size(),
                "Snapshot must be the prior set, not the new one");
    }

    @Test
    void drainChangedFormulaCells_onEmpty_returnsEmptySet() {
        Set<CellReference> snapshot = queue.drainChangedFormulaCells();
        assertEquals(0, snapshot.size());
        assertFalse(queue.hasChangedFormulaCells());
    }

    @Test
    void markedCellsAndRemovedCells_areIndependent() {
        // Both queues can carry the same logical cell with different
        // intents — pin that they don't interfere.
        queue.markForUpdate("col1 row1");
        CellData cd = new CellData();
        cd.col = 1;
        cd.row = 1;
        queue.enqueueRemoval(cd);

        assertTrue(queue.isMarkedForUpdate("col1 row1"));
        assertEquals(1, queue.getRemovedCells().size());
    }
}
