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

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.command.Command;
import com.vaadin.flow.component.spreadsheet.command.ValueChangeCommand;

/**
 * Applies the side effects of replaying a {@link Command} on a target
 * {@link Spreadsheet}. Three concerns, one per method:
 * <ul>
 * <li>{@link #makeSureCorrectSheetActive(Command)} — switch active sheet to
 * match the command and seed the initial selection address.</li>
 * <li>{@link #changeSelection(Command)} — route the selection update through
 * the {@link CellSelectionManager} based on whether the command carries a
 * single cell or a range.</li>
 * <li>{@link #fireCellValueChangeEvent(Command)} — emit a
 * {@link CellValueChangeEvent} when the command implements
 * {@link ValueChangeCommand}.</li>
 * </ul>
 *
 * <p>
 * Same-package access is used for the two operations that the
 * {@code Spreadsheet} class does not expose publicly: the selection a sheet is
 * shown with, and the {@code fireEvent} protected method. The replayer lives in
 * the same package as {@code Spreadsheet} and is otherwise package-private; no
 * public surface is added to {@code Spreadsheet} for this collaboration.
 */
final class CommandReplayer implements Serializable {

    private final Spreadsheet spreadsheet;

    CommandReplayer(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Switches the active sheet to the one recorded in the command if it
     * differs from the currently active sheet, and has the sheet shown with the
     * command's selection once the client has it.
     */
    void makeSureCorrectSheetActive(Command command) {
        if (spreadsheet.getActiveSheetIndex() != command
                .getActiveSheetIndex()) {
            spreadsheet.setActiveSheetIndex(command.getActiveSheetIndex());
            spreadsheet.pendingSelection()
                    .select(computeInitialSelection(command));
        }
    }

    /**
     * The command's selection as an address of the sheet shown: without the
     * sheet's name, which the commands put in their cell references and the
     * address field does not read.
     */
    private static String computeInitialSelection(Command command) {
        CellRangeAddress paintedCellRange = command.getPaintedCellRange();
        CellReference selectedCellReference = command
                .getSelectedCellReference();
        return paintedCellRange != null ? paintedCellRange.formatAsString()
                : selectedCellReference != null
                        ? selectedCellReference.formatAsString(false)
                        : "A1";
    }

    /**
     * Applies the command's selection by delegating to the
     * {@link CellSelectionManager}. Skips when a full rerender is already
     * pending — in that case the seeded initialSheetSelection (set by
     * {@link #makeSureCorrectSheetActive}) handles the post-rerender selection.
     * A command that selects nothing leaves the selection: what it changes in
     * the cells refreshes the formula bar, as any change of cells does.
     */
    void changeSelection(Command command) {
        if (spreadsheet.isRerenderPending()) {
            return;
        }
        CellReference selectedCellReference = command
                .getSelectedCellReference();
        CellRangeAddress paintedCellRange = command.getPaintedCellRange();
        if (selectedCellReference == null) {
            return;
        }
        if (paintedCellRange == null) {
            // Without the sheet's name, as computeInitialSelection.
            spreadsheet.getCellSelectionManager().onSheetAddressChanged(
                    selectedCellReference.formatAsString(false), false);
        } else {
            spreadsheet.getCellSelectionManager().handleCellRangeSelection(
                    selectedCellReference, paintedCellRange, true);
        }
    }

    /**
     * Fires a {@link CellValueChangeEvent} for the cells the command reports as
     * changed. No-op for commands that do not implement
     * {@link ValueChangeCommand}.
     */
    void fireCellValueChangeEvent(Command command) {
        if (command instanceof ValueChangeCommand) {
            ValueChangeCommand valueUpdaterCommand = (ValueChangeCommand) command;
            spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet,
                    valueUpdaterCommand.getChangedCells()));
        }
    }
}
