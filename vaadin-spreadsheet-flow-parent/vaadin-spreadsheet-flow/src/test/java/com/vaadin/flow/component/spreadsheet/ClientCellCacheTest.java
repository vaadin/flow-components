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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * The record {@link ClientCellCache} keeps of the cells sent to the client.
 *
 * <p>
 * Pins the cache's pure-data behavior: separate plain / formula sets,
 * try-plain-first drop semantics, prune-by-row-range and prune-by-column
 * operations.
 *
 * <p>
 * Pure-logic tests — no POI fixture needed. The cache stores cell-key strings
 * (the {@link SpreadsheetUtil#toKey} format `"col<N> row<M>"`) and operates
 * purely on those strings.
 */
class ClientCellCacheTest {

    private ClientCellCache cache;

    @BeforeEach
    void setUp() {
        cache = new ClientCellCache();
    }

    @Test
    void freshCache_containsNothing() {
        assertFalse(cache.containsNormal("col1 row1"));
        assertFalse(cache.containsFormula("col1 row1"));
    }

    @Test
    void markNormalSent_containsNormalReturnsTrue_containsFormulaStaysFalse() {
        cache.markNormalSent("col2 row3");
        assertTrue(cache.containsNormal("col2 row3"));
        assertFalse(cache.containsFormula("col2 row3"));
    }

    @Test
    void markFormulaSent_containsFormulaReturnsTrue_containsNormalStaysFalse() {
        cache.markFormulaSent("col5 row7");
        assertTrue(cache.containsFormula("col5 row7"));
        assertFalse(cache.containsNormal("col5 row7"));
    }

    @Test
    void clear_drainsBothSets() {
        cache.markNormalSent("col1 row1");
        cache.markFormulaSent("col2 row2");
        cache.clear();
        assertFalse(cache.containsNormal("col1 row1"));
        assertFalse(cache.containsFormula("col2 row2"));
    }

    @Test
    void dropSent_returnsTrue_whenKeyIsInPlainSet() {
        cache.markNormalSent("col1 row1");
        assertTrue(cache.dropSent("col1 row1"));
        assertFalse(cache.containsNormal("col1 row1"));
    }

    @Test
    void dropSent_returnsTrue_whenKeyIsInFormulaSet() {
        cache.markFormulaSent("col2 row2");
        assertTrue(cache.dropSent("col2 row2"));
        assertFalse(cache.containsFormula("col2 row2"));
    }

    @Test
    void dropSent_returnsFalse_whenKeyIsAbsent() {
        // Pin the original idiom: only-tries-each-set-once, returns false if
        // nowhere.
        assertFalse(cache.dropSent("col99 row99"));
    }

    @Test
    void dropSent_triesPlainSetFirst_thenFormulaSet() {
        // Edge case pinning: if (by inconsistency) a key appears in BOTH sets,
        // dropSent removes from plain first and short-circuits — formula entry
        // remains. Documents the actual behavior of the
        // `if (sentCells.remove(key)) return true;` idiom.
        cache.markNormalSent("col1 row1");
        cache.markFormulaSent("col1 row1");
        assertTrue(cache.dropSent("col1 row1"));
        assertFalse(cache.containsNormal("col1 row1"));
        // Formula entry still present because dropSent short-circuited.
        assertTrue(cache.containsFormula("col1 row1"));
    }

    @Test
    void pruneRowsRange_singleRow_removesPlainAndFormulaEntries() {
        cache.markNormalSent("col1 row5");
        cache.markFormulaSent("col2 row5");
        cache.markNormalSent("col3 row6"); // outside range

        List<CellData> removed = cache.pruneRowsRange(5, 5);

        assertEquals(2, removed.size());
        assertFalse(cache.containsNormal("col1 row5"));
        assertFalse(cache.containsFormula("col2 row5"));
        // Outside-range entry untouched.
        assertTrue(cache.containsNormal("col3 row6"));
    }

    @Test
    void pruneRowsRange_multipleRows_returnsRemovalsWithCorrectRowAndCol() {
        cache.markNormalSent("col2 row3");
        cache.markFormulaSent("col4 row5");

        List<CellData> removed = cache.pruneRowsRange(3, 5);

        assertEquals(2, removed.size());
        // Each removal carries the row index (from the loop) and the column
        // extracted via SpreadsheetUtil.getColumnIndexFromKey.
        boolean foundCol2Row3 = false;
        boolean foundCol4Row5 = false;
        for (CellData cd : removed) {
            if (cd.col == 2 && cd.row == 3) {
                foundCol2Row3 = true;
            }
            if (cd.col == 4 && cd.row == 5) {
                foundCol4Row5 = true;
            }
        }
        assertTrue(foundCol2Row3,
                "Expected col=2 row=3 in removals " + removed);
        assertTrue(foundCol4Row5,
                "Expected col=4 row=5 in removals " + removed);
    }

    @Test
    void pruneRowsRange_emptyCacheOrNoMatch_returnsEmptyList() {
        // Empty cache.
        assertEquals(0, cache.pruneRowsRange(1, 10).size());

        cache.markNormalSent("col1 row100");
        // Range that doesn't match anything.
        assertEquals(0, cache.pruneRowsRange(1, 10).size());
        // Cache untouched.
        assertTrue(cache.containsNormal("col1 row100"));
    }

    @Test
    void pruneColumn_removesAllKeysStartingWithColumnSelector() {
        cache.markNormalSent("col3 row1");
        cache.markNormalSent("col3 row2");
        cache.markFormulaSent("col3 row3");
        cache.markNormalSent("col4 row1"); // different column, should remain

        cache.pruneColumn(3);

        assertFalse(cache.containsNormal("col3 row1"));
        assertFalse(cache.containsNormal("col3 row2"));
        assertFalse(cache.containsFormula("col3 row3"));
        assertTrue(cache.containsNormal("col4 row1"));
    }

    @Test
    void pruneColumn_distinguishesColumnByExactPrefix() {
        // Pin the "col<N> r" prefix discipline: pruning col3 must not also
        // remove col30, col31, etc. The current implementation guards against
        // this by requiring "col3 r" (note trailing space + r) as the prefix.
        cache.markNormalSent("col3 row1");
        cache.markNormalSent("col30 row1");

        cache.pruneColumn(3);

        assertFalse(cache.containsNormal("col3 row1"));
        // Crucial — col30 must survive because its prefix is "col30 r", not
        // "col3 r".
        assertTrue(cache.containsNormal("col30 row1"));
    }
}
