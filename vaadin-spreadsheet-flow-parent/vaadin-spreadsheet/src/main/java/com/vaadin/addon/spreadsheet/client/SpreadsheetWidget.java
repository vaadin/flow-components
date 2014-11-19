package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.addon.spreadsheet.client.SheetTabSheet.SheetTabSheetHandler;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;

public class SpreadsheetWidget extends Composite implements SheetHandler,
        FormulaBarHandler, SheetTabSheetHandler {

    public interface SheetContextMenuHandler {
        /**
         * Right click (event) on top of the cell at the indexes.
         * 
         * @param event
         *            the browser event related (right mouse button click)
         * @param column
         *            1-based, index of cell
         * @param row
         *            1-based, index of cell
         */
        public void cellContextMenu(NativeEvent event, int column, int row);

        /**
         * Right click (event) on top of row header at the index
         * 
         * @param nativeEvent
         * @param rowIndex
         *            1-based
         */
        public void rowHeaderContextMenu(NativeEvent nativeEvent, int rowIndex);

        /**
         * Right click (event) on top of column header at the index
         * 
         * @param nativeEvent
         * @param columnIndex
         *            1-based
         */
        public void columnHeaderContextMenu(NativeEvent nativeEvent,
                int columnIndex);
    }

    private final SheetWidget sheetWidget;
    private final FormulaBarWidget formulaBarWidget;
    private final SheetTabSheet sheetTabSheet;

    private SpreadsheetHandler spreadsheetHandler;

    private SheetContextMenuHandler sheetContextMenuHandler;

    private SpreadsheetCustomEditorFactory customEditorFactory;

    private int rowBufferSize;

    private int columnBufferSize;

    private int rows;

    private int cols;

    private float defRowH;
    private int defColW;

    private float[] rowH;
    private int[] colW;

    private int sheets;
    /** 1-based */
    private int activeSheetIndex;

    private Map<Integer, String> cellStyleToCSSStyle;

    private boolean loaded;
    private boolean formulaBarEditing;
    private boolean inlineEditing;
    private boolean cancelDeferredCommit;
    private boolean selectedCellIsFormulaType;
    private boolean cellLocked;
    private boolean customCellEditorDisplayed;
    private boolean sheetProtected;
    private boolean cancelNextSheetRelayout;
    private boolean hasSelectedCellChangedOnClick;
    private String cachedCellValue;
    private int[] verticalScrollPositions;
    private int[] horizontalScrollPositions;
    // private int firstVisibleTab; Not working in POI -> disabled
    private String[] sheetNames;
    private List<Integer> hiddenColumnIndexes;
    private List<Integer> hiddenRowIndexes;
    private List<MergedRegion> mergedRegions;
    private int colBeforeMergedCell;
    private int rowBeforeMergedCell;

    /**
     * Timer flag for sending lazy RPCs to server. Used so that we don't send an
     * RPC for each key press. Default timeout is a second.
     */
    private boolean okToSendCellProtectRpc = true;

    private MergedRegionContainer mergedRegionContainer = new MergedRegionContainer() {

        @Override
        public MergedRegion getMergedRegionStartingFrom(int column, int row) {
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 == column && region.row1 == row) {
                        return region;
                    }
                }
            }
            return null;
        }

        @Override
        public MergedRegion getMergedRegion(int column, int row) {
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 <= column && region.row1 <= row
                            && region.col2 >= column && region.row2 >= row) {
                        return region;
                    }
                }
            }
            return null;
        }
    };

    public SpreadsheetWidget() {
        sheetWidget = new SheetWidget(this);
        formulaBarWidget = new FormulaBarWidget(this);
        sheetTabSheet = new SheetTabSheet(this);

        sheetWidget.getElement().appendChild(formulaBarWidget.getElement());
        sheetWidget.getElement().appendChild(sheetTabSheet.getElement());

        initWidget(sheetWidget);
    }

    public SheetWidget getSheetWidget() {
        return sheetWidget;
    }

    public void load() {
        if (loaded) {
            clearSpreadsheet(false);
        } else {
            loaded = true;
        }
        loadSheet(activeSheetIndex - 1);
    }

    public void sheetUpdated(String[] sheetNames, int sheetIndex) {
        if (!loaded) { // component first load
            sheetTabSheet.addTabs(sheetNames);
            sheetTabSheet.setSelectedTab(sheetIndex);
        } else {
            if (activeSheetIndex != sheetIndex) {
                // active sheet or whole spreadsheet has changed
                sheetTabSheet.setTabs(sheetNames, true);
                sheetTabSheet.setSelectedTab(sheetIndex);
            } else if (this.sheetNames == null
                    || !this.sheetNames.equals(sheetNames)) {
                // sheet renamed
                sheetTabSheet.setTabs(sheetNames, false);
            }
        }
        this.sheetNames = sheetNames;
        activeSheetIndex = sheetIndex;
    }

    public void widgetSizeChanged() {
        sheetWidget.onWidgetResize();
        sheetTabSheet.onWidgetResize();
    }

    /** Clear all current sheet related data */
    public void clearSpreadsheet(boolean removed) {
        colBeforeMergedCell = 0;
        rowBeforeMergedCell = 0;
        // reset function bar
        formulaBarWidget.clear();
        clearMergedRegions();
        // reset sheet
        sheetWidget.clearAll(removed);
    }

    /**
     * 
     * @param sheetIndex
     *            0-based index of the sheet to load
     */
    protected void loadSheet(final int sheetIndex) {
        // load all sheet stuff from model
        final int scrollLeft;
        if (horizontalScrollPositions.length > sheetIndex) {
            scrollLeft = horizontalScrollPositions[sheetIndex];
        } else {
            scrollLeft = 0;
        }
        final int scrollTop;
        if (verticalScrollPositions.length > sheetIndex) {
            scrollTop = verticalScrollPositions[sheetIndex];
        } else {
            scrollTop = 0;
        }
        sheetWidget.resetFromModel(scrollLeft, scrollTop);

        if (scrollLeft != 0 || scrollTop != 0) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    sheetWidget.setScrollPosition(scrollLeft, scrollTop);
                }
            });
        }
    }

    public void relayoutSheet() {
        if (cancelNextSheetRelayout) {
            cancelNextSheetRelayout = false;
        } else {
            sheetWidget.relayoutSheet(true);
        }
    }

    public void setSpreadsheetHandler(SpreadsheetHandler spreadsheetHandler) {
        this.spreadsheetHandler = spreadsheetHandler;
    }

    public void setSheetContextMenuHandler(
            SheetContextMenuHandler sheetContextMenuHandler) {
        this.sheetContextMenuHandler = sheetContextMenuHandler;
    }

    @Override
    public boolean hasCustomContextMenu() {
        return sheetContextMenuHandler != null;
    }

    public void showCellCustomComponents(HashMap<String, Widget> customWidgetMap) {
        sheetWidget.showCustomWidgets(customWidgetMap);
    }

    public void addPopupButton(PopupButtonWidget widget) {
        sheetWidget.addPopupButton(widget);
    }

    public void removePopupButton(PopupButtonWidget popupButton) {
        sheetWidget.removePopupButton(popupButton);
    }

    public void showCellValue(String value, int col, int row, boolean formula,
            boolean locked) {
        // do check in case the user has changed the selected cell before the
        // formula was sent
        if (sheetWidget.getSelectedCellColumn() == col
                && sheetWidget.getSelectedCellRow() == row) {
            cellLocked = locked;
            if (formula && !value.isEmpty()) {
                formulaBarWidget.setCellFormulaValue(value);
                sheetWidget.updateInputValue("=" + value);
            } else {
                formulaBarWidget.setCellPlainValue(value);
                if (!value.isEmpty()) {
                    cachedCellValue = value;
                }
            }
            if (!customCellEditorDisplayed) {
                formulaBarWidget.setFormulaFieldEnabled(!locked);
            }
        }
    }

    public void setCellSelection(int col, int row, String value,
            boolean formula, boolean locked) {
        if (customCellEditorDisplayed) {
            customCellEditorDisplayed = false;
            sheetWidget.removeCustomCellEditor();
        }
        cellLocked = locked;
        sheetWidget.setSelectedCell(col, row);
        newSelectedCellSet();
        sheetWidget.focusSheet();
        if (!sheetWidget.isCoherentSelection()) {
            sheetWidget.setCoherentSelection(true);
        }
        if (!sheetWidget.isSelectionRangeOutlineVisible()) {
            sheetWidget.setSelectionRangeOutlineVisible(true);
            sheetWidget.clearSelectedCellStyle();
        }
        sheetWidget.updateSelectionOutline(col, col, row, row);
        sheetWidget.updateSelectedCellStyles(col, col, row, row, true);
        if (formula) {
            formulaBarWidget.setCellFormulaValue(value);
        } else {
            formulaBarWidget.setCellPlainValue(value);
        }
        formulaBarWidget.setFormulaFieldEnabled(!locked);
        formulaBarWidget.setSelectedCellAddress(createCellAddress(col, row));

        // scroll the cell into view
        if (!sheetWidget.isSelectedCellCompletelyVisible()) {
            sheetWidget.scrollCellIntoView(col, row);
        }
        sheetWidget.focusSheet();
    }

    public void invalidCellAddress() {
        formulaBarWidget.revertCellAddressValue();
    }

    public void setCellRangeSelection(int selectedCellColumn,
            int selectedCellRow, int firstColumn, int lastColumn, int firstRow,
            int lastRow, String value, boolean formula, boolean locked) {
        cellLocked = locked;
        if (formula) {
            formulaBarWidget.setCellFormulaValue(value);
        } else {
            formulaBarWidget.setCellPlainValue(value);
        }
        formulaBarWidget.setFormulaFieldEnabled(!locked);
        formulaBarWidget.setSelectedCellAddress(createCellAddress(firstColumn,
                firstRow));
        if (!sheetWidget.isCoherentSelection()) {
            sheetWidget.setCoherentSelection(true);
        }
        if (!sheetWidget.isSelectionRangeOutlineVisible()) {
            sheetWidget.setSelectionRangeOutlineVisible(true);
            sheetWidget.clearSelectedCellStyle();
        }
        final int oldSelectedCellCol = sheetWidget.getSelectedCellColumn();
        final int oldSelectedCellRow = sheetWidget.getSelectedCellRow();
        if (oldSelectedCellCol != selectedCellColumn
                || oldSelectedCellRow != selectedCellRow) {
            sheetWidget.setSelectedCell(selectedCellColumn, selectedCellRow);
            newSelectedCellSet();
        }
        sheetWidget.updateSelectionOutline(firstColumn, lastColumn, firstRow,
                lastRow);
        sheetWidget.updateSelectedCellStyles(firstColumn, lastColumn, firstRow,
                lastRow, true);
        if (!sheetWidget.isAreaCompletelyVisible(firstColumn, lastColumn,
                firstRow, lastRow)) {
            sheetWidget.scrollAreaIntoView(firstColumn, lastColumn, firstRow,
                    lastRow);
        }

        sheetWidget.focusSheet();
    }

    public void focusSheet() {
        sheetWidget.focusSheet();
    }

    /**
     * @return the customEditorFactory
     */
    public SpreadsheetCustomEditorFactory getCustomEditorFactory() {
        return customEditorFactory;
    }

    /**
     * @param customEditorFactory
     *            the customEditorFactory to set
     */
    public void setCustomEditorFactory(
            SpreadsheetCustomEditorFactory customEditorFactory) {
        this.customEditorFactory = customEditorFactory;

        newSelectedCellSet();
    }

    /**
     * Called when the {@link #customEditorFactory} might have a new editor for
     * the currently selected cell.
     */
    public void loadSelectedCellEditor() {
        if (!sheetWidget.isSelectedCellCustomized()
                && !cellLocked
                && customEditorFactory != null
                && customEditorFactory.hasCustomEditor(sheetWidget
                        .getSelectedCellKey())) {
            Widget customEditor = customEditorFactory
                    .getCustomEditor(sheetWidget.getSelectedCellKey());
            if (customEditor != null) {
                customCellEditorDisplayed = true;
                formulaBarWidget.setFormulaFieldEnabled(false);
                sheetWidget.displayCustomCellEditor(customEditor);
            }
        }
    }

    public void addVisibleCellComment(String key) {
        sheetWidget.setCellCommentVisible(true, key);
    }

    public void removeVisibleCellComment(String key) {
        sheetWidget.setCellCommentVisible(false, key);
    }

    public void addImage(String key, String resourceURL, ImageInfo imageInfo) {
        SheetImage image = new SheetImage(resourceURL);
        updateSheetImageInfo(image, imageInfo);
        sheetWidget.addSheetImage(key, image);
    }

    public void updateImage(String key, ImageInfo imageInfo) {
        updateSheetImageInfo(sheetWidget.getSheetImage(key), imageInfo);
    }

    private void updateSheetImageInfo(SheetImage image, ImageInfo imageInfo) {
        image.setLocation(imageInfo.col, imageInfo.row);
        image.setHeight(imageInfo.height);
        image.setWidth(imageInfo.width);
        image.setPadding(imageInfo.dx, imageInfo.dy);
    }

    public void removeImage(String key) {
        sheetWidget.removeSheetImage(key);
    }

    public void updateMergedRegions(ArrayList<MergedRegion> mergedRegions) {
        // remove old, add new
        clearMergedRegions();
        if (mergedRegions != null) {
            int i = 0;
            while (i < mergedRegions.size()) {
                MergedRegion newMergedRegion = mergedRegions.get(i);
                sheetWidget.addMergedRegion(newMergedRegion);
                i++;
            }
        }

        // copy list for later
        this.mergedRegions = new ArrayList<MergedRegion>(mergedRegions);
    }

    private void clearMergedRegions() {
        if (this.mergedRegions != null) {
            while (0 < mergedRegions.size()) {
                sheetWidget.removeMergedRegion(this.mergedRegions.remove(0), 0);
            }
        }
    }

    @Override
    public void onScrollViewChanged(int firstRowIndex, int lastRowIndex,
            int firstColumnIndex, int lastColumnIndex) {
        spreadsheetHandler.onSheetScroll(firstRowIndex, lastRowIndex,
                firstColumnIndex, lastColumnIndex);
    }

    @Override
    public void onLinkCellClick(int column, int row) {
        spreadsheetHandler.linkCellClicked(column, row);
    }

    @Override
    public void onCellRightClick(NativeEvent event, int column, int row) {
        // logic handled on server side
        if (sheetContextMenuHandler != null) {
            if (column != sheetWidget.getSelectedCellColumn()
                    || row != sheetWidget.getSelectedCellRow()) {
                doCommitIfEditing();
            }
            sheetContextMenuHandler.cellContextMenu(event, column, row);
        }
    }

    @Override
    public void onRowHeaderRightClick(NativeEvent nativeEvent, int rowIndex) {
        if (sheetContextMenuHandler != null) {
            sheetContextMenuHandler.rowHeaderContextMenu(nativeEvent, rowIndex);
        }
    }

    @Override
    public void onColumnHeaderRightClick(NativeEvent nativeEvent,
            int columnIndex) {
        if (sheetContextMenuHandler != null) {
            sheetContextMenuHandler.columnHeaderContextMenu(nativeEvent,
                    columnIndex);
        }
    }

    @Override
    public void onCellClick(int column, int row, String value,
            boolean shiftKey, boolean metaOrCtrlKey,
            boolean updateToActionHandler) {
        doCommitIfEditing();
        if (column == 0 || row == 0) {
            return;
        }
        if (!updateToActionHandler) {
            hasSelectedCellChangedOnClick = row != sheetWidget
                    .getSelectedCellRow()
                    || column != sheetWidget.getSelectedCellColumn();
        }
        if (shiftKey) {
            // select everything from previously selected cell to the clicked
            // cell, keep the old selected cell as the selected
            final int selectedCellCol = sheetWidget.getSelectedCellColumn();
            final int selectedCellRow = sheetWidget.getSelectedCellRow();
            int c1 = selectedCellCol > column ? column : selectedCellCol;
            int c2 = selectedCellCol > column ? selectedCellCol : column;
            int r1 = selectedCellRow > row ? row : selectedCellRow;
            int r2 = selectedCellRow > row ? selectedCellRow : row;
            MergedRegion selectedRegion = MergedRegionUtil
                    .findIncreasingSelection(mergedRegionContainer, r1, r2, c1,
                            c2);
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.updateSelectionOutline(selectedRegion.col1,
                        selectedRegion.col2, selectedRegion.row1,
                        selectedRegion.row2);
                sheetWidget.updateSelectedCellStyles(selectedRegion.col1,
                        selectedRegion.col2, selectedRegion.row1,
                        selectedRegion.row2, true);
            } else {
                sheetWidget.updateSelectedCellStyles(selectedRegion.col1,
                        selectedRegion.col2, selectedRegion.row1,
                        selectedRegion.row2, false);
            }
            // TODO update the selection coherence, if the areas align properly
            if (!sheetWidget.isCoherentSelection()) {
            }
            if (updateToActionHandler) {
                if (sheetWidget.isSelectionRangeOutlineVisible()) {
                    spreadsheetHandler.cellRangeSelected(selectedRegion.col1,
                            selectedRegion.col2, selectedRegion.row1,
                            selectedRegion.row2);
                } else {
                    spreadsheetHandler.cellsAddedToRangeSelection(
                            selectedRegion.col1, selectedRegion.col2,
                            selectedRegion.row1, selectedRegion.row2);
                }
            }
        } else if (metaOrCtrlKey) {
            // add the selected cell into the selection, set it as the selected
            if (column == sheetWidget.getSelectedCellColumn()
                    && row == sheetWidget.getSelectedCellRow()) {
                // clicked on the selected cell again -> nothing happens
                return;
            }
            // TODO update the selection coherence, if the areas align properly
            if (sheetWidget.isCoherentSelection()) {
                sheetWidget.setCoherentSelection(false);
            }
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.setSelectionRangeOutlineVisible(false);
            }
            sheetWidget.swapCellSelection(column, row);
            newSelectedCellSet();
            // display cell data address
            formulaBarWidget.setSelectedCellAddress(createCellAddress(column,
                    row));
            if (hasSelectedCellChangedOnClick) {
                // do not update selected cell formula value unless needed
                formulaBarWidget.setCellPlainValue("");
            }
            if (updateToActionHandler) {
                spreadsheetHandler.cellAddedToSelectionAndSelected(column, row);
            }
        } else {
            // select cell
            // sheetWidget.clearCellRangeStyles();
            if (!sheetWidget.isCoherentSelection()) {
                sheetWidget.setCoherentSelection(true);
            }
            if (!sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.setSelectionRangeOutlineVisible(true);
                sheetWidget.clearSelectedCellStyle();
            }
            sheetWidget.setSelectedCell(column, row);
            MergedRegion cell = mergedRegionContainer
                    .getMergedRegionStartingFrom(column, row);
            if (cell != null) {
                sheetWidget.updateSelectionOutline(cell.col1, cell.col2,
                        cell.row1, cell.row2);
                sheetWidget.updateSelectedCellStyles(cell.col1, cell.col2,
                        cell.row1, cell.row2, true);
                colBeforeMergedCell = cell.col1;
                rowBeforeMergedCell = cell.row1;
            } else {
                sheetWidget.updateSelectionOutline(column, column, row, row);
                sheetWidget.updateSelectedCellStyles(column, column, row, row,
                        true);
            }
            // display cell data address
            formulaBarWidget.setSelectedCellAddress(createCellAddress(column,
                    row));
            if (updateToActionHandler) {
                newSelectedCellSet();
                if (hasSelectedCellChangedOnClick) {
                    // do not update selected cell formula value unless needed
                    formulaBarWidget.setCellPlainValue("");
                }
                spreadsheetHandler.cellSelected(column, row, true);
            }
        }
    }

    @Override
    public void onRowHeaderClick(int row, boolean shiftPressed,
            boolean metaOrCrtlPressed) {
        int firstColumnIndex = sheetWidget.getLeftVisibleColumnIndex();
        doCommitIfEditing();
        if (!shiftPressed) {
            formulaBarWidget.setSelectedCellAddress(createCellAddress(
                    firstColumnIndex, row));
            formulaBarWidget.setCellPlainValue("");
        }
        if (shiftPressed) {
            // keep selected, add the whole range from old selected as range
            // select all rows from previous to new
            int c1 = 1;
            int c2 = cols;
            final int selectedCellRow = sheetWidget.getSelectedCellRow();
            int r1 = selectedCellRow > row ? row : selectedCellRow;
            int r2 = selectedCellRow > row ? selectedCellRow : row;
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.updateSelectionOutline(c1, c2, r1, r2);
                sheetWidget.updateSelectedCellStyles(c1, c2, r1, r2, true);
            } else {
                sheetWidget.updateSelectedCellStyles(c1, c2, r1, r2, false);
            }
            // TODO update the selection coherence, if the areas align properly
            if (!sheetWidget.isCoherentSelection()) {
            }
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                spreadsheetHandler.cellRangeSelected(c1, c2, r1, r2);
            } else {
                spreadsheetHandler.cellsAddedToRangeSelection(c1, c2, r1, r2);
            }
        } else if (metaOrCrtlPressed) {
            // change selected, add whole row to range
            // TODO update the selection coherence, if the areas align properly
            if (sheetWidget.isCoherentSelection()) {
                sheetWidget.setCoherentSelection(false);
            }
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.setSelectionRangeOutlineVisible(false);
            }
            // set the row first cell as the selected
            sheetWidget.swapCellSelection(firstColumnIndex, row);
            newSelectedCellSet();
            // add the selection styles
            sheetWidget.updateSelectedCellStyles(1, cols, row, row, false);
            spreadsheetHandler.rowAddedToRangeSelection(row, firstColumnIndex);
        } else {
            if (!sheetWidget.isCoherentSelection()) {
                sheetWidget.setCoherentSelection(true);
            }
            if (!sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.setSelectionRangeOutlineVisible(true);
                sheetWidget.clearSelectedCellStyle();
            }

            sheetWidget.setSelectedCell(firstColumnIndex, row);
            sheetWidget.updateSelectionOutline(1, cols, row, row);
            sheetWidget.updateSelectedCellStyles(1, cols, row, row, true);
            newSelectedCellSet();
            spreadsheetHandler.rowSelected(row, firstColumnIndex);
        }
    }

    @Override
    public void onColumnHeaderClick(int column, boolean shiftPressed,
            boolean metaOrCrtlPressed) {
        doCommitIfEditing();
        int firstRowIndex = sheetWidget.getTopVisibleRowIndex();
        if (!shiftPressed) {
            formulaBarWidget.setSelectedCellAddress(createCellAddress(column,
                    firstRowIndex));
            formulaBarWidget.setCellPlainValue("");
        }
        if (shiftPressed) {
            // keep selected, add the whole range from old selected as range
            // select or columns from previous to new
            int selectedCellCol = sheetWidget.getSelectedCellColumn();
            int c1 = selectedCellCol > column ? column : selectedCellCol;
            int c2 = selectedCellCol > column ? selectedCellCol : column;
            int r1 = 1;
            int r2 = rows;
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.updateSelectionOutline(c1, c2, r1, r2);
                sheetWidget.updateSelectedCellStyles(c1, c2, r1, r2, true);
            } else {
                sheetWidget.updateSelectedCellStyles(c1, c2, r1, r2, false);
            }
            // TODO update the selection coherence, if the areas align properly
            // if (!sheetWidget.isCoherentSelection()) {
            // }
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                spreadsheetHandler.cellRangeSelected(c1, c2, r1, r2);
            } else {
                spreadsheetHandler.cellsAddedToRangeSelection(c1, c2, r1, r2);
            }
        } else if (metaOrCrtlPressed) {
            // change selected, add whole row to range
            // TODO update the selection coherence, if the areas align properly
            if (sheetWidget.isCoherentSelection()) {
                sheetWidget.setCoherentSelection(false);
            }
            if (sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.setSelectionRangeOutlineVisible(false);
            }
            // set the row first cell as the selected
            sheetWidget.swapCellSelection(column, firstRowIndex);
            newSelectedCellSet();
            // add the selection styles
            sheetWidget
                    .updateSelectedCellStyles(column, column, 1, rows, false);
            spreadsheetHandler.columnAddedToSelection(column, firstRowIndex);
        } else {
            if (!sheetWidget.isCoherentSelection()) {
                sheetWidget.setCoherentSelection(true);
            }
            if (!sheetWidget.isSelectionRangeOutlineVisible()) {
                sheetWidget.setSelectionRangeOutlineVisible(true);
                sheetWidget.clearSelectedCellStyle();
            }
            sheetWidget.setSelectedCell(column, firstRowIndex);
            sheetWidget.updateSelectionOutline(column, column, 1, rows);
            sheetWidget.updateSelectedCellStyles(column, column, 1, rows, true);
            newSelectedCellSet();
            spreadsheetHandler.columnSelected(column, firstRowIndex);
        }
    }

    @Override
    public void onColumnHeaderResizeDoubleClick(int columnIndex) {
        spreadsheetHandler.onColumnAutofit(columnIndex);
    }

    private void doCommitIfEditing() {
        if (inlineEditing || formulaBarEditing) {
            cancelDeferredCommit = true;
            final String editedValue = formulaBarWidget.getFormulaFieldValue();
            spreadsheetHandler.cellValueEdited(
                    sheetWidget.getSelectedCellColumn(),
                    sheetWidget.getSelectedCellRow(), editedValue);
            cellEditingDone(editedValue);
        } else if (customCellEditorDisplayed) {
            customCellEditorDisplayed = false;
            sheetWidget.removeCustomCellEditor();
            formulaBarWidget.setFormulaFieldEnabled(true);
        }
    }

    @Override
    public void onSelectingCellsWithDrag(int col, int row) {
        if (col == 0) {
            col = 1;
        } else if (col < 0) {
            col = sheetWidget.getRightVisibleColumnIndex() + 1;
        }
        if (col > cols) {
            col = cols;
        }
        if (row == 0) {
            row = 1;
        } else if (row < 0) {
            row = sheetWidget.getBottomVisibleRowIndex() + 1;
        }
        if (row > rows) {
            row = rows;
        }
        int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        int selectedCellRow = sheetWidget.getSelectedCellRow();
        int col1;
        int col2;
        int row1;
        int row2;
        if (col <= selectedCellColumn) {
            col1 = col;
            col2 = selectedCellColumn;
        } else {
            col1 = selectedCellColumn;
            col2 = col;
        }
        if (row <= selectedCellRow) {
            row1 = row;
            row2 = selectedCellRow;
        } else {
            row1 = selectedCellRow;
            row2 = row;
        }
        MergedRegion selectedRegion = MergedRegionUtil.findIncreasingSelection(
                mergedRegionContainer, row1, row2, col1, col2);
        sheetWidget.updateSelectionOutline(selectedRegion.col1,
                selectedRegion.col2, selectedRegion.row1, selectedRegion.row2);
        sheetWidget.updateSelectedCellStyles(selectedRegion.col1,
                selectedRegion.col2, selectedRegion.row1, selectedRegion.row2,
                true);

        formulaBarWidget.setSelectedCellAddress(createRangeSelectionString(
                selectedRegion.col1, selectedRegion.col2, selectedRegion.row1,
                selectedRegion.row2));
    }

    @Override
    public void onFinishedSelectingCellsWithDrag(int col1, int col2, int row1,
            int row2) {
        if (col1 == 0 || col2 == 0 || row1 == 0 || row2 == 0 || col1 == col2
                && row1 == row2 && col1 == sheetWidget.getSelectedCellColumn()
                && row1 == sheetWidget.getSelectedCellRow()) {
            return;
        }
        int temp;
        if (col1 > col2) {
            temp = col1;
            col1 = col2;
            col2 = temp;
        }
        if (row1 > row2) {
            temp = row1;
            row1 = row2;
            row2 = temp;
        }
        MergedRegion selectedRegion = MergedRegionUtil.findIncreasingSelection(
                mergedRegionContainer, row1, row2, col1, col2);
        spreadsheetHandler.cellRangePainted(
                sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow(), selectedRegion.col1,
                selectedRegion.col2, selectedRegion.row1, selectedRegion.row2);
        formulaBarWidget.setSelectedCellAddress(createCellAddress(
                sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow()));
        newSelectedCellSet();
    }

    @Override
    public void onCellDoubleClick(int column, int row, String value) {
        if (sheetWidget.getSelectedCellRow() != row
                && sheetWidget.getSelectedCellColumn() != column) {
            onCellClick(column, row, value, false, false, true);
        } else {
            cachedCellValue = value;
            formulaBarWidget.cacheFormulaFieldValue();
            value = formulaBarWidget.getFormulaFieldValue();
        }
        formulaBarEditing = false;
        checkEditableAndNotify();
        if (!cellLocked) {
            if (!inlineEditing && !customCellEditorDisplayed) {
                inlineEditing = true;
                sheetWidget.startEditingCell(true, true, true, value);
            }
        }
    }

    @Override
    public void onCellInputBlur(final String inputValue) {
        // need to do this deferred in case focus moved to the formula field
        if (inlineEditing) {
            doDeferredCellValueCommit(inputValue);
        }
    }

    /* This is only for when focus is changed from formula field to inline input */
    @Override
    public void onCellInputFocus() {
        if (!inlineEditing) {
            inlineEditing = true;
            cancelDeferredCommit = true;
            if (formulaBarEditing) { // just swap, everything should work
                formulaBarEditing = false;
            } else { // need to make sure the input value is correct
                sheetWidget.startEditingCell(true, true, false,
                        formulaBarWidget.getFormulaFieldValue());
            }
        }
    }

    @Override
    public void onCellInputCancel() {
        cellEditingDone(cachedCellValue);
        formulaBarWidget.revertCellValue();
        sheetWidget.focusSheet();
    }

    @Override
    public void onCellInputEnter(String value, boolean shift) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow(), value);
        cellEditingDone(value);
        sheetWidget.focusSheet();
        if (shift) {
            moveSelectedCellUp(false);
        } else {
            moveSelectedCellDown(false);
        }
    }

    @Override
    public void onCellInputTab(String value, boolean shift) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow(), value);
        cellEditingDone(value);
        sheetWidget.focusSheet();
        if (shift) {
            moveSelectedCellLeft(false);
        } else {
            moveSelectedCellRight(false);
        }
    }

    @Override
    public void onCellInputValueChange(String value) {
        formulaBarWidget.setFormulaFieldValue(value);
    }

    @Override
    public void onSheetKeyPress(NativeEvent event, String enteredCharacter) {
        switch (event.getKeyCode()) {
        case KeyCodes.KEY_DELETE:
            checkEditableAndNotify();
            if (!cellLocked) {
                spreadsheetHandler.deleteSelectedCells();
            }
            break;
        case KeyCodes.KEY_ENTER:
            if (event.getShiftKey()) {
                moveSelectedCellUp(false);
            } else {
                moveSelectedCellDown(false);
            }
            break;
        case KeyCodes.KEY_DOWN:
            if (event.getShiftKey()) {
                increaseVerticalSelection(true);
            } else {
                moveSelectedCellDown(true);
            }
            break;
        case KeyCodes.KEY_LEFT:
            if (event.getShiftKey()) {
                increaseHorizonalSelection(false);
            } else {
                moveSelectedCellLeft(true);
            }
            break;
        case KeyCodes.KEY_TAB:
            if (event.getShiftKey()) {
                moveSelectedCellLeft(false);
            } else {
                moveSelectedCellRight(false);
            }
            break;
        case KeyCodes.KEY_RIGHT:
            if (event.getShiftKey()) {
                increaseHorizonalSelection(true);
            } else {
                moveSelectedCellRight(true);
            }
            break;
        case KeyCodes.KEY_UP:
            if (event.getShiftKey()) {
                increaseVerticalSelection(false);
            } else {
                moveSelectedCellUp(true);
            }
            break;
        case KeyCodes.KEY_ALT:
        case KeyCodes.KEY_CTRL:
        case KeyCodes.KEY_END:
        case KeyCodes.KEY_ESCAPE:
        case KeyCodes.KEY_HOME:
        case KeyCodes.KEY_PAGEDOWN:
        case KeyCodes.KEY_PAGEUP:
        case KeyCodes.KEY_SHIFT:
            break;
        case KeyCodes.KEY_BACKSPACE:
            checkEditableAndNotify();
            if (!cellLocked && !customCellEditorDisplayed) {
                // cache value and start editing cell as empty
                inlineEditing = true;
                cachedCellValue = sheetWidget.getSelectedCellLatestValue();
                sheetWidget.startEditingCell(true, false, false, "");
                formulaBarWidget.cacheFormulaFieldValue();
                formulaBarWidget.setCellPlainValue("");
            }
            break;
        default:

            checkEditableAndNotify();

            if (!sheetWidget.isSelectedCellCustomized() && !inlineEditing
                    && !cellLocked && !customCellEditorDisplayed) {
                // cache value and start editing cell as empty
                inlineEditing = true;
                cachedCellValue = sheetWidget.getSelectedCellLatestValue();
                if (cachedCellValue.endsWith("%")) {
                    enteredCharacter = enteredCharacter + "%";
                    sheetWidget.startEditingCell(true, false, true,
                            enteredCharacter);
                    formulaBarWidget.setCellPlainValue(enteredCharacter);
                } else {
                    sheetWidget.startEditingCell(true, false, true,
                            enteredCharacter);
                    formulaBarWidget.cacheFormulaFieldValue();
                    formulaBarWidget.setCellPlainValue(enteredCharacter);
                }
            }
        }
    }

    /**
     * Checks if selected cell is locked, and sends an RPC to server if it is.
     */
    private void checkEditableAndNotify() {
        if (cellLocked) {

            if (!okToSendCellProtectRpc) {
                // don't send just yet
                return;
            }

            Timer timer = new Timer() {
                @Override
                public void run() {
                    okToSendCellProtectRpc = true;
                }
            };
            timer.schedule(1000);

            okToSendCellProtectRpc = false;

            ServerConnector connector = Util.findConnectorFor(this);
            SpreadsheetServerRpc rpc = RpcProxy.create(
                    SpreadsheetServerRpc.class, connector);

            rpc.protectedCellWriteAttempted();
        }
    }

    private void newSelectedCellSet() {
        if (customCellEditorDisplayed) {
            customCellEditorDisplayed = false;
            sheetWidget.removeCustomCellEditor();
        }

        if (!sheetWidget.isSelectedCellCustomized()
                && !cellLocked
                && customEditorFactory != null
                && customEditorFactory.hasCustomEditor(sheetWidget
                        .getSelectedCellKey())) {
            Widget customEditor = customEditorFactory
                    .getCustomEditor(sheetWidget.getSelectedCellKey());
            if (customEditor != null) {
                customCellEditorDisplayed = true;
                formulaBarWidget.setFormulaFieldEnabled(false);
                sheetWidget.displayCustomCellEditor(customEditor);
            }
        }
    }

    @Override
    public void onAddressEntered(String value) {
        spreadsheetHandler.sheetAddressChanged(value);
    }

    @Override
    public void onAddressFieldEsc() {
        sheetWidget.focusSheet();
    }

    @Override
    public void onSheetTabSelected(int sheetIndex) {
        int scrollLeft = sheetWidget.getSheetScrollLeft();
        int scrollTop = sheetWidget.getSheetScrollTop();
        spreadsheetHandler.sheetSelected(sheetIndex, scrollLeft, scrollTop);
    }

    @Override
    public void onFirstTabIndexChange(int firstVisibleTab) {
        // Disabled because not working in Apache POI
        // actionHandler.firstVisibleTabChanged(firstVisibleTab);
    }

    @Override
    public void onSheetRename(int sheetIndex, String newName) {
        spreadsheetHandler.sheetRenamed(sheetIndex, newName);
    }

    @Override
    public void onNewSheetCreated() {
        int scrollLeft = sheetWidget.getSheetScrollLeft();
        int scrollTop = sheetWidget.getSheetScrollTop();
        spreadsheetHandler.sheetCreated(scrollLeft, scrollTop);
    }

    @Override
    public void onSheetRenameCancel() {
        sheetWidget.focusSheet();
    }

    @Override
    public int[] getRowHeightsPX() {
        return sheetWidget.getRowHeights();
    }

    @Override
    public MergedRegion getMergedRegion(int column, int row) {
        return mergedRegionContainer.getMergedRegion(column, row);
    }

    @Override
    public MergedRegion getMergedRegionStartingFrom(int column, int row) {
        return mergedRegionContainer.getMergedRegionStartingFrom(column, row);
    }

    @Override
    public void onSelectionIncreasePainted(int c1, int c2, int r1, int r2) {
        MergedRegion evenedRegion = MergedRegionUtil.findIncreasingSelection(
                mergedRegionContainer, r1, r2, c1, c2);
        // discard painted area if merged cells don't align
        if (evenedRegion.col1 == c1 && evenedRegion.col2 == c2
                && evenedRegion.row1 == r1 && evenedRegion.row2 == r2) {
            spreadsheetHandler.selectionIncreasePainted(c1, c2, r1, r2);
        }
    }

    @Override
    public void onSelectionDecreasePainted(int col1, int col2,
            int colEdgeIndex, int row1, int row2, int rowEdgeIndex) {
        // the selection widget has made sure the decreasing area is not in
        // middle of merged cells.
        spreadsheetHandler.selectionDecreasePainted(colEdgeIndex, rowEdgeIndex);
    }

    @Override
    public MergedRegionContainer getMergedRegionContainer() {
        return mergedRegionContainer;
    }

    @Override
    public void onFormulaFieldFocus(String value) {
        formulaBarEditing = true;
        cancelDeferredCommit = true;
        if (inlineEditing) { // just swap and everything should work
            inlineEditing = false;
        } else {
            if (sheetWidget.isSelectedCellCustomized()) {
                cachedCellValue = "";
            } else {
                cachedCellValue = sheetWidget.getSelectedCellLatestValue();
                sheetWidget.startEditingCell(false, false, true, value);
            }
        }
    }

    @Override
    public void onFormulaFieldBlur(final String value) {
        // need to do this as deferred because in case the focus was passed to
        // inline input element
        if (formulaBarEditing) {
            doDeferredCellValueCommit(value);
        }
    }

    @Override
    public void onFormulaEnter(String value) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow(), value);
        cellEditingDone(value);
        sheetWidget.focusSheet();
        moveSelectedCellDown(false);
    }

    @Override
    public void onFormulaTab(String value) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow(), value);
        cellEditingDone(value);
        sheetWidget.focusSheet();
        moveSelectedCellRight(false);
    }

    @Override
    public void onFormulaEsc() {
        cellEditingDone(cachedCellValue);
        sheetWidget.focusSheet();
    }

    @Override
    public void onFormulaValueChange(String value) {
        if (!sheetWidget.isSelectedCellCustomized()) {
            sheetWidget.updateInputValue(value);
        }
    }

    @Override
    public void onRowsResized(Map<Integer, Float> newSizes) {
        for (Entry<Integer, Float> entry : newSizes.entrySet()) {
            int index = entry.getKey();
            float size = entry.getValue();
            if (size == 0.0F) {
                if (hiddenRowIndexes == null) {
                    hiddenRowIndexes = new ArrayList<Integer>();
                    hiddenRowIndexes.add(index);
                } else if (!hiddenRowIndexes.contains(index)) {
                    hiddenRowIndexes.add(index);
                }
            }
            rowH[index - 1] = size;
        }
        sheetWidget.relayoutSheet(false);
        if (mergedRegions != null) {
            for (MergedRegion region : mergedRegions) {
                sheetWidget.updateMergedRegionSize(region);
            }
        }
        cancelNextSheetRelayout = true;
        int[] x = sheetWidget.getSheetDisplayRange();
        spreadsheetHandler.rowsResized(newSizes, x[0], x[1], x[2], x[3]);
    }

    @Override
    public void onColumnsResized(Map<Integer, Integer> newSizes) {
        for (Entry<Integer, Integer> entry : newSizes.entrySet()) {
            int index = entry.getKey();
            int size = entry.getValue();
            if (size == 0F) {
                if (hiddenColumnIndexes == null) {
                    hiddenColumnIndexes = new ArrayList<Integer>();
                    hiddenColumnIndexes.add(index);
                } else if (!hiddenColumnIndexes.contains(index)) {
                    hiddenColumnIndexes.add(index);
                }
            }
            colW[index - 1] = size;
        }
        sheetWidget.relayoutSheet(false);
        if (mergedRegions != null) {
            for (MergedRegion region : mergedRegions) {
                sheetWidget.updateMergedRegionSize(region);
            }
        }
        cancelNextSheetRelayout = true;
        int[] x = sheetWidget.getSheetDisplayRange();
        spreadsheetHandler.columnResized(newSizes, x[0], x[1], x[2], x[3]);
    }

    @Override
    public void onRedoPress() {
        spreadsheetHandler.onRedo();
    }

    @Override
    public void onUndoPress() {
        spreadsheetHandler.onUndo();
    }

    /** update the sheet display after editing has finished */
    private void cellEditingDone(String value) {
        inlineEditing = false;
        formulaBarEditing = false;
        if (!sheetWidget.isSelectedCellCustomized()) {
            if (value == null) {
                value = "";
            }
            selectedCellIsFormulaType = value.startsWith("=");
            sheetWidget.stopEditingCell();
            if (!selectedCellIsFormulaType) {
                // this could be removed because the formatted value is always
                // returned after the server side round trip
                sheetWidget.updateSelectedCellValue(value);
            }
        }
    }

    /**
     * 
     * @param value
     */
    private void doDeferredCellValueCommit(final String value) {
        cancelDeferredCommit = false;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (!cancelDeferredCommit) {
                    spreadsheetHandler.cellValueEdited(
                            sheetWidget.getSelectedCellColumn(),
                            sheetWidget.getSelectedCellRow(), value);
                    cellEditingDone(value);
                }
            }
        });
    }

    private void moveSelectedCellDown(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = getMergedRegion(col, row);
        if (oldRegion != null && colBeforeMergedCell != 0) {
            col = colBeforeMergedCell;
            row = oldRegion.row2;
        }
        row++;

        while (hiddenRowIndexes != null && hiddenRowIndexes.contains(row)
                && row < rows) {
            row++;
        }

        if (!discardSelection
                && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1 || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (row > bottomRow) {
                // move highest and right
                row = topRow;
                // if the row on top is hidden, skip it
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row) && row < bottomRow) {
                    row++;
                }
                col++;
                // if the column on right is hidden, skip it
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(col) && col <= rightCol) {
                    col++;
                }
                if (col > rightCol) {
                    // move to left
                    col = leftCol;
                }
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(col) && col <= rightCol) {
                    col++;
                }
            }
            // if the new selected cell is a merged cell
            MergedRegion region = getMergedRegion(col, row);
            if (region != null) {
                colBeforeMergedCell = col;
                rowBeforeMergedCell = row;
                col = region.col1;
                row = region.row1;
            } else {
                colBeforeMergedCell = 0;
                rowBeforeMergedCell = 0;
            }

            sheetWidget.swapSelectedCellInsideSelection(col, row);
            sheetWidget.scrollCellIntoView(col, row);
            formulaBarWidget
                    .setSelectedCellAddress(createCellAddress(col, row));
            formulaBarWidget.setCellPlainValue("");
            newSelectedCellSet();
            spreadsheetHandler.cellSelected(col, row, false);
        } else {
            if (row <= rows) {
                // if the new selected cell is a merged cell
                MergedRegion region = getMergedRegion(col, row);
                if (region != null) {
                    colBeforeMergedCell = col;
                    rowBeforeMergedCell = row;
                    col = region.col1;
                    row = region.row1;
                } else {
                    colBeforeMergedCell = 0;
                    rowBeforeMergedCell = 0;
                }
                sheetWidget.scrollCellIntoView(col, row);
                onCellSelectedWithKeyboard(col, row,
                        sheetWidget.getCellValue(col, row), region);
            }
        }

    }

    private void moveSelectedCellRight(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = getMergedRegion(col, row);
        if (oldRegion != null && rowBeforeMergedCell != 0) {
            col = oldRegion.col2;
            row = rowBeforeMergedCell;
        }
        col++;

        while (hiddenColumnIndexes != null && hiddenColumnIndexes.contains(col)
                && col < cols) {
            col++;
        }
        if (!discardSelection
                && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1 || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (col > rightCol) {
                // move to leftmost and down
                col = leftCol;
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(new Integer(col))
                        && col <= rightCol) {
                    col++;
                }
                row++;
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row) && row <= bottomRow) {
                    row++;
                }
                if (row > bottomRow) {
                    // move to top
                    row = topRow;
                }
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row) && row <= bottomRow) {
                    row++;
                }
            }
            // if the new selected cell is a merged cell
            MergedRegion region = getMergedRegion(col, row);
            if (region != null) {
                colBeforeMergedCell = col;
                rowBeforeMergedCell = row;
                col = region.col1;
                row = region.row1;
            } else {
                colBeforeMergedCell = 0;
                rowBeforeMergedCell = 0;
            }

            sheetWidget.swapSelectedCellInsideSelection(col, row);
            sheetWidget.scrollCellIntoView(col, row);
            formulaBarWidget
                    .setSelectedCellAddress(createCellAddress(col, row));
            formulaBarWidget.setCellPlainValue("");
            newSelectedCellSet();
            spreadsheetHandler.cellSelected(col, row, false);
        } else {
            if (col <= cols) {
                // if the new selected cell is a merged cell
                MergedRegion region = getMergedRegion(col, row);
                if (region != null) {
                    colBeforeMergedCell = col;
                    rowBeforeMergedCell = row;
                    col = region.col1;
                    row = region.row1;
                } else {
                    colBeforeMergedCell = 0;
                    rowBeforeMergedCell = 0;
                }
                sheetWidget.scrollCellIntoView(col, row);
                onCellSelectedWithKeyboard(col, row,
                        sheetWidget.getCellValue(col, row), region);
            }
        }
    }

    private void moveSelectedCellUp(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = getMergedRegion(col, row);
        if (oldRegion != null && colBeforeMergedCell != 0) {
            col = colBeforeMergedCell;
            row = oldRegion.row1;
        }
        row--;

        while (hiddenRowIndexes != null && hiddenRowIndexes.contains(row)
                && row > 1) {
            row--;
        }
        if (!discardSelection
                && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1 || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (row < topRow) {
                // go to bottom and left
                row = bottomRow;
                // if row on bottom is hidden, skip it
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row) && row > topRow) {
                    row--;
                }
                col--;
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(col) && col >= leftCol) {
                    col--;
                }
                if (col < leftCol) {
                    // go to right most
                    col = rightCol;
                }
                while (hiddenColumnIndexes.contains(col) && col >= leftCol) {
                    col--;
                }
            }
            // if the new selected cell is a merged cell
            MergedRegion region = getMergedRegion(col, row);
            if (region != null) {
                colBeforeMergedCell = col;
                rowBeforeMergedCell = row;
                col = region.col1;
                row = region.row1;
            } else {
                colBeforeMergedCell = 0;
                rowBeforeMergedCell = 0;
            }
            sheetWidget.swapSelectedCellInsideSelection(col, row);
            sheetWidget.scrollCellIntoView(col, row);
            formulaBarWidget
                    .setSelectedCellAddress(createCellAddress(col, row));
            formulaBarWidget.setCellPlainValue("");
            newSelectedCellSet();
            spreadsheetHandler.cellSelected(col, row, false);
        } else {
            if (row > 0) {
                // if the new selected cell is a merged cell
                MergedRegion region = getMergedRegion(col, row);
                if (region != null) {
                    colBeforeMergedCell = col;
                    rowBeforeMergedCell = row;
                    col = region.col1;
                    row = region.row1;
                } else {
                    colBeforeMergedCell = 0;
                    rowBeforeMergedCell = 0;
                }
                sheetWidget.scrollCellIntoView(col, row);
                onCellSelectedWithKeyboard(col, row,
                        sheetWidget.getCellValue(col, row), region);
            }
        }
    }

    private void moveSelectedCellLeft(boolean discardSelection) {
        final int leftCol = sheetWidget.getSelectionLeftCol();
        final int rightCol = sheetWidget.getSelectionRightCol();
        final int topRow = sheetWidget.getSelectionTopRow();
        final int bottomRow = sheetWidget.getSelectionBottomRow();
        int col = sheetWidget.getSelectedCellColumn();
        int row = sheetWidget.getSelectedCellRow();
        // if the old selected cell was a merged cell, it changes the actual
        // selected cell
        MergedRegion oldRegion = getMergedRegion(col, row);
        if (oldRegion != null && rowBeforeMergedCell != 0) {
            col = oldRegion.col1;
            row = rowBeforeMergedCell;
        }

        col--;
        while (hiddenColumnIndexes != null && hiddenColumnIndexes.contains(col)
                && col > 0) {
            col--;
        }
        if (!discardSelection
                && (leftCol != rightCol || topRow != bottomRow)
                && (oldRegion == null || leftCol != oldRegion.col1
                        || rightCol != oldRegion.col2
                        || topRow != oldRegion.row1 || bottomRow != oldRegion.row2)) {
            // move the selected cell inside the selection
            if (col < leftCol) {
                // move to right most and up
                col = rightCol;
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(col) && col >= leftCol) {
                    col--;
                }
                row--;
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row) && row >= topRow) {
                    row--;
                }
                if (row < topRow) {
                    // go to bottom
                    row = bottomRow;
                }
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row) && row >= topRow) {
                    row--;
                }
            }
            // if the new selected cell is a merged cell
            MergedRegion region = getMergedRegion(col, row);
            if (region != null) {
                colBeforeMergedCell = col;
                rowBeforeMergedCell = row;
                col = region.col1;
                row = region.row1;
            } else {
                colBeforeMergedCell = 0;
                rowBeforeMergedCell = 0;
            }
            sheetWidget.swapSelectedCellInsideSelection(col, row);
            sheetWidget.scrollCellIntoView(col, row);
            formulaBarWidget
                    .setSelectedCellAddress(createCellAddress(col, row));
            formulaBarWidget.setCellPlainValue("");
            newSelectedCellSet();
            spreadsheetHandler.cellSelected(col, row, false);
        } else {
            if (col > 0) {
                // if the new selected cell is a merged cell
                MergedRegion region = getMergedRegion(col, row);
                if (region != null) {
                    colBeforeMergedCell = col;
                    rowBeforeMergedCell = row;
                    col = region.col1;
                    row = region.row1;
                } else {
                    colBeforeMergedCell = 0;
                    rowBeforeMergedCell = 0;
                }
                sheetWidget.scrollCellIntoView(col, row);
                onCellSelectedWithKeyboard(col, row,
                        sheetWidget.getCellValue(col, row), region);
            }
        }
    }

    /**
     * 
     * @param column
     * @param row
     * @param value
     * @param region
     *            null if selected cell is not a merged cell
     */
    private void onCellSelectedWithKeyboard(int column, int row, String value,
            MergedRegion region) {
        doCommitIfEditing();
        if (!sheetWidget.isCoherentSelection()) {
            sheetWidget.setCoherentSelection(true);
        }
        if (!sheetWidget.isSelectionRangeOutlineVisible()) {
            sheetWidget.setSelectionRangeOutlineVisible(true);
            sheetWidget.clearSelectedCellStyle();
        }
        sheetWidget.setSelectedCell(column, row);
        sheetWidget.updateSelectionOutline(column, column, row, row);
        if (region != null) {
            sheetWidget.updateSelectedCellStyles(column, region.col2, row,
                    region.row2, true);
        } else {
            sheetWidget
                    .updateSelectedCellStyles(column, column, row, row, true);
        }
        // display cell data address
        formulaBarWidget.setSelectedCellAddress(createCellAddress(column, row));
        newSelectedCellSet();
        formulaBarWidget.setCellPlainValue("");
        spreadsheetHandler.cellSelected(column, row, true);
    }

    private void increaseHorizonalSelection(boolean right) {
        int topRow = sheetWidget.getSelectionTopRow();
        int leftCol = sheetWidget.getSelectionLeftCol();
        final int oldLeftCol = leftCol;
        int rightCol = sheetWidget.getSelectionRightCol();
        final int oldRightCol = rightCol;
        int bottomRow = sheetWidget.getSelectionBottomRow();
        int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        final int selectedCellRow = sheetWidget.getSelectedCellRow();
        MergedRegion region = mergedRegionContainer
                .getMergedRegionStartingFrom(selectedCellColumn,
                        selectedCellRow);

        if (sheetWidget.isCoherentSelection()) {
            // the selection outline is the "correct", even with merged cells,
            // as with a merged cell the selected cell doesn't take the merged
            // edge into account.
            if (region != null
                    && (right && region.col1 != leftCol || !right
                            && region.col2 == rightCol)) {
                selectedCellColumn = region.col2;
            }
            MergedRegion selection = null;
            if (selectedCellColumn == leftCol) {
                if (right && rightCol + 1 <= cols) { // increase to right
                    rightCol++;
                    while (hiddenColumnIndexes != null
                            && hiddenColumnIndexes.contains(rightCol)
                            && rightCol < cols) {
                        rightCol++;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            mergedRegionContainer, topRow, bottomRow, leftCol,
                            rightCol);
                } else if (!right) {
                    if (rightCol != leftCol) { // decrease from right
                        rightCol--;
                        while (hiddenColumnIndexes != null
                                && hiddenColumnIndexes.contains(rightCol)
                                && (rightCol) > leftCol) {
                            rightCol--;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (leftCol - 1 > 0) { // increase to left
                        leftCol--;
                        while (hiddenColumnIndexes != null
                                && hiddenColumnIndexes.contains(leftCol)
                                && leftCol > 1) {
                            leftCol--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                }
            } else if (selectedCellColumn == rightCol) {
                if (right) {
                    if (rightCol != leftCol) { // decrease from left
                        leftCol++;
                        while (hiddenColumnIndexes != null
                                && hiddenColumnIndexes.contains(leftCol)
                                && leftCol < rightCol) {
                            leftCol++;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (rightCol + 1 <= cols) { // increase to right
                        rightCol++;
                        while (hiddenColumnIndexes != null
                                && hiddenColumnIndexes.contains(rightCol)
                                && rightCol < cols) {
                            rightCol++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                } else if (!right && leftCol - 1 > 0) { // increase to left
                    leftCol--;
                    while (hiddenColumnIndexes != null
                            && hiddenColumnIndexes.contains(leftCol)
                            && leftCol > 1) {
                        leftCol--;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            mergedRegionContainer, topRow, bottomRow, leftCol,
                            rightCol);
                }
            } else {
                if (right) { // increase to right
                    if (rightCol + 1 <= cols) {
                        rightCol++;
                        while (hiddenColumnIndexes != null
                                && hiddenColumnIndexes.contains(rightCol)
                                && rightCol < cols) {
                            rightCol++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                } else { // increase to left
                    if (leftCol - 1 > 0) {
                        leftCol--;
                        while (hiddenColumnIndexes != null
                                && hiddenColumnIndexes.contains(leftCol)
                                && leftCol > 1) {
                            leftCol--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                }
            }
            if (selection == null) {
                return;
            }
            sheetWidget.updateSelectionOutline(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceAsSelectedCells(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceHeadersAsSelected(selection.row1,
                    selection.row2, selection.col1, selection.col2);
            sheetWidget.scrollAreaIntoView(selection.col1, selection.col2,
                    selection.row1, selection.row2);
        } else { // previous selection not coherent
            // discard the old selection and start from previously selected cell
            int row2;
            int col2;
            if (region != null) {
                row2 = region.row2;
                col2 = region.col2;
            } else {
                row2 = selectedCellRow;
                col2 = selectedCellColumn;
            }
            if (right) {
                col2++;
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(col2) && col2 < cols) {
                    col2++;
                }
            } else {
                selectedCellColumn--;
                while (hiddenColumnIndexes != null
                        && hiddenColumnIndexes.contains(selectedCellColumn)
                        && selectedCellColumn > 1) {
                    selectedCellColumn--;
                }
            }
            if (selectedCellColumn > 0 && col2 < cols) {
                MergedRegion selection = MergedRegionUtil
                        .findIncreasingSelection(mergedRegionContainer,
                                selectedCellRow, row2, selectedCellColumn, col2);
                if (selection != null) {
                    // sheetWidget.clearCellRangeStyles();
                    sheetWidget.setCoherentSelection(true);
                    sheetWidget.setSelectionRangeOutlineVisible(true);
                    sheetWidget.clearSelectedCellStyle();
                    sheetWidget.updateSelectionOutline(selection.col1,
                            selection.col2, selection.row1, selection.row2);
                    sheetWidget.updateSelectedCellStyles(selection.col1,
                            selection.col2, selection.row1, selection.row2,
                            true);
                }
            }
            // scroll area into view
            sheetWidget.scrollSelectionAreaIntoView();
        }

        // update action handler
        if (oldLeftCol != sheetWidget.getSelectionLeftCol()
                || oldRightCol != sheetWidget.getSelectionRightCol()
                || topRow != sheetWidget.getSelectionTopRow()
                || bottomRow != sheetWidget.getSelectionBottomRow()) {
            spreadsheetHandler.cellRangeSelected(
                    sheetWidget.getSelectionLeftCol(),
                    sheetWidget.getSelectionRightCol(),
                    sheetWidget.getSelectionTopRow(),
                    sheetWidget.getSelectionBottomRow());
        }
    }

    /**
     * Goes through the given selection and checks that the cells on the edges
     * of the selection are not in "the beginning / middle / end" of a merged
     * cell. Returns the correct decreased selection, after taking the merged
     * cells into account.
     * 
     * Parameters 1-based.
     * 
     * @param topRow
     * @param bottomRow
     * @param leftColumn
     * @param rightColumn
     * @return
     */
    private MergedRegion findDecreasingSelection(int topRow, int bottomRow,
            int leftColumn, int rightColumn) {
        if (topRow == bottomRow && leftColumn == rightColumn) {
            MergedRegion mergedRegion = getMergedRegion(leftColumn, topRow);
            if (mergedRegion == null) {
                mergedRegion = new MergedRegion();
                mergedRegion.col1 = leftColumn;
                mergedRegion.col2 = rightColumn;
                mergedRegion.row1 = topRow;
                mergedRegion.row2 = bottomRow;
            }
            return mergedRegion;
        } else {
            MergedRegion merged = getMergedRegionStartingFrom(leftColumn,
                    topRow);
            if (merged != null && merged.col2 >= rightColumn
                    && merged.row2 >= bottomRow) {
                return merged;
            }
        }
        int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        int selectedCellRow = sheetWidget.getSelectedCellRow();

        if (selectedCellColumn < leftColumn || selectedCellColumn > rightColumn
                || selectedCellRow < topRow || selectedCellRow > bottomRow) {
            return getMergedRegion(selectedCellColumn,
                    sheetWidget.getSelectedCellRow());
        }

        boolean trouble = false;
        int i = leftColumn;
        // go through top edge
        while (i <= rightColumn) {
            MergedRegion region = getMergedRegion(i, topRow);
            if (region != null) {
                i = region.col2 + 1;
                if (topRow > region.row1) {
                    // check if the cell in top row is in middle or end of a
                    // merged cell -> decrease more if it is
                    trouble = true;
                    if (topRow < bottomRow) {
                        if (region.row2 > bottomRow) {
                            topRow = region.row2 + 1;
                        } else {
                            topRow = bottomRow;
                        }
                        i = leftColumn;
                    } else {
                        if (selectedCellColumn < region.col1) {
                            rightColumn = region.col1 - 1;
                        } else if (selectedCellColumn > region.col2) {
                            leftColumn = region.col2 + 1;
                        } else {
                            leftColumn = region.col1;
                            rightColumn = region.col2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (topRow > bottomRow) {
            topRow = bottomRow;
        }
        // go through right edge
        i = topRow;
        while (i <= bottomRow) {
            MergedRegion region = getMergedRegion(rightColumn, i);
            if (region != null) {
                i = region.row2 + 1;
                if (rightColumn < region.col2) {
                    trouble = true;
                    if (rightColumn > leftColumn) {
                        if (region.col1 > leftColumn) {
                            rightColumn = region.col1 - 1;
                        } else {
                            rightColumn = leftColumn;
                        }
                        i = topRow;
                    } else {
                        if (selectedCellRow < region.row1) {
                            bottomRow = region.row1 - 1;
                        } else if (selectedCellRow > region.row2) {
                            topRow = region.row2 + 1;
                        } else { // selected cell row is inside the region
                            topRow = region.row1;
                            bottomRow = region.row2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (rightColumn < leftColumn) {
            rightColumn = leftColumn;
        }
        // go through bottom edge
        i = leftColumn;
        while (i <= rightColumn) {
            MergedRegion region = getMergedRegion(i, bottomRow);
            if (region != null) {
                i = region.col2 + 1;
                if (bottomRow < region.row2) {
                    trouble = true;
                    if (bottomRow > topRow) {
                        if (topRow < region.row1) {
                            bottomRow = region.row1 - 1;
                        } else {
                            bottomRow = topRow;
                        }
                        i = leftColumn;
                    } else {
                        if (selectedCellColumn < region.col1) {
                            rightColumn = region.col1 - 1;
                        } else if (selectedCellColumn > region.col2) {
                            leftColumn = region.col2 + 1;
                        } else {
                            rightColumn = region.col1;
                            leftColumn = region.col2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (bottomRow < topRow) {
            bottomRow = topRow;
        }
        // go through left edge
        i = topRow;
        while (i <= bottomRow) {
            MergedRegion region = getMergedRegion(leftColumn, i);
            if (region != null) {
                i = region.row2 + 1;
                if (leftColumn > region.col1) {
                    trouble = true;
                    if (leftColumn < rightColumn) {
                        if (rightColumn > region.col2) {
                            leftColumn = region.col2 + 1;
                        } else {
                            leftColumn = rightColumn;
                        }
                        i = topRow;
                    } else {
                        if (selectedCellRow < region.row1) {
                            bottomRow = region.row1 - 1;
                        } else if (selectedCellRow > region.row2) {
                            topRow = region.row2 + 1;
                        } else {
                            topRow = region.row1;
                            bottomRow = region.row2;
                            break;
                        }
                    }
                }
            } else {
                i++;
            }
        }
        if (leftColumn > rightColumn) {
            leftColumn = rightColumn;
        }
        if (trouble) {
            return findDecreasingSelection(topRow, bottomRow, leftColumn,
                    rightColumn);
        } else if (topRow == bottomRow && leftColumn == rightColumn) {
            MergedRegion mergedRegion = getMergedRegion(leftColumn, topRow);
            if (mergedRegion == null) {
                mergedRegion = new MergedRegion();
                mergedRegion.col1 = leftColumn;
                mergedRegion.col2 = rightColumn;
                mergedRegion.row1 = topRow;
                mergedRegion.row2 = bottomRow;
            }
            return mergedRegion;
        } else {
            MergedRegion merged = getMergedRegionStartingFrom(leftColumn,
                    topRow);
            if (merged != null && merged.col2 >= rightColumn
                    && merged.row2 >= bottomRow) {
                return merged;
            }
        }
        MergedRegion result = new MergedRegion();
        result.col1 = leftColumn;
        result.col2 = rightColumn;
        result.row1 = topRow;
        result.row2 = bottomRow;
        return result;
    }

    private void increaseVerticalSelection(boolean down) {
        int topRow = sheetWidget.getSelectionTopRow();
        int oldTopRow = topRow;
        final int leftCol = sheetWidget.getSelectionLeftCol();
        int bottomRow = sheetWidget.getSelectionBottomRow();
        int oldBottomRow = bottomRow;
        final int rightCol = sheetWidget.getSelectionRightCol();
        int selectedCellRow = sheetWidget.getSelectedCellRow();
        final int selectedCellColumn = sheetWidget.getSelectedCellColumn();
        MergedRegion region = getMergedRegionStartingFrom(selectedCellColumn,
                selectedCellRow);

        if (sheetWidget.isCoherentSelection()) {
            if (region != null
                    && (down && region.row1 != topRow || !down
                            && region.row2 == bottomRow)) {
                selectedCellRow = region.row2;
            }
            MergedRegion selection = null;
            if (selectedCellRow == topRow) {
                if (down && bottomRow + 1 <= rows) { // increase selection down
                    bottomRow++;
                    while (hiddenRowIndexes != null
                            && hiddenRowIndexes.contains(bottomRow)
                            && bottomRow < rows) {
                        bottomRow++;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            mergedRegionContainer, topRow, bottomRow, leftCol,
                            rightCol);
                } else if (!down) {
                    if (topRow != bottomRow) { // decrease selection from bottom
                        bottomRow--;
                        while (hiddenRowIndexes != null
                                && hiddenRowIndexes.contains(bottomRow)
                                && bottomRow > topRow) {
                            bottomRow--;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (topRow - 1 > 0) { // increase selection up
                        topRow--;
                        while (hiddenRowIndexes != null
                                && hiddenRowIndexes.contains(topRow)
                                && topRow > 1) {
                            topRow--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                }
            } else if (selectedCellRow == bottomRow) {
                if (down) {
                    if (topRow != bottomRow) { // decrease from top
                        topRow++;
                        while (hiddenRowIndexes != null
                                && hiddenRowIndexes.contains(topRow)
                                && topRow < bottomRow) {
                            topRow++;
                        }
                        selection = findDecreasingSelection(topRow, bottomRow,
                                leftCol, rightCol);
                    } else if (bottomRow + 1 <= rows) { // increase selection
                        // down
                        bottomRow++;
                        while (hiddenRowIndexes != null
                                && hiddenRowIndexes.contains(bottomRow)
                                && bottomRow < rows) {
                            bottomRow++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                } else if (!down && topRow - 1 > 0) { // increase selection up
                    topRow--;
                    while (hiddenRowIndexes != null
                            && hiddenRowIndexes.contains(topRow) && topRow > 1) {
                        topRow--;
                    }
                    selection = MergedRegionUtil.findIncreasingSelection(
                            mergedRegionContainer, topRow, bottomRow, leftCol,
                            rightCol);
                }
            } else {
                // increase the selection on the desired direction
                if (down) {
                    if (bottomRow + 1 <= rows) {
                        bottomRow++;
                        while (hiddenRowIndexes != null
                                && hiddenRowIndexes.contains(bottomRow)
                                && bottomRow < rows) {
                            bottomRow++;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                } else {
                    if (topRow - 1 > 0) {
                        topRow--;
                        while (hiddenRowIndexes != null
                                && hiddenRowIndexes.contains(topRow)
                                && topRow > 1) {
                            topRow--;
                        }
                        selection = MergedRegionUtil.findIncreasingSelection(
                                mergedRegionContainer, topRow, bottomRow,
                                leftCol, rightCol);
                    }
                }
            }
            if (selection == null) {
                return;
            }
            sheetWidget.updateSelectionOutline(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceAsSelectedCells(selection.col1, selection.col2,
                    selection.row1, selection.row2);
            sheetWidget.replaceHeadersAsSelected(selection.row1,
                    selection.row2, selection.col1, selection.col2);
            sheetWidget.scrollAreaIntoView(selection.col1, selection.col2,
                    selection.row1, selection.row2);
        } else { // previous selection not coherent
            // discard the old selection and start from previously selected cell
            int row2;
            int col2;
            if (region != null) {
                row2 = region.row2;
                col2 = region.col2;
            } else {
                row2 = selectedCellRow;
                col2 = selectedCellColumn;
            }
            if (down) {
                row2++;
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(row2) && row2 < rows) {
                    row2++;
                }
            } else {
                selectedCellRow--;
                while (hiddenRowIndexes != null
                        && hiddenRowIndexes.contains(selectedCellRow)
                        && selectedCellRow > 1) {
                    selectedCellRow--;
                }
            }
            if (selectedCellRow > 0 && row2 <= rows) {
                MergedRegion selection = MergedRegionUtil
                        .findIncreasingSelection(mergedRegionContainer,
                                selectedCellRow, row2, selectedCellColumn, col2);
                if (selection != null) {
                    // sheetWidget.clearCellRangeStyles();
                    sheetWidget.setCoherentSelection(true);
                    sheetWidget.setSelectionRangeOutlineVisible(true);
                    sheetWidget.clearSelectedCellStyle();
                    sheetWidget.updateSelectionOutline(selection.col1,
                            selection.col2, selection.row1, selection.row2);
                    sheetWidget.updateSelectedCellStyles(selection.col1,
                            selection.col2, selection.row1, selection.row2,
                            true);
                }
            }
            // scroll area into view
            sheetWidget.scrollSelectionAreaIntoView();
        }
        // update action handler
        if (leftCol != sheetWidget.getSelectionLeftCol()
                || rightCol != sheetWidget.getSelectionRightCol()
                || oldTopRow != sheetWidget.getSelectionTopRow()
                || oldBottomRow != sheetWidget.getSelectionBottomRow()) {
            spreadsheetHandler.cellRangeSelected(
                    sheetWidget.getSelectionLeftCol(),
                    sheetWidget.getSelectionRightCol(),
                    sheetWidget.getSelectionTopRow(),
                    sheetWidget.getSelectionBottomRow());
        }
    }

    protected String createRangeSelectionString(int col1, int col2, int row1,
            int row2) {
        final StringBuffer sb = new StringBuffer();
        sb.append(Math.abs(row2 - row1) + 1);
        sb.append("R");
        sb.append(" x ");
        sb.append(Math.abs(col2 - col1) + 1);
        sb.append("C");
        return sb.toString();
    }

    protected String createCellAddress(int column, int row) {
        final String c = column > 0 ? getColHeader(column) : "";
        final String r = row > 0 ? Integer.toString(row) : "";
        return c + r;
    }

    public void setRowBufferSize(int rowBufferSize) {
        this.rowBufferSize = rowBufferSize;
    }

    public void setColumnBufferSize(int columnBufferSize) {
        this.columnBufferSize = columnBufferSize;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setRowH(float[] rowH) {
        this.rowH = rowH;
    }

    public void setColW(int[] colW) {
        this.colW = colW;
    }

    public void setDefRowH(float defRowH) {
        this.defRowH = defRowH;
    }

    public void setDefColW(int defColW) {
        this.defColW = defColW;
    }

    public void setVerticalScrollPositions(int[] verticalScrollPositions) {
        this.verticalScrollPositions = verticalScrollPositions;
    }

    public void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        this.horizontalScrollPositions = horizontalScrollPositions;
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        sheetWidget.setVerticalSplitPosition(verticalSplitPosition);
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        sheetWidget.setHorizontalSplitPosition(horizontalSplitPosition);
    }

    public void setCellStyleToCSSStyle(
            HashMap<Integer, String> cellStyleToCSSStyle) {
        if (this.cellStyleToCSSStyle == null) {
            this.cellStyleToCSSStyle = cellStyleToCSSStyle;
        } else {
            this.cellStyleToCSSStyle.clear();
            if (cellStyleToCSSStyle != null) {
                this.cellStyleToCSSStyle.putAll(cellStyleToCSSStyle);
            }
        }
    }

    public void setShiftedCellBorderStyles(
            ArrayList<String> shiftedCellBorderStyles) {
        sheetWidget.removeShiftedCellBorderStyles();
        if (shiftedCellBorderStyles != null) {
            sheetWidget.addShiftedCellBorderStyles(shiftedCellBorderStyles);
        }
    }

    public void setHyperlinksTooltips(HashMap<String, String> cellLinksMap) {
        sheetWidget.setCellLinks(cellLinksMap);
    }

    public void setSheetProtected(boolean sheetProtected) {
        if (this.sheetProtected != sheetProtected) {
            this.sheetProtected = sheetProtected;
            if (loaded) {
                if (sheetProtected) {
                    if (customCellEditorDisplayed) {
                        customCellEditorDisplayed = false;
                        sheetWidget.removeCustomCellEditor();
                    }
                } else { // might need to load the custom editor
                    cellLocked = false;
                    newSelectedCellSet();
                    if (customCellEditorDisplayed) {
                        // need to update the editor value on client side
                        spreadsheetHandler.cellSelected(
                                sheetWidget.getSelectedCellColumn(),
                                sheetWidget.getSelectedCellRow(), false);
                    }
                }
            }
        }
    }

    public void setWorkbookProtected(boolean workbookProtected) {
        sheetTabSheet.setReadOnly(workbookProtected);
    }

    public void setHiddenColumnIndexes(ArrayList<Integer> hiddenColumnIndexes) {
        this.hiddenColumnIndexes = new ArrayList<Integer>(hiddenColumnIndexes);
    }

    public void setHiddenRowIndexes(ArrayList<Integer> hiddenRowIndexes) {
        this.hiddenRowIndexes = new ArrayList<Integer>(hiddenRowIndexes);
    }

    public void setCellComments(HashMap<String, String> cellComments) {
        sheetWidget.setCellComments(cellComments);
    }

    @Override
    public Map<Integer, String> getCellStyleToCSSStyle() {
        return cellStyleToCSSStyle;
    }

    @Override
    public float getRowHeight(int row) {
        // doesn't take hidden rows into account! (but height is 0 for those)
        if (rowH.length >= row) {
            return rowH[row - 1];
        } else {
            return defRowH;
        }
    }

    @Override
    public int getColWidth(int col) {
        // doesn't take hidden columns into account! (but width is 0 for those)
        if (colW.length >= col) {
            return colW[col - 1];
        } else {
            return defColW;
        }
    }

    @Override
    public int getColWidthActual(int col) {
        if (hiddenColumnIndexes != null && hiddenColumnIndexes.contains(col)) {
            return 0;
        } else {
            return getColWidth(col);
        }
    }

    /** Get column header for column indexed 1.. */
    @Override
    public final String getColHeader(int col) {
        String h = "";
        while (col > 0) {
            h = (char) ('A' + (col - 1) % 26) + h;
            col = (col - 1) / 26;
        }
        return h;
    }

    public final int getColHeaderIndex(String header) {
        int x = 0;
        for (int i = 0; i < header.length(); i++) {
            char h = header.charAt(i);
            x = (h - 'A' + 1) + (x * 26);
        }
        return x;
    }

    /** Get row header for rows indexed 1.. */
    @Override
    public String getRowHeader(int row) {
        return "" + row;
    }

    @Override
    public int getDefinedRows() {
        return rowH.length;
    }

    @Override
    public int getDefinedCols() {
        return colW.length;
    }

    @Override
    public float[] getRowHeights() {
        return rowH;
    }

    @Override
    public int[] getColWidths() {
        return colW;
    }

    @Override
    public float getDefaultRowHeight() {
        return defRowH;
    }

    @Override
    public int getDefaultColumnWidth() {
        return defColW;
    }

    @Override
    public int getRowBufferSize() {
        return rowBufferSize;
    }

    @Override
    public int getColumnBufferSize() {
        return columnBufferSize;
    }

    @Override
    public int getMaximumCols() {
        return cols;
    }

    @Override
    public int getMaximumRows() {
        return rows;
    }

    @Override
    public int getActiveSheetIndex() {
        return activeSheetIndex;
    }

    @Override
    public int getNumberOfSheets() {
        return sheets;
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        return (hiddenColumnIndexes == null ? false : hiddenColumnIndexes
                .contains(columnIndex));
    }

    @Override
    public boolean isRowHidden(int rowIndex) {
        return (hiddenRowIndexes == null ? false : hiddenRowIndexes
                .contains(rowIndex));
    }

    @Override
    public boolean canResize() {
        return !sheetProtected;
    }

    public void setDisplayGridlines(boolean displayGridlines) {
        sheetWidget.setDisplayGridlines(displayGridlines);
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        sheetWidget.setDisplayRowColHeadings(displayRowColHeadings);
    }

    public void refreshOverlayPositions() {
        sheetWidget.refreshAlwaysVisibleCellCommentOverlays();
        sheetWidget.refreshCurrentCellCommentOverlay();
        sheetWidget.refreshPopupButtonOverlays();
    }

    public void updateBottomRightCellValues(ArrayList<CellData> cellData) {
        sheetWidget.updateBottomRightCellValues(cellData);
    }

    public void updateTopLeftCellValues(ArrayList<CellData> cellData) {
        sheetWidget.updateTopLeftCellValues(cellData);
    }

    public void updateTopRightCellValues(ArrayList<CellData> cellData) {
        sheetWidget.updateTopRightCellValues(cellData);
    }

    public void updateBottomLeftCellValues(ArrayList<CellData> cellData) {
        sheetWidget.updateBottomLeftCellValues(cellData);
    }

    /**
     * This can contain values for any of the panes or values that are just in
     * the client side cache, but the cell is not actually visible.
     * 
     * @param updatedCellData
     */
    public void cellValuesUpdated(ArrayList<CellData> updatedCellData) {
        sheetWidget.cellValuesUpdated(updatedCellData);
    }

    @Override
    public void setCellStyleWidthRatios(
            HashMap<Integer, Float> cellStyleWidthRatioMap) {
        spreadsheetHandler.setCellStyleWidthRatios(cellStyleWidthRatioMap);
    }

}
