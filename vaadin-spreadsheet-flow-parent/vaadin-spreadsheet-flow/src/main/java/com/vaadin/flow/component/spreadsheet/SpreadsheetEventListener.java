package com.vaadin.flow.component.spreadsheet;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentEventListener;

import elemental.json.JsonArray;
import elemental.json.JsonNumber;
import elemental.json.JsonValue;

@SuppressWarnings("serial")
public class SpreadsheetEventListener
        implements ComponentEventListener<Spreadsheet.SpreadsheetEvent> {
    private final SpreadsheetHandlerImpl handler;

    Logger LOGGER = LoggerFactory.getLogger(SpreadsheetEventListener.class);

    public SpreadsheetEventListener(SpreadsheetHandlerImpl spreadsheetHandler) {
        this.handler = spreadsheetHandler;
    }

    private int toInt(JsonArray o, int pos) {
        return o == null ? 0 : (int) o.getNumber(pos);
    }

    private String toStr(JsonArray o, int pos) {
        return o == null ? null : o.getString(pos);
    }

    private boolean toBool(JsonArray o, int pos) {
        return o == null ? null : o.getBoolean(pos);
    }

    private double toNumber(JsonValue v) {
        return (v instanceof JsonNumber) ? ((JsonNumber) v).asNumber() : 0;
    }

    private HashMap<Integer, Float> toMapFloat(JsonArray o, int pos) {
        HashMap<Integer, Float> m = new HashMap<>();
        if (o == null) {
            return m;
        }
        JsonArray jso = o.getArray(pos);
        for (int i = 0; i < jso.length(); i++) {
            m.put(i, (float) toNumber(jso.get(i)));
        }
        return m;
    }

    private HashMap<Integer, Integer> toMapInt(JsonArray o, int pos) {
        HashMap<Integer, Integer> m = new HashMap<>();
        if (o == null) {
            return m;
        }
        JsonArray jso = o.getArray(pos);
        for (int i = 0; i < jso.length(); i++) {
            m.put(i, (int) toNumber(jso.get(i)));
        }
        return m;
    }

    @Override
    public void onComponentEvent(Spreadsheet.SpreadsheetEvent event) {
        String type = event.getType();
        JsonArray pars = (JsonArray) event.getData();

        LOGGER.debug(type + " " + (pars == null ? "null" : pars.toJson()));

        if ("onConnectorInit".equals(type)) {
            handler.onConnectorInit();
        } else if ("contextMenuOpenOnSelection".equals(type)) {
            handler.contextMenuOpenOnSelection(toInt(pars, 0), toInt(pars, 1));
        } else if ("actionOnCurrentSelection".equals(type)) {
            handler.actionOnCurrentSelection(toStr(pars, 0));
        } else if ("rowHeaderContextMenuOpen".equals(type)) {
            handler.rowHeaderContextMenuOpen(toInt(pars, 0));
        } else if ("actionOnRowHeader".equals(type)) {
            handler.actionOnRowHeader(toStr(pars, 0));
        } else if ("columnHeaderContextMenuOpen".equals(type)) {
            handler.columnHeaderContextMenuOpen(toInt(pars, 0));
        } else if ("actionOnColumnHeader".equals(type)) {
            handler.actionOnColumnHeader(toStr(pars, 0));
        } else if ("onSheetScroll".equals(type)) {
            handler.onSheetScroll(toInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3));
        } else if ("sheetAddressChanged".equals(type)) {
            handler.sheetAddressChanged(toStr(pars, 0));
        } else if ("cellSelected".equals(type)) {
            handler.cellSelected(toInt(pars, 0), toInt(pars, 1),
                    toBool(pars, 2));
        } else if ("cellRangeSelected".equals(type)) {
            handler.cellRangeSelected(toInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3));
        } else if ("cellAddedToSelectionAndSelected".equals(type)) {
            handler.cellAddedToSelectionAndSelected(toInt(pars, 0),
                    toInt(pars, 1));
        } else if ("cellsAddedToRangeSelection".equals(type)) {
            handler.cellsAddedToRangeSelection(toInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3));
        } else if ("rowSelected".equals(type)) {
            handler.rowSelected(toInt(pars, 0), toInt(pars, 1));
        } else if ("rowAddedToRangeSelection".equals(type)) {
            handler.rowAddedToRangeSelection(toInt(pars, 0), toInt(pars, 1));
        } else if ("columnSelected".equals(type)) {
            handler.columnSelected(toInt(pars, 0), toInt(pars, 1));
        } else if ("columnAddedToSelection".equals(type)) {
            handler.columnAddedToSelection(toInt(pars, 0), toInt(pars, 1));
        } else if ("selectionIncreasePainted".equals(type)) {
            handler.selectionIncreasePainted(toInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3));
        } else if ("selectionDecreasePainted".equals(type)) {
            handler.selectionDecreasePainted(toInt(pars, 0), toInt(pars, 1));
        } else if ("cellValueEdited".equals(type)) {
            handler.cellValueEdited(toInt(pars, 0), toInt(pars, 1),
                    toStr(pars, 2));
        } else if ("sheetSelected".equals(type)) {
            handler.sheetSelected(toInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2));
        } else if ("sheetRenamed".equals(type)) {
            handler.sheetRenamed(toInt(pars, 0), toStr(pars, 1));
        } else if ("sheetCreated".equals(type)) {
            handler.sheetCreated(toInt(pars, 0), toInt(pars, 1));
        } else if ("cellRangePainted".equals(type)) {
            handler.cellRangePainted(toInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3), toInt(pars, 4),
                    toInt(pars, 5));
        } else if ("deleteSelectedCells".equals(type)) {
            handler.deleteSelectedCells();
        } else if ("linkCellClicked".equals(type)) {
            handler.linkCellClicked(toInt(pars, 0), toInt(pars, 1));
        } else if ("rowsResized".equals(type)) {
            handler.rowsResized(toMapFloat(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3), toInt(pars, 4));
        } else if ("columnResized".equals(type)) {
            handler.columnResized(toMapInt(pars, 0), toInt(pars, 1),
                    toInt(pars, 2), toInt(pars, 3), toInt(pars, 4));
        } else if ("onRowAutofit".equals(type)) {
            handler.onRowAutofit(toInt(pars, 0));
        } else if ("onColumnAutofit".equals(type)) {
            handler.onColumnAutofit(toInt(pars, 0));
        } else if ("onUndo".equals(type)) {
            handler.onUndo();
        } else if ("onRedo".equals(type)) {
            handler.onRedo();
        } else if ("setCellStyleWidthRatios".equals(type)) {
            handler.setCellStyleWidthRatios(toMapFloat(pars, 0));
        } else if ("protectedCellWriteAttempted".equals(type)) {
            handler.protectedCellWriteAttempted();
        } else if ("onPaste".equals(type)) {
            handler.onPaste(toStr(pars, 0));
        } else if ("clearSelectedCellsOnCut".equals(type)) {
            handler.clearSelectedCellsOnCut();
        } else if ("updateCellComment".equals(type)) {
            handler.updateCellComment(toStr(pars, 0), toInt(pars, 1),
                    toInt(pars, 2));
        } else if ("groupingCollapsed".equals(type)) {
            handler.setGroupingCollapsed(toBool(pars, 0), toInt(pars, 1),
                    toBool(pars, 2));
        } else if ("levelHeaderClicked".equals(type)) {
            handler.levelHeaderClicked(toBool(pars, 0), toInt(pars, 1));
        }
    }

}
