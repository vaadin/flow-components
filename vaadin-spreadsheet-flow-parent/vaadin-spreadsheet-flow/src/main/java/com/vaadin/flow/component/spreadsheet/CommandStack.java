/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.LinkedList;

import com.vaadin.flow.component.spreadsheet.command.Command;

/**
 * Bounded {@link Command} stack with a cursor for undo/redo navigation. Pure
 * data structure — no {@link Spreadsheet} dependency. Owns the cursor and the
 * maximum size; the backing list is shared with
 * {@link SpreadsheetHistoryManager}'s protected {@code commands} field so
 * subclasses that read that field continue to see the live list.
 *
 * <p>
 * Two behaviours that {@code SpreadsheetHistoryManager} has always had:
 * <ul>
 * <li>{@link #push} when the list overflows {@code maxSize} drops the oldest
 * command via {@code removeFirst()} and does <em>not</em> advance the cursor in
 * that branch.</li>
 * <li>{@link #setMaxSize} calls {@link #clipAfter} with the new max size as the
 * cursor argument, which is how
 * {@code SpreadsheetHistoryManager.setHistorySize} has always behaved.</li>
 * </ul>
 */
final class CommandStack implements Serializable {

    private final LinkedList<Command> commands;
    private int cursor = -1;
    private int maxSize;

    /**
     * @param backingList
     *            the list that holds the commands. Shared by reference with the
     *            host so it can expose the same list as a protected field.
     * @param initialMaxSize
     *            initial bound on the number of retained commands.
     */
    CommandStack(LinkedList<Command> backingList, int initialMaxSize) {
        this.commands = backingList;
        this.maxSize = initialMaxSize;
    }

    void clear() {
        commands.clear();
        cursor = -1;
    }

    /**
     * Pushes a new command on top of the cursor. Discards anything in the redo
     * branch first. When the resulting list size exceeds {@code maxSize}, drops
     * the oldest command instead of advancing the cursor.
     */
    void push(Command command) {
        clipAfter(cursor);
        commands.add(command);
        if (commands.size() > maxSize) {
            commands.removeFirst();
        } else {
            cursor++;
        }
    }

    boolean canUndo() {
        return cursor >= 0;
    }

    boolean canRedo() {
        return cursor + 1 < commands.size();
    }

    /**
     * Returns the next command to apply for an undo and decrements the cursor,
     * or {@code null} if undo is not currently possible.
     */
    Command nextForUndo() {
        if (!canUndo()) {
            return null;
        }
        return commands.get(cursor--);
    }

    /**
     * Returns the next command to apply for a redo and advances the cursor, or
     * {@code null} if redo is not currently possible.
     */
    Command nextForRedo() {
        if (!canRedo()) {
            return null;
        }
        return commands.get(++cursor);
    }

    /**
     * Discards all commands at indices greater than {@code afterIndex}. Does
     * not touch the cursor.
     */
    void clipAfter(int afterIndex) {
        while (commands.size() > (afterIndex + 1)) {
            commands.removeLast();
        }
    }

    int getCursor() {
        return cursor;
    }

    int getMaxSize() {
        return maxSize;
    }

    void setMaxSize(int newMaxSize) {
        this.maxSize = newMaxSize;
        // Keeps the first (newMaxSize + 1) commands and leaves the cursor
        // where it is, as setHistorySize always has.
        clipAfter(newMaxSize);
    }
}
