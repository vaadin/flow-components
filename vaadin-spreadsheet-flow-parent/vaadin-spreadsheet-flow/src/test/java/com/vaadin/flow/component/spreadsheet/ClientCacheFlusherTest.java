/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;

/**
 * Sending the marked and removed cells to the client with
 * {@link ClientCacheFlusher}.
 *
 * <p>
 * Pins: empty-sheet no-op, one-marked-plain-cell push, formula-with-stale-cache
 * eager re-push, in-band conditional-formatting marking, ordering of
 * formula-change event vs. RPC push, and the null-CellData fallback branch for
 * formula cells.
 *
 * <p>
 * Uses a real {@link XSSFWorkbook} for the sheet (the traversal contract is
 * POI's iterator behavior, not something worth mocking). Mocks the parent
 * {@link Spreadsheet} / {@link CellValueManager} / {@link ConditionalFormatter}
 * / {@link SpreadsheetClientRpc} so we can verify the outgoing side effects.
 * Uses the real {@link ClientCellCache} and {@link ClientFlushQueue} so post-
 * conditions (cache markings, queue clear) can be asserted directly.
 */
class ClientCacheFlusherTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CellValueManager parent;
    @Mock
    private ConditionalFormatter conditionalFormatter;
    @Mock
    private SpreadsheetClientRpc rpcProxy;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private ClientCellCache cache;
    private ClientFlushQueue queue;
    private ClientCacheFlusher flusher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        cache = new ClientCellCache();
        queue = new ClientFlushQueue();
        when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        when(spreadsheet.getConditionalFormatter())
                .thenReturn(conditionalFormatter);
        when(spreadsheet.getRpcProxy()).thenReturn(rpcProxy);
        flusher = new ClientCacheFlusher(spreadsheet, parent, cache, queue);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void emptySheet_noRpcFires_noEventFires() {
        // Pin the empty-batch guard: an empty sheet produces an empty
        // batch and no formula changes, so neither cellsUpdated nor
        // refreshCellStyles fire. fireFormulaValueChangeEvent also does
        // not fire.
        flusher.flush();

        verify(rpcProxy, never()).cellsUpdated(any());
        verify(rpcProxy, never()).refreshCellStyles();
        verify(parent, never()).fireFormulaValueChangeEvent(any());
        assertFalse(queue.isMarkedForUpdate("col1 row1"));
        assertEquals(0, queue.getRemovedCells().size());
    }

    @Test
    void markedPlainCell_pushedOnce_queueClearedCacheMarked() {
        // Pin: a single marked plain cell flows through to a single
        // cellsUpdated + refreshCellStyles RPC pair. The cache learns the
        // cell is sent (containsNormal=true) and the queue clears.
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("hello");
        queue.markForUpdate("col1 row1");

        CellData cd = new CellData();
        cd.col = 1;
        cd.row = 1;
        when(parent.createCellDataForCell(any(Cell.class))).thenReturn(cd);

        flusher.flush();

        ArrayList<CellData> expected = new ArrayList<>();
        expected.add(cd);
        verify(rpcProxy).cellsUpdated(expected);
        verify(rpcProxy).refreshCellStyles();
        assertTrue(cache.containsNormal("col1 row1"));
        assertFalse(queue.isMarkedForUpdate("col1 row1"));
    }

    @Test
    void formulaCellWithStaleCache_pushedEagerly_andMarkedFormulaSent() {
        // Pin the eager-re-flush invariant: any formula cell already in
        // containsFormula gets re-evaluated and re-pushed even without an
        // explicit mark. POI's evaluator may produce a new value from
        // dependent-cell changes the flusher doesn't track.
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellFormula("1+1");
        cache.markFormulaSent("col1 row1");

        CellData cd = new CellData();
        cd.col = 1;
        cd.row = 1;
        when(parent.createCellDataForCell(any(Cell.class))).thenReturn(cd);

        flusher.flush();

        verify(rpcProxy).cellsUpdated(any());
        verify(rpcProxy).refreshCellStyles();
        assertTrue(cache.containsFormula("col1 row1"));
    }

    @Test
    void conditionalFormattingOnPlainCell_marksInBand_andPushes() {
        // Pin the in-band CF-marking quirk: the CF check runs
        // first in the loop body and may call markForUpdate(key); the same
        // iteration's plain-cell branch then reads isMarkedForUpdate(key)
        // and sees true. End result: a CF-only cell (no caller-side mark)
        // still ends up in the batch.
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("formatted");

        when(parent.createCellDataForCell(any(Cell.class)))
                .thenReturn(new CellData());
        Set<Integer> indices = new HashSet<>();
        indices.add(1);
        when(conditionalFormatter.getCellFormattingIndex(any()))
                .thenReturn(indices);

        flusher.flush();

        verify(rpcProxy).cellsUpdated(any());
        verify(rpcProxy).refreshCellStyles();
        assertTrue(cache.containsNormal("col1 row1"),
                "Plain cell flagged by CF should be marked-as-sent");
    }

    @Test
    void changedFormulaCells_eventFiresBeforeRpcPush() {
        // Pin the ordering invariant: fireFormulaValueChangeEvent
        // runs BEFORE cellsUpdated / refreshCellStyles. Listeners can mutate
        // cell state during the event and have those mutations reflected in
        // the same RPC push.
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("x");
        queue.markForUpdate("col1 row1");
        queue.addChangedFormulaCell(new CellReference("B2"));
        when(parent.createCellDataForCell(any(Cell.class)))
                .thenReturn(new CellData());

        flusher.flush();

        InOrder inOrder = Mockito.inOrder(parent, rpcProxy);
        inOrder.verify(parent).fireFormulaValueChangeEvent(any());
        inOrder.verify(rpcProxy).cellsUpdated(any());
        inOrder.verify(rpcProxy).refreshCellStyles();
        assertFalse(queue.hasChangedFormulaCells());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void formulaCellWithNullCreateCellDataResult_fallbackEntryConstructed() {
        // Pin the defensive fallback branch (ClientCacheFlusher lines 92-101):
        // when createCellDataForCell returns null for a formula cell, the
        // flusher synthesizes a minimal CellData with col, row, and a
        // cellStyle string. The cache still marks formulaSent so the next
        // flush does not re-push the same fallback unless something else
        // marks it.
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellFormula("A2");
        cache.markFormulaSent("col1 row1");
        when(parent.createCellDataForCell(any(Cell.class))).thenReturn(null);

        flusher.flush();

        ArgumentCaptor<ArrayList> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(rpcProxy).cellsUpdated(captor.capture());
        ArrayList<CellData> batch = captor.getValue();
        assertEquals(1, batch.size());
        CellData fallback = batch.get(0);
        assertEquals(1, fallback.col);
        assertEquals(1, fallback.row);
        assertNotNull(fallback.cellStyle,
                "Fallback CellData must carry a cellStyle index");
        assertTrue(cache.containsFormula("col1 row1"));
    }
}
