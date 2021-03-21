package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ActionOnColumnHeaderCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ActionOnCurrentSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ActionOnRowHeaderCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellAddedToSelectionAndSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellRangePaintedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellRangeSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellValueEditedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellsAddedToRangeSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ClearSelectedCellsOnCutCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnAddedToSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnHeaderContextMenuOpenCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnResizedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ContextMenuOpenOnSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.DeleteSelectedCellsCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.GroupingCollapsedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.LevelHeaderClickedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.LinkCellClickedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnColumnAutofitCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnConnectorInitCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnPasteCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnRedoCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnRowAutofitCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnSheetScrollCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnUndoCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ProtectedCellWriteAttemptedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowAddedToRangeSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowHeaderContextMenuOpenCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowsResizedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SelectionDecreasePaintedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SelectionIncreasePaintedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SetCellStyleWidthRatiosCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetAddressChangedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetCreatedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetRenamedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.UpdateCellCommentCallback;

public class SpreadsheetServerRpcImpl implements SpreadsheetServerRpc {

    private OnConnectorInitCallback callbackForOnConnectorInit;
    private GroupingCollapsedCallback groupingCollapsedCallback;
    private LevelHeaderClickedCallback levelHeaderClickedCallback;
    private OnSheetScrollCallback onSheetScrollCallback;
    private SheetAddressChangedCallback sheetAddressChangedCallback;
    private CellSelectedCallback cellSelectedCallback;
    private CellRangeSelectedCallback cellRangeSelectedCallback;
    private CellAddedToSelectionAndSelectedCallback cellAddedToSelectionAndSelected;
    private CellsAddedToRangeSelectionCallback cellsAddedToRangeSelectionCallback;
    private RowSelectedCallback rowSelectedCallback;
    private RowAddedToRangeSelectionCallback rowAddedToRangeSelectionCallback;
    private ColumnSelectedCallback columnSelectedCallback;
    private ColumnAddedToSelectionCallback columnAddedToSelectionCallback;
    private SelectionIncreasePaintedCallback selectionIncreasePaintedCallback;
    private SelectionDecreasePaintedCallback selectionDecreasePaintedCallback;
    private CellValueEditedCallback cellValueEditedCallback;
    private SheetSelectedCallback sheetSelectedCallback;
    private SheetRenamedCallback sheetRenamedCallback;
    private SheetCreatedCallback sheetCreatedCallback;
    private CellRangePaintedCallback cellRangePaintedCallback;
    private DeleteSelectedCellsCallback deleteSelectedCellsCallback;
    private LinkCellClickedCallback linkCellClickedCallback;
    private RowsResizedCallback rowsResizedCallback;
    private ColumnResizedCallback columnResizedCallback;
    private OnRowAutofitCallback onRowAutofitCallback;
    private OnColumnAutofitCallback onColumnAutofitCallback;
    private OnUndoCallback onUndoCallback;
    private OnRedoCallback onRedoCallback;
    private SetCellStyleWidthRatiosCallback setCellStyleWidthRatiosCallback;
    private ProtectedCellWriteAttemptedCallback protectedCellWriteAttemptedCallback;
    private OnPasteCallback onPasteCallback;
    private ClearSelectedCellsOnCutCallback clearSelectedCellsOnCutCallback;
    private UpdateCellCommentCallback updateCellCommentCallback;
    private ContextMenuOpenOnSelectionCallback contextMenuOpenOnSelectionCallback;
    private ActionOnCurrentSelectionCallback actionOnCurrentSelectionCallback;
    private RowHeaderContextMenuOpenCallback rowHeaderContextMenuOpenCallback;
    private ActionOnRowHeaderCallback actionOnRowHeaderCallback;
    private ColumnHeaderContextMenuOpenCallback columnHeaderContextMenuOpenCallback;
    private ActionOnColumnHeaderCallback actionOnColumnHeaderCallback;

    public void setGroupingCollapsedCallback(GroupingCollapsedCallback callback) {
        groupingCollapsedCallback = callback;
    }

    public void setLevelHeaderClickedCallback(LevelHeaderClickedCallback callback) {
        levelHeaderClickedCallback = callback;
    }

    public void setOnSheetScrollCallback(OnSheetScrollCallback callback) {
        onSheetScrollCallback = callback;
    }

    public void setSheetAddressChangedCallback(SheetAddressChangedCallback callback) {
        sheetAddressChangedCallback = callback;
    }

    public void setCellSelectedCallback(CellSelectedCallback callback) {
        cellSelectedCallback = callback;
    }

    public void setCellRangeSelectedCallback(CellRangeSelectedCallback callback) {
        cellRangeSelectedCallback = callback;
    }

