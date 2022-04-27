package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;

import elemental.json.Json;
import elemental.json.JsonArray;
import jsinterop.annotations.JsType;

@SuppressWarnings("serial")
@JsType
public class SpreadsheetServerRpcImpl implements SpreadsheetServerRpc {

    private Runnable callbackForOnConnectorInit;
    private Consumer<String> groupingCollapsedCallback;
    private Consumer<String> levelHeaderClickedCallback;
    private Consumer<String> onSheetScrollCallback;
    private Consumer<String> sheetAddressChangedCallback;
    private Consumer<String> cellSelectedCallback;
    private Consumer<String> cellRangeSelectedCallback;
    private Consumer<String> cellAddedToSelectionAndSelected;
    private Consumer<String> cellsAddedToRangeSelectionCallback;
    private Consumer<String> rowSelectedCallback;
    private Consumer<String> rowAddedToRangeSelectionCallback;
    private Consumer<String> columnSelectedCallback;
    private Consumer<String> columnAddedToSelectionCallback;
    private Consumer<String> selectionIncreasePaintedCallback;
    private Consumer<String> selectionDecreasePaintedCallback;
    private Consumer<String> cellValueEditedCallback;
    private Consumer<String> sheetSelectedCallback;
    private Consumer<String> sheetRenamedCallback;
    private Consumer<String> sheetCreatedCallback;
    private Consumer<String> cellRangePaintedCallback;
    private Consumer<String> deleteSelectedCellsCallback;
    private Consumer<String> linkCellClickedCallback;
    private Consumer<String> rowsResizedCallback;
    private Consumer<String> columnResizedCallback;
    private Consumer<Integer> onRowAutofitCallback;
    private Consumer<Integer> onColumnAutofitCallback;
    private Runnable onUndoCallback;
    private Runnable onRedoCallback;
    private Consumer<String> setCellStyleWidthRatiosCallback;
    private Runnable protectedCellWriteAttemptedCallback;
    private Consumer<String> onPasteCallback;
    private Runnable clearSelectedCellsOnCutCallback;
    private Consumer<String> updateCellCommentCallback;
    private Consumer<String> contextMenuOpenOnSelectionCallback;
    private Consumer<String> actionOnCurrentSelectionCallback;
    private Consumer<Integer> rowHeaderContextMenuOpenCallback;
    private Consumer<String> actionOnRowHeaderCallback;
    private Consumer<Integer> columnHeaderContextMenuOpenCallback;
    private Consumer<String> actionOnColumnHeaderCallback;

    public SpreadsheetServerRpcImpl() {
    }

    public void setGroupingCollapsedCallback(Consumer<String> callback) {
        groupingCollapsedCallback = callback;
    }

    public void setLevelHeaderClickedCallback(Consumer<String> callback) {
        levelHeaderClickedCallback = callback;
    }

    public void setOnSheetScrollCallback(Consumer<String> callback) {
        onSheetScrollCallback = callback;
    }

    public void setSheetAddressChangedCallback(Consumer<String> callback) {
        sheetAddressChangedCallback = callback;
    }

    public void setCellSelectedCallback(Consumer<String> callback) {
        cellSelectedCallback = callback;
    }

    public void setCellRangeSelectedCallback(Consumer<String> callback) {
        cellRangeSelectedCallback = callback;
    }

    public void setCellAddedToSelectionAndSelectedCallback(Consumer<String> callback) {
        cellAddedToSelectionAndSelected = callback;
    }

    public void setCellsAddedToRangeSelectionCallback(Consumer<String> callback) {
        cellsAddedToRangeSelectionCallback = callback;
    }

    public void setRowSelectedCallback(Consumer<String> callback) {
        rowSelectedCallback = callback;
    }

    public void setRowAddedToRangeSelectionCallback(Consumer<String> callback) {
        rowAddedToRangeSelectionCallback = callback;
    }

    public void setColumnSelectedCallback(Consumer<String> callback) {
        columnSelectedCallback = callback;
    }

    public void setColumnAddedToSelectionCallback(Consumer<String> callback) {
        columnAddedToSelectionCallback = callback;
    }

    public void setSelectionIncreasePaintedCallback(Consumer<String> callback) {
        selectionIncreasePaintedCallback = callback;
    }


    public void setSelectionDecreasePaintedCallback(Consumer<String> callback) {
        selectionDecreasePaintedCallback = callback;
    }

    public void setCellValueEditedCallback(Consumer<String> callback) {
        cellValueEditedCallback = callback;
    }

    public void setSheetSelectedCallback(Consumer<String> callback) {
        sheetSelectedCallback = callback;
    }

    public void setSheetRenamedCallback(Consumer<String> callback) {
        sheetRenamedCallback = callback;
    }

