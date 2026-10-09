/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Whether an overlay is in view, as {@link SheetOverlayVisibility} decides it.
 *
 * <p>
 * Pins the visibility decision tree:
 *
 * <ol>
 * <li>{@code DONT_MOVE_AND_RESIZE} on an {@link XSSFClientAnchor} → always
 * visible (workaround for issue #3261).</li>
 * <li>Type-2 anchors (no col2 / row2) skip the hidden-range gate.</li>
 * <li>Non-type-2 anchors with EVERY anchored column hidden OR EVERY anchored
 * row hidden → invisible.</li>
 * <li>Otherwise, check visibility against the four viewport quadrants formed by
 * the freeze pane.</li>
 * </ol>
 *
 * <p>
 * Mocks the parent {@link Spreadsheet}, the {@link SheetOverlayWrapper}, and
 * the anchor types. Uses {@code lenient()} for the freeze-pane / viewport stubs
 * because not every test exercises every method (strict stubbing would
 * otherwise flag the unused ones).
 */
class SheetOverlayVisibilityTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SheetOverlayWrapper overlay;
    @Mock
    private ClientAnchor anchor;
    @Mock
    private XSSFClientAnchor xssfAnchor;

    private SheetOverlayVisibility visibility;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        visibility = new SheetOverlayVisibility(spreadsheet);
        // Default: no freeze pane, 1..10 viewport, no hidden rows/cols.
        lenient().when(spreadsheet.getLastFrozenColumn()).thenReturn(0);
        lenient().when(spreadsheet.getLastFrozenRow()).thenReturn(0);
        lenient().when(spreadsheet.getFirstRow()).thenReturn(1);
        lenient().when(spreadsheet.getLastRow()).thenReturn(10);
        lenient().when(spreadsheet.getFirstColumn()).thenReturn(1);
        lenient().when(spreadsheet.getLastColumn()).thenReturn(10);
        lenient().when(spreadsheet.isRowHidden(anyInt())).thenReturn(false);
        lenient().when(spreadsheet.isColumnHidden(anyInt())).thenReturn(false);
    }

    // ----- DONT_MOVE_AND_RESIZE special case --------------------------------

    @Test
    void dontMoveAndResize_onXSSFAnchor_alwaysVisible() {
        // Pin issue #3261 workaround: a DONT_MOVE_AND_RESIZE anchor whose
        // concrete type is XSSFClientAnchor returns true unconditionally.
        // The implementation can't tell whether the x/y coordinates are
        // in the viewport, so it errs on showing the overlay.
        when(overlay.getAnchor()).thenReturn(xssfAnchor);
        when(xssfAnchor.getAnchorType())
                .thenReturn(AnchorType.DONT_MOVE_AND_RESIZE);

        assertTrue(visibility.isVisible(overlay));
    }

    @Test
    void dontMoveAndResize_onNonXSSFAnchor_takesNormalPath() {
        // Pin: the special-case is ONLY for XSSFClientAnchor. A non-XSSF
        // anchor with the same anchor type still goes through the normal
        // visibility computation (no early-return).
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType())
                .thenReturn(AnchorType.DONT_MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 0);
        when(anchor.getCol2()).thenReturn((short) 2);
        when(anchor.getRow1()).thenReturn(0);
        when(anchor.getRow2()).thenReturn(2);
        when(overlay.isVisible(1, 1, 10, 10)).thenReturn(true);

        assertTrue(visibility.isVisible(overlay));
        // Confirmed the normal path ran — visibility called isVisible on
        // the overlay (not the XSSF early-return).
    }

    // ----- Hidden-range gate ------------------------------------------------

    @Test
    void allColumnsHidden_returnsFalse() {
        // Pin: if every column the anchor covers is hidden, the overlay is
        // invisible — short-circuits before the quadrant check.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 2);
        when(anchor.getCol2()).thenReturn((short) 4);
        when(anchor.getRow1()).thenReturn(1);
        when(anchor.getRow2()).thenReturn(3);
        when(spreadsheet.isColumnHidden(2)).thenReturn(true);
        when(spreadsheet.isColumnHidden(3)).thenReturn(true);
        when(spreadsheet.isColumnHidden(4)).thenReturn(true);

        assertFalse(visibility.isVisible(overlay));
    }

    @Test
    void partialColumnHidden_takesNormalPath() {
        // Pin: a partially-hidden column range is NOT treated as hidden.
        // The check requires ALL columns in [col1, col2] to be hidden.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 2);
        when(anchor.getCol2()).thenReturn((short) 4);
        when(anchor.getRow1()).thenReturn(1);
        when(anchor.getRow2()).thenReturn(3);
        when(spreadsheet.isColumnHidden(2)).thenReturn(true);
        when(spreadsheet.isColumnHidden(3)).thenReturn(false);
        // Quadrant check then decides visibility.
        when(overlay.isVisible(1, 1, 10, 10)).thenReturn(true);

        assertTrue(visibility.isVisible(overlay));
    }

    @Test
    void allRowsHidden_returnsFalse() {
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 1);
        when(anchor.getCol2()).thenReturn((short) 2);
        when(anchor.getRow1()).thenReturn(2);
        when(anchor.getRow2()).thenReturn(3);
        when(spreadsheet.isRowHidden(2)).thenReturn(true);
        when(spreadsheet.isRowHidden(3)).thenReturn(true);

        assertFalse(visibility.isVisible(overlay));
    }

    @Test
    void type2Anchor_skipsHiddenRangeGate() {
        // Pin: type-2 anchors (col2 == 0 && row2 == 0) bypass the hidden
        // check because they don't size with cells — their hidden-range
        // semantics are undefined.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 3);
        when(anchor.getCol2()).thenReturn((short) 0);
        when(anchor.getRow1()).thenReturn(2);
        when(anchor.getRow2()).thenReturn(0);
        // Pretend the columns ARE hidden — visibility should still
        // proceed to the quadrant check because type2 skips the gate.
        lenient().when(spreadsheet.isColumnHidden(3)).thenReturn(true);
        when(overlay.isVisible(1, 1, 10, 10)).thenReturn(true);

        assertTrue(visibility.isVisible(overlay));
    }

    // ----- 4-quadrant freeze-pane visibility --------------------------------

    @Test
    void noFreezePane_onlyArea4Considered() {
        // Pin: with horizontalSplit == 0 AND verticalSplit == 0, areas 1,
        // 2, 3 are guarded out (the `> 0` checks). Only area 4 (the bottom-
        // right viewport) is queried.
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 1);
        when(anchor.getCol2()).thenReturn((short) 2);
        when(anchor.getRow1()).thenReturn(1);
        when(anchor.getRow2()).thenReturn(2);
        when(overlay.isVisible(1, 1, 10, 10)).thenReturn(true);

        assertTrue(visibility.isVisible(overlay));
    }

    @Test
    void noFreezePane_overlayOutsideViewport_returnsFalse() {
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 20);
        when(anchor.getCol2()).thenReturn((short) 21);
        when(anchor.getRow1()).thenReturn(1);
        when(anchor.getRow2()).thenReturn(2);
        when(overlay.isVisible(1, 1, 10, 10)).thenReturn(false);

        assertFalse(visibility.isVisible(overlay));
    }

    @Test
    void freezePaneWithBothSplits_allFourAreasConsidered() {
        // Pin: with both splits > 0, all four areas (top-left, top-right,
        // bottom-left, bottom-right) are queried. Any single area
        // returning true makes the overlay visible.
        when(spreadsheet.getLastFrozenColumn()).thenReturn(3);
        when(spreadsheet.getLastFrozenRow()).thenReturn(2);
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 1);
        when(anchor.getCol2()).thenReturn((short) 2);
        when(anchor.getRow1()).thenReturn(1);
        when(anchor.getRow2()).thenReturn(1);
        // Area 1 (frozen quadrant: rows 1-2, cols 1-3) — match
        when(overlay.isVisible(1, 1, 2, 3)).thenReturn(true);
        // Others can return whatever; the OR short-circuits on area1
        lenient().when(overlay.isVisible(1, 1, 10, 3)).thenReturn(false);
        lenient().when(overlay.isVisible(1, 1, 2, 10)).thenReturn(false);
        lenient().when(overlay.isVisible(1, 1, 10, 10)).thenReturn(false);

        assertTrue(visibility.isVisible(overlay));
    }

    @Test
    void freezePaneWithBothSplits_areasFalse_returnsFalse() {
        when(spreadsheet.getLastFrozenColumn()).thenReturn(3);
        when(spreadsheet.getLastFrozenRow()).thenReturn(2);
        when(overlay.getAnchor()).thenReturn(anchor);
        when(anchor.getAnchorType()).thenReturn(AnchorType.MOVE_AND_RESIZE);
        when(anchor.getCol1()).thenReturn((short) 100);
        when(anchor.getCol2()).thenReturn((short) 101);
        when(anchor.getRow1()).thenReturn(100);
        when(anchor.getRow2()).thenReturn(101);
        when(overlay.isVisible(1, 1, 2, 3)).thenReturn(false);
        when(overlay.isVisible(1, 1, 10, 3)).thenReturn(false);
        when(overlay.isVisible(1, 1, 2, 10)).thenReturn(false);
        when(overlay.isVisible(1, 1, 10, 10)).thenReturn(false);

        assertFalse(visibility.isVisible(overlay));
    }
}
