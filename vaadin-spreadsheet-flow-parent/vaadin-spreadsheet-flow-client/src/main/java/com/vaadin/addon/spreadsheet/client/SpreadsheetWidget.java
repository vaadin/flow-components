package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.TouchEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.addon.spreadsheet.client.SheetTabSheet.SheetTabSheetHandler;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector.CommsTrigger;
import com.vaadin.addon.spreadsheet.shared.GroupingData;
import com.vaadin.client.Focusable;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.Util;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.communication.RpcProxy;

public class SpreadsheetWidget extends Composite implements SheetHandler,
        FormulaBarHandler, SheetTabSheetHandler, Focusable {

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

    private static final int DELAYED_SERVER_REQUEST_DELAY = 200; // ms
    private static final String DEFAULT_WIDTH = "500.0px";
    private static final String DEFAULT_HEIGHT = "400.0px";

    private final SheetWidget sheetWidget;
    final FormulaBarWidget formulaBarWidget;
    private final SheetTabSheet sheetTabSheet;

    private final SelectionHandler selectionHandler;

    SpreadsheetHandler spreadsheetHandler;

    private SheetContextMenuHandler sheetContextMenuHandler;

    SpreadsheetCustomEditorFactory customEditorFactory;

    private int rowBufferSize;

    private int columnBufferSize;

    private int rows;

    private int cols;

    private float defRowH;
    private int defColW;

    private float[] rowH;
    private int[] colW;

    /** 1-based */
    private int activeSheetIndex;

    private Map<Integer, String> cellStyleToCSSStyle;
    public Map<Integer, Integer> rowIndexToStyleIndex;
    public Map<Integer, Integer> columnIndexToStyleIndex;
    private Set<Integer> lockedColumnIndexes;
    private Set<Integer> lockedRowIndexes;

    private Map<Integer, String> conditionalFormattingStyles = new HashMap<Integer, String>();

    private boolean loaded;
    private boolean touchMode;
    private boolean formulaBarEditing;
    private boolean inlineEditing;
    private boolean cancelDeferredCommit;
    private boolean selectedCellIsFormulaType;
    boolean cellLocked;
    boolean customCellEditorDisplayed;
    private boolean sheetProtected;
    private boolean cancelNextSheetRelayout;
    private String cachedCellValue;
    private int[] verticalScrollPositions;
    private int[] horizontalScrollPositions;
    // private int firstVisibleTab; Not working in POI -> disabled
    private String[] sheetNames;
    List<Integer> hiddenColumnIndexes;
    List<Integer> hiddenRowIndexes;
    private List<MergedRegion> mergedRegions;
    private boolean lockFormatColumns = true;
    private boolean lockFormatRows = true;

    /**
     * Timer flag for sending lazy RPCs to server. Used so that we don't send an
     * RPC for each key press. Default timeout is a second.
     */
    private boolean okToSendCellProtectRpc = true;

    @SuppressWarnings("serial")
    MergedRegionContainer mergedRegionContainer = new MergedRegionContainer() {

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
    private CommsTrigger commsTrigger;

    /**
     * The last click coords when editing formula
     */
    private int tempSelectionStartCol;

    /**
     * The last click coords when editing formula
     */
    private int tempSelectionStartRow;

    public SpreadsheetWidget() {

        setTouchMode(TouchEvent.isSupported());

        sheetWidget = new SheetWidget(this, touchMode);
        formulaBarWidget = new FormulaBarWidget(this, sheetWidget);
        sheetTabSheet = new SheetTabSheet(this);
        selectionHandler = new SelectionHandler(this, sheetWidget);

        sheetWidget.getElement().appendChild(formulaBarWidget.getElement());
        sheetWidget.getElement().appendChild(sheetTabSheet.getElement());

        initWidget(sheetWidget);

        // There is a bug in CssLayout/VerticalLayout.
        // If a component calls setVisible(false) another component in the
        // layout
        // next to it is detached and then attached to the layout and the scroll
        // position is reset. We need to store the scroll position on detach and
        // then set on attach event.
        sheetWidget.addAttachHandler(new AttachEvent.Handler() {
            int leftScrollPosition = 0;
            int topScrollPosition = 0;

            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (attachEvent.isAttached()) {
                    sheetWidget.setScrollPosition(leftScrollPosition,
                            topScrollPosition);
                } else {
                    leftScrollPosition = sheetWidget.getSheetScrollLeft();
                    topScrollPosition = sheetWidget.getSheetScrollTop();
                }

            }
        });
    }

    @Override
    public void setHeight(final String height) {
        if (height != null && !height.isEmpty()) {
            super.setHeight(height);
        } else {
            super.setHeight(DEFAULT_HEIGHT);
        }
    }

    @Override
    public void setWidth(final String width) {
        if (width != null && !width.isEmpty()) {
            super.setWidth(width);
        } else {
            super.setWidth(DEFAULT_WIDTH);
        }
    }

    /**
     * Enable or disable Formatting columns locking.
     *
     * @param enabled
     *            the new content. Can not be HTML.
     */
    public void setLockFormatColumns(boolean enabled) {
        lockFormatColumns = enabled;
        if (lockFormatColumns) {
            if (!getStyleName().contains("lock-format-columns")) {
                addStyleName("lock-format-columns");
            }
        } else {
            removeStyleName("lock-format-columns");
        }
    }

    /**
     * Enable or disable Formatting rows locking.
     *
     * @param enabled
     *            the new content. Can not be HTML.
     */
    public void setLockFormatRows(boolean enabled) {
        lockFormatRows = enabled;
        if (lockFormatRows) {
            addStyleName("lock-format-rows");
        } else {
            removeStyleName("lock-format-rows");
        }
    }

    /**
     * Sets the content of the info label.
     *
     * @param value
     *            the new content. Can not be HTML.
     */
    public void setInfoLabelValue(String value) {
        sheetTabSheet.setInfoLabelValue(value);
    }

    /**
     * @return current content of the info label.
     */
    public String getInfoLabelValue() {
        return sheetTabSheet.getInfoLabelValue();
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

    public void sheetUpdated(String[] sheetNames, int sheetIndex,
            boolean clearScrollPosition) {
        if (!loaded) { // component first load
            sheetTabSheet.addTabs(sheetNames);
            sheetTabSheet.setSelectedTab(sheetIndex);
        } else {
            if (activeSheetIndex != sheetIndex) {
                // active sheet or whole spreadsheet has changed
                sheetTabSheet.setTabs(sheetNames, clearScrollPosition);
                sheetTabSheet.setSelectedTab(sheetIndex);
            } else if (this.sheetNames == null
                    || !Arrays.equals(this.sheetNames, sheetNames)) {
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
        selectionHandler.clearBeforeMergeCells();

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

    public SpreadsheetHandler getSpreadsheetHandler() {
        return spreadsheetHandler;
    }

    public void setSheetContextMenuHandler(
            SheetContextMenuHandler sheetContextMenuHandler) {
        this.sheetContextMenuHandler = sheetContextMenuHandler;
    }

    @Override
    public boolean hasCustomContextMenu() {
        return sheetContextMenuHandler != null;
    }

    public void showCellCustomComponents(
            HashMap<String, Widget> customWidgetMap) {
        sheetWidget.showCustomWidgets(customWidgetMap);
    }

    public void addPopupButton(PopupButtonWidget widget) {
        sheetWidget.addPopupButton(widget);
    }

    public void removePopupButton(PopupButtonWidget popupButton) {
        sheetWidget.removePopupButton(popupButton);
    }

    public void updateFormulaBar(String possibleName, int col, int row) {
        // do check in case the user has changed the selected cell before the
        // formula was sent
        if (sheetWidget.getSelectedCellColumn() == col
                && sheetWidget.getSelectedCellRow() == row) {
            updateSelectedCellValues(col, row);
            if (possibleName != null) {
                formulaBarWidget.setSelectedCellAddress(possibleName);
            }
        }
    }

    public void invalidCellAddress() {
        formulaBarWidget.revertCellAddressValue();
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

        selectionHandler.newSelectedCellSet();
    }

    /**
     * Called when the {@link #customEditorFactory} might have a new editor for
     * the currently selected cell.
     */
    public void loadSelectedCellEditor() {
        if (!sheetWidget.isSelectedCellCustomized() && !cellLocked
                && customEditorFactory != null && customEditorFactory
                        .hasCustomEditor(sheetWidget.getSelectedCellKey())) {
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

    /**
     * Handles overlays, currently images and charts.
     */
    void addOverlay(String key, Widget widget, OverlayInfo overlayInfo) {
        SheetOverlay overlay = new SheetOverlay(widget, overlayInfo);
        sheetWidget.addSheetOverlay(key, overlay);
    }

    void updateOverlay(String key, OverlayInfo overlayInfo) {
        sheetWidget.updateOverlayInfo(key, overlayInfo);
    }

    void removeOverlay(String key) {
        sheetWidget.removeSheetOverlay(key);
    }

    public void updateMergedRegions(
            final ArrayList<MergedRegion> mergedRegions) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                // remove old, add new
                clearMergedRegions();
                if (mergedRegions != null) {
                    int i = 0;
                    while (i < mergedRegions.size()) {
                        MergedRegion newMergedRegion = mergedRegions.get(i);
                        sheetWidget.addMergedRegion(newMergedRegion);
                        Cell cell = sheetWidget.getCell(newMergedRegion.col1,
                                newMergedRegion.row1);
                        if (cell != null) {
                            // initial display only used single column width,
                            // re-calculate with merged width
                            cell.setValue(cell.getValue(), cell.getCellStyle(),
                                    false);
                        }
                        i++;
                    }
                    sheetWidget.checkMergedRegionPositions();
                }

                // copy list for later
                if (mergedRegions == null) {
                    SpreadsheetWidget.this.mergedRegions = null;
                } else {
                    SpreadsheetWidget.this.mergedRegions = new ArrayList<MergedRegion>(
                            mergedRegions);
                }
            }
        });
    }

    private void clearMergedRegions() {
        if (mergedRegions != null) {
            while (0 < mergedRegions.size()) {
                sheetWidget.removeMergedRegion(mergedRegions.remove(0), 0);
            }
        }
    }

    @Override
    public void onScrollViewChanged(int firstRowIndex, int lastRowIndex,
            int firstColumnIndex, int lastColumnIndex) {
        spreadsheetHandler.onSheetScroll(firstRowIndex, firstColumnIndex,
                lastRowIndex, lastColumnIndex);
        startDelayedSendingTimer();
    }

    @Override
    public void onLinkCellClick(int column, int row) {
        spreadsheetHandler.linkCellClicked(row, column);
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
    public void onCellClick(int column, int row, String value, boolean shiftKey,
            boolean metaOrCtrlKey, boolean updateToActionHandler) {
        doCommitIfEditing();
        if (column == 0 || row == 0) {
            return;
        }
        boolean hasSelectedCellChangedOnClick = false;
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

            if (formulaBarWidget.isEditingFormula()) {
                // do nothing here
            } else if (sheetWidget.isSelectionRangeOutlineVisible()) {
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

            if (formulaBarWidget.isEditingFormula()) {

                formulaBarWidget.setFormulaCellRange(tempSelectionStartCol,
                        tempSelectionStartRow, column, row);

            } else if (updateToActionHandler) {
                if (sheetWidget.isSelectionRangeOutlineVisible()) {
                    spreadsheetHandler.cellRangeSelected(selectedRegion.row1,
                            selectedRegion.col1, selectedRegion.row2,
                            selectedRegion.col2);
                } else {
                    spreadsheetHandler.cellsAddedToRangeSelection(
                            selectedRegion.row1, selectedRegion.col1,
                            selectedRegion.row2, selectedRegion.col2);
                }
                startDelayedSendingTimer();
            }
        } else if (metaOrCtrlKey) {
            // add the selected cell into the selection, set it as the selected
            if (column == sheetWidget.getSelectedCellColumn()
                    && row == sheetWidget.getSelectedCellRow()) {
                // clicked on the selected cell again -> nothing happens
                return;
            }

            if (formulaBarWidget.isEditingFormula()) {
                formulaBarWidget.addFormulaCellRange(column, row, column, row);
            } else {

                // TODO update the selection coherence, if the areas align
                // properly
                if (sheetWidget.isCoherentSelection()) {
                    sheetWidget.setCoherentSelection(false);
                }
                if (sheetWidget.isSelectionRangeOutlineVisible()) {
                    sheetWidget.setSelectionRangeOutlineVisible(false);
                }
                sheetWidget.swapCellSelection(column, row);
                selectionHandler.newSelectedCellSet();
                if (hasSelectedCellChangedOnClick) {
                    updateSelectedCellValues(column, row);
                }
                if (updateToActionHandler) {
                    spreadsheetHandler.cellAddedToSelectionAndSelected(row,
                            column);
                    startDelayedSendingTimer();
                }
            }
        } else {
            // select cell
            MergedRegion cell = mergedRegionContainer
                    .getMergedRegionStartingFrom(column, row);

            if (formulaBarWidget.isEditingFormula()) {

                tempSelectionStartCol = column;
                tempSelectionStartRow = row;

                formulaBarWidget.setFormulaCellRange(column, row, column, row);
            } else {

                if (!sheetWidget.isCoherentSelection()) {
                    sheetWidget.setCoherentSelection(true);
                }
                if (!sheetWidget.isSelectionRangeOutlineVisible()) {
                    sheetWidget.setSelectionRangeOutlineVisible(true);
                    sheetWidget.clearSelectedCellStyle();
                }
                sheetWidget.setSelectedCell(column, row);
                if (cell != null) {
                    sheetWidget.updateSelectionOutline(cell.col1, cell.col2,
                            cell.row1, cell.row2);
                    sheetWidget.updateSelectedCellStyles(cell.col1, cell.col2,
                            cell.row1, cell.row2, true);

                    selectionHandler.setColBeforeMergedCell(cell.col1);
                    selectionHandler.setRowBeforeMergedCell(cell.row1);
                } else {
                    sheetWidget.updateSelectionOutline(column, column, row,
                            row);
                    sheetWidget.updateSelectedCellStyles(column, column, row,
                            row, true);
                }
                if (hasSelectedCellChangedOnClick) {
                    updateSelectedCellValues(column, row);
                }
                if (updateToActionHandler) {
                    selectionHandler.newSelectedCellSet();
                    spreadsheetHandler.cellSelected(row, column, true);
                    startDelayedSendingTimer();
                }
            }
        }
    }

    public void updateSelectedCellValues(int column, int row) {
        updateSelectedCellValues(column, row, null);
    }

    public void updateSelectedCellValues(int column, int row, String name) {
        if (!sheetWidget.isEditingCell()) {
            String formulaValue = sheetWidget.getCellFormulaValue(column, row);
            if (formulaValue != null && !formulaValue.isEmpty()) {
                formulaBarWidget.setCellFormulaValue(formulaValue);
                sheetWidget.updateInputValue("=" + formulaValue);
            } else {
                formulaBarWidget.setCellPlainValue(
                        sheetWidget.getOriginalCellValue(column, row));
            }
        }
        cellLocked = sheetWidget.isCellLocked(column, row);
        if (!customCellEditorDisplayed) {
            formulaBarWidget.setFormulaFieldEnabled(!cellLocked);
        } else {
            sheetWidget.displayCustomCellEditor(customEditorFactory
                    .getCustomEditor(sheetWidget.getSelectedCellKey()));
        }
        if (name != null) {
            formulaBarWidget.setSelectedCellAddress(name);
        } else {
            formulaBarWidget
                    .setSelectedCellAddress(createCellAddress(column, row));
        }
    }

    @Override
    public void onRowHeaderClick(int row, boolean shiftPressed,
            boolean metaOrCrtlPressed) {
        int firstColumnIndex = sheetWidget.hasFrozenColumns() ? 1
                : sheetWidget.getLeftVisibleColumnIndex();
        doCommitIfEditing();
        if (!shiftPressed) {
            updateSelectedCellValues(firstColumnIndex, row);
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
                spreadsheetHandler.cellRangeSelected(r1, c1, r2, c2);
            } else {
                spreadsheetHandler.cellsAddedToRangeSelection(r1, c1, r2, c2);
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
            selectionHandler.newSelectedCellSet();
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
            selectionHandler.newSelectedCellSet();
            spreadsheetHandler.rowSelected(row, firstColumnIndex);
        }
        startDelayedSendingTimer();
    }

    @Override
    public void onColumnHeaderClick(int column, boolean shiftPressed,
            boolean metaOrCrtlPressed) {
        doCommitIfEditing();
        int firstRowIndex = sheetWidget.hasFrozenRows() ? 1
                : sheetWidget.getTopVisibleRowIndex();
        if (!shiftPressed) {
            updateSelectedCellValues(column, firstRowIndex);
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
                spreadsheetHandler.cellRangeSelected(r1, c1, r2, c2);
            } else {
                spreadsheetHandler.cellsAddedToRangeSelection(r1, c1, r2, c2);
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
            selectionHandler.newSelectedCellSet();
            // add the selection styles
            sheetWidget.updateSelectedCellStyles(column, column, 1, rows,
                    false);
            spreadsheetHandler.columnAddedToSelection(firstRowIndex, column);
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
            selectionHandler.newSelectedCellSet();
            spreadsheetHandler.columnSelected(column, firstRowIndex);
        }
        startDelayedSendingTimer();
    }

    @Override
    public void onRowHeaderDoubleClick(int rowIndex) {
        spreadsheetHandler.onRowAutofit(rowIndex);
    }

    @Override
    public void onColumnHeaderResizeDoubleClick(int columnIndex) {
        spreadsheetHandler.onColumnAutofit(columnIndex);
    }

    void doCommitIfEditing() {

        if (formulaBarWidget.isEditingFormula()) {
            // do nothing
        } else if (inlineEditing || formulaBarEditing) {
            cancelDeferredCommit = true;
            final String editedValue = formulaBarWidget.getFormulaFieldValue();
            spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellRow(),
                    sheetWidget.getSelectedCellColumn(), editedValue);
            cellEditingDone(editedValue, true);
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

        if (formulaBarWidget.isEditingFormula()) {

            formulaBarWidget.setFormulaCellRange(tempSelectionStartCol,
                    tempSelectionStartRow, col, row);
        } else {
            MergedRegion selectedRegion = MergedRegionUtil
                    .findIncreasingSelection(mergedRegionContainer, row1, row2,
                            col1, col2);
            sheetWidget.updateSelectionOutline(selectedRegion.col1,
                    selectedRegion.col2, selectedRegion.row1,
                    selectedRegion.row2);
            sheetWidget.updateSelectedCellStyles(selectedRegion.col1,
                    selectedRegion.col2, selectedRegion.row1,
                    selectedRegion.row2, true);

            formulaBarWidget.setSelectedCellAddress(createRangeSelectionString(
                    selectedRegion.col1, selectedRegion.col2,
                    selectedRegion.row1, selectedRegion.row2));
        }
    }

    @Override
    public void onFinishedSelectingCellsWithDrag(int col1, int col2, int row1,
            int row2) {
        if (col1 == 0 || col2 == 0 || row1 == 0 || row2 == 0
                || col1 == col2 && row1 == row2
                        && col1 == sheetWidget.getSelectedCellColumn()
                        && row1 == sheetWidget.getSelectedCellRow()) {
            return;
        }

        int origCol2 = col2;
        int origRow2 = row2;

        // swap coordinates so that 1 is smaller than 2
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

        if (formulaBarWidget.isEditingFormula()) {
            formulaBarWidget.setFormulaCellRange(tempSelectionStartCol,
                    tempSelectionStartRow, origCol2, origRow2);
            formulaBarWidget.clearFormulaSelection();
        } else {

            MergedRegion selectedRegion = MergedRegionUtil
                    .findIncreasingSelection(mergedRegionContainer, row1, row2,
                            col1, col2);
            spreadsheetHandler.cellRangePainted(
                    sheetWidget.getSelectedCellRow(),
                    sheetWidget.getSelectedCellColumn(), selectedRegion.row1,
                    selectedRegion.col1, selectedRegion.row2,
                    selectedRegion.col2);
            formulaBarWidget.setSelectedCellAddress(
                    createCellAddress(sheetWidget.getSelectedCellColumn(),
                            sheetWidget.getSelectedCellRow()));
            selectionHandler.newSelectedCellSet();
            startDelayedSendingTimer();
        }
    }

    @Override
    public void onCellDoubleClick(int column, int row, String value) {
        if (sheetWidget.getSelectedCellRow() != row
                && sheetWidget.getSelectedCellColumn() != column) {
            onCellClick(column, row, value, false, false, true);
        } else {
            Cell cell = sheetWidget.getCell(column, row);
            value = cell.getValue();
            cachedCellValue = value;
            formulaBarWidget.cacheFormulaFieldValue();
            value = formulaBarWidget.getFormulaFieldValue();
        }
        formulaBarEditing = false;
        checkEditableAndNotify();
        if (!cellLocked) {
            if (!inlineEditing && !customCellEditorDisplayed) {
                inlineEditing = true;
                sheetWidget.startEditingCell(true, true, value);
                formulaBarWidget.setInFullFocus(true);
                formulaBarWidget.startInlineEdit(true);
            }
        }
    }

    @Override
    public void onCellInputBlur(final String inputValue) {
        // need to do this deferred in case focus moved to the formula field
        if (inlineEditing && !formulaBarWidget.isEditingFormula()) {
            doDeferredCellValueCommit(inputValue, true);
        }
    }

    /*
     * This is only for when focus is changed from formula field to inline input
     */
    @Override
    public void onCellInputFocus() {
        if (!inlineEditing && !formulaBarWidget.isEditingFormula()) {
            inlineEditing = true;
            cancelDeferredCommit = true;
            if (formulaBarEditing) { // just swap, everything should work
                formulaBarEditing = false;
            } else { // need to make sure the input value is correct
                sheetWidget.startEditingCell(true, false,
                        formulaBarWidget.getFormulaFieldValue());
                formulaBarWidget.startInlineEdit(true);
            }
        }
    }

    @Override
    public void onCellInputCancel() {
        cellEditingDone(cachedCellValue, true);
        formulaBarWidget.revertCellValue();
        sheetWidget.focusSheet();
    }

    @Override
    public void onCellInputEnter(String value, boolean shift) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellRow(),
                sheetWidget.getSelectedCellColumn(), value);
        cellEditingDone(value, true);
        sheetWidget.focusSheet();
        if (shift) {
            selectionHandler.moveSelectionUp(false);
        } else {
            selectionHandler.moveSelectionDown(false);
        }
    }

    @Override
    public void onCellInputTab(String value, boolean shift) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellRow(),
                sheetWidget.getSelectedCellColumn(), value);
        cellEditingDone(value, true);
        sheetWidget.focusSheet();
        if (shift) {
            selectionHandler.moveSelectionLeft(false);
        } else {
            selectionHandler.moveSelectionRight(false);
        }
    }

    @Override
    public void onCellInputValueChange(String value) {
        formulaBarWidget.setCellPlainValue(value);
    }

    @Override
    public void onSheetKeyPress(NativeEvent event, String enteredCharacter) {
        // Here we need to also check for char code 13 (which is code for enter)
        // since for some reason the enter key is reported having both
        // KeyCodes.ENTER and the char code 13, whereas other such non-character
        // keys here have no char codes. Enter key must be detected here to
        // start editing a cell.
        if ((event.getCharCode() == 0
                && event.getKeyCode() != KeyCodes.KEY_SPACE)
                || event.getCharCode() == 13) {
            switch (event.getKeyCode()) {
            case KeyCodes.KEY_BACKSPACE:
            case KeyCodes.KEY_DELETE:
                checkEditableAndNotify();
                if (!cellLocked) {
                    spreadsheetHandler.deleteSelectedCells();
                    formulaBarWidget.setCellPlainValue("");
                }
                break;
            case KeyCodes.KEY_DOWN:
                if (event.getShiftKey()) {
                    selectionHandler.increaseVerticalSelection(true);
                } else {
                    selectionHandler.moveSelectionDown(true);
                }
                break;
            case KeyCodes.KEY_LEFT:
                if (event.getShiftKey()) {
                    selectionHandler.increaseHorizontalSelection(false);
                } else {
                    selectionHandler.moveSelectionLeft(true);
                }
                break;
            case KeyCodes.KEY_TAB:
                if (event.getShiftKey()) {
                    selectionHandler.moveSelectionLeft(isSelectedCellHidden());
                } else {
                    selectionHandler.moveSelectionRight(isSelectedCellHidden());
                }
                break;
            case KeyCodes.KEY_RIGHT:
                if (event.getShiftKey()) {
                    selectionHandler.increaseHorizontalSelection(true);
                } else {
                    selectionHandler.moveSelectionRight(true);
                }
                break;
            case KeyCodes.KEY_UP:
                if (event.getShiftKey()) {
                    selectionHandler.increaseVerticalSelection(false);
                } else {
                    selectionHandler.moveSelectionUp(true);
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
            case KeyCodes.KEY_F2:
            case KeyCodes.KEY_ENTER:
                if (KeyCodes.KEY_ENTER == event.getKeyCode()) {
                    if (isSelectedCellHidden()) {
                        selectionHandler.moveSelectionDown(true);
                        break;
                    } else {
                        if (sheetWidget.getSelectionLeftCol() != sheetWidget
                                .getSelectionRightCol()
                                || sheetWidget
                                        .getSelectionTopRow() != sheetWidget
                                                .getSelectionBottomRow()) {
                            if (event.getShiftKey()) {
                                selectionHandler.moveSelectionUp(false);
                            } else {
                                selectionHandler.moveSelectionDown(false);
                            }
                            break;
                        }
                    }
                }
                checkEditableAndNotify();
                if (!sheetWidget.isSelectedCellCustomized() && !inlineEditing
                        && !cellLocked && !customCellEditorDisplayed) {
                    cachedCellValue = sheetWidget.getSelectedCellLatestValue();
                    formulaBarWidget.cacheFormulaFieldValue();
                    formulaBarEditing = false;
                    inlineEditing = true;
                    sheetWidget.startEditingCell(true, true,
                            formulaBarWidget.getFormulaFieldValue());
                    formulaBarWidget.setInFullFocus(true);
                    formulaBarWidget.startInlineEdit(true);
                }
                break;
            }
        } else {
            if (!isSelectedCellHidden()) {
                checkEditableAndNotify();

                if (!sheetWidget.isSelectedCellCustomized() && !inlineEditing
                        && !cellLocked && !customCellEditorDisplayed) {
                    // cache value and start editing cell as empty
                    inlineEditing = true;
                    cachedCellValue = sheetWidget.getSelectedCellLatestValue();

                    formulaBarWidget.startInlineEdit(true);

                    if (cachedCellValue.endsWith("%")
                            || sheetWidget.isSelectedCellPergentage()) {

                        if (isNumericChar(enteredCharacter)) {
                            enteredCharacter = enteredCharacter + "%";
                        }
                        sheetWidget.startEditingCell(true, true,
                                enteredCharacter);
                    } else {
                        sheetWidget.startEditingCell(true, true,
                                enteredCharacter);
                        formulaBarWidget.cacheFormulaFieldValue();
                    }
                    formulaBarWidget.setCellPlainValue(enteredCharacter);
                }
            }
        }
    }

    private static boolean isNumericChar(String input) {
        Set<String> allowedChars = new HashSet<String>();
        allowedChars.add("0");
        allowedChars.add("1");
        allowedChars.add("2");
        allowedChars.add("3");
        allowedChars.add("4");
        allowedChars.add("5");
        allowedChars.add("6");
        allowedChars.add("7");
        allowedChars.add("8");
        allowedChars.add("9");
        allowedChars.add("-");
        allowedChars.add("+");

        return allowedChars.contains(input);
    }

    private boolean isSelectedCellHidden() {
        return hiddenColumnIndexes.contains(sheetWidget.getSelectedCellColumn())
                || hiddenRowIndexes.contains(sheetWidget.getSelectedCellRow());
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
            SpreadsheetServerRpc rpc = RpcProxy
                    .create(SpreadsheetServerRpc.class, connector);

            rpc.protectedCellWriteAttempted();
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
        if (formulaBarWidget.isEditingFormula()) {
            // TODO commit or ignore value? this ignores. Excel remembers that
            // editor was open. If editing from formula bar, value is stored..
            formulaBarWidget.stopInlineEdit();
            sheetWidget.stopEditingCell(false);
        }

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
    public void onSheetTabSheetFocus() {
        sheetWidget.focusSheet(false);
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
        MergedRegion evenedRegion = MergedRegionUtil
                .findIncreasingSelection(mergedRegionContainer, r1, r2, c1, c2);
        // discard painted area if merged cells don't align
        if (evenedRegion.col1 == c1 && evenedRegion.col2 == c2
                && evenedRegion.row1 == r1 && evenedRegion.row2 == r2) {
            spreadsheetHandler.selectionIncreasePainted(r1, c1, r2, c2);
            startDelayedSendingTimer();
        }
    }

    @Override
    public void onSelectionDecreasePainted(int colEdgeIndex, int rowEdgeIndex) {
        // the selection widget has made sure the decreasing area is not in
        // middle of merged cells.
        spreadsheetHandler.selectionDecreasePainted(rowEdgeIndex, colEdgeIndex);
        startDelayedSendingTimer();
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
            }
        }
    }

    @Override
    public void onFormulaFieldBlur(final String value) {
        // need to do this as deferred because in case the focus was passed to
        // inline input element
        if (formulaBarEditing) {
            doDeferredCellValueCommit(value, false);
        }
    }

    @Override
    public void onFormulaEnter(String value) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellRow(),
                sheetWidget.getSelectedCellColumn(), value);
        cellEditingDone(value, true);
        sheetWidget.focusSheet();
        selectionHandler.moveSelectionDown(false);
    }

    @Override
    public void onFormulaTab(String value, boolean focusSheet) {
        spreadsheetHandler.cellValueEdited(sheetWidget.getSelectedCellRow(),
                sheetWidget.getSelectedCellColumn(), value);
        cellEditingDone(value, focusSheet);
        if (focusSheet) {
            sheetWidget.focusSheet();
            selectionHandler.moveSelectionRight(false);
        }
    }

    @Override
    public void onFormulaEsc() {
        cellEditingDone(cachedCellValue, true);
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

    /**
     * update the sheet display after editing has finished
     *
     * @param focusSheet
     *
     * @param focusSheet
     */
    private void cellEditingDone(String value, boolean focusSheet) {
        inlineEditing = false;
        formulaBarWidget.stopInlineEdit();
        formulaBarEditing = false;
        if (!sheetWidget.isSelectedCellCustomized()) {
            if (value == null) {
                value = "";
            }
            selectedCellIsFormulaType = value.startsWith("=")
                    || value.startsWith("+");
            sheetWidget.stopEditingCell(focusSheet);

            if (!selectedCellIsFormulaType) {
                sheetWidget.updateSelectedCellValue(value);
            }
        }
    }

    /**
     *
     * @param value
     * @param focusSheet
     */
    private void doDeferredCellValueCommit(final String value,
            final boolean focusSheet) {
        cancelDeferredCommit = false;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                if (!cancelDeferredCommit) {
                    spreadsheetHandler.cellValueEdited(
                            sheetWidget.getSelectedCellRow(),
                            sheetWidget.getSelectedCellColumn(), value);
                    cellEditingDone(value, focusSheet);
                }
            }
        });
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

    @Override
    public String createCellAddress(int column, int row) {
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

    public void setColGroupingData(List<GroupingData> data) {
        sheetWidget.setColGroupingData(data);
    }

    public void setRowGroupingData(List<GroupingData> data) {
        sheetWidget.setRowGroupingData(data);
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

    public void setRowIndexToStyleIndex(
            HashMap<Integer, Integer> rowIndexToStyleIndex) {
        if (this.rowIndexToStyleIndex == null) {
            this.rowIndexToStyleIndex = rowIndexToStyleIndex;
        } else {
            this.rowIndexToStyleIndex.clear();
            if (rowIndexToStyleIndex != null) {
                this.rowIndexToStyleIndex.putAll(rowIndexToStyleIndex);
            }
        }
    }

    public void setColumnIndexToStyleIndex(
            HashMap<Integer, Integer> columnIndexToStyleIndex) {
        if (this.columnIndexToStyleIndex == null) {
            this.columnIndexToStyleIndex = columnIndexToStyleIndex;
        } else {
            this.columnIndexToStyleIndex.clear();
            if (columnIndexToStyleIndex != null) {
                this.columnIndexToStyleIndex.putAll(columnIndexToStyleIndex);
            }
        }
    }

    public void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
        if (this.lockedColumnIndexes == null) {
            this.lockedColumnIndexes = lockedColumnIndexes;
        } else {
            this.lockedColumnIndexes.clear();
            if (lockedColumnIndexes != null) {
                this.lockedColumnIndexes.addAll(lockedColumnIndexes);
            }
        }
    }

    public void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
        if (this.lockedRowIndexes == null) {
            this.lockedRowIndexes = lockedRowIndexes;
        } else {
            this.lockedRowIndexes.clear();
            if (lockedRowIndexes != null) {
                this.lockedRowIndexes.addAll(lockedRowIndexes);
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
            if (sheetProtected) {
                addStyleName("protected");
            } else {
                removeStyleName("protected");
            }
            if (loaded) {
                if (sheetProtected) {
                    if (customCellEditorDisplayed) {
                        customCellEditorDisplayed = false;
                        sheetWidget.removeCustomCellEditor();
                    }
                } else { // might need to load the custom editor
                    cellLocked = false;
                    selectionHandler.newSelectedCellSet();
                    if (customCellEditorDisplayed) {
                        // need to update the editor value on client side
                        spreadsheetHandler.cellSelected(
                                sheetWidget.getSelectedCellRow(),
                                sheetWidget.getSelectedCellColumn(), false);
                        startDelayedSendingTimer();
                    }
                }
            }
        }
    }

    @Override
    public boolean isSheetProtected() {
        return sheetProtected;
    }

    @Override
    public boolean isColProtected(int col) {
        return lockedColumnIndexes.contains(col);
    }

    @Override
    public boolean isRowProtected(int row) {
        return lockedRowIndexes.contains(row);
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

    public void setCellComments(HashMap<String, String> cellComments,
            HashMap<String, String> cellCommentAuthors) {
        sheetWidget.setCellComments(cellComments, cellCommentAuthors);
    }

    public void setInvalidFormulaCells(Set<String> invalidFormulaCells) {
        sheetWidget.setInvalidFormulaCells(invalidFormulaCells);
    }

    public void setInvalidFormulaErrorMessage(String invalidFormulaMessage) {
        sheetWidget.setInvalidFormulaMessage(invalidFormulaMessage);
    }

    @Override
    public Map<Integer, String> getCellStyleToCSSStyle() {
        return cellStyleToCSSStyle;
    }

    @Override
    public Map<Integer, Integer> getRowIndexToStyleIndex() {
        return rowIndexToStyleIndex;
    }

    @Override
    public Map<Integer, Integer> getColumnIndexToStyleIndex() {
        return columnIndexToStyleIndex;
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
        if (col > 0 && colW.length >= col) {
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
    public int[] getColWidths() {
        return colW;
    }

    @Override
    public float getDefaultRowHeight() {
        return defRowH;
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
    public int getMaxColumns() {
        return cols;
    }

    @Override
    public int getMaxRows() {
        return rows;
    }

    @Override
    public boolean isColumnHidden(int columnIndex) {
        return (hiddenColumnIndexes == null ? false
                : hiddenColumnIndexes.contains(columnIndex));
    }

    @Override
    public boolean isRowHidden(int rowIndex) {
        return (hiddenRowIndexes == null ? false
                : hiddenRowIndexes.contains(rowIndex));
    }

    @Override
    public boolean canResizeColumn() {
        return (!sheetProtected || !lockFormatColumns) && !touchMode;
    }

    @Override
    public boolean canResizeRow() {
        return (!sheetProtected || !lockFormatRows) && !touchMode;
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

    @Override
    public void onSheetPaste(String text) {
        spreadsheetHandler.onPaste(text);
    }

    @Override
    public void clearSelectedCellsOnCut() {
        spreadsheetHandler.clearSelectedCellsOnCut();
    }

    @Override
    public Map<Integer, String> getConditionalFormattingStyles() {
        return conditionalFormattingStyles;
    }

    public void setConditionalFormattingStyles(HashMap<Integer, String> map) {
        conditionalFormattingStyles.clear();
        if (map != null) {
            conditionalFormattingStyles.putAll(map);
        }
    }

    public void selectCell(String name, int col, int row, String value,
            boolean formula, boolean locked, boolean initialSelection) {
        selectionHandler.selectCell(name, col, row, value, formula, locked,
                initialSelection);
    }

    public void selectCellRange(String name, int selectedCellColumn,
            int selectedCellRow, int firstColumn, int lastColumn, int firstRow,
            int lastRow, boolean scroll) {

        selectionHandler.selectCellRange(name, selectedCellColumn,
                selectedCellRow, firstColumn, lastColumn, firstRow, lastRow,
                scroll);
    }

    public void refreshCellStyles() {
        getSheetWidget().refreshCellStyles();
    }

    @Override
    public boolean isTouchMode() {
        return touchMode;
    }

    public void setTouchMode(boolean touchMode) {
        this.touchMode = touchMode;
    }

    @Override
    public FormulaBarWidget getFormulaBarWidget() {
        return formulaBarWidget;
    }

    public void editCellComment(int col, int row) {
        sheetWidget.editCellComment(col, row);
    }

    @Override
    public void updateCellComment(String text, int col, int row) {
        spreadsheetHandler.updateCellComment(text, col, row);
    }

    @Override
    public void selectAll() {
        sheetWidget.setSelectedCell(1, 1);
        onSelectingCellsWithDrag(cols, rows);
        onFinishedSelectingCellsWithDrag(1, cols, 1, rows);
        updateSelectedCellValues(1, 1);
    }

    @Override
    public void focus() {
        focusSheet();
    }

    public void setCommsTrigger(CommsTrigger commsTrigger) {
        this.commsTrigger = commsTrigger;
    }

    private Timer delayedSending = new Timer() {

        @Override
        public void run() {
            commsTrigger.sendUpdates();
        }
    };

    void startDelayedSendingTimer() {
        delayedSending.schedule(DELAYED_SERVER_REQUEST_DELAY);
    }

    static int getTouchOrMouseClientX(Event event) {
        int scrollLeft = Document.get().getScrollLeft();
        if (WidgetUtil.isTouchEvent(event)) {
            return event.getChangedTouches().get(0).getClientX() + scrollLeft;
        } else {
            return event.getClientX() + scrollLeft;
        }
    }

    static int getTouchOrMouseClientY(Event event) {
        int scrollTop = Document.get().getScrollTop();
        if (WidgetUtil.isTouchEvent(event)) {
            return event.getChangedTouches().get(0).getClientY() + scrollTop;
        } else {
            return event.getClientY() + scrollTop;
        }
    }

    static int getTouchOrMouseClientY(NativeEvent currentGwtEvent) {
        return getTouchOrMouseClientY(Event.as(currentGwtEvent));
    }

    static int getTouchOrMouseClientX(NativeEvent event) {
        return getTouchOrMouseClientX(Event.as(event));
    }

    @Override
    public void setSheetFocused(boolean focused) {
        sheetWidget.setFocused(focused);
    }

    public void setId(String connectorId) {
        sheetWidget.postInit(connectorId);
    }

    @Override
    public String[] getSheetNames() {
        return sheetNames;
    }

    @Override
    public String getActiveSheetName() {
        return sheetNames[activeSheetIndex - 1];
    }

    @Override
    public void setGroupingCollapsed(boolean isCols, int colIndex,
            boolean collapsed) {
        spreadsheetHandler.setGroupingCollapsed(isCols, colIndex, collapsed);
    }

    @Override
    public void levelHeaderClicked(boolean cols, int level) {
        spreadsheetHandler.levelHeaderClicked(cols, level);
    }

    public void setColGroupingMax(int max) {
        sheetWidget.setColGroupingMax(max);
    }

    public void setRowGroupingMax(int max) {
        sheetWidget.setRowGroupingMax(max);
    }

    public void setColGroupingInversed(boolean inversed) {
        sheetWidget.setColGroupingInversed(inversed);
    }

    public void setRowGroupingInversed(boolean inversed) {
        sheetWidget.setRowGroupingInversed(inversed);
    }

    public void setNamedRanges(List<String> namedRanges) {
        formulaBarWidget.setNamedRanges(namedRanges);
    }
}
