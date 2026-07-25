/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.component.spreadsheet.client.js;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.addon.spreadsheet.client.CellData;
import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;

class SpreadsheetJsApiTest {

    private SpreadsheetConnector connector;
    private SpreadsheetClientRpc clientRpc;
    private SpreadsheetServerRpcImpl serverRpc = new SpreadsheetServerRpcImpl();
    private SpreadsheetJsApi api;
    private SpreadsheetState sharedState = new SpreadsheetState();

    class SpreadsheetJsApiHack extends SpreadsheetJsApi {
        public SpreadsheetJsApiHack(SpreadsheetConnector connector) {
            super(null, null, null);
            spreadsheetConnector = connector;
        }

        @Override
        protected SpreadsheetState getState() {
            return sharedState;
        }
    }

    @BeforeEach
    void before() {
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
    void should_updateState_when_setRowBufferSize_isCalled() {
        api.setRowBufferSize(100);
        assertEquals(100, sharedState.rowBufferSize);
    }

    @Test
    void should_updateState_when_setColumnBufferSize_isCalled() {
        api.setColumnBufferSize(100);
        assertEquals(100, sharedState.columnBufferSize);
    }

    @Test
    void should_updateState_when_setRows_isCalled() {
        api.setRows(100);
        assertEquals(100, sharedState.rows);
    }

    @Test
    void should_updateState_when_setCols_isCalled() {
        api.setCols(100);
        assertEquals(100, sharedState.cols);
    }

    @Test
    void should_updateState_when_setColGroupingData_isCalled() {
        api.setColGroupingData("");
        assertEquals(new ArrayList<>(), sharedState.colGroupingData);
    }

    @Test
    void should_updateState_when_setRowGroupingData_isCalled() {
        api.setRowGroupingData("");
        assertEquals(new ArrayList<>(), sharedState.rowGroupingData);
    }

    @Test
    void should_updateState_when_setColGroupingMax_isCalled() {
        api.setColGroupingMax(100);
        assertEquals(100, sharedState.colGroupingMax);
    }

    @Test
    void should_updateState_when_setRowGroupingMax_isCalled() {
        api.setRowGroupingMax(100);
        assertEquals(100, sharedState.rowGroupingMax);
    }

    @Test
    void should_updateState_when_setColGroupingInversed_isCalled() {
        api.setColGroupingInversed(true);
        assertEquals(true, sharedState.colGroupingInversed);
    }

    @Test
    void should_updateState_when_setRowGroupingInversed_isCalled() {
        api.setRowGroupingInversed(true);
        assertEquals(true, sharedState.rowGroupingInversed);
    }

    @Test
    void should_updateState_when_setDefRowH_isCalled() {
        api.setDefRowH(100);
        assertEquals(100f, sharedState.defRowH, 0);
    }

    @Test
    void should_updateState_when_setDefColW_isCalled() {
        api.setDefColW(100);
        assertEquals(100f, sharedState.defColW, 0);
    }

    @Test
    void should_updateState_when_setRowH_isCalled() {
        api.setRowH("[0]");
        assertTrue(Arrays.equals(new float[] { 0 }, sharedState.rowH));
    }

    @Test
    void should_updateState_when_setColW_isCalled() {
        api.setColW("[0]");
        assertArrayEquals(new int[] { 0 }, sharedState.colW);
    }

    @Test
    void should_updateState_when_setReload_isCalled() {
        api.setReload(true);
        assertEquals(true, sharedState.reload);
    }

    @Test
    void should_updateState_when_setSheetIndex_isCalled() {
        api.setSheetIndex(100);
        assertEquals(100, sharedState.sheetIndex);
    }

    @Test
    void should_updateState_when_setSheetNames_isCalled() {
        api.setSheetNames("");
        assertArrayEquals(new String[0], sharedState.sheetNames);
    }

    @Test
    void should_updateState_when_setCellStyleToCSSStyle_isCalled() {
        api.setCellStyleToCSSStyle("{}");
        assertTrue(new HashMap().equals(sharedState.cellStyleToCSSStyle));
    }

    @Test
    void should_updateState_when_setRowIndexToStyleIndex_isCalled() {
        api.setRowIndexToStyleIndex("{}");
        assertTrue(new HashMap().equals(sharedState.rowIndexToStyleIndex));
    }

    @Test
    void should_updateState_when_setColumnIndexToStyleIndex_isCalled() {
        api.setColumnIndexToStyleIndex("{}");
        assertTrue(new HashMap().equals(sharedState.columnIndexToStyleIndex));
    }

    @Test
    void should_updateState_when_setLockedColumnIndexes_isCalled() {
        api.setLockedColumnIndexes("");
        assertTrue(new HashSet().equals(sharedState.lockedColumnIndexes));
    }

    @Test
    void should_updateState_when_setLockedRowIndexes_isCalled() {
        api.setLockedRowIndexes("");
        assertTrue(new HashSet().equals(sharedState.lockedRowIndexes));
    }

    @Test
    void should_updateState_when_setShiftedCellBorderStyles_isCalled() {
        ArrayList<String> value = new ArrayList<>();
        api.setShiftedCellBorderStyles("");
        assertEquals(value, sharedState.shiftedCellBorderStyles);
    }

    @Test
    void should_updateState_when_setConditionalFormattingStyles_isCalled() {
        HashMap<Integer, String> value = new HashMap<>();
        api.setConditionalFormattingStyles("{}");
        assertEquals(value, sharedState.conditionalFormattingStyles);
    }

    @Test
    void should_updateState_when_setHiddenColumnIndexes_isCalled() {
        ArrayList<Integer> value = new ArrayList<>();
        api.setHiddenColumnIndexes("");
        assertEquals(value, sharedState.hiddenColumnIndexes);
    }

    @Test
    void should_updateState_when_setHiddenRowIndexes_isCalled() {
        ArrayList<Integer> value = new ArrayList<>();
        api.setHiddenRowIndexes("");
        assertEquals(value, sharedState.hiddenRowIndexes);
    }

    @Test
    void should_updateState_when_setVerticalScrollPositions_isCalled() {
        api.setVerticalScrollPositions("");
        assertArrayEquals(new int[0], sharedState.verticalScrollPositions);
    }

    @Test
    void should_updateState_when_setHorizontalScrollPositions_isCalled() {
        api.setHorizontalScrollPositions("");
        assertArrayEquals(new int[0], sharedState.horizontalScrollPositions);
    }

    @Test
    void should_updateState_when_setSheetProtected_isCalled() {
        api.setSheetProtected(true);
        assertEquals(true, sharedState.sheetProtected);
    }

    @Test
    void should_updateState_when_setWorkbookProtected_isCalled() {
        api.setWorkbookProtected(true);
        assertEquals(true, sharedState.workbookProtected);
    }

    @Test
    void should_updateState_when_setCellKeysToEditorIdMap_isCalled() {
        api.setCellKeysToEditorIdMap("{}");
        assertTrue(new HashMap().equals(sharedState.cellKeysToEditorIdMap));
    }

    @Test
    void should_updateState_when_setComponentIDtoCellKeysMap_isCalled() {
        api.setComponentIDtoCellKeysMap("{}");
        assertTrue(new HashMap().equals(sharedState.componentIDtoCellKeysMap));
    }

    @Test
    void should_updateState_when_setHyperlinksTooltips_isCalled() {
        api.setHyperlinksTooltips("{}");
        assertTrue(new HashMap().equals(sharedState.hyperlinksTooltips));
    }

    @Test
    void should_updateState_when_setCellComments_isCalled() {
        api.setCellComments("{}");
        assertTrue(new HashMap().equals(sharedState.cellComments));
    }

    @Test
    void should_updateState_when_setCellCommentAuthors_isCalled() {
        api.setCellCommentAuthors("{}");
        assertTrue(new HashMap().equals(sharedState.cellCommentAuthors));
    }

    @Test
    void should_updateState_when_setVisibleCellComments_isCalled() {
        api.setVisibleCellComments("");
        assertTrue(new ArrayList<>().equals(sharedState.visibleCellComments));
    }

    @Test
    void should_updateState_when_setInvalidFormulaCells_isCalled() {
        api.setInvalidFormulaCells("");
        assertTrue(new HashSet().equals(sharedState.invalidFormulaCells));
    }

    @Test
    void should_updateState_when_setHasActions_isCalled() {
        api.setHasActions(true);
        assertEquals(true, sharedState.hasActions);
    }

    @Test
    void should_updateState_when_setOverlays_isCalled() {
        api.setOverlays("{}");
        assertTrue(new HashMap().equals(sharedState.overlays));
    }

    @Test
    void should_updateState_when_setMergedRegions_isCalled() {
        api.setMergedRegions("");
        assertTrue(new ArrayList().equals(sharedState.mergedRegions));
    }

    @Test
    void should_updateState_when_setDisplayGridlines_isCalled() {
        api.setDisplayGridlines(true);
        assertEquals(true, sharedState.displayGridlines);
    }

    @Test
    void should_updateState_when_setDisplayRowColHeadings_isCalled() {
        api.setDisplayRowColHeadings(true);
        assertEquals(true, sharedState.displayRowColHeadings);
    }

    @Test
    void should_updateState_when_setVerticalSplitPosition_isCalled() {
        api.setVerticalSplitPosition(100);
        assertEquals(100, sharedState.verticalSplitPosition);
    }

    @Test
    void should_updateState_when_setHorizontalSplitPosition_isCalled() {
        api.setHorizontalSplitPosition(100);
        assertEquals(100, sharedState.horizontalSplitPosition);
    }

    @Test
    void should_updateState_when_setInfoLabelValue_isCalled() {
        api.setInfoLabelValue("a");
        assertEquals("a", sharedState.infoLabelValue);
    }

    @Test
    void should_updateState_when_setWorkbookChangeToggle_isCalled() {
        api.setWorkbookChangeToggle(true);
        assertEquals(true, sharedState.workbookChangeToggle);
    }

    @Test
    void should_updateState_when_setInvalidFormulaErrorMessage_isCalled() {
        api.setInvalidFormulaErrorMessage("a");
        assertEquals("a", sharedState.invalidFormulaErrorMessage);
    }

    @Test
    void should_updateState_when_setLockFormatColumns_isCalled() {
        api.setLockFormatColumns(true);
        assertEquals(true, sharedState.lockFormatColumns);
    }

    @Test
    void should_updateState_when_setLockFormatRows_isCalled() {
        api.setLockFormatRows(true);
        assertEquals(true, sharedState.lockFormatRows);
    }

    @Test
    void should_updateState_when_setNamedRanges_isCalled() {
        api.setNamedRanges("");
        assertTrue(new ArrayList().equals(sharedState.namedRanges));
    }

    @Test
    void should_updateState_when_setShowCustomEditorOnFocus_isCalled() {
        api.setShowCustomEditorOnFocus(true);
        assertEquals(true, sharedState.showCustomEditorOnFocus);
    }

    // CLIENT RPC METHODS

    @Test
    void should_callClientRpc_when_updateBottomRightCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateBottomRightCellValues("");
        verify(clientRpc, times(1)).updateBottomRightCellValues(value);
    }

