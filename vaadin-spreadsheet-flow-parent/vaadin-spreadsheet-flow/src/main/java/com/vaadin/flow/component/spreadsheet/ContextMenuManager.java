/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.poi.ss.util.CellRangeAddress;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.framework.Action.Handler;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.dom.Element;

/**
 * ContextMenuManager is an utility class for the Spreadsheet component. This
 * class handles all context menu -related tasks within the Spreadsheet it is
 * tied to.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class ContextMenuManager implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ContextMenuManager.class);

    private LinkedList<Handler> actionHandlers;

    private KeyMapper<Action> actionMapper;

    private final Spreadsheet spreadsheet;

    private final ArrayList<Element> iconsInContextMenu = new ArrayList<>();

    private int contextMenuHeaderIndex = -1;

    /**
     * Enum for spreadsheet action types.
     */
    enum ActionType {
        CELL(0), ROW(1), COLUMN(2);

        private final int value;

        ActionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

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
                actionHandlers = new LinkedList<Handler>();
                actionMapper = new KeyMapper<>();
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
            LOGGER.trace(e.getMessage(), e);
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
        ArrayList<SpreadsheetActionDetails> actions = createActionsListForRow(
                rowIndex);
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
        ArrayList<SpreadsheetActionDetails> actions = createActionsListForColumn(
                columnIndex);
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
        for (Handler ah : actionHandlers) {
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
        for (Handler ah : actionHandlers) {
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
        for (Handler ah : actionHandlers) {
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
            Action[] handlerActions = handler.getActions(spreadsheet
                    .getCellSelectionManager().getLatestSelectionEvent(),
                    spreadsheet);
            actions.addAll(
                    createActionDetailsList(handlerActions, ActionType.CELL));
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
            Action[] handlerActions = handler.getActions(column, spreadsheet);
            actions.addAll(
                    createActionDetailsList(handlerActions, ActionType.COLUMN));
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
            Action[] handlerActions = handler.getActions(row, spreadsheet);
            actions.addAll(
                    createActionDetailsList(handlerActions, ActionType.ROW));
        }
        return actions;
    }

    public void onContextMenuClosed() {
        removeActionIcons();
    }

    /**
     * Helper method to create SpreadsheetActionDetails from actions.
     *
     * @param actions
     *            Array of actions to convert
     * @param actionType
     *            Type of the action (cell, row, or column)
     * @return List of SpreadsheetActionDetails
     */
    private ArrayList<SpreadsheetActionDetails> createActionDetailsList(
            Action[] actions, ActionType actionType) {
        ArrayList<SpreadsheetActionDetails> actionDetailsList = new ArrayList<SpreadsheetActionDetails>();
        if (actions != null) {
            for (Action action : actions) {
                String key = actionMapper.key(action);
                SpreadsheetActionDetails spreadsheetActionDetails = new SpreadsheetActionDetails();
                String caption = action.getCaption();
                if (caption == null) {
                    caption = "";
                }
                spreadsheetActionDetails.caption = Jsoup.clean(caption,
                        Safelist.relaxed());
                if (action.getIcon() != null) {
                    var icon = action.getIcon().getElement();
                    // Attach the icon to the spreadsheet as a virtual child so
                    // that it can be fetched by the client-side context menu.
                    spreadsheet.getElement().appendVirtualChild(icon);
                    iconsInContextMenu.add(icon);
                    spreadsheetActionDetails.iconNodeId = icon.getNode()
                            .getId();
                }
                spreadsheetActionDetails.key = key;
                spreadsheetActionDetails.type = actionType.getValue();
                actionDetailsList.add(spreadsheetActionDetails);
            }
        }
        return actionDetailsList;
    }

    private void removeActionIcons() {
        for (var icon : iconsInContextMenu) {
            spreadsheet.getElement().removeVirtualChild(icon);
        }
        iconsInContextMenu.clear();
    }
}
