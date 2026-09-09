/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentEventListener;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @since 23.1
 */
public class SpreadsheetEventListener
        implements ComponentEventListener<Spreadsheet.SpreadsheetEvent> {
    private final SpreadsheetHandlerImpl handler;

    Logger LOGGER = LoggerFactory.getLogger(SpreadsheetEventListener.class);

    public SpreadsheetEventListener(SpreadsheetHandlerImpl spreadsheetHandler) {
        this.handler = spreadsheetHandler;
    }

    private int toInt(ObjectNode o, String field) {
        JsonNode value = o == null ? null : o.get(field);
        return value == null || value.isNull() ? 0 : value.intValue();
    }

    private String toStr(ObjectNode o, String field) {
        JsonNode value = o == null ? null : o.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private boolean toBool(ObjectNode o, String field) {
        JsonNode value = o == null ? null : o.get(field);
        return value != null && value.asBoolean();
    }

    /**
     * Zip two parallel arrays — indexes and their new sizes — into the map the
     * resize handlers take. The client sends them separately because that is
     * the natural shape on the JavaScript side.
     */
    private HashMap<Integer, Float> toFloatMap(ObjectNode o, String indexField,
            String valueField) {
        HashMap<Integer, Float> m = new HashMap<>();
        JsonNode indexes = o == null ? null : o.get(indexField);
        JsonNode values = o == null ? null : o.get(valueField);
        if (indexes == null || values == null) {
            return m;
        }
        for (int i = 0; i < Math.min(indexes.size(), values.size()); i++) {
            m.put(indexes.get(i).intValue(), (float) values.get(i).asDouble());
        }
        return m;
    }

    private HashMap<Integer, Integer> toIntMap(ObjectNode o, String indexField,
            String valueField) {
        HashMap<Integer, Integer> m = new HashMap<>();
        JsonNode indexes = o == null ? null : o.get(indexField);
        JsonNode values = o == null ? null : o.get(valueField);
        if (indexes == null || values == null) {
            return m;
        }
        for (int i = 0; i < Math.min(indexes.size(), values.size()); i++) {
            m.put(indexes.get(i).intValue(), values.get(i).intValue());
        }
        return m;
    }

    /** Smallest value in an index array, or 0 when it is empty. */
    private int minIndex(ObjectNode o, String field) {
        JsonNode indexes = o == null ? null : o.get(field);
        if (indexes == null || indexes.isEmpty()) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        for (JsonNode n : indexes) {
            min = Math.min(min, n.intValue());
        }
        return min;
    }

    /** Largest value in an index array, or 0 when it is empty. */
    private int maxIndex(ObjectNode o, String field) {
        JsonNode indexes = o == null ? null : o.get(field);
        if (indexes == null || indexes.isEmpty()) {
            return 0;
        }
        int max = 0;
        for (JsonNode n : indexes) {
            max = Math.max(max, n.intValue());
        }
        return max;
    }

    @Override
    public void onComponentEvent(Spreadsheet.SpreadsheetEvent event) {
        String type = event.getType();
        JsonNode data = event.getData();
        // The client sends one named object per event; the field names are
        // the ones declared in rpc-client-to-server.ts. Reading by name
        // rather than by index means a field added or reordered on either
        // side can no longer silently shift every argument along.
        ObjectNode pars = data instanceof ObjectNode ? (ObjectNode) data : null;

        LOGGER.debug(type + " " + (pars == null ? "null" : pars.toString()));

        if ("onConnectorInit".equals(type)) {
            handler.onConnectorInit();
        } else if ("contextMenuOpenOnSelection".equals(type)) {
            handler.contextMenuOpenOnSelection(toInt(pars, "row"),
                    toInt(pars, "column"));
        } else if ("actionOnCurrentSelection".equals(type)) {
            handler.actionOnCurrentSelection(toStr(pars, "actionKey"));
        } else if ("rowHeaderContextMenuOpen".equals(type)) {
            handler.rowHeaderContextMenuOpen(toInt(pars, "rowIndex"));
        } else if ("actionOnRowHeader".equals(type)) {
            handler.actionOnRowHeader(toStr(pars, "actionKey"));
        } else if ("columnHeaderContextMenuOpen".equals(type)) {
            handler.columnHeaderContextMenuOpen(toInt(pars, "columnIndex"));
        } else if ("actionOnColumnHeader".equals(type)) {
            handler.actionOnColumnHeader(toStr(pars, "actionKey"));
        } else if ("onSheetScroll".equals(type)) {
            handler.onSheetScroll(toInt(pars, "firstRow"),
                    toInt(pars, "firstColumn"), toInt(pars, "lastRow"),
                    toInt(pars, "lastColumn"));
        } else if ("sheetAddressChanged".equals(type)) {
            handler.sheetAddressChanged(toStr(pars, "value"));
        } else if ("cellSelected".equals(type)) {
            handler.cellSelected(toInt(pars, "row"), toInt(pars, "column"),
                    toBool(pars, "oldSelectionRangeDiscarded"));
        } else if ("cellRangeSelected".equals(type)) {
            handler.cellRangeSelected(toInt(pars, "row1"), toInt(pars, "col1"),
                    toInt(pars, "row2"), toInt(pars, "col2"));
        } else if ("cellAddedToSelectionAndSelected".equals(type)) {
            handler.cellAddedToSelectionAndSelected(toInt(pars, "row"),
                    toInt(pars, "column"));
        } else if ("cellsAddedToRangeSelection".equals(type)) {
            handler.cellsAddedToRangeSelection(toInt(pars, "row1"),
                    toInt(pars, "col1"), toInt(pars, "row2"),
                    toInt(pars, "col2"));
        } else if ("rowSelected".equals(type)) {
            handler.rowSelected(toInt(pars, "row"),
                    toInt(pars, "firstColumnIndex"));
        } else if ("rowAddedToRangeSelection".equals(type)) {
            handler.rowAddedToRangeSelection(toInt(pars, "row"),
                    toInt(pars, "firstColumnIndex"));
        } else if ("columnSelected".equals(type)) {
            handler.columnSelected(toInt(pars, "column"),
                    toInt(pars, "firstRowIndex"));
        } else if ("columnAddedToSelection".equals(type)) {
            // Note the argument order: this one takes the row index first,
            // unlike columnSelected above.
            handler.columnAddedToSelection(toInt(pars, "firstRowIndex"),
                    toInt(pars, "column"));
        } else if ("selectionIncreasePainted".equals(type)) {
            handler.selectionIncreasePainted(toInt(pars, "r1"),
                    toInt(pars, "c1"), toInt(pars, "r2"), toInt(pars, "c2"));
        } else if ("selectionDecreasePainted".equals(type)) {
            handler.selectionDecreasePainted(toInt(pars, "row"),
                    toInt(pars, "col"));
        } else if ("cellValueEdited".equals(type)) {
            handler.cellValueEdited(toInt(pars, "row"), toInt(pars, "col"),
                    toStr(pars, "value"));
        } else if ("sheetSelected".equals(type)) {
            handler.sheetSelected(toInt(pars, "sheetIndex"),
                    toInt(pars, "scrollLeft"), toInt(pars, "scrollTop"));
        } else if ("sheetRenamed".equals(type)) {
            handler.sheetRenamed(toInt(pars, "sheetIndex"),
                    toStr(pars, "newName"));
        } else if ("sheetCreated".equals(type)) {
            handler.sheetCreated(toInt(pars, "scrollLeft"),
                    toInt(pars, "scrollTop"));
        } else if ("cellRangePainted".equals(type)) {
            handler.cellRangePainted(toInt(pars, "selectedCellRow"),
                    toInt(pars, "selectedCellColumn"), toInt(pars, "row1"),
                    toInt(pars, "col1"), toInt(pars, "row2"),
                    toInt(pars, "col2"));
        } else if ("deleteSelectedCells".equals(type)) {
            handler.deleteSelectedCells();
        } else if ("linkCellClicked".equals(type)) {
            handler.linkCellClicked(toInt(pars, "row"), toInt(pars, "column"));
        } else if ("rowsResized".equals(type)) {
            // The client reports which rows changed and their new heights;
            // the affected span doubles as the range to refresh.
            handler.rowsResized(toFloatMap(pars, "rowIndexes", "newHeights"),
                    minIndex(pars, "rowIndexes"), 1,
                    maxIndex(pars, "rowIndexes"), 1);
        } else if ("columnResized".equals(type)) {
            handler.columnResized(toIntMap(pars, "colIndexes", "newWidths"), 1,
                    minIndex(pars, "colIndexes"), 1,
                    maxIndex(pars, "colIndexes"));
        } else if ("onRowAutofit".equals(type)) {
            handler.onRowAutofit(toInt(pars, "rowIndex"));
        } else if ("onColumnAutofit".equals(type)) {
            handler.onColumnAutofit(toInt(pars, "columnIndex"));
        } else if ("onUndo".equals(type)) {
            handler.onUndo();
        } else if ("onRedo".equals(type)) {
            handler.onRedo();
        } else if ("setCellStyleWidthRatios".equals(type)) {
            handler.setCellStyleWidthRatios(
                    toFloatMap(pars, "styleIndexes", "ratios"));
        } else if ("protectedCellWriteAttempted".equals(type)) {
            handler.protectedCellWriteAttempted();
        } else if ("onPaste".equals(type)) {
            handler.onPaste(toStr(pars, "text"));
        } else if ("clearSelectedCellsOnCut".equals(type)) {
            handler.clearSelectedCellsOnCut();
        } else if ("updateCellComment".equals(type)) {
            handler.updateCellComment(toStr(pars, "text"), toInt(pars, "col"),
                    toInt(pars, "row"));
        } else if ("groupingCollapsed".equals(type)) {
            handler.setGroupingCollapsed(toBool(pars, "cols"),
                    toInt(pars, "colIndex"), toBool(pars, "collapsed"));
        } else if ("levelHeaderClicked".equals(type)) {
            handler.levelHeaderClicked(toBool(pars, "cols"),
                    toInt(pars, "level"));
        } else if ("popupButtonClick".equals(type)) {
            handler.onPopupButtonClick(toInt(pars, "row"),
                    toInt(pars, "column"));
        } else if ("popupClose".equals(type)) {
            handler.onPopupClose(toInt(pars, "row"), toInt(pars, "column"));
        } else if ("contextMenuClosed".equals(type)) {
            handler.contextMenuClosed();
        }
    }

}