    public void setSheetCreatedCallback(Consumer<String> callback) {
        sheetCreatedCallback = callback;
    }

    public void setCellRangePaintedCallback(Consumer<String> callback) {
        cellRangePaintedCallback = callback;
    }

    public void setDeleteSelectedCellsCallback(Consumer<String> callback) {
        deleteSelectedCellsCallback = callback;
    }

    public void setLinkCellClickedCallback(Consumer<String> callback) {
        linkCellClickedCallback = callback;
    }

    public void setRowsResizedCallback(Consumer<String> callback) {
        rowsResizedCallback = callback;
    }

    public void setColumnResizedCallback(Consumer<String> callback) {
        columnResizedCallback = callback;
    }

    public void setOnRowAutofitCallback(Consumer<Integer> callback) {
        onRowAutofitCallback = callback;
    }

    public void setOnColumnAutofitCallback(Consumer<Integer> callback) {
        onColumnAutofitCallback = callback;
    }

    public void setOnUndoCallback(Runnable callback) {
        onUndoCallback = callback;
    }

    public void setOnRedoCallback(Runnable callback) {
        onRedoCallback = callback;
    }

    public void setSetCellStyleWidthRatiosCallback(Consumer<String> callback) {
        setCellStyleWidthRatiosCallback = callback;
    }

    public void setProtectedCellWriteAttemptedCallback(Runnable callback) {
        protectedCellWriteAttemptedCallback = callback;
    }

    public void setOnPasteCallback(Consumer<String> callback) {
        onPasteCallback = callback;
    }

    public void setClearSelectedCellsOnCutCallback(Runnable callback) {
        clearSelectedCellsOnCutCallback = callback;
    }

    public void setUpdateCellCommentCallback(Consumer<String> callback) {
        updateCellCommentCallback = callback;
    }

    public void setOnConnectorInitCallback(Runnable callback) {
        callbackForOnConnectorInit = callback;
    }

    public void setContextMenuOpenOnSelectionCallback(Consumer<String> callback) {
        contextMenuOpenOnSelectionCallback = callback;
    }

    public void setActionOnCurrentSelectionCallback(Consumer<String> callback) {
        actionOnCurrentSelectionCallback = callback;
    }

    public void setRowHeaderContextMenuOpenCallback(Consumer<Integer> callback) {
        rowHeaderContextMenuOpenCallback = callback;
    }

    public void setActionOnRowHeaderCallback(Consumer<String> callback) {
        actionOnRowHeaderCallback = callback;
    }

    public void setColumnHeaderContextMenuOpenCallback(Consumer<Integer> callback) {
        columnHeaderContextMenuOpenCallback = callback;
    }

    public void setActionOnColumnHeaderCallback(Consumer<String> callback) {
        actionOnColumnHeaderCallback = callback;
    }

    private native void call(Object fnc, Object ...args) /*-{
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
    public void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed) {
        call(groupingCollapsedCallback, cols, colIndex, collapsed);
    }

    @Override
    public void levelHeaderClicked(boolean cols, int level) {
        call(levelHeaderClickedCallback, cols, level);
    }

    @Override
    public void onSheetScroll(int firstRow, int firstColumn, int lastRow, int lastColumn) {
        call(onSheetScrollCallback, firstRow, firstColumn, lastRow, lastColumn);
    }

    @Override
    public void sheetAddressChanged(String value) {
        call(sheetAddressChangedCallback, value);
    }

    @Override
    public void cellSelected(int row, int column, boolean oldSelectionRangeDiscarded) {
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
    public void cellsAddedToRangeSelection(int row1, int col1, int row2, int col2) {
        call(cellsAddedToRangeSelectionCallback,row1, col1, row2 , col2);
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
    public void cellRangePainted(int selectedCellRow, int selectedCellColumn, int row1, int col1, int row2, int col2) {
        call(cellRangePaintedCallback, selectedCellRow, selectedCellColumn, row1, col1, row2, col2);
    }

    @Override
    public void deleteSelectedCells() {
        call(deleteSelectedCellsCallback);
    }

    @Override
    public void linkCellClicked(int row, int column) {
        call(linkCellClickedCallback,row, column);
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int row1, int col1, int row2, int col2) {
        call(rowsResizedCallback, toJsFloatArr(newRowSizes), row1, col1, row2, col2);
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int row1, int col1, int row2, int col2) {
        call(columnResizedCallback, toJsIntArr(newColumnSizes), row1, col1, row2, col2);
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
    public void setCellStyleWidthRatios(HashMap<Integer, Float> cellStyleWidthRatioMap) {
        call(setCellStyleWidthRatiosCallback, toJsFloatArr(cellStyleWidthRatioMap));
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
