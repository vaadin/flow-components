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
 * SpreadsheetHistoryManager is an utility class of the Spreadsheet add-on. This
 * class handles remembering any actions done in the Spreadsheet. The purpose is
 * to allow the user to undo and redo any action.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 */
public class SpreadsheetHistoryManager implements Serializable {

    /**
     * Current index within the history.
     * <p>
     * You can read it in a subclass. The public methods update it after every
     * change to the history, so do not change it directly.
     */
    protected int historyIndex = -1;

    /**
     * All executed commands in chronological order. You can read this list in a
     * subclass; changing it directly has undefined results.
     */
    protected final LinkedList<Command> commands = new LinkedList<Command>();

    /**
     * Target Spreadsheet component.
     */
    protected final Spreadsheet spreadsheet;

    private final CommandStack stack = new CommandStack(commands, 20);
    private final CommandReplayer replayer;

    /**
     * Creates a new history manager for the given Spreadsheet.
     *
     * @param spreadsheet
     *            Target spreadsheet
     */
    public SpreadsheetHistoryManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        this.replayer = new CommandReplayer(spreadsheet);
    }

    /**
     * Clears the history.
     */
    public void clear() {
        stack.clear();
        historyIndex = stack.getCursor();
    }

    /**
     * Adds a command to the end of the command history. Discards commands after
     * the current position (historyIndex) within the history.
     *
     * @param command
     *            Command to add as the latest command in history
     */
    public void addCommand(Command command) {
        SpreadsheetFactory.logMemoryUsage();
        // Through the protected hook, for a subclass that overrides it.
        discardAllAfter(historyIndex);
        stack.push(command);
        historyIndex = stack.getCursor();
        SpreadsheetFactory.logMemoryUsage();
    }

    /**
     * Gets the Command at the given history index.
     *
     * @param historyIndex
     *            Index of Command to get, 0-based
     * @return The command at the index or {@link IndexOutOfBoundsException}
     */
    public Command getCommand(int historyIndex) {
        return commands.get(historyIndex);
    }

    /**
     * Determines if redo is possible at the moment. In practice tells if there
     * is at least one Command available after the current history index.
     *
     * @return true if {@link #redo()} possible, false otherwise.
     */
    public boolean canRedo() {
        return stack.canRedo();
    }

    /**
     * Determines if undo is possible at the moment. In practice tells if there
     * is at least one Command available before the current history index.
     *
     * @return true if {@link #undo()} possible, false otherwise.
     */
    public boolean canUndo() {
        return stack.canUndo();
    }

    /**
     * Does redo if possible. Changes the active sheet to match the one the
     * command belongs to and updates the selection if needed.
     */
    public void redo() {
        Command command = stack.nextForRedo();
        if (command != null) {
            historyIndex = stack.getCursor();
            applyCommand(command);
        }
    }

    /**
     * Does undo if possible. Changes the active sheet to match the one that the
     * command belongs to and updates the selection if needed.
     */
    public void undo() {
        Command command = stack.nextForUndo();
        if (command != null) {
            historyIndex = stack.getCursor();
            applyCommand(command);
        }
    }

    private void applyCommand(Command command) {
        // Calls through this manager's protected hooks so subclasses that
        // override makeSureCorrectSheetActive or changeSelection keep working.
        makeSureCorrectSheetActive(command);
        command.execute();
        changeSelection(command);
        replayer.fireCellValueChangeEvent(command);
    }

    /**
     * Changes the history size. Discards the commands that won't fit the size
     * anymore: those that could be redone first, latest first, then the oldest.
     *
     * @param historySize
     *            New size for Command history
     */
    public void setHistorySize(int historySize) {
        // The commands that could be redone and no longer fit go through the
        // protected hook, for a subclass that overrides it; then the oldest.
        discardAllAfter(Math.max(Math.max(0, historySize) - 1, historyIndex));
        stack.setMaxSize(historySize);
        historyIndex = stack.getCursor();
    }

    /**
     * Gets the current size of the Command history. The default size is 20
     * commands.
     *
     * @return Current size of history.
     */
    public int getHistorySize() {
        return stack.getMaxSize();
    }

    /**
     * Gets the current index within the Command history.
     *
     * @return Current history index, 0-based
     */
    public int getHistoryIndex() {
        return historyIndex;
    }

    /**
     * Ensures that the correct sheet is active, as recorded in the given
     * Command. Subclasses may override to inject custom sheet-switch behavior.
     *
     * @param command
     *            Command to fetch the sheet from
     */
    protected void makeSureCorrectSheetActive(Command command) {
        replayer.makeSureCorrectSheetActive(command);
    }

    /**
     * Applies the cell selection from the given Command. Subclasses may
     * override to inject custom selection behavior.
     *
     * @param command
     *            Command to fetch the cell selection from.
     */
    protected void changeSelection(Command command) {
        replayer.changeSelection(command);
    }

    /**
     * Clears all history after the given history index NOT including the
     * command at the given index. Called when a command is added, for the
     * commands that could be redone, and when the history size changes, for
     * those of them that no longer fit.
     *
     * @param index
     *            History index to start the clearing from.
     */
    protected void discardAllAfter(int index) {
        stack.clipAfter(index);
    }
}
