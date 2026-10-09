/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * How {@link SheetChangeDispatcher} handles selecting, creating and renaming a
 * sheet.
 *
 * <p>
 * Three client-event handlers: tab selected, new-sheet, rename. Pins the
 * stash-before-mutate ordering for the first two (so the scroll memory has a
 * chance to record before the active-sheet pointer moves).
 */
class SheetChangeDispatcherTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SheetScrollMemory scrollMemory;
    @Mock
    private Sheet oldSheet;
    @Mock
    private Sheet newSheet;

    private SheetChangeDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dispatcher = new SheetChangeDispatcher(spreadsheet, scrollMemory);
        spreadsheet.defaultNewSheetRows = 100;
        spreadsheet.defaultNewSheetColumns = 26;
    }

    // ----- onSheetSelected --------------------------------------------------

    @Test
    void onSheetSelected_stashesScroll_atOldIndex_beforeActivating() {
        // Pin: scroll position is stashed against the OLD (current) sheet
        // index BEFORE the active sheet switches.
        when(spreadsheet.getSheetIndex()).thenReturn(2);
        when(spreadsheet.getActiveSheet()).thenReturn(oldSheet, newSheet);

        dispatcher.onSheetSelected(3, 50, 75);

        InOrder inOrder = Mockito.inOrder(scrollMemory, spreadsheet);
        // oldIndex = abs(2) - 1 = 1 → stash at index 1.
        inOrder.verify(scrollMemory).rememberForSheetAtIndex(1, 50, 75);
        inOrder.verify(spreadsheet).setActiveSheetIndex(3);
    }

    @Test
    void onSheetSelected_negativeSheetIndex_normalized_viaAbsoluteValue() {
        // Pin a defensive case: the source comment says "the very rare
        // occasion when the sheet has been selected and the selected
        // sheet value is still negative". abs() normalizes — index -3
        // becomes oldIndex 2 (= |-3| - 1).
        when(spreadsheet.getSheetIndex()).thenReturn(-3);
        when(spreadsheet.getActiveSheet()).thenReturn(oldSheet, newSheet);

        dispatcher.onSheetSelected(0, 10, 20);

        verify(scrollMemory).rememberForSheetAtIndex(2, 10, 20);
    }

    @Test
    void onSheetSelected_firesSheetChangeEventWithOldAndNewSheet() {
        when(spreadsheet.getSheetIndex()).thenReturn(1);
        // Active sheet returns oldSheet for the pre-switch read, newSheet
        // for the post-switch read.
        when(spreadsheet.getActiveSheet()).thenReturn(oldSheet, newSheet);

        dispatcher.onSheetSelected(2, 0, 0);

        verify(spreadsheet).fireSheetChangeEvent(oldSheet, newSheet);
    }

    @Test
    void onSheetSelected_orderedCascade_stashThenActivateThenFire() {
        when(spreadsheet.getSheetIndex()).thenReturn(1);
        when(spreadsheet.getActiveSheet()).thenReturn(oldSheet, newSheet);

        dispatcher.onSheetSelected(2, 30, 40);

        InOrder inOrder = Mockito.inOrder(scrollMemory, spreadsheet);
        inOrder.verify(scrollMemory).rememberForSheetAtIndex(0, 30, 40);
        inOrder.verify(spreadsheet).setActiveSheetIndex(2);
        inOrder.verify(spreadsheet).fireSheetChangeEvent(oldSheet, newSheet);
    }

    // ----- onNewSheetCreated ------------------------------------------------

    @Test
    void onNewSheetCreated_stashesAtCurrentIndex_thenCreates() {
        when(spreadsheet.getSheetIndex()).thenReturn(2);

        dispatcher.onNewSheetCreated(15, 25);

        InOrder inOrder = Mockito.inOrder(scrollMemory, spreadsheet);
        // Stash at sheetIndex - 1 = 1 (no abs() on this path — different
        // from onSheetSelected).
        inOrder.verify(scrollMemory).rememberForSheetAtIndex(1, 15, 25);
        inOrder.verify(spreadsheet).createNewSheet(null, 100, 26);
    }

    @Test
    void onNewSheetCreated_usesSpreadsheetDefaults_forRowsAndColumns() {
        // Pin: createNewSheet is invoked with the parent's default-rows /
        // default-columns fields. Changing those changes the new sheet's
        // shape.
        spreadsheet.defaultNewSheetRows = 50;
        spreadsheet.defaultNewSheetColumns = 10;
        when(spreadsheet.getSheetIndex()).thenReturn(1);

        dispatcher.onNewSheetCreated(0, 0);

        verify(spreadsheet).createNewSheet(null, 50, 10);
    }

    // ----- onSheetRename ----------------------------------------------------

    @Test
    void onSheetRename_translatesVisibleIndex_thenRenamesViaSpreadsheet() {
        // Pin: visible index → POI index via getVisibleSheetPOIIndex,
        // then setSheetNameWithPOIIndex.
        when(spreadsheet.getVisibleSheetPOIIndex(2)).thenReturn(3);

        dispatcher.onSheetRename(2, "Renamed");

        verify(spreadsheet).setSheetNameWithPOIIndex(3, "Renamed");
    }

    @Test
    void onSheetRename_doesNotTouchScrollMemory_norActiveSheet() {
        // Pin: rename is a pure metadata change. No scroll-stash, no
        // active-sheet shuffle, no sheet-change event.
        when(spreadsheet.getVisibleSheetPOIIndex(0)).thenReturn(0);

        dispatcher.onSheetRename(0, "NewName");

        verify(scrollMemory, never()).rememberForSheetAtIndex(anyInt(),
                anyInt(), anyInt());
        verify(spreadsheet, never()).setActiveSheetIndex(anyInt());
        verify(spreadsheet, never()).fireSheetChangeEvent(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
