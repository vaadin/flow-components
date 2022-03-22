package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.KeyMapper;

/**
 * ContextMenuManager is an utility class for the Spreadsheet component. This
 * class handles all context menu -related tasks within the Spreadsheet it is
 * tied to.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class ContextMenuManager implements Serializable {

    private static final Logger LOGGER = Logger
            .getLogger(ContextMenuManager.class.getName());

    private LinkedList<Handler> actionHandlers;

    private KeyMapper<Action> actionMapper;

    private final Spreadsheet spreadsheet;

    private int contextMenuHeaderIndex = -1;

    /**
     * Constructs a new ContextMenuManager and ties it to the given Spreadsheet.
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public ContextMenuManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Adds the given context menu action handler to the target spreadsheet.
     * 
     * @param actionHandler
     *            Handler to add
     */
    public void addActionHandler(Handler actionHandler) {
        if (actionHandler != null) {
            if (actionHandlers == null) {
                actionHandlers = new LinkedList<Action.Handler>();
                actionMapper = new KeyMapper<Action>();
            }
            if (!actionHandlers.contains(actionHandler)) {
                actionHandlers.add(actionHandler);
            }
        }
    }

    /**
     * Removes the given context menu action handler from the target
     * spreadsheet.
     * 
     * @param actionHandler
     *            Handler to remove
     */
    public void removeActionHandler(Handler actionHandler) {
        if (actionHandlers != null && actionHandlers.contains(actionHandler)) {
            actionHandlers.remove(actionHandler);
            if (actionHandlers.isEmpty()) {
                actionHandlers = null;
                actionMapper = null;
            }
        }
    }

    /**
     * Determines if there are currently any action handlers attached to the
     * target Spreadsheet.
     * 
     * @return true if action handlers exist, false otherwise
     */
    public boolean hasActionHandlers() {
        return actionHandlers != null && actionHandlers.size() > 0;
    }

    /**
     * This method is called when a context menu event has happened on any cell
     * of the target Spreadsheet.
     * 
     * @param row
     *            Row index at context menu target, 1-based
     * @param column
     *            Column index at context menu target, 1-based
     */
    public void onContextMenuOpenOnSelection(int row, int column) {
        try {
            // update the selection if the context menu wasn't triggered on
            // top of any of the cells inside the current selection.
            boolean keepSelection = spreadsheet.getCellSelectionManager()
                    .isCellInsideSelection(row, column);

            if (!keepSelection) {
                // click was on top of a cell that is not the selected cell,
                // not one of the individual cells nor part of any cell
                // ranges -> set as the selected cell
                spreadsheet.getCellSelectionManager().onCellSelected(row,
                        column, true);
            }
            ArrayList<SpreadsheetActionDetails> actions = createActionsListForSelection();
            if (!actions.isEmpty()) {
                spreadsheet.getRpcProxy().showActions(actions);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
    }

    /**
     * This method is called when a context menu event has happened on top of a
     * row header.
     * 
     * @param rowIndex
     *            Index of the target row, 1-based
     */
    public void onRowHeaderContextMenuOpen(int rowIndex) {
        ArrayList<SpreadsheetActionDetails> actions = createActionsListForRow(rowIndex);
        if (!actions.isEmpty()) {
            spreadsheet.getRpcProxy().showActions(actions);
            contextMenuHeaderIndex = rowIndex;
        }
    }

    /**
     * This method is called when a context menu event has happened on top of a
     * column header.
     * 
     * @param columnIndex
     *            Index of the target column, 1-based
     */
    public void onColumnHeaderContextMenuOpen(int columnIndex) {
        ArrayList<SpreadsheetActionDetails> actions = createActionsListForColumn(columnIndex);
        if (!actions.isEmpty()) {
            spreadsheet.getRpcProxy().showActions(actions);
            contextMenuHeaderIndex = columnIndex;
        }
    }

    /**
     * This method is called when an action has been selected on top of the
     * currently selected cell(s).
     * 
     * @param actionKey
     *            Key of the selected action
     */
    public void onActionOnCurrentSelection(String actionKey) {
        Action action = actionMapper.get(actionKey);
        for (Action.Handler ah : actionHandlers) {
            ah.handleAction(action, spreadsheet, spreadsheet
                    .getCellSelectionManager().getLatestSelectionEvent());
        }
    }

    /**
     * This method is called when an action has been selected on top of a row
     * header.
     * 
     * @param actionKey
     *            Key of the selected action
     */
    public void onActionOnRowHeader(String actionKey) {
        Action action = actionMapper.get(actionKey);
        final CellRangeAddress row = new CellRangeAddress(
                contextMenuHeaderIndex - 1, contextMenuHeaderIndex - 1, -1, -1);
        for (Action.Handler ah : actionHandlers) {
            ah.handleAction(action, spreadsheet, row);
        }
    }

    /**
     * This method is called when an action has been selected on top of a column
     * header.
     * 
     * @param actionKey
     *            Key of the selected action
     */
    public void onActionOnColumnHeader(String actionKey) {
        Action action = actionMapper.get(actionKey);
        final CellRangeAddress column = new CellRangeAddress(-1, -1,
                contextMenuHeaderIndex - 1, contextMenuHeaderIndex - 1);
        for (Action.Handler ah : actionHandlers) {
            ah.handleAction(action, spreadsheet, column);
        }
    }

    /**
     * Gets a list of available actions for the current selection.
     * 
     * @return List of actions
     */
    protected ArrayList<SpreadsheetActionDetails> createActionsListForSelection() {
        ArrayList<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        for (Handler handler : actionHandlers) {
            Action[] actions2 = handler.getActions(spreadsheet
                    .getCellSelectionManager().getLatestSelectionEvent(),
                    spreadsheet);
            if (actions2 != null) {
                for (Action action : actions2) {
                    String key = actionMapper.key(action);
                    spreadsheet.setResource(key, action.getIcon());
                    SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                    spreadsheetActionDetails.caption = action.getCaption();
                    spreadsheetActionDetails.key = key;
                    spreadsheetActionDetails.type = 0;
                    actions.add(spreadsheetActionDetails);
                }
            }
        }
        return actions;
    }

    /**
     * Gets a list of available actions for the column at the given index.
     * 
     * @param columnIndex
     *            Index of the target column, 1-based
     * @return List of actions
     */
    protected ArrayList<SpreadsheetActionDetails> createActionsListForColumn(
            int columnIndex) {
        ArrayList<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        final CellRangeAddress column = new CellRangeAddress(-1, -1,
                columnIndex - 1, columnIndex - 1);
        for (Handler handler : actionHandlers) {
            for (Action action : handler.getActions(column, spreadsheet)) {
                String key = actionMapper.key(action);
                spreadsheet.setResource(key, action.getIcon());
                SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                spreadsheetActionDetails.caption = action.getCaption();
                spreadsheetActionDetails.key = key;
                spreadsheetActionDetails.type = 2;
                actions.add(spreadsheetActionDetails);
            }
        }
        return actions;
    }

    /**
     * Gets a list of available actions for the row at the given index.
     * 
     * @param rowIndex
     *            Index of the target row, 1-based
     * @return List of actions
     */
    protected ArrayList<SpreadsheetActionDetails> createActionsListForRow(
            int rowIndex) {
        ArrayList<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        final CellRangeAddress row = new CellRangeAddress(rowIndex - 1,
                rowIndex - 1, -1, -1);
        for (Handler handler : actionHandlers) {
            for (Action action : handler.getActions(row, spreadsheet)) {
                String key = actionMapper.key(action);
                spreadsheet.setResource(key, action.getIcon());
                SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                spreadsheetActionDetails.caption = action.getCaption();
                spreadsheetActionDetails.key = key;
                spreadsheetActionDetails.type = 1;
                actions.add(spreadsheetActionDetails);
            }
        }
        return actions;
    }

}
