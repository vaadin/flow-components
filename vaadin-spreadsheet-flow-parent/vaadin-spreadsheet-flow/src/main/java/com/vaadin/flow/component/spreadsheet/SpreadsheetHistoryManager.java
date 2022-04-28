package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;
import java.util.LinkedList;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.command.Command;
import com.vaadin.flow.component.spreadsheet.command.ValueChangeCommand;

/**
 * SpreadsheetHistoryManager is an utility class of the Spreadsheet add-on. This
 * class handles remembering any actions done in the Spreadsheet. The purpose is
 * to allow the user to undo and redo any action.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class SpreadsheetHistoryManager implements Serializable {

    private int historySize = 20;

    /**
     * Current index within the history
     */
    protected int historyIndex = -1;

    /**
     * All executed command in chronological order
     */
    protected final LinkedList<Command> commands = new LinkedList<Command>();

    /**
     * Target Spreadsheet component
     */
    protected final Spreadsheet spreadsheet;

    /**
     * Creates a new history manager for the given Spreadsheet.
     *
     * @param spreadsheet
     *            Target spreadsheet
     */
    public SpreadsheetHistoryManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Clears the history.
     */
    public void clear() {
        historyIndex = -1;
        commands.clear();
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
        discardAllAfter(historyIndex);
        commands.add(command);
        if (commands.size() > historySize) {
            commands.removeFirst();
        } else {
            historyIndex++;
        }
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
        return (historyIndex + 1) < commands.size();
    }

    /**
     * Determines if undo is possible at the moment. In practice tells if there
     * is at least one Command available before the current history index.
     *
     * @return true if {@link #undo()} possible, false otherwise.
     */
    public boolean canUndo() {
        return historyIndex >= 0;
    }

    /**
     * Does redo if possible. Changes the active sheet to match the one the
     * command belongs to and updates the selection if needed.
     */
    public void redo() {
        if (canRedo()) {
            Command command = commands.get(++historyIndex);
            makeSureCorrectSheetActive(command);
            command.execute();
            changeSelection(command);
            fireCellValueChangeEvent(command);
        }
    }

    /**
     * Does undo if possible. Changes the active sheet to match the one that the
     * command belongs to and updates the selection if needed.
     */
    public void undo() {
        if (canUndo()) {
            Command command = commands.get(historyIndex--);
            makeSureCorrectSheetActive(command);
            command.execute();
            changeSelection(command);
            fireCellValueChangeEvent(command);
        }
    }

    private void fireCellValueChangeEvent(Command command) {
        if (command instanceof ValueChangeCommand) {
            ValueChangeCommand valueUpdaterCommand = (ValueChangeCommand) command;
            spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet,
                    valueUpdaterCommand.getChangedCells()));
        }
    }

    /**
     * Changes the history size. Discards possible commands that won't fit the
     * size anymore.
     *
     * @param historySize
     *            New size for Command history
     */
    public void setHistorySize(int historySize) {
        this.historySize = historySize;
        discardAllAfter(historySize);
    }

    /**
     * Gets the current size of the Command history. The default size is 20
     * commands.
     *
     * @return Current size of history.
     */
    public int getHistorySize() {
        return historySize;
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
     * Command.
     *
     * @param command
     *            Command to fetch the sheet from
     */
    protected void makeSureCorrectSheetActive(Command command) {
        if (spreadsheet.getActiveSheetIndex() != command
                .getActiveSheetIndex()) {
            spreadsheet.setActiveSheetIndex(command.getActiveSheetIndex());
            CellRangeAddress paintedCellRange = command.getPaintedCellRange();
            CellReference selectedCellReference = command
                    .getSelectedCellReference();
            String initialSheetSelection = paintedCellRange != null
                    ? paintedCellRange.formatAsString()
                    : selectedCellReference != null
                            ? selectedCellReference.formatAsString()
                            : "A1";
            spreadsheet.initialSheetSelection = initialSheetSelection;
        }
    }

    /**
     * Applies the cell selection from the given Command.
     *
     * @param command
     *            Command to fetch the cell selection from.
     */
    protected void changeSelection(Command command) {
        // if the sheet has changed, the selected cell can't be set
        if (!spreadsheet.isRerenderPending()) {
            CellReference selectedCellReference = command
                    .getSelectedCellReference();
            CellRangeAddress paintedCellRange = command.getPaintedCellRange();
            if (selectedCellReference != null) {
                if (paintedCellRange == null) {
                    spreadsheet.getCellSelectionManager().onSheetAddressChanged(
                            selectedCellReference.formatAsString(), false);
                } else {
                    spreadsheet.getCellSelectionManager()
                            .handleCellRangeSelection(selectedCellReference,
                                    paintedCellRange, true);
                }
            } else {
                // the selected cell value might have changed, thus need to
                // make sure it gets updated to formula field
                spreadsheet.getCellSelectionManager().reSelectSelectedCell();
            }
        }
    }

    /**
     * Clears all history after the given history index NOT including the
     * command at the given index.
     *
     * @param index
     *            History index to start the clearing from.
     */
    protected void discardAllAfter(int index) {
        while (commands.size() > (index + 1)) {
            commands.removeLast();
        }
    }
}