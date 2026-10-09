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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Storing, removing and listing tables in {@link TableRegistry}, and its loaded
 * latch.
 *
 * <p>
 * Pure storage class with a single latch ({@code tablesLoaded}). Pins:
 *
 * <ul>
 * <li>HashSet add/remove/getAll semantics.</li>
 * <li>{@code getForSheet} filters by sheet identity (via {@code equals}), so
 * two tables on the same sheet both come back together while a table on a
 * different sheet does not.</li>
 * <li>The {@code tablesLoaded} latch is initially false, flips true via
 * {@code markLoadedForActiveSheet}, and flips back to false via
 * {@code markNeedsReload}.</li>
 * </ul>
 */
class TableRegistryTest {

    private TableRegistry registry;
    private SpreadsheetTable table1;
    private SpreadsheetTable table2;
    private SpreadsheetTable table3;
    private Sheet sheetA;
    private Sheet sheetB;

    @BeforeEach
    void setUp() {
        registry = new TableRegistry();
        sheetA = mock(Sheet.class);
        sheetB = mock(Sheet.class);
        table1 = mock(SpreadsheetTable.class);
        table2 = mock(SpreadsheetTable.class);
        table3 = mock(SpreadsheetTable.class);
        when(table1.getSheet()).thenReturn(sheetA);
        when(table2.getSheet()).thenReturn(sheetA);
        when(table3.getSheet()).thenReturn(sheetB);
    }

    @Test
    void freshRegistry_isEmpty_andNotLoaded() {
        assertTrue(registry.getAll().isEmpty());
        assertFalse(registry.isLoadedForActiveSheet());
    }

    @Test
    void addAndRemove_areHashSetSemantics() {
        registry.add(table1);
        registry.add(table2);
        assertEquals(2, registry.getAll().size());

        registry.remove(table1);
        assertEquals(1, registry.getAll().size());
        assertTrue(registry.getAll().contains(table2));
    }

    @Test
    void addSameTableTwice_isIdempotent() {
        registry.add(table1);
        registry.add(table1);
        assertEquals(1, registry.getAll().size());
    }

    @Test
    void remove_missingTable_isNoop() {
        registry.add(table1);
        registry.remove(table2);
        assertEquals(1, registry.getAll().size());
    }

    @Test
    void getAll_returnsLiveReference() {
        // Pin: getAll returns the underlying HashSet directly (no
        // defensive copy). Mutations through the returned reference
        // affect the registry — this is INTENTIONAL per the source
        // comment in TableRegistry, kept for performance (the registry
        // is read-heavy from SpreadsheetFactory and SheetChangeDispatcher).
        // A future refactor that adds a defensive copy here would break
        // existing callers that rely on the live mutation. If we ever
        // want to harden the API, the change must come WITH a sweep of
        // the call sites.
        registry.add(table1);
        HashSet<SpreadsheetTable> all = registry.getAll();
        all.add(table2);
        assertEquals(2, registry.getAll().size());
    }

    @Test
    void getForSheet_filtersBySheetIdentity() {
        registry.add(table1); // sheetA
        registry.add(table2); // sheetA
        registry.add(table3); // sheetB

        List<SpreadsheetTable> forSheetA = registry.getForSheet(sheetA);
        assertEquals(2, forSheetA.size());
        assertTrue(forSheetA.contains(table1));
        assertTrue(forSheetA.contains(table2));
        assertFalse(forSheetA.contains(table3));
    }

    @Test
    void getForSheet_unmatchedSheet_returnsEmptyList() {
        registry.add(table1);
        List<SpreadsheetTable> result = registry.getForSheet(sheetB);
        assertTrue(result.isEmpty());
    }

    @Test
    void getForSheet_returnsFreshList_notLiveReference() {
        // Pin: getForSheet builds a new ArrayList per call — mutations of
        // the returned list don't affect the registry.
        registry.add(table1);
        List<SpreadsheetTable> result = registry.getForSheet(sheetA);
        result.clear();
        assertEquals(1, registry.getAll().size());
    }

    @Test
    void tablesLoadedLatch_initiallyFalse() {
        assertFalse(registry.isLoadedForActiveSheet());
    }

    @Test
    void markLoadedForActiveSheet_flipsLatchTrue() {
        registry.markLoadedForActiveSheet();
        assertTrue(registry.isLoadedForActiveSheet());
    }

    @Test
    void markNeedsReload_flipsLatchFalse() {
        registry.markLoadedForActiveSheet();
        registry.markNeedsReload();
        assertFalse(registry.isLoadedForActiveSheet());
    }

    @Test
    void latchAndTables_areIndependent() {
        // Pin: add/remove don't affect the latch; markLoaded* doesn't
        // affect the tables set. The two pieces of state are orthogonal.
        registry.markLoadedForActiveSheet();
        registry.add(table1);
        registry.remove(table1);
        assertTrue(registry.isLoadedForActiveSheet());
        assertTrue(registry.getAll().isEmpty());
    }
}
