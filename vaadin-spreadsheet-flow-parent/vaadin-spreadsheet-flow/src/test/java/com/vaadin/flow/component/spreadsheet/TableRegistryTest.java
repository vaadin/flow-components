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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Storing, removing, listing and reloading tables in {@link TableRegistry}. The
 * tables are mocks; how a table reloads is its own business.
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
    void freshRegistry_isEmpty() {
        assertTrue(registry.getAll().isEmpty());
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
    void clear_forgetsEveryTable() {
        registry.add(table1);
        registry.add(table3);

        registry.clear();

        assertTrue(registry.getAll().isEmpty());
    }

    @Test
    void reloadForSheet_reloadsTheTablesOfThatSheetAlone() {
        registry.add(table1);
        registry.add(table2);
        registry.add(table3);

        registry.reloadForSheet(sheetA);

        verify(table1).reload();
        verify(table2).reload();
        verify(table3, never()).reload();
    }

    @Test
    void reloadForSheet_reloadsOnce_untilAnotherSheetIsShown() {
        registry.add(table1);

        registry.reloadForSheet(sheetA);
        registry.reloadForSheet(sheetA);
        verify(table1, times(1)).reload();

        registry.markNeedsReload();
        registry.reloadForSheet(sheetA);
        verify(table1, times(2)).reload();
    }

    @Test
    void reloadForSheet_withNoTables_stillCountsAsReloaded() {
        registry.reloadForSheet(sheetA);
        registry.add(table1);

        registry.reloadForSheet(sheetA);

        verify(table1, never()).reload();
    }
}
