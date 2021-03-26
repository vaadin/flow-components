package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import jsinterop.annotations.JsType;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;

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
        consoleLog("instantiated");
        //debugger();
    }

    native void debugger() /*-{
      debugger;
  }-*/;


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


    native void consoleLog(String message) /*-{
      console.log( "serverrpc", message );
  }-*/;

    native void call(Object f, Object ...args) /*-{
      f(args);
  }-*/;



    @Override
    public void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed) {
        if (groupingCollapsedCallback != null) call(groupingCollapsedCallback, "" + cols + "," + colIndex + "," + collapsed);
        else consoleLog("setGroupingCollapsed callback not set");
    }

    @Override
    public void levelHeaderClicked(boolean cols, int level) {
        if (levelHeaderClickedCallback != null) call(levelHeaderClickedCallback, "" + cols + "," + level);
        else consoleLog("levelHeaderClicked callback not set");
    }

    @Override
    public void onSheetScroll(int firstRow, int firstColumn, int lastRow, int lastColumn) {
        if (onSheetScrollCallback != null) call(onSheetScrollCallback, "" + firstRow + "," + firstColumn + "," + lastRow + "," + lastColumn);
        else consoleLog("onSheetScroll callback not set");
    }

    @Override
    public void sheetAddressChanged(String value) {
        if (sheetAddressChangedCallback!= null) call(sheetAddressChangedCallback, value);
        else consoleLog("sheetAddressChanged callback not set");
    }

    @Override
    public void cellSelected(int row, int column, boolean oldSelectionRangeDiscarded) {
        if (cellSelectedCallback != null) call(cellSelectedCallback, row + "," + column + "," + oldSelectionRangeDiscarded);
        else consoleLog("cellSelected callback not set");
    }

    @Override
    public void cellRangeSelected(int row1, int col1, int row2, int col2) {
        if (cellRangeSelectedCallback != null) call(cellRangeSelectedCallback, row1 + "," + col1 + "," + row2 + "," +  col2);
        else consoleLog("cellRangeSelected callback not set");
    }

    @Override
    public void cellAddedToSelectionAndSelected(int row, int column) {
        if (cellAddedToSelectionAndSelected != null) call(cellAddedToSelectionAndSelected, row + "," + column);
        else consoleLog("cellAddedToSelectionAndSelected callback not set");
    }

    @Override
    public void cellsAddedToRangeSelection(int row1, int col1, int row2, int col2) {
        if (cellsAddedToRangeSelectionCallback != null) call(cellsAddedToRangeSelectionCallback,row1 + "," + col1 + "," + row2 + "," + col2);
        else consoleLog("cellsAddedToRangeSelection callback not set");
    }

    @Override
    public void rowSelected(int row, int firstColumnIndex) {
        if (rowSelectedCallback != null) call(rowSelectedCallback, row + "," + firstColumnIndex);
        else consoleLog("rowSelected callback not set");
    }

    @Override
    public void rowAddedToRangeSelection(int row, int firstColumnIndex) {
        if (rowAddedToRangeSelectionCallback != null) call(rowAddedToRangeSelectionCallback, row + "," + firstColumnIndex);
        else consoleLog("rowAddedToRangeSelection callback not set");
    }

    @Override
    public void columnSelected(int column, int firstRowIndex) {
        if (columnSelectedCallback!= null) call(columnSelectedCallback, column + "," + firstRowIndex);
        else consoleLog("columnSelected callback not set");
    }

    @Override
    public void columnAddedToSelection(int firstRowIndex, int column) {
        if (columnAddedToSelectionCallback != null) call(columnAddedToSelectionCallback, firstRowIndex + "," + column);
        else consoleLog("columnAddedToSelection callback not set");
    }

    @Override
    public void selectionIncreasePainted(int r1, int c1, int r2, int c2) {
        if (selectionIncreasePaintedCallback != null) call(selectionIncreasePaintedCallback, r1 + "," + c1 + "," + r2 + "," + c2);
        else consoleLog("selectionIncreasePainted callback not set");
    }

    @Override
    public void selectionDecreasePainted(int row, int col) {
        if (selectionDecreasePaintedCallback != null) call(selectionDecreasePaintedCallback, "" + row + "," + col);
        else consoleLog("selectionDecreasePainted callback not set");
    }

    @Override
    public void cellValueEdited(int row, int col, String value) {
        if (cellValueEditedCallback!= null) call(cellValueEditedCallback, row + "," + col + "," + "\"" + value.replaceAll("\"", "\\\"") + "\"");
        else consoleLog("cellValueEdited callback not set");
    }

    @Override
    public void sheetSelected(int sheetIndex, int scrollLeft, int scrollTop) {
        if (sheetSelectedCallback != null) call(sheetSelectedCallback, sheetIndex + "," + scrollLeft + "," + scrollTop);
        else consoleLog("sheetSelected callback not set");
    }

    @Override
    public void sheetRenamed(int sheetIndex, String newName) {
        if (sheetRenamedCallback != null) call(sheetRenamedCallback, sheetIndex + "," + newName);
        else consoleLog("sheetRenamed callback not set");
    }

    @Override
    public void sheetCreated(int scrollLeft, int scrollTop) {
        if (sheetCreatedCallback != null) call(sheetCreatedCallback, scrollLeft + "," + scrollTop);
        else consoleLog("sheetCreated callback not set");
    }

    @Override
    public void cellRangePainted(int selectedCellRow, int selectedCellColumn, int row1, int col1, int row2, int col2) {
        if (cellRangePaintedCallback != null) call(cellRangePaintedCallback, selectedCellRow + "," + selectedCellColumn + "," + row1 + "," + col1 + "," + row2 + "," + col2);
        else consoleLog("cellRangePainted callback not set");
    }

    @Override
    public void deleteSelectedCells() {
        if (deleteSelectedCellsCallback != null) call(deleteSelectedCellsCallback);
        else consoleLog("deleteSelectedCells callback not set");
    }

    @Override
    public void linkCellClicked(int row, int column) {
        if (linkCellClickedCallback != null) call(linkCellClickedCallback,row + "," + column);
        else consoleLog("linkCellClicked callback not set");
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int row1, int col1, int row2, int col2) {
        if (rowsResizedCallback != null) call(rowsResizedCallback, Serializer.serializeMapIntegerFloat(newRowSizes) + "," + row1 + "," + col1 + "," + row2 + "," + col2);
        else consoleLog("rowsResized callback not set");
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int row1, int col1, int row2, int col2) {
        if (columnResizedCallback != null) call(columnResizedCallback, Serializer.serializeMapIntegerInteger(newColumnSizes) + "," + row1 + "," + col1 + "," + row2 + "," + col2);
        else consoleLog("columnResized callback not set");
    }

    @Override
    public void onRowAutofit(int rowIndex) {
        if (onRowAutofitCallback != null) call(onRowAutofitCallback, rowIndex);
        else consoleLog("onRowAutofit callback not set");
    }

    @Override
    public void onColumnAutofit(int columnIndex) {
        if (onColumnAutofitCallback != null) call(onColumnAutofitCallback, columnIndex);
        else consoleLog("onColumnAutofit callback not set");
    }

    @Override
    public void onUndo() {
        if (onUndoCallback != null) call(onUndoCallback);
        else consoleLog("onUndo callback not set");
    }

    @Override
    public void onRedo() {
        if (onRedoCallback != null) call(onRedoCallback);
        else consoleLog("onRedo callback not set");
    }

    @Override
    public void setCellStyleWidthRatios(HashMap<Integer, Float> cellStyleWidthRatioMap) {
        if (setCellStyleWidthRatiosCallback != null) call(setCellStyleWidthRatiosCallback, Serializer.serializeMapIntegerFloat(cellStyleWidthRatioMap));
        else consoleLog("setCellStyleWidthRatios callback not set");
    }

    @Override
    public void protectedCellWriteAttempted() {
        if (protectedCellWriteAttemptedCallback != null) call(protectedCellWriteAttemptedCallback);
        else consoleLog("protectedCellWriteAttempted callback not set");
    }

    @Override
    public void onPaste(String text) {
        if (onPasteCallback != null) call(onPasteCallback, text);
        else consoleLog("onPaste callback not set");
    }

    @Override
    public void clearSelectedCellsOnCut() {
        if (clearSelectedCellsOnCutCallback!= null) call(clearSelectedCellsOnCutCallback);
        else consoleLog("clearSelectedCellsOnCut callback not set");
    }

    @Override
    public void updateCellComment(String text, int col, int row) {
        if (updateCellCommentCallback != null) call(updateCellCommentCallback, "\"" + text.replaceAll("\"","\\\"") + "\"," + col + "," + row);
        else consoleLog("updateCellComment callback not set");
    }

    @Override
    public void onConnectorInit() {
        if (callbackForOnConnectorInit != null) call(callbackForOnConnectorInit);
        else consoleLog("onConnectorInit callback not set");
    }

    @Override
    public void contextMenuOpenOnSelection(int row, int column) {
        if (contextMenuOpenOnSelectionCallback != null) call(contextMenuOpenOnSelectionCallback, "" + row + "," + column);
        else consoleLog("contextMenuOpenOnSelection callback not set");
    }

    @Override
    public void actionOnCurrentSelection(String actionKey) {
        if (actionOnCurrentSelectionCallback != null) call(actionOnCurrentSelectionCallback, actionKey);
        else consoleLog("actionOnCurrentSelection callback not set");
    }

    @Override
    public void rowHeaderContextMenuOpen(int rowIndex) {
        if (rowHeaderContextMenuOpenCallback != null) call(rowHeaderContextMenuOpenCallback, rowIndex);
        else consoleLog("rowHeaderContextMenuOpen callback not set");
    }

    @Override
    public void actionOnRowHeader(String actionKey) {
        if (actionOnRowHeaderCallback != null) call(actionOnRowHeaderCallback, actionKey);
        else consoleLog("actionOnRowHeader callback not set");
    }

    @Override
    public void columnHeaderContextMenuOpen(int columnIndex) {
        if (columnHeaderContextMenuOpenCallback!= null) call(columnHeaderContextMenuOpenCallback, columnIndex);
        else consoleLog("columnHeaderContextMenuOpen callback not set");
    }

    @Override
    public void actionOnColumnHeader(String actionKey) {
        if (actionOnColumnHeaderCallback != null) call(actionOnColumnHeaderCallback, actionKey);
        else consoleLog("actionOnColumnHeader callback not set");
    }
}
