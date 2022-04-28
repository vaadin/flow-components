package com.vaadin.flow.component.spreadsheet.action;

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
 * @since 1.0
 *
 */
@SuppressWarnings("serial")
public class SpreadsheetDefaultActionHandler implements Action.Handler {

    private final List<SpreadsheetAction> defaultActions;

    public SpreadsheetDefaultActionHandler() {
        defaultActions = new ArrayList<SpreadsheetAction>();
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
        ArrayList<Action> temp = new ArrayList<Action>();
        Spreadsheet spreadsheet = (Spreadsheet) sender;
        if (target instanceof SelectionChangeEvent) {
            for (SpreadsheetAction action : defaultActions) {
                if (action.isApplicableForSelection(spreadsheet,
                        (SelectionChangeEvent) target)) {
                    temp.add(action);
                }
            }
        } else if (target instanceof CellRangeAddress) {
            for (SpreadsheetAction action : defaultActions) {
                if (action.isApplicableForHeader(spreadsheet,
                        (CellRangeAddress) target)) {
                    temp.add(action);
                }
            }
        }
        return temp.toArray(new Action[temp.size()]);
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        if (action instanceof SpreadsheetAction) {
            if (target instanceof SelectionChangeEvent) {
                ((SpreadsheetAction) action).executeActionOnSelection(
                        ((Spreadsheet) sender), (SelectionChangeEvent) target);
            } else if (target instanceof CellRangeAddress) {
                ((SpreadsheetAction) action).executeActionOnHeader(
                        ((Spreadsheet) sender), (CellRangeAddress) target);
            }
        }
    }

}
