/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Deleting a table with {@link TableLifecycle}.
 *
 * <p>
 * Pins {@code delete}'s side-effect cascade:
 *
 * <ol>
 * <li>Always: unregister, through the component's {@code unregisterTable}, so
 * an override of it applies.</li>
 * <li>If the table's sheet is NOT active → early-return; no popup cleanup, no
 * filter clear, no table.clear.</li>
 * <li>If active: for each popup button, clear it via
 * {@code spreadsheet.setPopup(ref, null)}; then if it's a filter table, clear
 * all filters; finally {@code table.clear()}.</li>
 * </ol>
 */
class TableLifecycleTest {

    @Mock
    private Spreadsheet spreadsheet;

    private TableLifecycle lifecycle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lifecycle = new TableLifecycle(spreadsheet);
    }

    @Test
    void delete_alwaysUnregistersFromRegistry() {
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.isTableSheetCurrentlyActive()).thenReturn(false);

        lifecycle.delete(table);

        verify(spreadsheet).unregisterTable(table);
    }

    @Test
    void delete_inactiveSheet_earlyReturnsAfterRegistryRemove() {
        // Pin: when the table's sheet isn't currently active, NONE of the
        // post-unregister side effects fire.
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.isTableSheetCurrentlyActive()).thenReturn(false);

        lifecycle.delete(table);

        verify(spreadsheet).unregisterTable(table);
        verify(table, never()).getPopupButtons();
        verify(table, never()).clear();
        verify(spreadsheet, never()).setPopup(any(CellReference.class),
                any(PopupButton.class));
    }

    @Test
    void delete_activeSheet_clearsEveryPopupButton() {
        // Pin: every popup button on the active-sheet table has its
        // setPopup(ref, null) fired via the spreadsheet.
        PopupButton btn1 = mock(PopupButton.class);
        PopupButton btn2 = mock(PopupButton.class);
        CellReference ref1 = new CellReference("Sheet1", 0, 0, true, true);
        CellReference ref2 = new CellReference("Sheet1", 1, 1, true, true);
        when(btn1.getCellReference()).thenReturn(ref1);
        when(btn2.getCellReference()).thenReturn(ref2);
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.isTableSheetCurrentlyActive()).thenReturn(true);
        when(table.getPopupButtons()).thenReturn(Arrays.asList(btn1, btn2));

        lifecycle.delete(table);

        verify(spreadsheet).setPopup(eq(ref1), isNull());
        verify(spreadsheet).setPopup(eq(ref2), isNull());
    }

    @Test
    void delete_activeSheet_noPopupButtons_skipsSetPopupCalls() {
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.isTableSheetCurrentlyActive()).thenReturn(true);
        when(table.getPopupButtons()).thenReturn(Collections.emptyList());

        lifecycle.delete(table);

        verify(spreadsheet, never()).setPopup(any(CellReference.class),
                any(PopupButton.class));
        verify(table).clear();
    }

    @Test
    void delete_filterTable_clearsAllFilters() {
        // Pin: SpreadsheetFilterTable triggers clearAllFilters before
        // table.clear(). Plain tables skip this step.
        SpreadsheetFilterTable filterTable = mock(SpreadsheetFilterTable.class);
        when(filterTable.isTableSheetCurrentlyActive()).thenReturn(true);
        when(filterTable.getPopupButtons()).thenReturn(Collections.emptyList());

        lifecycle.delete(filterTable);

        verify(filterTable).clearAllFilters();
        verify(filterTable).clear();
    }

    @Test
    void delete_nonFilterTable_doesNotCallClearAllFilters() {
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.isTableSheetCurrentlyActive()).thenReturn(true);
        when(table.getPopupButtons()).thenReturn(Collections.emptyList());

        lifecycle.delete(table);

        verify(table).clear();
        // No clearAllFilters method exists on the non-filter subtype, but
        // verify the post-popup branch still reaches table.clear().
    }

    @Test
    void delete_activeSheet_orderedCascade_unregisterFirst_clearLast() {
        // Pin the canonical order:
        // 1. unregisterTable
        // 2. setPopup(ref, null) for each button
        // 3. (filter table only) clearAllFilters
        // 4. table.clear
        SpreadsheetFilterTable table = mock(SpreadsheetFilterTable.class);
        PopupButton btn = mock(PopupButton.class);
        CellReference ref = new CellReference("Sheet1", 0, 0, true, true);
        when(btn.getCellReference()).thenReturn(ref);
        when(table.isTableSheetCurrentlyActive()).thenReturn(true);
        when(table.getPopupButtons()).thenReturn(Arrays.asList(btn));

        lifecycle.delete(table);

        InOrder inOrder = Mockito.inOrder(spreadsheet, table);
        inOrder.verify(spreadsheet).unregisterTable(table);
        inOrder.verify(spreadsheet).setPopup(ref, null);
        inOrder.verify(table).clearAllFilters();
        inOrder.verify(table).clear();
    }

    @Test
    void delete_activeSheet_singleSetPopup_thenClear() {
        // Sanity: one popup button → one setPopup call → table.clear once.
        PopupButton btn = mock(PopupButton.class);
        CellReference ref = new CellReference("Sheet1", 0, 0, true, true);
        when(btn.getCellReference()).thenReturn(ref);
        SpreadsheetTable table = mock(SpreadsheetTable.class);
        when(table.isTableSheetCurrentlyActive()).thenReturn(true);
        when(table.getPopupButtons()).thenReturn(Arrays.asList(btn));

        lifecycle.delete(table);

        verify(spreadsheet, times(1)).setPopup(ref, null);
        verify(table, times(1)).clear();
    }
}
