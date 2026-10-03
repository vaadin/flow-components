/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.function.IntPredicate;

/**
 * How far a cell's text may run past its own column.
 *
 * <p>
 * A spreadsheet does not clip text at the column edge the way a table does.
 * Text too wide for its column carries on across the cells to its right for as
 * long as they are empty, and stops at the first one holding anything.
 *
 * <p>
 * The rules are the ones the GWT client applied in {@code Cell.updateOverflow}:
 *
 * <ul>
 * <li>A right-aligned cell stays inside its column. Excel runs those leftwards
 * instead; the GWT client never did, and this is a parity port.</li>
 * <li>The text stops at the first column of the row that holds a value.</li>
 * <li>The text stops at the frozen/scrolling split. The two sides are drawn in
 * separate quadrants, so text crossing the split would be cut by the quadrant
 * edge and reappear in the wrong place as soon as the sheet scrolled.</li>
 * <li>The text stops at the last column of the sheet.</li>
 * <li>Text still too wide where it stops is cut short with an ellipsis. With
 * nowhere to go at all, that happens inside its own column: "Wednesd...".</li>
 * </ul>
 *
 * <p>
 * The GWT client measured the rendered text with {@code scrollWidth}. Here the
 * width is the estimate the server already makes from the per-style width
 * ratios the client reports, the same one {@link CellFitCalculator} uses to
 * decide whether a value fits at all — so a cell that reports as not fitting
 * cannot then be computed as having nothing to spill.
 *
 * <p>
 * That estimate is one character's width times the number of characters, and
 * the character the client measures is a digit. Text of capitals and wide
 * letters therefore measures narrower than it draws, and text that only just
 * runs past its column is judged to fit and stays clipped. Both halves read the
 * same estimate, so the sheet stays self-consistent — a value the server thinks
 * fits is neither shortened into scientific notation nor spilled — but it is
 * not the pixel-exact answer {@code scrollWidth} gave. Measuring per cell in
 * the browser is what that would cost.
 */
final class CellOverflowCalculator implements Serializable {

    /** Left + right padding inside a cell, in pixels. */
    private static final int CELL_PADDING = 4;

    private CellOverflowCalculator() {
    }

    /**
     * Width to draw the cell's text at: its own column and the empty ones
     * beside it that it runs into.
     *
     * @param columnIndex
     *            the cell's column, 0-based
     * @param textWidth
     *            what the text needs, in pixels
     * @param columnWidths
     *            pixel width per column, indexed by 0-based column
     * @param horizontalSplitPosition
     *            number of frozen columns; 0 when the sheet has no column
     *            freeze
     * @param occupied
     *            whether the given 0-based column of this row holds a value
     * @return the width covering the cell's own column and every empty column
     *         the text reaches into - only its own when it has nowhere to go -
     *         or {@code 0} when the text fits
     */
    static int spillWidth(int columnIndex, int textWidth, int[] columnWidths,
            int horizontalSplitPosition, IntPredicate occupied) {
        // Right-aligned text runs on to the right as well: the GWT client
        // let a value too wide for its column overflow its cell whatever its
        // alignment, and the text then starts at the cell's left edge.
        if (columnIndex < 0 || columnIndex >= columnWidths.length) {
            return 0;
        }
        int columnWidth = columnWidths[columnIndex];
        int wanted = textWidth - (columnWidth - CELL_PADDING);
        if (wanted <= 0) {
            return 0;
        }

        boolean startsFrozen = isFrozen(columnIndex, horizontalSplitPosition);
        int gained = 0;
        int reached = columnIndex;
        while (gained < wanted) {
            int next = reached + 1;
            if (next >= columnWidths.length) {
                break;
            }
            if (startsFrozen != isFrozen(next, horizontalSplitPosition)) {
                break;
            }
            if (occupied.test(next)) {
                break;
            }
            gained += columnWidths[next];
            reached = next;
        }

        // With nowhere to go - the neighbour holds a value, sits on the other
        // side of the split, or does not exist - this is the cell's own
        // width: still too wide, so still cut short with an ellipsis rather
        // than clipped mid-letter.
        return columnWidth + gained;
    }

    private static boolean isFrozen(int columnIndex,
            int horizontalSplitPosition) {
        return columnIndex < horizontalSplitPosition;
    }
}
