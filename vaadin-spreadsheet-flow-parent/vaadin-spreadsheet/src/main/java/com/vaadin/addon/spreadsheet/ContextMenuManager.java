package com.vaadin.addon.spreadsheet;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.KeyMapper;

public class ContextMenuManager {
    private LinkedList<Handler> actionHandlers;

    private KeyMapper<Action> actionMapper;

    private final Spreadsheet spreadsheet;

    private int contextMenuHeaderIndex = -1;

    public ContextMenuManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }

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

    public void removeActionHandler(Handler actionHandler) {
        if (actionHandlers != null && actionHandlers.contains(actionHandler)) {
            actionHandlers.remove(actionHandler);
            if (actionHandlers.isEmpty()) {
                actionHandlers = null;
                actionMapper = null;
            }
        }
    }

    public boolean hasActionHandlers() {
        return actionHandlers != null && actionHandlers.size() > 0;
    }

    public void onContextMenuOpenOnSelection(int column, int row) {
        try {
            // update the selection if the context menu wasn't triggered on
            // top of any of the cells inside the current selection.
            boolean keepSelection = spreadsheet.getCellSelectionManager()
                    .isCellInsideSelection(column, row);

            if (!keepSelection) {
                // click was on top of a cell that is not the selected cell,
                // not one of the individual cells nor part of any cell
                // ranges -> set as the selected cell
                spreadsheet.getCellSelectionManager().onCellSelected(column,
                        row, true);
            }
            ArrayList<SpreadsheetActionDetails> actions = createActionsListForSelection();
            if (!actions.isEmpty()) {
                spreadsheet.getSpreadsheetRpcProxy().showActions(actions);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // rather catch it than let the component crash and burn
        }
    }

    public void onRowHeaderContextMenuOpen(int rowIndex) {
        ArrayList<SpreadsheetActionDetails> actions = createActionsListForRow(rowIndex);
        if (!actions.isEmpty()) {
            spreadsheet.getSpreadsheetRpcProxy().showActions(actions);
            contextMenuHeaderIndex = rowIndex;
        }
    }

    public void onColumnHeaderContextMenuOpen(int columnIndex) {
        ArrayList<SpreadsheetActionDetails> actions = createActionsListForColumn(columnIndex);
        if (!actions.isEmpty()) {
            spreadsheet.getSpreadsheetRpcProxy().showActions(actions);
            contextMenuHeaderIndex = columnIndex;
        }
    }

    public void onActionOnCurrentSelection(String actionKey) {
        Action action = actionMapper.get(actionKey);
        for (Action.Handler ah : actionHandlers) {
            ah.handleAction(action, spreadsheet, spreadsheet
                    .getCellSelectionManager().getLatestSelectionEvent());
        }
    }

    public void onActionOnRowHeader(String actionKey) {
        Action action = actionMapper.get(actionKey);
        final CellRangeAddress row = new CellRangeAddress(
                contextMenuHeaderIndex - 1, contextMenuHeaderIndex - 1, -1, -1);
        for (Action.Handler ah : actionHandlers) {
            ah.handleAction(action, spreadsheet, row);
        }
    }

    public void onActionOnColumnHeader(String actionKey) {
        Action action = actionMapper.get(actionKey);
        final CellRangeAddress column = new CellRangeAddress(-1, -1,
                contextMenuHeaderIndex - 1, contextMenuHeaderIndex - 1);
        for (Action.Handler ah : actionHandlers) {
            ah.handleAction(action, spreadsheet, column);
        }
    }

    protected ArrayList<SpreadsheetActionDetails> createActionsListForSelection() {
        ArrayList<SpreadsheetActionDetails> actions = new ArrayList<SpreadsheetActionDetails>();
        for (Handler handler : actionHandlers) {
            Action[] actions2 = handler.getActions(getCellSelectionManager()
                    .getLatestSelectionEvent(), spreadsheet);
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