    public void setCellAddedToSelectionAndSelectedCallback(CellAddedToSelectionAndSelectedCallback callback) {
        cellAddedToSelectionAndSelected = callback;
    }

    public void setCellsAddedToRangeSelectionCallback(CellsAddedToRangeSelectionCallback callback) {
        cellsAddedToRangeSelectionCallback = callback;
    }

    public void setRowSelectedCallback(RowSelectedCallback callback) {
        rowSelectedCallback = callback;
    }

    public void setRowAddedToRangeSelectionCallback(RowAddedToRangeSelectionCallback callback) {
        rowAddedToRangeSelectionCallback = callback;
    }

    public void setColumnSelectedCallback(ColumnSelectedCallback callback) {
        columnSelectedCallback = callback;
    }

    public void setColumnAddedToSelectionCallback(ColumnAddedToSelectionCallback callback) {
        columnAddedToSelectionCallback = callback;
    }

    public void setSelectionIncreasePaintedCallback(SelectionIncreasePaintedCallback callback) {
        selectionIncreasePaintedCallback = callback;
    }


    public void setSelectionDecreasePaintedCallback(SelectionDecreasePaintedCallback callback) {
        selectionDecreasePaintedCallback = callback;
    }

    public void setCellValueEditedCallback(CellValueEditedCallback callback) {
        cellValueEditedCallback = callback;
    }

    public void setSheetSelectedCallback(SheetSelectedCallback callback) {
        sheetSelectedCallback = callback;
    }

    public void setSheetRenamedCallback(SheetRenamedCallback callback) {
        sheetRenamedCallback = callback;
    }

    public void setSheetCreatedCallback(SheetCreatedCallback callback) {
        sheetCreatedCallback = callback;
    }

    public void setCellRangePaintedCallback(CellRangePaintedCallback callback) {
        cellRangePaintedCallback = callback;
    }

    public void setDeleteSelectedCellsCallback(DeleteSelectedCellsCallback callback) {
        deleteSelectedCellsCallback = callback;
    }

    public void setLinkCellClickedCallback(LinkCellClickedCallback callback) {
        linkCellClickedCallback = callback;
    }

    public void setRowsResizedCallback(RowsResizedCallback callback) {
        rowsResizedCallback = callback;
    }

    public void setColumnResizedCallback(ColumnResizedCallback callback) {
        columnResizedCallback = callback;
    }

    public void setOnRowAutofitCallback(OnRowAutofitCallback callback) {
        onRowAutofitCallback = callback;
    }

    public void setOnColumnAutofitCallback(OnColumnAutofitCallback callback) {
        onColumnAutofitCallback = callback;
    }

    public void setOnUndoCallback(OnUndoCallback callback) {
        onUndoCallback = callback;
    }

    public void setOnRedoCallback(OnRedoCallback callback) {
        onRedoCallback = callback;
    }

    public void setSetCellStyleWidthRatiosCallback(SetCellStyleWidthRatiosCallback callback) {
        setCellStyleWidthRatiosCallback = callback;
    }

    public void setProtectedCellWriteAttemptedCallback(ProtectedCellWriteAttemptedCallback callback) {
        protectedCellWriteAttemptedCallback = callback;
    }

    public void setOnPasteCallback(OnPasteCallback callback) {
        onPasteCallback = callback;
    }

    public void setClearSelectedCellsOnCutCallback(ClearSelectedCellsOnCutCallback callback) {
        clearSelectedCellsOnCutCallback = callback;
    }

    public void setUpdateCellCommentCallback(UpdateCellCommentCallback callback) {
        updateCellCommentCallback = callback;
    }

    public void setOnConnectorInitCallback(OnConnectorInitCallback callback) {
        callbackForOnConnectorInit = callback;
    }

    public void setContextMenuOpenOnSelectionCallback(ContextMenuOpenOnSelectionCallback callback) {
        contextMenuOpenOnSelectionCallback = callback;
    }

    public void setActionOnCurrentSelectionCallback(ActionOnCurrentSelectionCallback callback) {
        actionOnCurrentSelectionCallback = callback;
    }

    public void setRowHeaderContextMenuOpenCallback(RowHeaderContextMenuOpenCallback callback) {
        rowHeaderContextMenuOpenCallback = callback;
    }

    public void setActionOnRowHeaderCallback(ActionOnRowHeaderCallback callback) {
        actionOnRowHeaderCallback = callback;
    }

    public void setColumnHeaderContextMenuOpenCallback(ColumnHeaderContextMenuOpenCallback callback) {
        columnHeaderContextMenuOpenCallback = callback;
    }

    public void setActionOnColumnHeaderCallback(ActionOnColumnHeaderCallback callback) {
        actionOnColumnHeaderCallback = callback;
    }


