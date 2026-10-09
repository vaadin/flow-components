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
 * Loading the hyperlinks of the visible panes with {@link HyperlinkLoader}.
 *
 * <p>
 * Thin orchestrator over {@link HyperlinkRangeScanner}. Pins:
 *
 * <ul>
 * <li>{@code setHyperlinksTooltips} fires with a fresh empty map BEFORE any
 * scanner call (load is a full rebuild, not an incremental update).</li>
 * <li>4-quadrant scan dispatch based on freeze pane: area 1 (frozen × frozen)
 * only if BOTH splits {@literal >} 0; area 2 (frozen rows × all cols) only if
 * vertical split {@literal >} 0; area 3 (all rows × frozen cols) only if
 * horizontal split {@literal >} 0; area 4 (main viewport) always.</li>
 * </ul>
 */
class HyperlinkLoaderTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private HyperlinkRangeScanner scanner;

    private HyperlinkLoader loader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loader = new HyperlinkLoader(spreadsheet, scanner);
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(spreadsheet.getFirstColumn()).thenReturn(1);
        lenient().when(spreadsheet.getLastColumn()).thenReturn(10);
    }

    @Test
    void load_resetsTooltipsMap_beforeAnyScan() {
        // Pin: setHyperlinksTooltips(new HashMap<>()) runs BEFORE any
        // scanner call. This is the full-rebuild contract — old tooltips
        // for now-invisible cells are dropped first.
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        InOrder inOrder = Mockito.inOrder(spreadsheet, scanner);
        inOrder.verify(spreadsheet).setHyperlinksTooltips(any());
        inOrder.verify(scanner).scan(1, 1, 10, 10);
    }

    @Test
    void load_noFreezePane_onlyArea4Scanned() {
        // Pin: with both splits == 0, only the main viewport scan fires.
        // verifyNoMoreInteractions sharpens the original two-verify form —
        // a regression that calls scan(...) with DIFFERENT args (e.g. an
        // additional spurious area scan) would slip past the loose
        // any()-matched count.
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        verify(scanner, times(1)).scan(1, 1, 10, 10);
        org.mockito.Mockito.verifyNoMoreInteractions(scanner);
    }

    @Test
    void load_bothSplits_allFourAreasScanned() {
        // Pin: with verticalSplit > 0 AND horizontalSplit > 0, area 1 fires
        // (top-left frozen corner). The other three follow.
        when(spreadsheet.getLastFrozenRow()).thenReturn(3); // verticalSplit
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();

        verify(scanner).scan(1, 1, 3, 2); // area 1
        verify(scanner).scan(1, 1, 3, 10); // area 2
        verify(scanner).scan(1, 1, 10, 2); // area 3
        verify(scanner).scan(1, 1, 10, 10); // area 4
    }

    @Test
    void load_onlyVerticalSplit_skipsArea1AndArea3() {
        // Pin: with verticalSplit > 0 but horizontalSplit == 0:
        // - area 1 skipped (needs both > 0)
        // - area 2 fires (verticalSplit > 0)
        // - area 3 skipped (needs horizontalSplit > 0)
        // - area 4 always fires
        when(spreadsheet.getLastFrozenRow()).thenReturn(3);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        verify(scanner, never()).scan(1, 1, 3, 0);
        verify(scanner).scan(1, 1, 3, 10); // area 2
        verify(scanner).scan(1, 1, 10, 10); // area 4
        verify(scanner, times(2)).scan(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void load_onlyHorizontalSplit_skipsArea1AndArea2() {
        // Pin: with horizontalSplit > 0 but verticalSplit == 0:
        // - area 1 skipped
        // - area 2 skipped (needs verticalSplit > 0)
        // - area 3 fires (horizontalSplit > 0)
        // - area 4 always fires
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(2);

        loader.load();

        verify(scanner).scan(1, 1, 10, 2); // area 3
        verify(scanner).scan(1, 1, 10, 10); // area 4
        verify(scanner, times(2)).scan(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void load_alwaysScansArea4_evenWithoutFreezePane() {
        // Sanity check (already implied by other tests but worth a
        // dedicated assertion): area 4 is unconditional.
        when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        when(spreadsheet.getLastFrozenColumn()).thenReturn(0);

        loader.load();

        verify(scanner).scan(1, 1, 10, 10);
    }
}
