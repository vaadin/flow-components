package com.vaadin.component.spreadsheet.client.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.addon.spreadsheet.client.CellData;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.OverlayInfo;
import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.addon.spreadsheet.shared.GroupingData;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;
import com.vaadin.component.spreadsheet.client.js.SpreadsheetJsApi;
import com.vaadin.component.spreadsheet.client.js.SpreadsheetServerRpcImpl;
import com.vaadin.component.spreadsheet.client.js.callbacks.ActionOnColumnHeaderCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ActionOnCurrentSelectionCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.CellAddedToSelectionAndSelectedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.CellRangePaintedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.CellRangeSelectedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.CellSelectedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.CellValueEditedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.CellsAddedToRangeSelectionCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ClearSelectedCellsOnCutCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ColumnAddedToSelectionCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ColumnHeaderContextMenuOpenCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ColumnResizedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ColumnSelectedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ContextMenuOpenOnSelectionCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.DeleteSelectedCellsCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.GroupingCollapsedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.LevelHeaderClickedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.LinkCellClickedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnColumnAutofitCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnConnectorInitCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnPasteCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnRedoCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnRowAutofitCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnSheetScrollCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.OnUndoCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.ProtectedCellWriteAttemptedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.RowAddedToRangeSelectionCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.RowHeaderContextMenuOpenCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.RowSelectedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.RowsResizedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SelectionDecreasePaintedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SelectionIncreasePaintedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SetCellStyleWidthRatiosCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SheetAddressChangedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SheetCreatedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SheetRenamedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.SheetSelectedCallback;
import com.vaadin.component.spreadsheet.client.js.callbacks.UpdateCellCommentCallback;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpreadsheetJsApiTest {

    private SpreadsheetConnector connector;
    private SpreadsheetClientRpc clientRpc;
    private SpreadsheetServerRpcImpl serverRpc = new SpreadsheetServerRpcImpl();
    private SpreadsheetJsApi api;
    private SpreadsheetState sharedState = new SpreadsheetState();

    class SpreadsheetJsApiHack extends SpreadsheetJsApi {
        public SpreadsheetJsApiHack(SpreadsheetConnector connector) {
            super(null);
            spreadsheetConnector = connector;
        }

        @Override
        protected SpreadsheetState getState() {
            return sharedState;
        }
    }

    @Before
    public void before() {
        connector = Mockito.mock(SpreadsheetConnector.class);
        clientRpc = Mockito.mock(SpreadsheetClientRpc.class);
        String rpcInterfaceId = SpreadsheetClientRpc.class.getName()
                .replaceAll("\\$", ".");
        when(connector.getRpcImplementations(rpcInterfaceId))
                .thenReturn(Lists.newArrayList(clientRpc));
        when(connector.getProtectedRpcProxy(SpreadsheetServerRpc.class))
                .thenReturn(serverRpc);
        api = new SpreadsheetJsApiHack(connector);
    }

    @Test
    public void should_updateState_when_setRowBufferSize_isCalled() {
        api.setRowBufferSize(100);
        assertEquals(100, sharedState.rowBufferSize);
    }

    @Test
    public void should_updateState_when_setColumnBufferSize_isCalled() {
        api.setColumnBufferSize(100);
        assertEquals(100, sharedState.columnBufferSize);
    }

    @Test
    public void should_updateState_when_setRows_isCalled() {
        api.setRows(100);
        assertEquals(100, sharedState.rows);
    }

    @Test
    public void should_updateState_when_setCols_isCalled() {
        api.setCols(100);
        assertEquals(100, sharedState.cols);
    }

    @Test
    public void should_updateState_when_setColGroupingData_isCalled() {
        api.setColGroupingData("");
        assertEquals(new ArrayList<>(), sharedState.colGroupingData);
    }

    @Test
    public void should_updateState_when_setRowGroupingData_isCalled() {
        api.setRowGroupingData("");
        assertEquals(new ArrayList<>(), sharedState.rowGroupingData);
    }

    @Test
    public void should_updateState_when_setColGroupingMax_isCalled() {
        api.setColGroupingMax(100);
        assertEquals(100, sharedState.colGroupingMax);
    }

    @Test
    public void should_updateState_when_setRowGroupingMax_isCalled() {
        api.setRowGroupingMax(100);
        assertEquals(100, sharedState.rowGroupingMax);
    }

    @Test
    public void should_updateState_when_setColGroupingInversed_isCalled() {
        api.setColGroupingInversed(true);
        assertEquals(true, sharedState.colGroupingInversed);
    }

    @Test
    public void should_updateState_when_setRowGroupingInversed_isCalled() {
        api.setRowGroupingInversed(true);
        assertEquals(true, sharedState.rowGroupingInversed);
    }

    @Test
    public void should_updateState_when_setDefRowH_isCalled() {
        api.setDefRowH(100);
        assertEquals(100f, sharedState.defRowH, 0);
    }

    @Test
    public void should_updateState_when_setDefColW_isCalled() {
        api.setDefColW(100);
        assertEquals(100f, sharedState.defColW, 0);
    }

    @Test
    public void should_updateState_when_setRowH_isCalled() {
        api.setRowH("[0]");
        assertTrue(Arrays.equals(new float[] { 0 }, sharedState.rowH));
    }

    @Test
    public void should_updateState_when_setColW_isCalled() {
        api.setColW("[0]");
        assertArrayEquals(new int[] { 0 }, sharedState.colW);
    }

    @Test
    public void should_updateState_when_setReload_isCalled() {
        api.setReload(true);
        assertEquals(true, sharedState.reload);
    }

    @Test
    public void should_updateState_when_setSheetIndex_isCalled() {
        api.setSheetIndex(100);
        assertEquals(100, sharedState.sheetIndex);
    }

    @Test
    public void should_updateState_when_setSheetNames_isCalled() {
        api.setSheetNames("");
        assertArrayEquals(new String[0], sharedState.sheetNames);
    }

    @Test
    public void should_updateState_when_setCellStyleToCSSStyle_isCalled() {
        api.setCellStyleToCSSStyle("{}");
        assertTrue(new HashMap().equals(sharedState.cellStyleToCSSStyle));
    }

    @Test
    public void should_updateState_when_setRowIndexToStyleIndex_isCalled() {
        api.setRowIndexToStyleIndex("{}");
        assertTrue(new HashMap().equals(sharedState.rowIndexToStyleIndex));
    }

    @Test
    public void should_updateState_when_setColumnIndexToStyleIndex_isCalled() {
        api.setColumnIndexToStyleIndex("{}");
        assertTrue(new HashMap().equals(sharedState.columnIndexToStyleIndex));
    }

    @Test
    public void should_updateState_when_setLockedColumnIndexes_isCalled() {
        api.setLockedColumnIndexes("");
        assertTrue(new HashSet().equals(sharedState.lockedColumnIndexes));
    }

    @Test
    public void should_updateState_when_setLockedRowIndexes_isCalled() {
        api.setLockedRowIndexes("");
        assertTrue(new HashSet().equals(sharedState.lockedRowIndexes));
    }

    @Test
    public void should_updateState_when_setShiftedCellBorderStyles_isCalled() {
        ArrayList<String> value = new ArrayList<>();
        api.setShiftedCellBorderStyles("");
        assertEquals(value, sharedState.shiftedCellBorderStyles);
    }

    @Test
    public void should_updateState_when_setConditionalFormattingStyles_isCalled() {
        HashMap<Integer, String> value = new HashMap<>();
        api.setConditionalFormattingStyles("{}");
        assertEquals(value, sharedState.conditionalFormattingStyles);
    }

    @Test
    public void should_updateState_when_setHiddenColumnIndexes_isCalled() {
        ArrayList<Integer> value = new ArrayList<>();
        api.setHiddenColumnIndexes("");
        assertEquals(value, sharedState.hiddenColumnIndexes);
    }

    @Test
    public void should_updateState_when_setHiddenRowIndexes_isCalled() {
        ArrayList<Integer> value = new ArrayList<>();
        api.setHiddenRowIndexes("");
        assertEquals(value, sharedState.hiddenRowIndexes);
    }

    @Test
    public void should_updateState_when_setVerticalScrollPositions_isCalled() {
        api.setVerticalScrollPositions("");
        assertArrayEquals(new int[0], sharedState.verticalScrollPositions);
    }

    @Test
    public void should_updateState_when_setHorizontalScrollPositions_isCalled() {
        api.setHorizontalScrollPositions("");
        assertArrayEquals(new int[0], sharedState.horizontalScrollPositions);
    }

    @Test
    public void should_updateState_when_setSheetProtected_isCalled() {
        api.setSheetProtected(true);
        assertEquals(true, sharedState.sheetProtected);
    }

    @Test
    public void should_updateState_when_setWorkbookProtected_isCalled() {
        api.setWorkbookProtected(true);
        assertEquals(true, sharedState.workbookProtected);
    }

    @Test
    public void should_updateState_when_setCellKeysToEditorIdMap_isCalled() {
        api.setCellKeysToEditorIdMap("{}");
        assertTrue(new HashMap().equals(sharedState.cellKeysToEditorIdMap));
    }

    @Test
    public void should_updateState_when_setComponentIDtoCellKeysMap_isCalled() {
        api.setComponentIDtoCellKeysMap("{}");
        assertTrue(new HashMap().equals(sharedState.componentIDtoCellKeysMap));
    }

    @Test
    public void should_updateState_when_setHyperlinksTooltips_isCalled() {
        api.setHyperlinksTooltips("{}");
        assertTrue(new HashMap().equals(sharedState.hyperlinksTooltips));
    }

    @Test
    public void should_updateState_when_setCellComments_isCalled() {
        api.setCellComments("{}");
        assertTrue(new HashMap().equals(sharedState.cellComments));
    }

    @Test
    public void should_updateState_when_setCellCommentAuthors_isCalled() {
        api.setCellCommentAuthors("{}");
        assertTrue(new HashMap().equals(sharedState.cellCommentAuthors));
    }

    @Test
    public void should_updateState_when_setVisibleCellComments_isCalled() {
        api.setVisibleCellComments("");
        assertTrue(new ArrayList<>().equals(sharedState.visibleCellComments));
    }

    @Test
    public void should_updateState_when_setInvalidFormulaCells_isCalled() {
        api.setInvalidFormulaCells("");
        assertTrue(new HashSet().equals(sharedState.invalidFormulaCells));
    }

    @Test
    public void should_updateState_when_setHasActions_isCalled() {
        api.setHasActions(true);
        assertEquals(true, sharedState.hasActions);
    }

    @Test
    public void should_updateState_when_setOverlays_isCalled() {
        api.setOverlays("{}");
        assertTrue(new HashMap().equals(sharedState.overlays));
    }

    @Test
    public void should_updateState_when_setMergedRegions_isCalled() {
        api.setMergedRegions("");
        assertTrue(new ArrayList().equals(sharedState.mergedRegions));
    }

    @Test
    public void should_updateState_when_setDisplayGridlines_isCalled() {
        api.setDisplayGridlines(true);
        assertEquals(true, sharedState.displayGridlines);
    }

    @Test
    public void should_updateState_when_setDisplayRowColHeadings_isCalled() {
        api.setDisplayRowColHeadings(true);
        assertEquals(true, sharedState.displayRowColHeadings);
    }

    @Test
    public void should_updateState_when_setVerticalSplitPosition_isCalled() {
        api.setVerticalSplitPosition(100);
        assertEquals(100, sharedState.verticalSplitPosition);
    }

    @Test
    public void should_updateState_when_setHorizontalSplitPosition_isCalled() {
        api.setHorizontalSplitPosition(100);
        assertEquals(100, sharedState.horizontalSplitPosition);
    }

    @Test
    public void should_updateState_when_setInfoLabelValue_isCalled() {
        api.setInfoLabelValue("a");
        assertEquals("a", sharedState.infoLabelValue);
    }

    @Test
    public void should_updateState_when_setWorkbookChangeToggle_isCalled() {
        api.setWorkbookChangeToggle(true);
        assertEquals(true, sharedState.workbookChangeToggle);
    }

    @Test
    public void should_updateState_when_setInvalidFormulaErrorMessage_isCalled() {
        api.setInvalidFormulaErrorMessage("a");
        assertEquals("a", sharedState.invalidFormulaErrorMessage);
    }

    @Test
    public void should_updateState_when_setLockFormatColumns_isCalled() {
        api.setLockFormatColumns(true);
        assertEquals(true, sharedState.lockFormatColumns);
    }

    @Test
    public void should_updateState_when_setLockFormatRows_isCalled() {
        api.setLockFormatRows(true);
        assertEquals(true, sharedState.lockFormatRows);
    }

    @Test
    public void should_updateState_when_setNamedRanges_isCalled() {
        api.setNamedRanges("");
        assertTrue(new ArrayList().equals(sharedState.namedRanges));
    }

    // CLIENT RPC METHODS

    @Test
    public void should_callClientRpc_when_updateBottomRightCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateBottomRightCellValues("");
        verify(clientRpc, times(1)).updateBottomRightCellValues(value);
    }

    @Test
    public void should_callClientRpc_when_updateTopLeftCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateTopLeftCellValues("");
        verify(clientRpc, times(1)).updateTopLeftCellValues(value);
    }

    @Test
    public void should_callClientRpc_when_updateTopRightCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateTopRightCellValues("");
        verify(clientRpc, times(1)).updateTopRightCellValues(value);
    }

    @Test
    public void should_callClientRpc_when_updateBottomLeftCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateBottomLeftCellValues("");
        verify(clientRpc, times(1)).updateBottomLeftCellValues(value);
    }

    @Test
    public void should_callClientRpc_when_updateFormulaBar_isCalled() {
        api.updateFormulaBar("a", 1, 2);
        verify(clientRpc, times(1)).updateFormulaBar("a", 1, 2);
    }

    @Test
    public void should_callClientRpc_when_invalidCellAddress_isCalled() {
        api.invalidCellAddress();
        verify(clientRpc, times(1)).invalidCellAddress();
    }

    @Test
    public void should_callClientRpc_when_showSelectedCell_isCalled() {
        api.showSelectedCell("a", 1, 2, "b", true, true, true);
        verify(clientRpc, times(1)).showSelectedCell("a", 1, 2, "b", true, true,
                true);
    }

    @Test
    public void should_callClientRpc_when_showActions_isCalled() {
        ArrayList<SpreadsheetActionDetails> value = new ArrayList<>();
        api.showActions("");
        verify(clientRpc, times(1)).showActions(value);
    }

    @Test
    public void should_callClientRpc_when_setSelectedCellAndRange_isCalled() {
        api.setSelectedCellAndRange("a", 1, 2, 3, 4, 5, 6, true);
        verify(clientRpc, times(1)).setSelectedCellAndRange("a", 1, 2, 3, 4, 5,
                6, true);
    }

    @Test
    public void should_callClientRpc_when_cellsUpdated_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.cellsUpdated("");
        verify(clientRpc, times(1)).cellsUpdated(value);
    }

    @Test
    public void should_callClientRpc_when_refreshCellStyles_isCalled() {
        api.refreshCellStyles();
        verify(clientRpc, times(1)).refreshCellStyles();
    }

    @Test
    public void should_callClientRpc_when_editCellComment_isCalled() {
        api.editCellComment(1, 2);
        verify(clientRpc, times(1)).editCellComment(1, 2);
    }

    // SERVER RPC METHOD CALLBACKS
    /*
     * @Test public void
     * should_callServerRpcCallback_when_groupingCollapsed_isCalled() {
     * GroupingCollapsedCallback callback =
     * mock(GroupingCollapsedCallback.class);
     * api.setGroupingCollapsedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .setGroupingCollapsed(true, 1, true); verify(callback,
     * times(1)).apply(true, 1, true); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_levelHeaderClicked_isCalled() {
     * LevelHeaderClickedCallback callback =
     * mock(LevelHeaderClickedCallback.class);
     * api.setLevelHeaderClickedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .levelHeaderClicked(true, 2); verify(callback, times(1)).apply(true, 2);
     * }
     *
     * @Test public void
     * should_callServerRpcCallback_when_onSheetScroll_isCalled() {
     * OnSheetScrollCallback callback = mock(OnSheetScrollCallback.class);
     * api.setOnSheetScrollCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onSheetScroll(1, 2, 3, 4); verify(callback, times(1)).apply(1, 2, 3, 4);
     * }
     *
     * @Test public void
     * should_callServerRpcCallback_when_sheetAddressChanged_isCalled() {
     * SheetAddressChangedCallback callback =
     * mock(SheetAddressChangedCallback.class);
     * api.setSheetAddressChangedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .sheetAddressChanged("a"); verify(callback, times(1)).apply("a"); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_cellSelected_isCalled() {
     * CellSelectedCallback callback = mock(CellSelectedCallback.class);
     * api.setCellSelectedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .cellSelected(1, 2, true); verify(callback, times(1)).apply(1, 2, true);
     * }
     *
     * @Test public void
     * should_callServerRpcCallback_when_cellRangeSelected_isCalled() {
     * CellRangeSelectedCallback callback =
     * mock(CellRangeSelectedCallback.class);
     * api.setCellRangeSelectedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .cellRangeSelected(1, 2, 3, 4); verify(callback, times(1)).apply(1, 2, 3,
     * 4); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_cellAddedToSelectionAndSelected_isCalled
     * () { CellAddedToSelectionAndSelectedCallback callback =
     * mock(CellAddedToSelectionAndSelectedCallback.class);
     * api.setCellAddedToSelectionAndSelectedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .cellAddedToSelectionAndSelected(1, 2); verify(callback,
     * times(1)).apply(1, 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_cellsAddedToRangeSelection_isCalled() {
     * CellsAddedToRangeSelectionCallback callback =
     * mock(CellsAddedToRangeSelectionCallback.class);
     * api.setCellsAddedToRangeSelectionCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .cellsAddedToRangeSelection(1, 2, 3, 4); verify(callback,
     * times(1)).apply(1, 2, 3, 4); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_rowSelected_isCalled() {
     * RowSelectedCallback callback = mock(RowSelectedCallback.class);
     * api.setRowSelectedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .rowSelected(1, 2); verify(callback, times(1)).apply(1, 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_rowAddedToRangeSelection_isCalled() {
     * RowAddedToRangeSelectionCallback callback =
     * mock(RowAddedToRangeSelectionCallback.class);
     * api.setRowAddedToRangeSelectionCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .rowAddedToRangeSelection(1, 2); verify(callback, times(1)).apply(1, 2);
     * }
     *
     * @Test public void
     * should_callServerRpcCallback_when_columnSelected_isCalled() {
     * ColumnSelectedCallback callback = mock(ColumnSelectedCallback.class);
     * api.setColumnSelectedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .columnSelected(1, 2); verify(callback, times(1)).apply(1, 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_columnAddedToSelection_isCalled() {
     * ColumnAddedToSelectionCallback callback =
     * mock(ColumnAddedToSelectionCallback.class);
     * api.setColumnAddedToSelectionCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .columnAddedToSelection(1, 2); verify(callback, times(1)).apply(1, 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_selectionIncreasePainted_isCalled() {
     * SelectionIncreasePaintedCallback callback =
     * mock(SelectionIncreasePaintedCallback.class);
     * api.setSelectionIncreasePaintedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .selectionIncreasePainted(1, 2, 3, 4); verify(callback,
     * times(1)).apply(1, 2, 3, 4); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_selectionDecreasePainted_isCalled() {
     * SelectionDecreasePaintedCallback callback =
     * mock(SelectionDecreasePaintedCallback.class);
     * api.setSelectionDecreasePaintedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .selectionDecreasePainted(1, 2); verify(callback, times(1)).apply(1, 2);
     * }
     *
     * @Test public void
     * should_callServerRpcCallback_when_cellValueEdited_isCalled() {
     * CellValueEditedCallback callback = mock(CellValueEditedCallback.class);
     * api.setCellValueEditedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .cellValueEdited(1, 2, "a"); verify(callback, times(1)).apply(1, 2, "a");
     * }
     *
     * @Test public void
     * should_callServerRpcCallback_when_sheetSelected_isCalled() {
     * SheetSelectedCallback callback = mock(SheetSelectedCallback.class);
     * api.setSheetSelectedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .sheetSelected(1, 2, 3); verify(callback, times(1)).apply(1, 2, 3); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_sheetRenamed_isCalled() {
     * SheetRenamedCallback callback = mock(SheetRenamedCallback.class);
     * api.setSheetRenamedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .sheetRenamed(1, "a"); verify(callback, times(1)).apply(1, "a"); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_sheetCreated_isCalled() {
     * SheetCreatedCallback callback = mock(SheetCreatedCallback.class);
     * api.setSheetCreatedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .sheetCreated(1, 2); verify(callback, times(1)).apply(1, 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_cellRangePainted_isCalled() {
     * CellRangePaintedCallback callback = mock(CellRangePaintedCallback.class);
     * api.setCellRangePaintedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .cellRangePainted(1, 2, 3, 4, 5, 6); verify(callback, times(1)).apply(1,
     * 2, 3, 4, 5, 6); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_deleteSelectedCells_isCalled() {
     * DeleteSelectedCellsCallback callback =
     * mock(DeleteSelectedCellsCallback.class);
     * api.setDeleteSelectedCellsCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .deleteSelectedCells(); verify(callback, times(1)).apply(); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_linkCellClicked_isCalled() {
     * LinkCellClickedCallback callback = mock(LinkCellClickedCallback.class);
     * api.setLinkCellClickedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .linkCellClicked(1, 2); verify(callback, times(1)).apply(1, 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_rowsResized_isCalled() {
     * RowsResizedCallback callback = mock(RowsResizedCallback.class);
     * api.setRowsResizedCallback(callback); Map<Integer, Float> map = new
     * HashMap<>();
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .rowsResized(map, 1, 2, 3, 4); verify(callback, times(1)).apply(map, 1,
     * 2, 3, 4); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_columnResized_isCalled() {
     * ColumnResizedCallback callback = mock(ColumnResizedCallback.class);
     * api.setColumnResizedCallback(callback); Map<Integer, Integer> map = new
     * HashMap<>();
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .columnResized(map, 1, 2, 3, 4); verify(callback, times(1)).apply(map, 1,
     * 2, 3, 4); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_onRowAutofit_isCalled() {
     * OnRowAutofitCallback callback = mock(OnRowAutofitCallback.class);
     * api.setOnRowAutofitCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onRowAutofit(1); verify(callback, times(1)).apply(1); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_onColumnAutofit_isCalled() {
     * OnColumnAutofitCallback callback = mock(OnColumnAutofitCallback.class);
     * api.setOnColumnAutofitCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onColumnAutofit(1); verify(callback, times(1)).apply(1); }
     *
     * @Test public void should_callServerRpcCallback_when_onUndo_isCalled() {
     * OnUndoCallback callback = mock(OnUndoCallback.class);
     * api.setOnUndoCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onUndo(); verify(callback, times(1)).apply(); }
     *
     * @Test public void should_callServerRpcCallback_when_onRedo_isCalled() {
     * OnRedoCallback callback = mock(OnRedoCallback.class);
     * api.setOnRedoCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onRedo(); verify(callback, times(1)).apply(); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_setCellStyleWidthRatios_isCalled() {
     * SetCellStyleWidthRatiosCallback callback =
     * mock(SetCellStyleWidthRatiosCallback.class);
     * api.setSetCellStyleWidthRatiosCallback(callback); HashMap<Integer, Float>
     * map = new HashMap<>();
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .setCellStyleWidthRatios(map); verify(callback, times(1)).apply(map); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_protectedCellWriteAttempted_isCalled()
     * { ProtectedCellWriteAttemptedCallback callback =
     * mock(ProtectedCellWriteAttemptedCallback.class);
     * api.setProtectedCellWriteAttemptedCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .protectedCellWriteAttempted(); verify(callback, times(1)).apply(); }
     *
     * @Test public void should_callServerRpcCallback_when_onPaste_isCalled() {
     * OnPasteCallback callback = mock(OnPasteCallback.class);
     * api.setOnPasteCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onPaste("a"); verify(callback, times(1)).apply("a"); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_clearSelectedCellsOnCut_isCalled() {
     * ClearSelectedCellsOnCutCallback callback =
     * mock(ClearSelectedCellsOnCutCallback.class);
     * api.setClearSelectedCellsOnCutCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .clearSelectedCellsOnCut(); verify(callback, times(1)).apply(); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_updateCellComment_isCalled() {
     * UpdateCellCommentCallback callback =
     * mock(UpdateCellCommentCallback.class);
     * api.setUpdateCellCommentCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .updateCellComment("a", 1, 2); verify(callback, times(1)).apply("a", 1,
     * 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_onConnectorInit_isCalled() {
     * OnConnectorInitCallback callback = mock(OnConnectorInitCallback.class);
     * api.setOnConnectorInitCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .onConnectorInit(); verify(callback, times(1)).apply(); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_contextMenuOpenOnSelection_isCalled() {
     * ContextMenuOpenOnSelectionCallback callback =
     * mock(ContextMenuOpenOnSelectionCallback.class);
     * api.setContextMenuOpenOnSelectionCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .contextMenuOpenOnSelection(1, 2); verify(callback, times(1)).apply(1,
     * 2); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_actionOnCurrentSelection_isCalled() {
     * ActionOnCurrentSelectionCallback callback =
     * mock(ActionOnCurrentSelectionCallback.class);
     * api.setActionOnCurrentSelectionCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .actionOnCurrentSelection("a"); verify(callback, times(1)).apply("a"); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_rowHeaderContextMenuOpen_isCalled() {
     * RowHeaderContextMenuOpenCallback callback =
     * mock(RowHeaderContextMenuOpenCallback.class);
     * api.setRowHeaderContextMenuOpenCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .rowHeaderContextMenuOpen(1); verify(callback, times(1)).apply(1); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_actionOnRowHeader_isCalled() {
     * RowHeaderContextMenuOpenCallback callback =
     * mock(RowHeaderContextMenuOpenCallback.class);
     * api.setRowHeaderContextMenuOpenCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .rowHeaderContextMenuOpen(1); verify(callback, times(1)).apply(1); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_columnHeaderContextMenuOpen_isCalled()
     * { ColumnHeaderContextMenuOpenCallback callback =
     * mock(ColumnHeaderContextMenuOpenCallback.class);
     * api.setColumnHeaderContextMenuOpenCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .columnHeaderContextMenuOpen(1); verify(callback, times(1)).apply(1); }
     *
     * @Test public void
     * should_callServerRpcCallback_when_actionOnColumnHeader_isCalled() {
     * ActionOnColumnHeaderCallback callback =
     * mock(ActionOnColumnHeaderCallback.class);
     * api.setActionOnColumnHeaderCallback(callback);
     * api.spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class)
     * .actionOnColumnHeader("a"); verify(callback, times(1)).apply("a"); }
     */

}
