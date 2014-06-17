package com.vaadin.addon.spreadsheet;

import java.util.LinkedList;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.command.Command;

public class SpreadsheetHistoryManager {

    private int historySize = 20;

    protected int historyIndex = -1;

    protected final LinkedList<Command> commands = new LinkedList<Command>();

    protected final Spreadsheet spreadsheet;

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
     * Adds a command to the end of the line. Discards commands after current
     * one (if any).
     * 
     * @param command
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
     * 
     * @param historyIndex
     *            0-based
     * @return the command at the index or {@link IndexOutOfBoundsException}
     */
    public Command getCommand(int historyIndex) {
        return commands.get(historyIndex);
    }

    /**
     * 
     * @return is {@link #redo()} possible
     */
    public boolean canRedo() {
        return (historyIndex + 1) < commands.size();
    }

    /**
     * 
     * @return is {@link #undo()} possible
     */
    public boolean canUndo() {
        return historyIndex >= 0;
    }

    /**
     * Does redo if possible. Changes the active sheet to match the one the
     * command belonds to, updates the selection if needed.
     */
    public void redo() {
        if (canRedo()) {
            Command command = commands.get(++historyIndex);
            makeSureCorrectSheetActive(command);
            command.execute();
            changeSelection(command);
        }
    }

    /**
     * Does undo if possible. Changes the active sheet to match the one that the
     * command belongs to, updates the selection if needed.
     */
    public void undo() {
        if (canUndo()) {
            Command command = commands.get(historyIndex--);
            makeSureCorrectSheetActive(command);
            command.execute();
            changeSelection(command);
        }
    }

    /**
     * Changes the history size. Discards possible commands that won't fit the
     * size anymore.
     * 
     * @param historySize
     */
    public void setHistorySize(int historySize) {
        this.historySize = historySize;
        discardAllAfter(historySize);
    }

    /**
     * 
     * @return size of history. default is 20
     */
    public int getHistorySize() {
        return historyIndex;
    }

    /**
     * 
     * @return 0-based
     */
    public int getHistoryIndex() {
        return historyIndex;
    }

    protected void makeSureCorrectSheetActive(Command command) {
        if (spreadsheet.getActiveSheetIndex() != command.getActiveSheetIndex()) {
            spreadsheet.setActiveSheetIndex(command.getActiveSheetIndex());
            CellRangeAddress paintedCellRange = command.getPaintedCellRange();
            CellReference selectedCellReference = command
                    .getSelectedCellReference();
            String initialSheetSelection = paintedCellRange != null ? paintedCellRange
                    .formatAsString()
                    : selectedCellReference != null ? selectedCellReference
                            .formatAsString() : "A1";
            spreadsheet.initialSheetSelection = initialSheetSelection;
        }
    }

    protected void changeSelection(Command command) {
        // if the sheet has changed, the selected cell can't be set
        if (!spreadsheet.isRealoadingOnThisRoundtrip()) {
            CellReference selectedCellReference = command
                    .getSelectedCellReference();
            CellRangeAddress paintedCellRange = command.getPaintedCellRange();
            if (selectedCellReference != null) {
                if (paintedCellRange == null) {
                    spreadsheet.getCellSelectionManager()
                            .onSheetAddressChanged(
                                    selectedCellReference.formatAsString());
                } else {
                    spreadsheet.getCellSelectionManager()
                            .handleCellRangeSelection(selectedCellReference,
                                    paintedCellRange);
                }
            } else { // the selected cell value might have changed, thus need to
                     // make sure it gets updated to formula field
                spreadsheet.getCellSelectionManager().reSelectSelectedCell();
            }
        }
    }

    protected void discardAllAfter(int index) {
        while (commands.size() > (index + 1)) {
            commands.removeLast();
        }
    }

}
