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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.command.Command;
import com.vaadin.flow.component.spreadsheet.command.ValueChangeCommand;

/**
 * Undo and redo with {@link SpreadsheetHistoryManager}.
 *
 * <p>
 * These tests pin the manager's current observable behavior — bugs included —
 * so that a change to it can be verified as behavior-preserving. They do
 * <strong>not</strong> assert that the behavior is correct; they assert that it
 * is unchanged.
 *
 * <p>
 * Test layout, grouped by behavior path:
 * <ol>
 * <li>Constructor defaults and empty-history boundary state.</li>
 * <li>{@code addCommand} / {@code getHistoryIndex} progression including the
 * overflow shape at {@code commands.size() &gt; historySize}.</li>
 * <li>{@code clear} resets to empty.</li>
 * <li>{@code undo} / {@code redo} happy path and out-of-range no-ops.</li>
 * <li>{@code ValueChangeCommand} triggers a {@code CellValueChangeEvent} with
 * the command's changed cells.</li>
 * <li>Sheet-swap path: {@code makeSureCorrectSheetActive} flips active sheet
 * and seeds {@code initialSheetSelection} from painted range / selected cell /
 * default {@code "A1"}.</li>
 * <li>{@code setHistorySize} clips the existing history.</li>
 * <li>{@code addCommand} mid-history discards everything after the cursor.</li>
 * <li>{@code getCommand} bounds checking.</li>
 * </ol>
 *
 * <p>
 * The test lives in the {@code spreadsheet} package on purpose: it needs access
 * to the protected {@code Spreadsheet.initialSheetSelection} field so the
 * sheet-swap assertions can read it.
 */
class SpreadsheetHistoryManagerTest {

    private Spreadsheet spreadsheet;
    private SpreadsheetHistoryManager history;

    @BeforeEach
    void setUp() {
        spreadsheet = new Spreadsheet();
        history = spreadsheet.getSpreadsheetHistoryManager();
    }

    // 1. Empty-history boundary state ----------------------------------------

