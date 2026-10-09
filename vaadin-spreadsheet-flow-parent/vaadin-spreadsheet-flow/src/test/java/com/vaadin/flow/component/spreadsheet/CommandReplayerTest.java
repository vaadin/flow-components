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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.command.Command;
import com.vaadin.flow.component.spreadsheet.command.ValueChangeCommand;

/**
 * What {@link CommandReplayer} does around undoing or redoing a command.
 *
 * <p>
 * Pins the three concerns documented on the SUT: switching the active sheet
 * with the initial-selection seed, routing selection updates through the
 * {@link CellSelectionManager}, and firing a {@link CellValueChangeEvent} for
 * {@link ValueChangeCommand} subtypes. {@link SpreadsheetHistoryManagerTest}
 * exercises these paths end-to-end via a real {@code Spreadsheet}; this file
 * targets the same paths directly with a mocked host so the four-way
 * {@code changeSelection} dispatch can be verified at the call-on-collaborator
 * level — the existing test punts on it because a real, headless
 * {@code CellSelectionManager} swallows the routing.
 *
 * <p>
 * Lives in the {@code spreadsheet} package on purpose so the protected
 * {@code Spreadsheet.initialSheetSelection} field is readable.
 */
class CommandReplayerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CellSelectionManager selectionManager;

    private CommandReplayer replayer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(spreadsheet.getCellSelectionManager())
                .thenReturn(selectionManager);
        replayer = new CommandReplayer(spreadsheet);
    }

    // ----- makeSureCorrectSheetActive --------------------------------------

    @Test
    void makeSureCorrectSheetActive_sameSheet_doesNothing() {
        // Pin: when the command's sheet matches the active sheet, the
        // method MUST NOT call setActiveSheetIndex, MUST NOT touch
        // initialSheetSelection. This protects the "no-op" branch.
        when(spreadsheet.getActiveSheetIndex()).thenReturn(2);
        Command command = stubCommand(2, null, null);

        replayer.makeSureCorrectSheetActive(command);

        verify(spreadsheet, never())
                .setActiveSheetIndex(org.mockito.ArgumentMatchers.anyInt());
        assertNull(spreadsheet.initialSheetSelection,
                "initialSheetSelection MUST stay untouched on same-sheet path");
    }

    @Test
    void makeSureCorrectSheetActive_differentSheet_setsActiveAndSeedsSelection() {
        // Pin: differing sheet index triggers setActiveSheetIndex(target)
        // AND seeds initialSheetSelection. With paintedCellRange + selected
        // cell both set, paintedCellRange wins the seed.
        when(spreadsheet.getActiveSheetIndex()).thenReturn(0);
        Command command = stubCommand(1, new CellReference(2, 3),
                new CellRangeAddress(0, 2, 0, 2));

        replayer.makeSureCorrectSheetActive(command);

        verify(spreadsheet).setActiveSheetIndex(1);
        assertEquals("A1:C3", spreadsheet.initialSheetSelection,
                "Painted range MUST win over selected cell when both set");
    }

    @Test
    void makeSureCorrectSheetActive_differentSheet_paintedRangeOnly_seedsRange() {
        when(spreadsheet.getActiveSheetIndex()).thenReturn(0);
        Command command = stubCommand(1, null,
                new CellRangeAddress(1, 3, 1, 3));

        replayer.makeSureCorrectSheetActive(command);

        assertEquals("B2:D4", spreadsheet.initialSheetSelection);
    }

    @Test
    void makeSureCorrectSheetActive_differentSheet_selectedCellOnly_seedsCellAddress() {
        when(spreadsheet.getActiveSheetIndex()).thenReturn(0);
        Command command = stubCommand(1, new CellReference(4, 2), null);

        replayer.makeSureCorrectSheetActive(command);

        assertEquals("C5", spreadsheet.initialSheetSelection);
    }

    @Test
    void makeSureCorrectSheetActive_differentSheet_noSelection_defaultsToA1() {
        // Pin: when both selectedCellReference AND paintedCellRange are
        // null, the seed defaults to "A1". Removing the literal default
        // would silently break command replay onto a swapped sheet.
        when(spreadsheet.getActiveSheetIndex()).thenReturn(0);
        Command command = stubCommand(1, null, null);

        replayer.makeSureCorrectSheetActive(command);

        verify(spreadsheet).setActiveSheetIndex(1);
        assertEquals("A1", spreadsheet.initialSheetSelection);
    }

    @Test
    void makeSureCorrectSheetActive_sameSheet_doesNotConsultSelection() {
        // Pin: same-sheet branch must not compute the initial selection
        // at all. Stub getters that the seeding path would call; verify
        // none of them are queried.
        when(spreadsheet.getActiveSheetIndex()).thenReturn(3);
        Command command = stubCommand(3, new CellReference(0, 0),
                new CellRangeAddress(0, 1, 0, 1));

        replayer.makeSureCorrectSheetActive(command);

        verify(command, never()).getPaintedCellRange();
        verify(command, never()).getSelectedCellReference();
    }

    // ----- changeSelection -------------------------------------------------

    @Test
    void changeSelection_rerenderPending_isNoOp() {
        // Pin: when a full rerender is queued, the seeded
        // initialSheetSelection (set by makeSureCorrectSheetActive) handles
        // selection on the next render and changeSelection MUST short-
        // circuit before touching the selection manager.
        when(spreadsheet.isRerenderPending()).thenReturn(true);
        Command command = stubCommand(0, new CellReference(0, 0), null);

        replayer.changeSelection(command);

        verify(spreadsheet, never()).getCellSelectionManager();
        verify(selectionManager, never()).reSelectSelectedCell();
        verify(selectionManager, never()).onSheetAddressChanged(any(),
                org.mockito.ArgumentMatchers.anyBoolean());
        verify(selectionManager, never()).handleCellRangeSelection(any(), any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void changeSelection_nullSelection_callsReSelectSelectedCell() {
        // Pin the "null selection" branch: routes to reSelectSelectedCell
        // (per the source comment: "selected cell value might have changed,
        // formula field needs refresh"). Not the two-arg path.
        when(spreadsheet.isRerenderPending()).thenReturn(false);
        Command command = stubCommand(0, null, null);

        replayer.changeSelection(command);

        verify(selectionManager).reSelectSelectedCell();
        verify(selectionManager, never()).onSheetAddressChanged(any(),
                org.mockito.ArgumentMatchers.anyBoolean());
        verify(selectionManager, never()).handleCellRangeSelection(any(), any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void changeSelection_singleCell_callsOnSheetAddressChanged_withFalseInteractive() {
        // Pin the "single cell, no range" branch: routes to
        // onSheetAddressChanged with the cell's formatted address and
        // updateOriginatesFromTextField=false. The false flag matters —
        // true would tell the manager the change came from the address bar.
        when(spreadsheet.isRerenderPending()).thenReturn(false);
        Command command = stubCommand(0, new CellReference(2, 3), null);

        replayer.changeSelection(command);

        verify(selectionManager).onSheetAddressChanged("D3", false);
        verify(selectionManager, never()).reSelectSelectedCell();
        verify(selectionManager, never()).handleCellRangeSelection(any(), any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void changeSelection_cellAndRange_callsHandleCellRangeSelection_withTrueScroll() {
        // Pin the "selected cell + painted range" branch: routes to
        // handleCellRangeSelection(selectedCell, paintedRange, true). The
        // trailing "true" tells the manager to scroll into view; flipping
        // it to false would silently break range replay scrolling.
        when(spreadsheet.isRerenderPending()).thenReturn(false);
        CellReference cell = new CellReference(0, 0);
        CellRangeAddress range = new CellRangeAddress(0, 2, 0, 2);
        Command command = stubCommand(0, cell, range);

        replayer.changeSelection(command);

        verify(selectionManager).handleCellRangeSelection(cell, range, true);
        verify(selectionManager, never()).reSelectSelectedCell();
        verify(selectionManager, never()).onSheetAddressChanged(any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    // ----- fireCellValueChangeEvent ----------------------------------------

    @Test
    void fireCellValueChangeEvent_valueChangeCommand_firesWithChangedCells() {
        // Pin: ValueChangeCommand subtype triggers a CellValueChangeEvent
        // carrying the command's changedCells set. The event must reference
        // the same Spreadsheet as source and carry the exact cell set
        // (identity, not a copy — the SUT does no defensive copy).
        Set<CellReference> changedCells = new HashSet<>();
        changedCells.add(new CellReference(0, 0));
        changedCells.add(new CellReference(1, 1));
        ValueChangeCommand command = mock(ValueChangeCommand.class);
        when(command.getChangedCells()).thenReturn(changedCells);

        replayer.fireCellValueChangeEvent(command);

        ArgumentCaptor<ComponentEvent<?>> captor = ArgumentCaptor
                .forClass(ComponentEvent.class);
        verify(spreadsheet).fireEvent(captor.capture());
        ComponentEvent<?> fired = captor.getValue();
        org.junit.jupiter.api.Assertions.assertInstanceOf(
                CellValueChangeEvent.class, fired,
                "Fired event MUST be a CellValueChangeEvent");
        CellValueChangeEvent event = (CellValueChangeEvent) fired;
        assertEquals(changedCells, event.getChangedCells(),
                "Event MUST carry the command's changedCells set");
    }

    @Test
    void fireCellValueChangeEvent_plainCommand_doesNotFireEvent() {
        // Pin: a Command that is NOT a ValueChangeCommand must produce no
        // fireEvent call. Removing the instanceof check would regress
        // command types that intentionally skip value-change notification.
        Command command = mock(Command.class);

        replayer.fireCellValueChangeEvent(command);

        verify(spreadsheet, never()).fireEvent(any(ComponentEvent.class));
    }

    @Test
    void fireCellValueChangeEvent_valueChangeCommand_emptyChangedCells_stillFires() {
        // Pin: empty changedCells does NOT short-circuit. The event still
        // fires AND carries the empty set verbatim. Adding an
        // "if (cells.isEmpty()) return" optimization would regress
        // listeners that count CellValueChangeEvent invocations (e.g.
        // observers measuring undo activity). Capture the event so a
        // regression that fired with a NON-empty placeholder set is also
        // caught.
        Set<CellReference> empty = new HashSet<>();
        ValueChangeCommand command = mock(ValueChangeCommand.class);
        when(command.getChangedCells()).thenReturn(empty);

        replayer.fireCellValueChangeEvent(command);

        ArgumentCaptor<ComponentEvent<?>> captor = ArgumentCaptor
                .forClass(ComponentEvent.class);
        verify(spreadsheet).fireEvent(captor.capture());
        org.junit.jupiter.api.Assertions.assertInstanceOf(
                CellValueChangeEvent.class, captor.getValue());
        CellValueChangeEvent event = (CellValueChangeEvent) captor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(
                event.getChangedCells().isEmpty(),
                "Event MUST carry the empty changedCells set verbatim");
    }

    // ----- helpers ----------------------------------------------------------

    private static Command stubCommand(int activeSheetIndex,
            CellReference selectedCell, CellRangeAddress paintedRange) {
        Command command = mock(Command.class);
        lenient().when(command.getActiveSheetIndex())
                .thenReturn(activeSheetIndex);
        lenient().when(command.getSelectedCellReference())
                .thenReturn(selectedCell);
        lenient().when(command.getPaintedCellRange()).thenReturn(paintedRange);
        return command;
    }
}
