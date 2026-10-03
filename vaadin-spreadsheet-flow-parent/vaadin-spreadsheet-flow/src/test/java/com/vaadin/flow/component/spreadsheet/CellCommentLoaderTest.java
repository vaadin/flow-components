/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link CellCommentLoader}.
 *
 * <p>
 * Same 4-quadrant scan dispatch as {@link HyperlinkLoader} plus two extras:
 *
 * <ul>
 * <li>Early-return when {@code getFirstColumn() == -1} (spreadsheet not yet
 * loaded).</li>
 * <li>Area 1 (frozen × frozen) is scanned on every load like the others. A
 * latch that scanned it once lost its comments: every load empties the maps
 * first.</li>
 * </ul>
 */
class CellCommentLoaderTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private CellCommentRangeScanner scanner;

    private CellCommentLoader loader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loader = new CellCommentLoader(spreadsheet, scanner);
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(spreadsheet.getFirstColumn()).thenReturn(1);
        lenient().when(spreadsheet.getLastColumn()).thenReturn(10);
    }

    @Test
    void load_spreadsheetNotLoaded_earlyReturns() {
        // Pin the early-return: getFirstColumn() == -1 means the spreadsheet
        // hasn't loaded a sheet yet. No state resets, no scans. The method
        // will be called again later.
        when(spreadsheet.getFirstColumn()).thenReturn(-1);

        loader.load();

        verify(spreadsheet, never()).setCellComments(any());
        verify(spreadsheet, never()).setCellCommentAuthors(any());
        verify(spreadsheet, never()).setVisibleCellComments(any());
        verify(spreadsheet, never()).setInvalidFormulaCells(any());
        verify(scanner, never()).scan(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void load_resetsFourStateMaps_beforeAnyScan() {
        // Pin: the four state maps are reset to fresh empty collections in
        // a specific order BEFORE the first scan call. All four reset
        // points fire on every load() (modulo the early-return).
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        InOrder inOrder = Mockito.inOrder(spreadsheet, scanner);
        inOrder.verify(spreadsheet).setCellComments(any());
        inOrder.verify(spreadsheet).setCellCommentAuthors(any());
        inOrder.verify(spreadsheet).setVisibleCellComments(any());
        inOrder.verify(spreadsheet).setInvalidFormulaCells(any());
        inOrder.verify(scanner).scan(1, 1, 10, 10);
    }

    @Test
    void load_noFreezePane_onlyArea4Scanned() {
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        verify(scanner).scan(1, 1, 10, 10);
        verify(scanner, times(1)).scan(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void load_bothSplits_firstCall_allFourAreasScanned() {
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();

        verify(scanner).scan(1, 1, 3, 2); // area 1
        verify(scanner).scan(1, 1, 3, 10); // area 2
        verify(scanner).scan(1, 1, 10, 2); // area 3
        verify(scanner).scan(1, 1, 10, 10); // area 4
    }

    @Test
    void load_bothSplits_secondCall_scansTheFrozenCornerAgain() {
        // Every load empties the comment maps before scanning. The refactor
        // latched area 1 after its first scan, so the second load - any
        // scroll or refresh - dropped every comment in the frozen corner.
        // The GWT-era Spreadsheet never set its latch.
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();
        loader.load();

        verify(scanner, times(2)).scan(1, 1, 3, 2);
        verify(scanner, times(2)).scan(1, 1, 3, 10);
        verify(scanner, times(2)).scan(1, 1, 10, 2);
        verify(scanner, times(2)).scan(1, 1, 10, 10);
    }

    @Test
    void load_onlyVerticalSplit_skipsArea1AndArea3() {
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        verify(scanner).scan(1, 1, 3, 10); // area 2
        verify(scanner).scan(1, 1, 10, 10); // area 4
        verify(scanner, times(2)).scan(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void load_onlyHorizontalSplit_skipsArea1AndArea2() {
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();

        verify(scanner).scan(1, 1, 10, 2); // area 3
        verify(scanner).scan(1, 1, 10, 10); // area 4
        verify(scanner, times(2)).scan(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void load_bothSplits_area1FiresFirst() {
        // Pin: area 1 is at the head of the dispatch.
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();

        InOrder inOrder = Mockito.inOrder(scanner);
        inOrder.verify(scanner).scan(1, 1, 3, 2); // area 1 first
        inOrder.verify(scanner).scan(1, 1, 3, 10); // then area 2
        inOrder.verify(scanner).scan(1, 1, 10, 2); // then area 3
        inOrder.verify(scanner).scan(1, 1, 10, 10); // then area 4
    }
}
