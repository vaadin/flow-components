/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.framework.Action;

/**
 * Default action handler for Spreadsheet actions. By default this handler adds
 * all available actions to the Spreadsheet.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 *
 */
public class SpreadsheetDefaultActionHandler implements Action.Handler {

    private final List<SpreadsheetAction> defaultActions;

    public SpreadsheetDefaultActionHandler() {
        defaultActions = new ArrayList<>();
        defaultActions.add(new HideHeaderAction());
        defaultActions.add(new UnHideHeadersAction());
        defaultActions.add(new MergeCellsAction());
        defaultActions.add(new UnMergeCellsAction());
        defaultActions.add(new InsertDeleteCellCommentAction());
        defaultActions.add(new EditCellCommentAction());
        defaultActions.add(new ShowHideCellCommentAction());
        defaultActions.add(new InsertNewRowAction());
        defaultActions.add(new DeleteRowAction());
        defaultActions.add(new InsertTableAction());
        defaultActions.add(new DeleteTableAction());
    }

    /**
     * Adds the given SpreadsheetAction to this handler.
     *
     * @param action
     *            SpreadsheetAction to add
     */
    public void addSpreadsheetAction(SpreadsheetAction action) {
        defaultActions.add(action);
    }

    /**
     * Removes the given SpreadsheetAction from this handler.
     *
     * @param action
     *            SpreadsheetAction to remove
     * @return true if the action was present in this handler, false otherwise
     */
    public boolean removeSpreadsheetAction(SpreadsheetAction action) {
        return defaultActions.remove(action);
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        ArrayList<Action> temp = new ArrayList<>();
        Spreadsheet spreadsheet = (Spreadsheet) sender;
        if (target instanceof SelectionChangeEvent selection) {
            for (SpreadsheetAction action : defaultActions) {
                if (action.isApplicableForSelection(spreadsheet, selection)) {
                    temp.add(action);
                }
            }
        } else if (target instanceof CellRangeAddress header) {
            for (SpreadsheetAction action : defaultActions) {
                if (action.isApplicableForHeader(spreadsheet, header)) {
                    temp.add(action);
                }
            }
        }
        return temp.toArray(new Action[temp.size()]);
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        if (action instanceof SpreadsheetAction spreadsheetAction) {
            if (target instanceof SelectionChangeEvent selection) {
                spreadsheetAction.executeActionOnSelection((Spreadsheet) sender,
                        selection);
            } else if (target instanceof CellRangeAddress header) {
                spreadsheetAction.executeActionOnHeader((Spreadsheet) sender,
                        header);
            }
        }
    }

}
