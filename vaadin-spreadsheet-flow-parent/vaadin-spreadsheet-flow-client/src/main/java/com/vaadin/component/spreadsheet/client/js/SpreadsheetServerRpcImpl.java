package com.vaadin.component.spreadsheet.client.js;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;

import elemental.json.Json;
import elemental.json.JsonArray;
import jsinterop.annotations.JsFunction;

@SuppressWarnings("serial")
public class SpreadsheetServerRpcImpl implements SpreadsheetServerRpc {

    @FunctionalInterface
    @JsFunction
    public interface JsConsumer<T> {
        void accept(T t);
    }

    private JsConsumer<Void> callbackForOnConnectorInit;
    private JsConsumer<String> groupingCollapsedCallback;
    private JsConsumer<String> levelHeaderClickedCallback;
    private JsConsumer<String> onSheetScrollCallback;
    private JsConsumer<String> sheetAddressChangedCallback;
    private JsConsumer<String> cellSelectedCallback;
    private JsConsumer<String> cellRangeSelectedCallback;
    private JsConsumer<String> cellAddedToSelectionAndSelected;
    private JsConsumer<String> cellsAddedToRangeSelectionCallback;
    private JsConsumer<String> rowSelectedCallback;
    private JsConsumer<String> rowAddedToRangeSelectionCallback;
    private JsConsumer<String> columnSelectedCallback;
    private JsConsumer<String> columnAddedToSelectionCallback;
    private JsConsumer<String> selectionIncreasePaintedCallback;
    private JsConsumer<String> selectionDecreasePaintedCallback;
    private JsConsumer<String> cellValueEditedCallback;
    private JsConsumer<String> sheetSelectedCallback;
    private JsConsumer<String> sheetRenamedCallback;
    private JsConsumer<String> sheetCreatedCallback;
    private JsConsumer<String> cellRangePaintedCallback;
    private JsConsumer<String> deleteSelectedCellsCallback;
    private JsConsumer<String> linkCellClickedCallback;
    private JsConsumer<String> rowsResizedCallback;
    private JsConsumer<String> columnResizedCallback;
    private JsConsumer<Integer> onRowAutofitCallback;
    private JsConsumer<Integer> onColumnAutofitCallback;
    private JsConsumer<Void> onUndoCallback;
    private JsConsumer<Void> onRedoCallback;
    private JsConsumer<String> setCellStyleWidthRatiosCallback;
    private JsConsumer<Void> protectedCellWriteAttemptedCallback;
    private JsConsumer<String> onPasteCallback;
    private JsConsumer<Void> clearSelectedCellsOnCutCallback;
    private JsConsumer<String> updateCellCommentCallback;
    private JsConsumer<String> contextMenuOpenOnSelectionCallback;
    private JsConsumer<String> actionOnCurrentSelectionCallback;
    private JsConsumer<Integer> rowHeaderContextMenuOpenCallback;
    private JsConsumer<String> actionOnRowHeaderCallback;
    private JsConsumer<Integer> columnHeaderContextMenuOpenCallback;
    private JsConsumer<String> actionOnColumnHeaderCallback;

    public SpreadsheetServerRpcImpl() {
    }

    public void setGroupingCollapsedCallback(JsConsumer<String> callback) {
        groupingCollapsedCallback = callback;
    }

    public void setLevelHeaderClickedCallback(JsConsumer<String> callback) {
        levelHeaderClickedCallback = callback;
    }

    public void setOnSheetScrollCallback(JsConsumer<String> callback) {
        onSheetScrollCallback = callback;
    }

    public void setSheetAddressChangedCallback(JsConsumer<String> callback) {
        sheetAddressChangedCallback = callback;
    }

    public void setCellSelectedCallback(JsConsumer<String> callback) {
        cellSelectedCallback = callback;
    }

    public void setCellRangeSelectedCallback(JsConsumer<String> callback) {
        cellRangeSelectedCallback = callback;
    }

    public void setCellAddedToSelectionAndSelectedCallback(
            JsConsumer<String> callback) {
        cellAddedToSelectionAndSelected = callback;
    }

    public void setCellsAddedToRangeSelectionCallback(
            JsConsumer<String> callback) {
        cellsAddedToRangeSelectionCallback = callback;
    }

    public void setRowSelectedCallback(JsConsumer<String> callback) {
        rowSelectedCallback = callback;
    }

    public void setRowAddedToRangeSelectionCallback(
            JsConsumer<String> callback) {
        rowAddedToRangeSelectionCallback = callback;
    }

    public void setColumnSelectedCallback(JsConsumer<String> callback) {
        columnSelectedCallback = callback;
    }

    public void setColumnAddedToSelectionCallback(JsConsumer<String> callback) {
        columnAddedToSelectionCallback = callback;
    }

    public void setSelectionIncreasePaintedCallback(
            JsConsumer<String> callback) {
        selectionIncreasePaintedCallback = callback;
    }

    public void setSelectionDecreasePaintedCallback(
            JsConsumer<String> callback) {
        selectionDecreasePaintedCallback = callback;
    }