    @Test
    void newHistory_hasDefaultSizeAndEmptyState() {
        assertEquals(20, history.getHistorySize());
        assertEquals(-1, history.getHistoryIndex());
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoOnEmptyHistory_isNoOp() {
        history.undo();
        assertEquals(-1, history.getHistoryIndex());
        assertFalse(history.canUndo());
    }

    @Test
    void redoOnEmptyHistory_isNoOp() {
        history.redo();
        assertEquals(-1, history.getHistoryIndex());
        assertFalse(history.canRedo());
    }

    // 2. addCommand progression ----------------------------------------------

    @Test
    void addCommand_advancesIndexAndUnlocksUndo() {
        var cmd = new RecordingCommand();
        history.addCommand(cmd);

        assertEquals(0, history.getHistoryIndex());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertSame(cmd, history.getCommand(0));
    }

    @Test
    void addCommand_secondCommand_indexAdvancesToOne() {
        history.addCommand(new RecordingCommand());
        history.addCommand(new RecordingCommand());

        assertEquals(1, history.getHistoryIndex());
        assertTrue(history.canUndo());
    }

    @Test
    void addCommand_overflowAtSizeBoundary_pinsExistingBehavior() {
        // Default history size is 20. Add 21 commands. The first one is
        // dropped via removeFirst(). The 21st addCommand call hits the
        // commands.size() > historySize branch, where historyIndex is
        // intentionally NOT incremented. Pin that exact shape.
        history.setHistorySize(20);

        for (int i = 0; i < 20; i++) {
            history.addCommand(new RecordingCommand());
        }
        // After 20 adds, index is 19 (20 commands present, 0-based).
        assertEquals(19, history.getHistoryIndex());

        history.addCommand(new RecordingCommand());

        // Index stays at 19 even though a new command was added — the first
        // command was dropped to keep size at 20. This is the actual shape
        // of the code today.
        assertEquals(19, history.getHistoryIndex());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    // 3. clear ---------------------------------------------------------------

    @Test
    void clear_resetsToEmptyState() {
        history.addCommand(new RecordingCommand());
        history.addCommand(new RecordingCommand());

        history.clear();

        assertEquals(-1, history.getHistoryIndex());
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }

    // 4. undo / redo happy path ----------------------------------------------

    @Test
    void undo_executesCommandAndDecrementsIndex() {
        var cmd = new RecordingCommand();
        history.addCommand(cmd);

        history.undo();

        assertEquals(1, cmd.executeCount);
        assertEquals(-1, history.getHistoryIndex());
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    void redo_executesCommandAndReadvancesIndex() {
        var cmd = new RecordingCommand();
        history.addCommand(cmd);
        history.undo();

        history.redo();

        assertEquals(2, cmd.executeCount);
        assertEquals(0, history.getHistoryIndex());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoRedo_alternation_pinsExecuteCount() {
        // Pins the fact that undo and redo both call execute() — the command
        // itself is responsible for being its own inverse on the second call.
        var cmd = new RecordingCommand();
        history.addCommand(cmd);

        history.undo();
        history.redo();
        history.undo();
        history.redo();

        assertEquals(4, cmd.executeCount);
    }

    // 5. ValueChangeCommand -> CellValueChangeEvent --------------------------

    @Test
    void undo_withValueChangeCommand_firesCellValueChangeEvent() {
        var changed = new HashSet<CellReference>();
        changed.add(new CellReference(0, 0));
        changed.add(new CellReference(0, 1));
        var cmd = new RecordingValueChangeCommand(changed);
        history.addCommand(cmd);

        var capturedCells = new java.util.concurrent.atomic.AtomicReference<Set<CellReference>>();
        var eventCount = new AtomicInteger();
        spreadsheet.addCellValueChangeListener(event -> {
            eventCount.incrementAndGet();
            capturedCells.set(event.getChangedCells());
        });

        history.undo();

        assertEquals(1, eventCount.get());
        assertEquals(changed, capturedCells.get());
    }

    @Test
    void undo_withNonValueChangeCommand_doesNotFireCellValueChangeEvent() {
        var cmd = new RecordingCommand();
        history.addCommand(cmd);

        var eventCount = new AtomicInteger();
        spreadsheet.addCellValueChangeListener(event -> {
            eventCount.incrementAndGet();
        });

        history.undo();

        assertEquals(0, eventCount.get());
    }

    // 6. Sheet swap (makeSureCorrectSheetActive) -----------------------------

    @Test
    void undo_withDifferentActiveSheet_flipsSheetAndSeedsSelection() {
        spreadsheet.createNewSheet("Second", 10, 10);
        // Active sheet is now Second (index 1). Command targets sheet 0.
        var cmd = new RecordingCommand();
        cmd.activeSheetIndex = 0;
        cmd.selectedCellReference = new CellReference(2, 3);
        history.addCommand(cmd);
        // Move active back to index 1 so undo has to flip it to 0.
        spreadsheet.setActiveSheetIndex(1);

        history.undo();

        assertEquals(0, spreadsheet.getActiveSheetIndex());
        // makeSureCorrectSheetActive seeded initialSheetSelection from the
        // command's selectedCellReference; isRerenderPending is now true so
        // changeSelection skipped, and initialSheetSelection remains set.
        assertEquals("D3", spreadsheet.initialSheetSelection);
    }

    @Test
    void undo_withDifferentActiveSheetAndRange_seedsRangeSelection() {
        spreadsheet.createNewSheet("Second", 10, 10);
        var cmd = new RecordingCommand();
        cmd.activeSheetIndex = 0;
        cmd.selectedCellReference = new CellReference(0, 0);
        cmd.paintedCellRange = new CellRangeAddress(0, 2, 0, 2);
        history.addCommand(cmd);
        spreadsheet.setActiveSheetIndex(1);

        history.undo();

        assertEquals(0, spreadsheet.getActiveSheetIndex());
        // When the command has a painted range, the range wins over the
        // single cell reference in the seed.
        assertEquals("A1:C3", spreadsheet.initialSheetSelection);
    }

    @Test
    void undo_withDifferentActiveSheetAndNoSelection_defaultsToA1() {
        spreadsheet.createNewSheet("Second", 10, 10);
        var cmd = new RecordingCommand();
        cmd.activeSheetIndex = 0;
        // No selectedCellReference, no paintedCellRange.
        history.addCommand(cmd);
        spreadsheet.setActiveSheetIndex(1);

        history.undo();

        assertEquals(0, spreadsheet.getActiveSheetIndex());
        assertEquals("A1", spreadsheet.initialSheetSelection);
    }

    @Test
    void undo_sameActiveSheet_doesNotTouchInitialSelection() {
        // The active sheet matches the command's sheet, so
        // makeSureCorrectSheetActive should not call setInitialSelection or
        // setActiveSheetIndex. initialSheetSelection stays null.
        var cmd = new RecordingCommand();
        cmd.activeSheetIndex = spreadsheet.getActiveSheetIndex();
        cmd.selectedCellReference = new CellReference(0, 0);
        history.addCommand(cmd);

        history.undo();

        // initialSheetSelection was not set by the sheet-swap path;
        // changeSelection
        // ran instead and routed through the selection manager.
        assertNull(spreadsheet.initialSheetSelection);
    }

    // What an undo selects, as the client is told it, is in
    // SelectionAfterUndoTest.

    // 7. setHistorySize clips -----------------------------------------------

    @Test
    void setHistorySize_smaller_keepsTheLatestSteps_andUndoStillWorks() {
        history.setHistorySize(5);
        RecordingCommand[] added = new RecordingCommand[5];
        for (int i = 0; i < 5; i++) {
            added[i] = new RecordingCommand();
            history.addCommand(added[i]);
        }

        history.setHistorySize(2);

        // Keeping the oldest steps left the index past the end of the list,
        // and the next undo threw.
        assertEquals(1, history.getHistoryIndex());
        assertEquals(2, history.getHistorySize());
        assertSame(added[4], history.getCommand(1));
        history.undo();
        assertEquals(0, history.getHistoryIndex());
    }

    // 8. addCommand mid-history discards future -----------------------------

    @Test
    void addCommand_afterUndo_discardsRedoBranch() {
        history.addCommand(new RecordingCommand());
        history.addCommand(new RecordingCommand());
        history.addCommand(new RecordingCommand());
        assertEquals(2, history.getHistoryIndex());

        history.undo();
        history.undo();
        assertEquals(0, history.getHistoryIndex());
        assertTrue(history.canRedo());

        history.addCommand(new RecordingCommand());

        // Old commands at indices 2 and 3 are gone; new command sits at
        // index 1.
        assertEquals(1, history.getHistoryIndex());
        assertFalse(history.canRedo());
    }

    // 8b. the protected discardAllAfter hook ------------------------------

    /** A history whose discardAllAfter records the index it is given. */
    private static final class DiscardRecordingHistory
            extends SpreadsheetHistoryManager {
        final List<Integer> discardedAfter = new ArrayList<>();

        DiscardRecordingHistory(Spreadsheet spreadsheet) {
            super(spreadsheet);
        }

        @Override
        protected void discardAllAfter(int index) {
            discardedAfter.add(index);
            super.discardAllAfter(index);
        }
    }

    @Test
    void addCommand_afterUndo_discardsTheRedoBranchThroughTheProtectedHook() {
        DiscardRecordingHistory recording = new DiscardRecordingHistory(
                spreadsheet);
        recording.addCommand(new RecordingCommand());
        recording.addCommand(new RecordingCommand());
        recording.addCommand(new RecordingCommand());
        recording.undo();
        recording.undo();
        recording.discardedAfter.clear();

        recording.addCommand(new RecordingCommand());

        assertEquals(List.of(0), recording.discardedAfter);
        assertEquals(2, recording.commands.size());
        assertEquals(1, recording.getHistoryIndex());
    }

    @Test
    void setHistorySize_smaller_discardsTheRedoBranchThroughTheProtectedHook() {
        DiscardRecordingHistory recording = new DiscardRecordingHistory(
                spreadsheet);
        for (int i = 0; i < 5; i++) {
            recording.addCommand(new RecordingCommand());
        }
        recording.undo();
        recording.discardedAfter.clear();

        recording.setHistorySize(3);

        // The command that could be redone goes through the hook, then the
        // oldest goes to make room, as before.
        assertEquals(List.of(3), recording.discardedAfter);
        assertEquals(3, recording.commands.size());
        assertEquals(2, recording.getHistoryIndex());
        assertFalse(recording.canRedo());
    }

    // 9. getCommand bounds -------------------------------------------------

    @Test
    void getCommand_validIndex_returnsAddedCommand() {
        var a = new RecordingCommand();
        var b = new RecordingCommand();
        history.addCommand(a);
        history.addCommand(b);

        assertSame(a, history.getCommand(0));
        assertSame(b, history.getCommand(1));
    }

    @Test
    void getCommand_indexOutOfRange_throws() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> history.getCommand(0));
        history.addCommand(new RecordingCommand());
        assertThrows(IndexOutOfBoundsException.class,
                () -> history.getCommand(5));
    }

    // -----------------------------------------------------------------------
    // Fake Command implementations used by these tests
    // -----------------------------------------------------------------------

    private static class RecordingCommand implements Command {

        int executeCount;
        int activeSheetIndex;
        CellReference selectedCellReference;
        CellRangeAddress paintedCellRange;

        @Override
        public void execute() {
            executeCount++;
        }

        @Override
        public int getActiveSheetIndex() {
            return activeSheetIndex;
        }

        @Override
        public void setActiveSheetIndex(int index) {
            this.activeSheetIndex = index;
        }

        @Override
        public CellReference getSelectedCellReference() {
            return selectedCellReference;
        }

        @Override
        public CellRangeAddress getPaintedCellRange() {
            return paintedCellRange;
        }
    }

    private static class RecordingValueChangeCommand extends RecordingCommand
            implements ValueChangeCommand {

        private final Set<CellReference> changed;

        RecordingValueChangeCommand(Set<CellReference> changed) {
            this.changed = changed;
        }

        @Override
        public Set<CellReference> getChangedCells() {
            return changed;
        }
    }
}
