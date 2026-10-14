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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.command.Command;

/**
 * The undo history list and cursor of {@link CommandStack}.
 *
 * <p>
 * Pure data structure — no {@link Spreadsheet} dependency. Pins the overflow
 * and cursor-navigation shapes that {@link SpreadsheetHistoryManager} relies
 * on. The backing list is shared by reference with the host's protected
 * {@code commands} field, so the test exercises that contract too.
 */
class CommandStackTest {

    private LinkedList<Command> backing;
    private CommandStack stack;

    @BeforeEach
    void setUp() {
        backing = new LinkedList<>();
        stack = new CommandStack(backing, 20);
    }

    // ----- construction + initial state -------------------------------------

    @Test
    void freshStack_cursorIsMinusOne_andCannotUndoOrRedo() {
        // Pin the initial state: cursor at -1 (empty), neither undo nor
        // redo possible.
        assertEquals(-1, stack.getCursor());
        assertFalse(stack.canUndo());
        assertFalse(stack.canRedo());
    }

    @Test
    void freshStack_getMaxSize_returnsConstructorValue() {
        assertEquals(20, stack.getMaxSize());
    }

    @Test
    void backingList_isSharedByReference_notCopied() {
        // Pin the load-bearing contract: the LinkedList passed in is the
        // SAME instance the host's protected field holds. Subclasses that
        // read the protected field expect to see live mutations.
        Command cmd = mock(Command.class);
        stack.push(cmd);

        assertSame(cmd, backing.getFirst(),
                "Push must mutate the caller's LinkedList directly");
    }

    // ----- push happy path --------------------------------------------------

    @Test
    void push_singleCommand_advancesCursorToZero() {
        Command cmd = mock(Command.class);
        stack.push(cmd);

        assertEquals(0, stack.getCursor());
        assertEquals(1, backing.size());
        assertSame(cmd, backing.get(0));
    }

    @Test
    void push_multipleCommands_advancesCursorOnePerPush() {
        Command a = mock(Command.class);
        Command b = mock(Command.class);
        Command c = mock(Command.class);
        stack.push(a);
        stack.push(b);
        stack.push(c);

        assertEquals(2, stack.getCursor());
        assertEquals(3, backing.size());
    }

    // ----- push: overflow shape ---------------------------------------------

    @Test
    void push_atExactMaxSize_advancesCursorNormally() {
        // Pin the maxSize=20 boundary: with 20 commands pushed, cursor
        // sits at 19 and no overflow has happened yet.
        for (int i = 0; i < 20; i++) {
            stack.push(mock(Command.class));
        }

        assertEquals(19, stack.getCursor());
        assertEquals(20, backing.size());
    }

    @Test
    void push_overflowingMaxSize_dropsOldest_andDoesNotAdvanceCursor() {
        // Pin the overflow shape: when size > maxSize, removeFirst() drops
        // the oldest command;
        // cursor STAYS at maxSize - 1 instead of advancing to maxSize.
        Command first = mock(Command.class);
        Command second = mock(Command.class);
        stack.push(first); // cursor 0
        for (int i = 0; i < 19; i++) {
            stack.push(mock(Command.class));
        }
        // 20 pushed so far, cursor at 19, size 20, first command still at
        // index 0.
        assertEquals(19, stack.getCursor());
        assertSame(first, backing.get(0));

        // 21st push: size would become 21, exceeds maxSize. The contract
        // is "drop oldest, keep cursor at 19".
        stack.push(second);

        assertEquals(19, stack.getCursor(),
                "Overflow MUST NOT advance the cursor");
        assertEquals(20, backing.size());
        assertFalse(backing.contains(first),
                "Oldest command MUST be dropped on overflow");
        assertSame(second, backing.getLast(),
                "New command MUST land at the tail");
    }