    public void setCellValueEditedCallback(JsConsumer<String> callback) {
        cellValueEditedCallback = callback;
    }

    public void setSheetSelectedCallback(JsConsumer<String> callback) {
        sheetSelectedCallback = callback;
    }

    public void setSheetRenamedCallback(JsConsumer<String> callback) {
        sheetRenamedCallback = callback;
    }

    public void setSheetCreatedCallback(JsConsumer<String> callback) {
        sheetCreatedCallback = callback;
    }

    public void setCellRangePaintedCallback(JsConsumer<String> callback) {
        cellRangePaintedCallback = callback;
    }

    public void setDeleteSelectedCellsCallback(JsConsumer<String> callback) {
        deleteSelectedCellsCallback = callback;
    }

    public void setLinkCellClickedCallback(JsConsumer<String> callback) {
        linkCellClickedCallback = callback;
    }

    public void setRowsResizedCallback(JsConsumer<String> callback) {
        rowsResizedCallback = callback;
    }

    public void setColumnResizedCallback(JsConsumer<String> callback) {
        columnResizedCallback = callback;
    }

    public void setOnRowAutofitCallback(JsConsumer<Integer> callback) {
        onRowAutofitCallback = callback;
    }

    public void setOnColumnAutofitCallback(JsConsumer<Integer> callback) {
        onColumnAutofitCallback = callback;
    }

    public void setOnUndoCallback(JsConsumer<Void> callback) {
        onUndoCallback = callback;
    }

    public void setOnRedoCallback(JsConsumer<Void> callback) {
        onRedoCallback = callback;
    }

    public void setSetCellStyleWidthRatiosCallback(
            JsConsumer<String> callback) {
        setCellStyleWidthRatiosCallback = callback;
    }

    public void setProtectedCellWriteAttemptedCallback(
            JsConsumer<Void> callback) {
        protectedCellWriteAttemptedCallback = callback;
    }

    public void setOnPasteCallback(JsConsumer<String> callback) {
        onPasteCallback = callback;
    }

    public void setClearSelectedCellsOnCutCallback(JsConsumer<Void> callback) {
        clearSelectedCellsOnCutCallback = callback;
    }

    public void setUpdateCellCommentCallback(JsConsumer<String> callback) {
        updateCellCommentCallback = callback;
    }

    public void setOnConnectorInitCallback(JsConsumer<Void> callback) {
        callbackForOnConnectorInit = callback;
    }

    public void setContextMenuOpenOnSelectionCallback(
            JsConsumer<String> callback) {
        contextMenuOpenOnSelectionCallback = callback;
    }

    public void setActionOnCurrentSelectionCallback(
            JsConsumer<String> callback) {
        actionOnCurrentSelectionCallback = callback;
    }

    public void setRowHeaderContextMenuOpenCallback(
            JsConsumer<Integer> callback) {
        rowHeaderContextMenuOpenCallback = callback;
    }

    public void setActionOnRowHeaderCallback(JsConsumer<String> callback) {
        actionOnRowHeaderCallback = callback;
    }

    public void setColumnHeaderContextMenuOpenCallback(
            JsConsumer<Integer> callback) {
        columnHeaderContextMenuOpenCallback = callback;
    }

    public void setActionOnColumnHeaderCallback(JsConsumer<String> callback) {
        actionOnColumnHeaderCallback = callback;
    }

    private native void call(JsConsumer<?> fnc, Object... args) /*-{
      if (!fnc) {
          return;
      }
      var jsArr = [];
      for (var i = 0; i < args.length; i++) {
        var param = args[i]
        var gwtKey = Object.getOwnPropertyNames(param)
          .find(function(k) {return /^(a|value_0|value.*g\$)$/.test(k)});
        var value = gwtKey ? param[gwtKey] : param;
        jsArr.push(value)
      }
      fnc(jsArr);
    }-*/;

    private static JsonArray toJsFloatArr(Map<Integer, Float> value) {
        JsonArray a = Json.createArray();
        value.forEach((k, v) -> a.set(k, v));
        return a;
    }

    private static JsonArray toJsIntArr(Map<Integer, Integer> value) {
        JsonArray a = Json.createArray();
        value.forEach((k, v) -> a.set(k, v));
        return a;
    }

    @Override
    public void setGroupingCollapsed(boolean cols, int colIndex,
            boolean collapsed) {
        call(groupingCollapsedCallback, cols, colIndex, collapsed);
    }

    @Override
    public void levelHeaderClicked(boolean cols, int level) {
        call(levelHeaderClickedCallback, cols, level);
    }

    @Override
    public void onSheetScroll(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        call(onSheetScrollCallback, firstRow, firstColumn, lastRow, lastColumn);
    }

    @Override
    public void sheetAddressChanged(String value) {
        call(sheetAddressChangedCallback, value);
    }

    @Override
    public void cellSelected(int row, int column,
            boolean oldSelectionRangeDiscarded) {
        call(cellSelectedCallback, row, column, oldSelectionRangeDiscarded);

    }