    @Test
    void should_callClientRpc_when_updateTopLeftCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateTopLeftCellValues("");
        verify(clientRpc, times(1)).updateTopLeftCellValues(value);
    }

    @Test
    void should_callClientRpc_when_updateTopRightCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateTopRightCellValues("");
        verify(clientRpc, times(1)).updateTopRightCellValues(value);
    }

    @Test
    void should_callClientRpc_when_updateBottomLeftCellValues_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.updateBottomLeftCellValues("");
        verify(clientRpc, times(1)).updateBottomLeftCellValues(value);
    }

    @Test
    void should_callClientRpc_when_updateFormulaBar_isCalled() {
        api.updateFormulaBar("a", 1, 2);
        verify(clientRpc, times(1)).updateFormulaBar("a", 1, 2);
    }

    @Test
    void should_callClientRpc_when_invalidCellAddress_isCalled() {
        api.invalidCellAddress();
        verify(clientRpc, times(1)).invalidCellAddress();
    }

    @Test
    void should_callClientRpc_when_showSelectedCell_isCalled() {
        api.showSelectedCell("a", 1, 2, "b", true, true, true);
        verify(clientRpc, times(1)).showSelectedCell("a", 1, 2, "b", true, true,
                true);
    }

    @Test
    void should_callClientRpc_when_showActions_isCalled() {
        ArrayList<SpreadsheetActionDetails> value = new ArrayList<>();
        api.showActions("");
        verify(clientRpc, times(1)).showActions(value);
    }

    @Test
    void should_callClientRpc_when_setSelectedCellAndRange_isCalled() {
        api.setSelectedCellAndRange("a", 1, 2, 3, 4, 5, 6, true);
        verify(clientRpc, times(1)).setSelectedCellAndRange("a", 1, 2, 3, 4, 5,
                6, true);
    }

    @Test
    void should_callClientRpc_when_cellsUpdated_isCalled() {
        ArrayList<CellData> value = new ArrayList<>();
        api.cellsUpdated("");
        verify(clientRpc, times(1)).cellsUpdated(value);
    }

    @Test
    void should_callClientRpc_when_refreshCellStyles_isCalled() {
        api.refreshCellStyles();
        verify(clientRpc, times(1)).refreshCellStyles();
    }

    @Test
    void should_callClientRpc_when_editCellComment_isCalled() {
        api.editCellComment(1, 2);
        verify(clientRpc, times(1)).editCellComment(1, 2);
    }

}
