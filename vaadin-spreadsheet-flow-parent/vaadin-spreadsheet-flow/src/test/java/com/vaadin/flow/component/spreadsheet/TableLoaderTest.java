/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Loading the tables of the active sheet with {@link TableLoader}.
 *
 * <p>
 * Thin orchestrator over {@link TableRegistry}. Pins:
 *
 * <ul>
 * <li>Latch short-circuit: if {@code isLoadedForActiveSheet} returns true, no
 * tables are reloaded.</li>
 * <li>Per-table sheet filter: only tables whose {@code getSheet()} equals the
 * spreadsheet's active sheet get {@code reload()}.</li>
 * <li>After reload, the registry is latched via
 * {@code markLoadedForActiveSheet}.</li>
 * </ul>
 */
class TableLoaderTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private TableRegistry registry;
    @Mock
    private Sheet activeSheet;
    @Mock
    private Sheet otherSheet;

    private TableLoader loader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loader = new TableLoader(spreadsheet, registry);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(activeSheet);
    }

    @Test
    void loadForActiveSheet_latchSet_earlyReturns_doesNotReload() {
        // Pin the duplicate-call guard: when the latch is already set,
        // no reload calls fire, no markLoaded call fires.
        when(registry.isLoadedForActiveSheet()).thenReturn(true);

        loader.loadForActiveSheet();

        verify(registry, never()).getAll();
        verify(registry, never()).markLoadedForActiveSheet();
    }

    @Test
    void loadForActiveSheet_emptyRegistry_stillLatches() {
        // Pin: even with zero tables, the latch is set after the (empty)
        // iteration so subsequent calls short-circuit.
        when(registry.isLoadedForActiveSheet()).thenReturn(false);
        when(registry.getAll()).thenReturn(new HashSet<>());

        loader.loadForActiveSheet();

        verify(registry).markLoadedForActiveSheet();
    }

    @Test
    void loadForActiveSheet_tableOnActiveSheet_reloaded() {
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.getSheet()).thenReturn(activeSheet);
        when(registry.isLoadedForActiveSheet()).thenReturn(false);
        when(registry.getAll()).thenReturn(new HashSet<>(Arrays.asList(table)));

        loader.loadForActiveSheet();

        verify(table).reload();
        verify(registry).markLoadedForActiveSheet();
    }

    @Test
    void loadForActiveSheet_tableOnOtherSheet_skipped() {
        // Pin the per-table filter: tables whose sheet ≠ active sheet are
        // NOT reloaded. The registry-wide latch still fires.
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.getSheet()).thenReturn(otherSheet);
        when(registry.isLoadedForActiveSheet()).thenReturn(false);
        when(registry.getAll()).thenReturn(new HashSet<>(Arrays.asList(table)));

        loader.loadForActiveSheet();

        verify(table, never()).reload();
        verify(registry).markLoadedForActiveSheet();
    }

    @Test
    void loadForActiveSheet_mixedTables_onlyActiveReloaded() {
        // Pin: each table is filtered independently.
        SpreadsheetTable onActive1 = mock(SpreadsheetTable.class);
        SpreadsheetTable onActive2 = mock(SpreadsheetTable.class);
        SpreadsheetTable onOther = mock(SpreadsheetTable.class);
        when(onActive1.getSheet()).thenReturn(activeSheet);
        when(onActive2.getSheet()).thenReturn(activeSheet);
        when(onOther.getSheet()).thenReturn(otherSheet);
        when(registry.isLoadedForActiveSheet()).thenReturn(false);
        when(registry.getAll()).thenReturn(
                new HashSet<>(Arrays.asList(onActive1, onActive2, onOther)));

        loader.loadForActiveSheet();

        verify(onActive1).reload();
        verify(onActive2).reload();
        verify(onOther, never()).reload();
    }

    @Test
    void loadForActiveSheet_reloadsBeforeMarkingLatch() {
        // Pin the ordering: all reloads finish BEFORE the latch is set.
        // If markLoadedForActiveSheet ran first and a reload threw, the
        // latch would falsely report "loaded" and prevent retry.
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.getSheet()).thenReturn(activeSheet);
        when(registry.isLoadedForActiveSheet()).thenReturn(false);
        when(registry.getAll()).thenReturn(new HashSet<>(Arrays.asList(table)));

        loader.loadForActiveSheet();

        InOrder inOrder = Mockito.inOrder(table, registry);
        inOrder.verify(table).reload();
        inOrder.verify(registry).markLoadedForActiveSheet();
    }

    @Test
    void loadForActiveSheet_calledTwice_secondCallShortCircuits() {
        // Pin the interaction between the two calls: first sets the latch,
        // second sees it and returns.
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.getSheet()).thenReturn(activeSheet);
        when(registry.isLoadedForActiveSheet()).thenReturn(false, true);
        when(registry.getAll()).thenReturn(new HashSet<>(Arrays.asList(table)));

        loader.loadForActiveSheet();
        loader.loadForActiveSheet();

        // table.reload() fired exactly once total.
        verify(table, times(1)).reload();
        verify(registry, times(1)).markLoadedForActiveSheet();
    }

    @Test
    void loadForActiveSheet_emptyRegistry_doesNotConsultActiveSheet() {
        // Sanity: with an empty registry the active sheet isn't queried,
        // because the loop body never runs.
        when(registry.isLoadedForActiveSheet()).thenReturn(false);
        when(registry.getAll()).thenReturn(new HashSet<>());

        loader.loadForActiveSheet();

        verify(spreadsheet, never()).getActiveSheet();
    }
}