    native void consoleLog(String message) /*-{
      console.log( "serverrpc" + message );
  }-*/;

    native void call(Object f, Object ...args) /*-{
      f(args);
  }-*/;



    @Override
    public void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed) {
        if (groupingCollapsedCallback != null) groupingCollapsedCallback.apply(cols, colIndex, collapsed);
        else consoleLog("setGroupingCollapsed");
    }

    @Override
    public void levelHeaderClicked(boolean cols, int level) {
        if (levelHeaderClickedCallback != null) levelHeaderClickedCallback.apply(cols, level);
        else consoleLog("levelHeaderClicked");
    }

    @Override
    public void onSheetScroll(int firstRow, int firstColumn, int lastRow, int lastColumn) {
        if (onSheetScrollCallback != null) onSheetScrollCallback.apply(firstRow, firstColumn, lastRow, lastColumn);
        else consoleLog("onSheetScroll");
    }

    @Override
    public void sheetAddressChanged(String value) {
        if (sheetAddressChangedCallback!= null) sheetAddressChangedCallback.apply(value);
        else consoleLog("sheetAddressChanged");
    }

    @Override
    public void cellSelected(int row, int column, boolean oldSelectionRangeDiscarded) {
        if (cellSelectedCallback != null) cellSelectedCallback.apply(row, column, oldSelectionRangeDiscarded);
        else consoleLog("cellSelected");
    }

    @Override
    public void cellRangeSelected(int row1, int col1, int row2, int col2) {
        if (cellRangeSelectedCallback != null) cellRangeSelectedCallback.apply(row1, col1, row2, col2);
        else consoleLog("cellRangeSelected");
    }

    @Override
    public void cellAddedToSelectionAndSelected(int row, int column) {
        if (cellAddedToSelectionAndSelected != null) cellAddedToSelectionAndSelected.apply(row, column);
        else consoleLog("cellAddedToSelectionAndSelected");
    }

    @Override
    public void cellsAddedToRangeSelection(int row1, int col1, int row2, int col2) {
        if (cellsAddedToRangeSelectionCallback != null) cellsAddedToRangeSelectionCallback.apply(row1, col1, row2, col2);
        else consoleLog("cellsAddedToRangeSelection");
    }

    @Override
    public void rowSelected(int row, int firstColumnIndex) {
        if (rowSelectedCallback != null) rowSelectedCallback.apply(row, firstColumnIndex);
        else consoleLog("rowSelected");
    }

    @Override
    public void rowAddedToRangeSelection(int row, int firstColumnIndex) {
        if (rowAddedToRangeSelectionCallback != null) rowAddedToRangeSelectionCallback.apply(row, firstColumnIndex);
        else consoleLog("rowAddedToRangeSelection");
    }

    @Override
    public void columnSelected(int column, int firstRowIndex) {
        if (columnSelectedCallback!= null) columnSelectedCallback.apply(column, firstRowIndex);
        else consoleLog("columnSelected");
    }

    @Override
    public void columnAddedToSelection(int firstRowIndex, int column) {
        if (columnAddedToSelectionCallback != null) columnAddedToSelectionCallback.apply(firstRowIndex, column);
        else consoleLog("columnAddedToSelection");
    }

    @Override
    public void selectionIncreasePainted(int r1, int c1, int r2, int c2) {
        if (selectionIncreasePaintedCallback != null) selectionIncreasePaintedCallback.apply(r1, c1, r2, c2);
        else consoleLog("selectionIncreasePainted");
    }

    @Override
    public void selectionDecreasePainted(int row, int col) {
        if (selectionDecreasePaintedCallback != null) selectionDecreasePaintedCallback.apply(row, col);
        else consoleLog("selectionDecreasePainted");
    }

    @Override
    public void cellValueEdited(int row, int col, String value) {
        if (cellValueEditedCallback!= null) cellValueEditedCallback.apply(row, col, value);
        else consoleLog("cellValueEdited");
    }

    @Override
    public void sheetSelected(int sheetIndex, int scrollLeft, int scrollTop) {
        if (sheetSelectedCallback != null) sheetSelectedCallback.apply(sheetIndex, scrollLeft, scrollTop);
        else consoleLog("sheetSelected");
    }

    @Override
    public void sheetRenamed(int sheetIndex, String newName) {
        if (sheetRenamedCallback != null) sheetRenamedCallback.apply(sheetIndex, newName);
        else consoleLog("sheetRenamed");
    }

    @Override
    public void sheetCreated(int scrollLeft, int scrollTop) {
        if (sheetCreatedCallback != null) sheetCreatedCallback.apply(scrollLeft, scrollTop);
        else consoleLog("sheetCreated");
    }

    @Override
    public void cellRangePainted(int selectedCellRow, int selectedCellColumn, int row1, int col1, int row2, int col2) {
        if (cellRangePaintedCallback != null) cellRangePaintedCallback.apply(selectedCellRow, selectedCellColumn, row1, col1, row2, col2);
        else consoleLog("cellRangePainted");
    }

    @Override
    public void deleteSelectedCells() {
        if (deleteSelectedCellsCallback != null) deleteSelectedCellsCallback.apply();
        else consoleLog("deleteSelectedCells");
    }

    @Override
    public void linkCellClicked(int row, int column) {
        if (linkCellClickedCallback != null) linkCellClickedCallback.apply(row, column);
        else consoleLog("linkCellClicked");
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int row1, int col1, int row2, int col2) {
        if (rowsResizedCallback != null) rowsResizedCallback.apply(newRowSizes, row1, col1, row2, col2);
        else consoleLog("rowsResized");
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int row1, int col1, int row2, int col2) {
        if (columnResizedCallback != null) columnResizedCallback.apply(newColumnSizes, row1, col1, row2, col2);
        else consoleLog("columnResized");
    }

    @Override
    public void onRowAutofit(int rowIndex) {
        if (onRowAutofitCallback != null) onRowAutofitCallback.apply(rowIndex);
        else consoleLog("onRowAutofit");
    }

    @Override
    public void onColumnAutofit(int columnIndex) {
        if (onColumnAutofitCallback != null) onColumnAutofitCallback.apply(columnIndex);
        else consoleLog("onColumnAutofit");
    }

    @Override
    public void onUndo() {
        if (onUndoCallback != null) onUndoCallback.apply();
        else consoleLog("onUndo");
    }

    @Override
    public void onRedo() {
        if (onRedoCallback != null) onRedoCallback.apply();
        else consoleLog("onRedo");
    }

    @Override
    public void setCellStyleWidthRatios(HashMap<Integer, Float> cellStyleWidthRatioMap) {
        if (setCellStyleWidthRatiosCallback != null) setCellStyleWidthRatiosCallback.apply(cellStyleWidthRatioMap);
        else consoleLog("setCellStyleWidthRatios");
    }

    @Override
    public void protectedCellWriteAttempted() {
        if (protectedCellWriteAttemptedCallback != null) protectedCellWriteAttemptedCallback.apply();
        else consoleLog("protectedCellWriteAttempted");
    }

    @Override
    public void onPaste(String text) {
        if (onPasteCallback != null) onPasteCallback.apply(text);
        else consoleLog("onPaste");
    }

    @Override
    public void clearSelectedCellsOnCut() {
        if (clearSelectedCellsOnCutCallback!= null) clearSelectedCellsOnCutCallback.apply();
        else consoleLog("clearSelectedCellsOnCut");
    }

    @Override
    public void updateCellComment(String text, int col, int row) {
        if (updateCellCommentCallback != null) updateCellCommentCallback.apply(text, col, row);
        else consoleLog("updateCellComment");
    }

    @Override
    public void onConnectorInit() {
        if (callbackForOnConnectorInit != null) callbackForOnConnectorInit.apply();
        else consoleLog("onConnectorInit");
    }

    @Override
    public void contextMenuOpenOnSelection(int row, int column) {
        if (contextMenuOpenOnSelectionCallback != null) contextMenuOpenOnSelectionCallback.apply(row, column);
        else consoleLog("contextMenuOpenOnSelection");
    }

    @Override
    public void actionOnCurrentSelection(String actionKey) {
        if (actionOnCurrentSelectionCallback != null) actionOnCurrentSelectionCallback.apply(actionKey);
        else consoleLog("actionOnCurrentSelection");
    }

    @Override
    public void rowHeaderContextMenuOpen(int rowIndex) {
        if (rowHeaderContextMenuOpenCallback != null) rowHeaderContextMenuOpenCallback.apply(rowIndex);
        else consoleLog("rowHeaderContextMenuOpen");
    }

    @Override
    public void actionOnRowHeader(String actionKey) {
        if (actionOnRowHeaderCallback != null) actionOnRowHeaderCallback.apply(actionKey);
        else consoleLog("actionOnRowHeader");
    }

    @Override
    public void columnHeaderContextMenuOpen(int columnIndex) {
        if (columnHeaderContextMenuOpenCallback!= null) columnHeaderContextMenuOpenCallback.apply(columnIndex);
        else consoleLog("columnHeaderContextMenuOpen");
    }

    @Override
    public void actionOnColumnHeader(String actionKey) {
        if (actionOnColumnHeaderCallback != null) actionOnColumnHeaderCallback.apply(actionKey);
        else consoleLog("actionOnColumnHeader");
    }
}
