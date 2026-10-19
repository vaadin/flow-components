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
import java.util.Map;
import java.util.function.Function;

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

    /*
     * The decoders below never throw. A value of an unexpected type reads as if
     * it were missing, so a malformed event does what an event without that
     * field does instead of failing the request.
     */

    private static JsonNode field(ObjectNode o, String field) {
        return o == null ? null : o.get(field);
    }

    private static boolean isNumber(JsonNode value) {
        return value != null && value.isNumber();
    }

    /**
     * A number rounded to the nearest int, or 0 when the value is missing,
     * {@code null} or not a number. Browsers report scroll offsets with a
     * fraction on scaled displays, so whole numbers cannot be assumed.
     */
    private static int toInt(JsonNode value) {
        if (!isNumber(value)) {
            return 0;
        }
        return Math.clamp(Math.round(value.asDouble()), Integer.MIN_VALUE,
                Integer.MAX_VALUE);
    }

    private static int toInt(ObjectNode o, String field) {
        return toInt(field(o, field));
    }

    /** The text of a scalar, or {@code null} for anything else. */
    private static String toStr(ObjectNode o, String field) {
        JsonNode value = field(o, field);
        return value == null ? null : value.asString(null);
    }

    private static boolean toBool(ObjectNode o, String field) {
        JsonNode value = field(o, field);
        return value != null && value.asBoolean(false);
    }

    /**
     * Zip two parallel arrays — indexes and their new sizes — into the map the
     * resize handlers take. The client sends them separately because that is
     * the natural shape on the JavaScript side. A pair whose index or size is
     * not a number is left out.
     */
    private static <V> HashMap<Integer, V> toMap(ObjectNode o,
            String indexField, String valueField,
            Function<JsonNode, V> toValue) {
        HashMap<Integer, V> m = new HashMap<>();
        JsonNode indexes = field(o, indexField);
        JsonNode values = field(o, valueField);
        if (indexes == null || values == null) {
            return m;
        }
        for (int i = 0; i < Math.min(indexes.size(), values.size()); i++) {
            JsonNode index = indexes.get(i);
            JsonNode value = values.get(i);
            if (isNumber(index) && isNumber(value)) {
                m.put(toInt(index), toValue.apply(value));
            }
        }
        return m;
    }

    private static HashMap<Integer, Float> toFloatMap(ObjectNode o,
            String indexField, String valueField) {
        return toMap(o, indexField, valueField,
                value -> (float) value.asDouble());
    }

    private static HashMap<Integer, Integer> toIntMap(ObjectNode o,
            String indexField, String valueField) {
        return toMap(o, indexField, valueField,
                SpreadsheetEventListener::toInt);
    }

    /** Leaves out the entries whose index is outside the given range. */
    private static <V> HashMap<Integer, V> keysWithin(HashMap<Integer, V> m,
            int firstIndex, int lastIndex) {
        m.keySet().removeIf(index -> index < firstIndex || index > lastIndex);
        return m;
    }

    /**
     * Whether a workbook of {@code styleCount} styles has a style keyed this
     * way. Styles are keyed by their index as a short, as
     * {@code CellStyle.getIndex()} gives it, so in an XLSX workbook the ones
     * past 32767 are keyed by negative numbers.
     */
    private static boolean isStyleKey(int key, int styleCount) {
        return key >= Short.MIN_VALUE && key <= Short.MAX_VALUE
                && Short.toUnsignedInt((short) key) < styleCount;
    }

    /** Smallest key of the map, or 0 when it is empty. */
    private static int minKey(Map<Integer, ?> m) {
        return m.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
    }

    /** Largest key of the map, or 0 when it is empty. */
    private static int maxKey(Map<Integer, ?> m) {
        return m.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    @Override
    public void onComponentEvent(Spreadsheet.SpreadsheetEvent event) {
        String type = event.getType();
        JsonNode data = event.getData();
        // The client sends one named object per event; the field names are
        // the ones declared in rpc-client-to-server.ts. Reading by name
        // rather than by index means a field added or reordered on either
        // side can no longer silently shift every argument along.
        ObjectNode pars = data instanceof ObjectNode node ? node : null;

        LOGGER.debug(type + " " + (pars == null ? "null" : pars.toString()));

        switch (type) {
        case "onConnectorInit" -> handler.onConnectorInit();
        case "contextMenuOpenOnSelection" -> handler.contextMenuOpenOnSelection(
                toInt(pars, "row"), toInt(pars, "column"));
        case "actionOnCurrentSelection" ->
            handler.actionOnCurrentSelection(toStr(pars, "actionKey"));
        case "rowHeaderContextMenuOpen" ->
            handler.rowHeaderContextMenuOpen(toInt(pars, "rowIndex"));
        case "actionOnRowHeader" ->
            handler.actionOnRowHeader(toStr(pars, "actionKey"));
        case "columnHeaderContextMenuOpen" ->
            handler.columnHeaderContextMenuOpen(toInt(pars, "columnIndex"));
        case "actionOnColumnHeader" ->
            handler.actionOnColumnHeader(toStr(pars, "actionKey"));
        case "onSheetScroll" -> {
            // The cells drawn come with the range to load, which is wider by
            // the margin loaded ahead. They are taken first, so that what the
            // load runs into sees them.
            int drawnFirstRow = toInt(pars, "drawnFirstRow");
            if (drawnFirstRow > 0) {
                handler.sheetDrawn(drawnFirstRow,
                        toInt(pars, "drawnFirstColumn"),
                        toInt(pars, "drawnLastRow"),
                        toInt(pars, "drawnLastColumn"));
            }
            handler.onSheetScroll(toInt(pars, "firstRow"),
                    toInt(pars, "firstColumn"), toInt(pars, "lastRow"),
                    toInt(pars, "lastColumn"));
        }
        case "sheetAddressChanged" ->
            handler.sheetAddressChanged(toStr(pars, "value"));
        case "cellSelected" ->
            handler.cellSelected(toInt(pars, "row"), toInt(pars, "column"),
                    toBool(pars, "oldSelectionRangeDiscarded"));
        case "cellRangeSelected" ->
            handler.cellRangeSelected(toInt(pars, "row1"), toInt(pars, "col1"),
                    toInt(pars, "row2"), toInt(pars, "col2"));
        case "cellAddedToSelectionAndSelected" ->
            handler.cellAddedToSelectionAndSelected(toInt(pars, "row"),
                    toInt(pars, "column"));
        case "cellsAddedToRangeSelection" -> handler.cellsAddedToRangeSelection(
                toInt(pars, "row1"), toInt(pars, "col1"), toInt(pars, "row2"),
                toInt(pars, "col2"));
        case "rowSelected" -> handler.rowSelected(toInt(pars, "row"),
                toInt(pars, "firstColumnIndex"));
        case "rowAddedToRangeSelection" -> handler.rowAddedToRangeSelection(
                toInt(pars, "row"), toInt(pars, "firstColumnIndex"));
        case "columnSelected" -> handler.columnSelected(toInt(pars, "column"),
                toInt(pars, "firstRowIndex"));
        case "columnAddedToSelection" -> {
            // Note the argument order: this one takes the row index first,
            // unlike columnSelected above.
            handler.columnAddedToSelection(toInt(pars, "firstRowIndex"),
                    toInt(pars, "column"));
        }
        case "selectionIncreasePainted" ->
            handler.selectionIncreasePainted(toInt(pars, "r1"),
                    toInt(pars, "c1"), toInt(pars, "r2"), toInt(pars, "c2"));
        case "selectionDecreasePainted" -> handler.selectionDecreasePainted(
                toInt(pars, "row"), toInt(pars, "col"));
        case "cellValueEdited" -> handler.cellValueEdited(toInt(pars, "row"),
                toInt(pars, "col"), toStr(pars, "value"));
        case "sheetSelected" -> handler.sheetSelected(toInt(pars, "sheetIndex"),
                toInt(pars, "scrollLeft"), toInt(pars, "scrollTop"));
        case "sheetRenamed" -> handler.sheetRenamed(toInt(pars, "sheetIndex"),
                toStr(pars, "newName"));
        case "sheetCreated" -> handler.sheetCreated(toInt(pars, "scrollLeft"),
                toInt(pars, "scrollTop"));
        case "cellRangePainted" -> handler.cellRangePainted(
                toInt(pars, "selectedCellRow"),
                toInt(pars, "selectedCellColumn"), toInt(pars, "row1"),
                toInt(pars, "col1"), toInt(pars, "row2"), toInt(pars, "col2"));
        case "deleteSelectedCells" -> handler.deleteSelectedCells();
        case "linkCellClicked" ->
            handler.linkCellClicked(toInt(pars, "row"), toInt(pars, "column"));
        case "rowsResized" -> {
            // The client reports which rows changed and their new heights;
            // the affected span doubles as the range to refresh. Rows are
            // 1-based, and only the rows of the sheet can be resized.
            HashMap<Integer, Float> heights = keysWithin(
                    toFloatMap(pars, "rowIndexes", "newHeights"), 1,
                    event.getSource().getRows());
            if (!heights.isEmpty()) {
                handler.rowsResized(heights, minKey(heights), 1,
                        maxKey(heights), 1);
            }
        }
        case "columnResized" -> {
            HashMap<Integer, Integer> widths = keysWithin(
                    toIntMap(pars, "colIndexes", "newWidths"), 1,
                    event.getSource().getColumns());
            if (!widths.isEmpty()) {
                handler.columnResized(widths, 1, minKey(widths), 1,
                        maxKey(widths));
            }
        }
        case "onRowAutofit" -> handler.onRowAutofit(toInt(pars, "rowIndex"));
        case "onColumnAutofit" ->
            handler.onColumnAutofit(toInt(pars, "columnIndex"));
        case "onUndo" -> handler.onUndo();
        case "onRedo" -> handler.onRedo();
        case "setCellStyleWidthRatios" -> {
            // Only styles the workbook has.
            HashMap<Integer, Float> ratios = toFloatMap(pars, "styleIndexes",
                    "ratios");
            int styleCount = event.getSource().getWorkbook().getNumCellStyles();
            ratios.keySet().removeIf(key -> !isStyleKey(key, styleCount));
            handler.setCellStyleWidthRatios(ratios);
        }
        case "protectedCellWriteAttempted" ->
            handler.protectedCellWriteAttempted();
        case "onPaste" -> handler.onPaste(toStr(pars, "text"));
        case "clearSelectedCellsOnCut" -> handler.clearSelectedCellsOnCut();
        case "updateCellComment" -> handler.updateCellComment(
                toStr(pars, "text"), toInt(pars, "col"), toInt(pars, "row"));
        case "groupingCollapsed" ->
            handler.setGroupingCollapsed(toBool(pars, "cols"),
                    toInt(pars, "colIndex"), toBool(pars, "collapsed"));
        case "levelHeaderClicked" -> handler
                .levelHeaderClicked(toBool(pars, "cols"), toInt(pars, "level"));
        case "popupButtonClick" -> handler
                .onPopupButtonClick(toInt(pars, "row"), toInt(pars, "column"));
        case "popupClose" ->
            handler.onPopupClose(toInt(pars, "row"), toInt(pars, "column"));
        case "contextMenuClosed" -> handler.contextMenuClosed();
        case null, default -> {
            // Not an event of this component: nothing to do. The debug log
            // above has it.
        }
        }
    }

}
