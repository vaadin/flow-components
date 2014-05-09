package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.addon.spreadsheet.client.SheetTabSheet.SheetTabSheetHandler;

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
        public void cellContextMenu(Event event, int column, int row);

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

    private SpreadsheetViewActionHandler actionHandler;

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

    private List<String> customCellBorderStyles;

    private boolean loaded;
    private boolean formulaBarEditing;
    private boolean inlineEditing;
    private boolean cancelDeferredCommit;
    private boolean selectedCellIsFormulaType;
    private boolean cellLocked;
    private boolean customCellEditorDisplayed;
    private boolean sheetProtected;
    private boolean cancelNextSheetRelayout;
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
            clearSpreadsheet();
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
    }

    /** Clear all current sheet related data */
    public void clearSpreadsheet() {
        colBeforeMergedCell = 0;
        rowBeforeMergedCell = 0;
        // reset function bar
        formulaBarWidget.clear();
        if (mergedRegions != null) {
            while (0 < mergedRegions.size()) {
                sheetWidget.removeMergedRegion(mergedRegions.remove(0), 0);
            }
            mergedRegions = null;
        }
        // reset sheet
        sheetWidget.clearAll();
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

    public void setActionHandler(
            SpreadsheetViewActionHandler spreadsheetActionHandler) {
        actionHandler = spreadsheetActionHandler;
    }

    public void setSheetContextMenuHandler(
            SheetContextMenuHandler sheetContextMenuHandler) {
        this.sheetContextMenuHandler = sheetContextMenuHandler;
    }

    @Override
    public boolean hasCustomContextMenu() {
        return sheetContextMenuHandler != null;
    }

    public void setCellStyleToCSSSelector(
            HashMap<Integer, String> cellStyleToCSSSelector) {
        sheetWidget.updateCellStyleCSSRules(cellStyleToCSSSelector);
    }

    public void addRequestedCells(HashMap<String, String> cellData) {
        sheetWidget.addCellsData(cellData, null);
    }

    public void updateCellValues(HashMap<String, String> updatedCellData,
            ArrayList<String> removedCells) {
        sheetWidget.addCellsData(updatedCellData, removedCells);
    }

    public void showCellCustomComponents(Map<String, Widget> customWidgetMap) {
        sheetWidget.showCustomWidgets(customWidgetMap);
    }

    public void updatePopupButtons(List<PopupButtonWidget> popupButtons) {
        sheetWidget.updatePopupButtons(popupButtons);
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
                sheetWidget.updateInputValue(value);
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
        if (!sheetWidget.isCellInView(col, row)) {
            sheetWidget.scrollCellIntoView(col, row);
        }
        sheetWidget.focusSheet();
    }

    public void invalidCellAddress() {
        formulaBarWidget.revertCellAddressValue();
    }

    public void setCellRangeSelection(int firstColumn, int lastColumn,
            int firstRow, int lastRow, String value, boolean formula,
            boolean locked) {
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
        if (oldSelectedCellCol != firstColumn || oldSelectedCellRow != firstRow) {
            sheetWidget.setSelectedCell(firstColumn, firstRow);
            newSelectedCellSet();
        }
        sheetWidget.updateSelectionOutline(firstColumn, lastColumn, firstRow,
                lastRow);
        sheetWidget.updateSelectedCellStyles(firstColumn, lastColumn, firstRow,
                lastRow, true);
        if (!sheetWidget.isAreaInView(firstColumn, lastColumn, firstRow,
                lastRow)) {
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

    public void updateMergedRegions(List<MergedRegion> mergedRegions) {
        // remove removed, update updated ones
        if (this.mergedRegions != null) {
            int index = 0;
            if (mergedRegions != null) {
                for (Iterator<MergedRegion> iterator = this.mergedRegions
                        .iterator(); iterator.hasNext();) {
                    MergedRegion oldRegion = iterator.next();
                    if (index < mergedRegions.size()) {
                        MergedRegion newRegion = mergedRegions.get(index);
                        if (oldRegion.id == newRegion.id) {
                            if (oldRegion.col1 != newRegion.col1
                                    || oldRegion.col2 != newRegion.col2
                                    || oldRegion.row1 != newRegion.row1
                                    || oldRegion.row2 != newRegion.row2) {
                                String oldKey = SheetWidget.toKey(
                                        oldRegion.col1, oldRegion.row1);

                                sheetWidget.updateMergedRegionSizeAndPosition(
                                        newRegion, oldKey, index);
                            } else {
                                sheetWidget.updateMergedRegionSize(oldRegion);
                            }
                            index++;
                        } else {
                            iterator.remove();
                            sheetWidget.removeMergedRegion(oldRegion, index);
                        }
                    } else {
                        iterator.remove();
                        sheetWidget.removeMergedRegion(oldRegion, index);
                    }
                }
            } else {
                // remove all
                for (MergedRegion region : this.mergedRegions) {
                    sheetWidget.removeMergedRegion(region, index++);
                }
            }
        }
        if (mergedRegions != null) {
            int i = (this.mergedRegions == null ? 0 : this.mergedRegions.size());
            while (i < mergedRegions.size()) {
                MergedRegion newMergedRegion = mergedRegions.get(i);
                sheetWidget.addMergedRegion(newMergedRegion);
                i++;
            }
        }
        this.mergedRegions = mergedRegions;
    }

    @Override
    public void onScrollViewChanged(int firstRowIndex, int lastRowIndex,
            int firstColumnIndex, int lastColumnIndex) {
        actionHandler.loadCellsData(firstRowIndex, lastRowIndex,
                firstColumnIndex, lastColumnIndex);
    }

    @Override
    public void onLinkCellClick(int column, int row) {
        actionHandler.linkCellClicked(column, row);
    }

    @Override
    public void onCellRightClick(Event event, int column, int row) {
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
        colBeforeMergedCell = 0;
        rowBeforeMergedCell = 0;

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
                    actionHandler.cellRangeSelected(selectedRegion.col1,
                            selectedRegion.col2, selectedRegion.row1,
                            selectedRegion.row2);
                } else {
                    actionHandler.cellsAddedToRangeSelection(
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
            formulaBarWidget.setCellPlainValue("");
            if (updateToActionHandler) {
                actionHandler.cellAddedToSelectionAndSelected(column, row);
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
                formulaBarWidget.setCellPlainValue("");
                actionHandler.cellSelected(column, row, true);
            }
        }
    }

    @Override
    public void onRowHeaderClick(int row, boolean shiftPressed,
            boolean metaOrCrtlPressed) {
        int firstColumnIndex = sheetWidget.getLeftColumnIndex();
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
                actionHandler.cellRangeSelected(c1, c2, r1, r2);
            } else {
                actionHandler.cellsAddedToRangeSelection(c1, c2, r1, r2);
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
            actionHandler.rowAddedToRangeSelection(row, firstColumnIndex);
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
            actionHandler.rowSelected(row, firstColumnIndex);
        }
    }

    @Override
    public void onColumnHeaderClick(int column, boolean shiftPressed,
            boolean metaOrCrtlPressed) {
        doCommitIfEditing();
        int firstRowIndex = sheetWidget.getTopRowIndex();
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
                actionHandler.cellRangeSelected(c1, c2, r1, r2);
            } else {
                actionHandler.cellsAddedToRangeSelection(c1, c2, r1, r2);
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
            actionHandler.columnAddedToSelection(column, firstRowIndex);
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
            actionHandler.columnSelected(column, firstRowIndex);
        }
    }

    @Override
    public void onColumnHeaderResizeDoubleClick(int columnIndex) {
        actionHandler.onColumnAutofit(columnIndex);
    }

    private void doCommitIfEditing() {
        if (inlineEditing || formulaBarEditing) {
            cancelDeferredCommit = true;
            final String editedValue = formulaBarWidget.getFormulaFieldValue();
            actionHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
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
            col = sheetWidget.getRightColumnIndex() + 1;
        }
        if (col > cols) {
            col = cols;
        }
        if (row == 0) {
            row = 1;
        } else if (row < 0) {
            row = sheetWidget.getBottomRowIndex() + 1;
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
        actionHandler.cellRangePainted(sheetWidget.getSelectedCellColumn(),
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
            value = "";
        } else {
            cachedCellValue = value;
            value = formulaBarWidget.getFormulaFieldValue();
        }
        formulaBarEditing = false;
        if (!cellLocked) {
            if (!inlineEditing && !customCellEditorDisplayed) {
                sheetWidget.updateInputValue(value);
                sheetWidget.startEditingCell(true, true, value);
                inlineEditing = true;
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
        inlineEditing = true;
        cancelDeferredCommit = true;
        if (formulaBarEditing) { // just swap, everything should work
            formulaBarEditing = false;
        } else { // need to make sure the input value is correct
            sheetWidget.startEditingCell(true, true,
                    formulaBarWidget.getFormulaFieldValue());
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
        actionHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
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
        actionHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
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
    public void onSheetKeyPress(Event event, String enteredCharacter) {
        switch (event.getKeyCode()) {
        case KeyCodes.KEY_DELETE:
            if (!cellLocked) {
                actionHandler.deleteSelectedCells();
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
            if (!cellLocked && !customCellEditorDisplayed) {
                // cache value and start editing cell as empty
                cachedCellValue = sheetWidget.getSelectedCellLatestValue();
                sheetWidget.updateInputValue("");
                sheetWidget.startEditingCell(true, true, "");
                formulaBarWidget.setCellPlainValue("");
                inlineEditing = true;
            }
            break;
        default:
            if (!sheetWidget.isSelectedCellCustomized() && !inlineEditing
                    && !cellLocked && !customCellEditorDisplayed) {
                // cache value and start editing cell as empty
                cachedCellValue = sheetWidget.getSelectedCellLatestValue();
                sheetWidget.startEditingCell(true, true, enteredCharacter);
                formulaBarWidget.setCellPlainValue(enteredCharacter);
                inlineEditing = true;
            }
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
        actionHandler.sheetAddressChanged(value);
    }

    @Override
    public void onAddressFieldEsc() {
        sheetWidget.focusSheet();
    }

    @Override
    public void onSheetTabSelected(int sheetIndex) {
        int scrollLeft = sheetWidget.getSheetScrollLeft();
        int scrollTop = sheetWidget.getSheetScrollTop();
        actionHandler.sheetSelected(sheetIndex, scrollLeft, scrollTop);
    }

    @Override
    public void onFirstTabIndexChange(int firstVisibleTab) {
        // Disabled because not working in Apache POI
        // actionHandler.firstVisibleTabChanged(firstVisibleTab);
    }

    @Override
    public void onSheetRename(int sheetIndex, String newName) {
        actionHandler.sheetRenamed(sheetIndex, newName);
    }

    @Override
    public void onNewSheetCreated() {
        int scrollLeft = sheetWidget.getSheetScrollLeft();
        int scrollTop = sheetWidget.getSheetScrollTop();
        actionHandler.sheetCreated(scrollLeft, scrollTop);
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
            actionHandler.selectionIncreasePainted(c1, c2, r1, r2);
        }
    }

    @Override
    public void onSelectionDecreasePainted(int col1, int col2,
            int colEdgeIndex, int row1, int row2, int rowEdgeIndex) {
        // the selection widget has made sure the decreasing area is not in
        // middle of merged cells.
        actionHandler.selectionDecreasePainted(colEdgeIndex, rowEdgeIndex);
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
                sheetWidget.updateInputValue(value);
                sheetWidget.startEditingCell(false, true, value);
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
        actionHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
                sheetWidget.getSelectedCellRow(), value);
        cellEditingDone(value);
        sheetWidget.focusSheet();
        moveSelectedCellDown(false);
    }

    @Override
    public void onFormulaTab(String value) {
        actionHandler.cellValueEdited(sheetWidget.getSelectedCellColumn(),
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
        actionHandler.rowsResized(newSizes, x[0], x[1], x[2], x[3]);
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
        actionHandler.columnResized(newSizes, x[0], x[1], x[2], x[3]);
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
                    actionHandler.cellValueEdited(
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
            actionHandler.cellSelected(col, row, false);
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
                        sheetWidget.getCellValue(col, row));
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
            actionHandler.cellSelected(col, row, false);
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
                        sheetWidget.getCellValue(col, row));
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
            actionHandler.cellSelected(col, row, false);
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
                        sheetWidget.getCellValue(col, row));
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
            actionHandler.cellSelected(col, row, false);
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
                        sheetWidget.getCellValue(col, row));
            }
        }
    }

    private void onCellSelectedWithKeyboard(int column, int row, String value) {
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
        sheetWidget.updateSelectedCellStyles(column, column, row, row, true);
        // display cell data address
        formulaBarWidget.setSelectedCellAddress(createCellAddress(column, row));
        newSelectedCellSet();
        formulaBarWidget.setCellPlainValue("");
        actionHandler.cellSelected(column, row, true);
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
            actionHandler.cellRangeSelected(sheetWidget.getSelectionLeftCol(),
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
            actionHandler.cellRangeSelected(sheetWidget.getSelectionLeftCol(),
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

    // public void setFirstVisibleTab(int firstVisibleTab) {
    // this.firstVisibleTab = firstVisibleTab;
    // }

    public void setVerticalScrollPositions(int[] verticalScrollPositions) {
        this.verticalScrollPositions = verticalScrollPositions;
    }

    public void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        this.horizontalScrollPositions = horizontalScrollPositions;
    }

    public void setCellStyleToCSSStyle(Map<Integer, String> cellStyleToCSSStyle) {
        this.cellStyleToCSSStyle = cellStyleToCSSStyle;
    }

    public void setCustomCellBorderStyles(List<String> customCellBorderStyles) {
        if (this.customCellBorderStyles != null) {
            sheetWidget.removeCustomCellStyles();
            sheetWidget.addCustomCellStyles(customCellBorderStyles);
        }
        this.customCellBorderStyles = customCellBorderStyles;
    }

    public void setHyperlinksTooltips(Map<String, String> cellLinksMap) {
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
                        actionHandler.cellSelected(
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

    public void setHiddenColumnIndexes(List<Integer> hiddenColumnIndexes) {
        this.hiddenColumnIndexes = hiddenColumnIndexes;
    }

    public void setHiddenRowIndexes(List<Integer> hiddenRowIndexes) {
        this.hiddenRowIndexes = hiddenRowIndexes;
    }

    public void setCellComments(Map<String, String> cellComments) {
        sheetWidget.setCellComments(cellComments);
    }

    @Override
    public List<String> getCustomCellBorderStyles() {
        return customCellBorderStyles;
    }

    @Override
    public Map<Integer, String> getCellStyleToCSSStyle() {
        return cellStyleToCSSStyle;
    }

    @Override
    public float getRowHeight(int row) {
        // doesn't take hidden rows into account!
        if (rowH.length >= row) {
            return rowH[row - 1];
        } else {
            return defRowH;
        }
    }

    @Override
    public int getColWidth(int col) {
        // doesn't take hidden columns into account!
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
}