    @Test
    void push_overflowsBy2_dropsOldestTwice() {
        // Pin the contract: overflow drops ONE per push that overflows.
        // Two overflowing pushes drop two oldest commands; cursor stays
        // at maxSize - 1.
        Command first = mock(Command.class);
        Command second = mock(Command.class);
        stack.push(first);
        stack.push(second);
        for (int i = 0; i < 18; i++) {
            stack.push(mock(Command.class));
        }
        // size 20, cursor 19, [first, second, ...] present.

        stack.push(mock(Command.class)); // overflow 1: drops first
        stack.push(mock(Command.class)); // overflow 2: drops second

        assertEquals(19, stack.getCursor());
        assertFalse(backing.contains(first));
        assertFalse(backing.contains(second));
    }

    // ----- push: redo branch discard ---------------------------------------

    @Test
    void push_afterUndo_discardsRedoBranch() {
        // Pin the canonical "after-undo-push-discards-redo" semantic.
        // Push three, undo twice (cursor moves from 2 → 0), then push.
        // The previously-pushed commands at indices 1 and 2 are
        // discarded (clipAfter); the new command lands at index 1.
        Command a = mock(Command.class);
        Command b = mock(Command.class);
        Command c = mock(Command.class);
        Command d = mock(Command.class);
        stack.push(a);
        stack.push(b);
        stack.push(c);
        stack.nextForUndo(); // cursor 2 → 1
        stack.nextForUndo(); // cursor 1 → 0
        assertEquals(0, stack.getCursor());

        stack.push(d);

        assertEquals(1, stack.getCursor());
        assertEquals(2, backing.size());
        assertSame(a, backing.get(0));
        assertSame(d, backing.get(1));
        assertFalse(backing.contains(b));
        assertFalse(backing.contains(c));
    }

    // ----- canUndo / canRedo ------------------------------------------------

    @Test
    void canUndo_falseWhenCursorMinusOne_trueAtCursorZero() {
        assertFalse(stack.canUndo());
        stack.push(mock(Command.class));
        assertTrue(stack.canUndo());
    }

    @Test
    void canRedo_falseAtTopOfStack_trueAfterUndo() {
        stack.push(mock(Command.class));
        stack.push(mock(Command.class));
        assertFalse(stack.canRedo(),
                "Cursor at top of stack: no redo possible");
        stack.nextForUndo();
        assertTrue(stack.canRedo(), "After undo, redo IS possible");
    }

    // ----- nextForUndo / nextForRedo ---------------------------------------

    @Test
    void nextForUndo_returnsCommandAtCursor_andDecrementsCursor() {
        Command a = mock(Command.class);
        Command b = mock(Command.class);
        stack.push(a);
        stack.push(b);

        Command first = stack.nextForUndo();

        assertSame(b, first, "Undo returns the LAST pushed command first");
        assertEquals(0, stack.getCursor());
    }

    @Test
    void nextForUndo_emptyStack_returnsNull_andDoesNotDecrementCursor() {
        Command result = stack.nextForUndo();

        assertNull(result);
        assertEquals(-1, stack.getCursor());
    }

    @Test
    void nextForRedo_afterUndo_returnsTheUndoneCommand_andIncrementsCursor() {
        Command a = mock(Command.class);
        stack.push(a);
        stack.nextForUndo(); // cursor 0 → -1

        Command result = stack.nextForRedo();

        assertSame(a, result);
        assertEquals(0, stack.getCursor());
    }

    @Test
    void nextForRedo_atTopOfStack_returnsNull_andDoesNotMoveCursor() {
        stack.push(mock(Command.class));

        Command result = stack.nextForRedo();

        assertNull(result);
        assertEquals(0, stack.getCursor());
    }

    // ----- clipAfter --------------------------------------------------------

    @Test
    void clipAfter_truncatesToIndexInclusive_doesNotMoveCursor() {
        // Pin: clipAfter(N) leaves indices 0..N. Cursor untouched (the
        // source comment says explicitly "Does not touch the cursor").
        Command a = mock(Command.class);
        Command b = mock(Command.class);
        Command c = mock(Command.class);
        Command d = mock(Command.class);
        stack.push(a);
        stack.push(b);
        stack.push(c);
        stack.push(d);
        int cursorBefore = stack.getCursor();

        stack.clipAfter(1);

        assertEquals(2, backing.size());
        assertSame(a, backing.get(0));
        assertSame(b, backing.get(1));
        assertEquals(cursorBefore, stack.getCursor(),
                "clipAfter MUST NOT touch the cursor");
    }