    @Override
    public void cellRangeSelected(int row1, int col1, int row2, int col2) {
        call(cellRangeSelectedCallback, row1, col1, row2, col2);
    }

    @Override
    public void cellAddedToSelectionAndSelected(int row, int column) {
        call(cellAddedToSelectionAndSelected, row, column);

    }

    @Override
    public void cellsAddedToRangeSelection(int row1, int col1, int row2,
            int col2) {
        call(cellsAddedToRangeSelectionCallback, row1, col1, row2, col2);
    }

    @Override
    public void rowSelected(int row, int firstColumnIndex) {
        call(rowSelectedCallback, row, firstColumnIndex);
    }

    @Override
    public void rowAddedToRangeSelection(int row, int firstColumnIndex) {
        call(rowAddedToRangeSelectionCallback, row, firstColumnIndex);
    }

    @Override
    public void columnSelected(int column, int firstRowIndex) {
        call(columnSelectedCallback, column, firstRowIndex);
    }

    @Override
    public void columnAddedToSelection(int firstRowIndex, int column) {
        call(columnAddedToSelectionCallback, firstRowIndex, column);
    }

    @Override
    public void selectionIncreasePainted(int r1, int c1, int r2, int c2) {
        call(selectionIncreasePaintedCallback, r1, c1, r2, c2);
    }

    @Override
    public void selectionDecreasePainted(int row, int col) {
        call(selectionDecreasePaintedCallback, row, col);
    }

    @Override
    public void cellValueEdited(int row, int col, String value) {
        call(cellValueEditedCallback, row, col, value);
    }

    @Override
    public void sheetSelected(int sheetIndex, int scrollLeft, int scrollTop) {
        call(sheetSelectedCallback, sheetIndex, scrollLeft, scrollTop);
    }

    @Override
    public void sheetRenamed(int sheetIndex, String newName) {
        call(sheetRenamedCallback, sheetIndex, newName);
    }

    @Override
    public void sheetCreated(int scrollLeft, int scrollTop) {
        call(sheetCreatedCallback, scrollLeft, scrollTop);
    }

    @Override
    public void cellRangePainted(int selectedCellRow, int selectedCellColumn,
            int row1, int col1, int row2, int col2) {
        call(cellRangePaintedCallback, selectedCellRow, selectedCellColumn,
                row1, col1, row2, col2);
    }

    @Override
    public void deleteSelectedCells() {
        call(deleteSelectedCellsCallback);
    }

    @Override
    public void linkCellClicked(int row, int column) {
        call(linkCellClickedCallback, row, column);
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int row1, int col1,
            int row2, int col2) {
        call(rowsResizedCallback, toJsFloatArr(newRowSizes), row1, col1, row2,
                col2);
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int row1,
            int col1, int row2, int col2) {
        call(columnResizedCallback, toJsIntArr(newColumnSizes), row1, col1,
                row2, col2);
    }

    @Override
    public void onRowAutofit(int rowIndex) {
        call(onRowAutofitCallback, rowIndex);
    }

    @Override
    public void onColumnAutofit(int columnIndex) {
        call(onColumnAutofitCallback, columnIndex);
    }

    @Override
    public void onUndo() {
        call(onUndoCallback);
    }

    @Override
    public void onRedo() {
        call(onRedoCallback);
    }

    @Override
    public void setCellStyleWidthRatios(
            HashMap<Integer, Float> cellStyleWidthRatioMap) {
        call(setCellStyleWidthRatiosCallback,
                toJsFloatArr(cellStyleWidthRatioMap));
    }

    @Override
    public void protectedCellWriteAttempted() {
        call(protectedCellWriteAttemptedCallback);
    }

    @Override
    public void onPaste(String text) {
        call(onPasteCallback, text);
    }

    @Override
    public void clearSelectedCellsOnCut() {
        call(clearSelectedCellsOnCutCallback);
    }

    @Override
    public void updateCellComment(String text, int col, int row) {
        call(updateCellCommentCallback, text, col, row);
    }

    @Override
    public void onConnectorInit() {
        call(callbackForOnConnectorInit);
    }

    @Override
    public void contextMenuOpenOnSelection(int row, int column) {
        call(contextMenuOpenOnSelectionCallback, row, column);
    }

    @Override
    public void actionOnCurrentSelection(String actionKey) {
        call(actionOnCurrentSelectionCallback, actionKey);
    }

    @Override
    public void rowHeaderContextMenuOpen(int rowIndex) {
        call(rowHeaderContextMenuOpenCallback, rowIndex);
    }

    @Override
    public void actionOnRowHeader(String actionKey) {
        call(actionOnRowHeaderCallback, actionKey);
    }

    @Override
    public void columnHeaderContextMenuOpen(int columnIndex) {
        call(columnHeaderContextMenuOpenCallback, columnIndex);
    }

    @Override
    public void actionOnColumnHeader(String actionKey) {
        call(actionOnColumnHeaderCallback, actionKey);
    }
}