    @Test
    void clipAfter_minusOne_clearsEverything() {
        stack.push(mock(Command.class));
        stack.push(mock(Command.class));

        stack.clipAfter(-1);

        assertEquals(0, backing.size());
    }

    @Test
    void clipAfter_largerThanSize_isNoOp() {
        stack.push(mock(Command.class));
        stack.push(mock(Command.class));

        stack.clipAfter(99);

        assertEquals(2, backing.size());
    }

    // ----- clear ------------------------------------------------------------

    @Test
    void clear_emptiesListAndResetsCursorToMinusOne() {
        stack.push(mock(Command.class));
        stack.push(mock(Command.class));

        stack.clear();

        assertEquals(0, backing.size());
        assertEquals(-1, stack.getCursor());
        assertFalse(stack.canUndo());
        assertFalse(stack.canRedo());
    }

    // ----- setMaxSize -------------------------------------------------------

    @Test
    void setMaxSize_storesNewMaxSize() {
        stack.setMaxSize(5);

        assertEquals(5, stack.getMaxSize());
    }

    @Test
    void setMaxSize_smaller_keepsTheLatestCommands_andTheCursorOnTheLast() {
        // The oldest commands go: keeping them instead left the cursor past
        // the end of the list, and the next undo failed.
        Command[] pushed = new Command[10];
        for (int i = 0; i < 10; i++) {
            pushed[i] = mock(Command.class);
            stack.push(pushed[i]);
        }

        stack.setMaxSize(3);

        assertEquals(List.of(pushed[7], pushed[8], pushed[9]), backing);
        assertEquals(2, stack.getCursor());
        assertSame(pushed[9], stack.nextForUndo());
    }

    @Test
    void setMaxSize_smaller_dropsWhatCouldBeRedoneFirst_latestFirst() {
        Command[] pushed = new Command[10];
        for (int i = 0; i < 10; i++) {
            pushed[i] = mock(Command.class);
            stack.push(pushed[i]);
        }
        for (int i = 0; i < 4; i++) {
            stack.nextForUndo();
        }
        assertEquals(5, stack.getCursor());

        stack.setMaxSize(8);

        assertEquals(8, backing.size());
        assertEquals(5, stack.getCursor());
        assertSame(pushed[6], stack.nextForRedo());
        assertSame(pushed[7], stack.nextForRedo());
        assertFalse(stack.canRedo());
    }

    @Test
    void setMaxSize_smallerThanWhatWasDone_dropsAllRedoAndTheOldest() {
        Command[] pushed = new Command[10];
        for (int i = 0; i < 10; i++) {
            pushed[i] = mock(Command.class);
            stack.push(pushed[i]);
        }
        for (int i = 0; i < 4; i++) {
            stack.nextForUndo();
        }

        stack.setMaxSize(4);

        assertEquals(List.of(pushed[2], pushed[3], pushed[4], pushed[5]),
                backing);
        assertEquals(3, stack.getCursor());
        assertFalse(stack.canRedo());
        assertSame(pushed[5], stack.nextForUndo());
    }

    @Test
    void setMaxSize_zero_emptiesTheHistory() {
        stack.push(mock(Command.class));
        stack.push(mock(Command.class));

        stack.setMaxSize(0);

        assertEquals(0, backing.size());
        assertEquals(-1, stack.getCursor());
        assertFalse(stack.canUndo());
        assertFalse(stack.canRedo());
    }

    @Test
    void setMaxSize_largerThanCurrent_doesNotShrink() {
        stack.push(mock(Command.class));
        stack.push(mock(Command.class));

        stack.setMaxSize(50);

        assertEquals(2, backing.size());
        assertEquals(50, stack.getMaxSize());
    }
}
