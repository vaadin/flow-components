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

import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

/**
 * Answers whether a {@link SheetOverlayWrapper} is currently visible in any of
 * the four viewport regions formed by the active sheet's freeze pane. Handles
 * the special-case where an anchor is {@code DONT_MOVE_AND_RESIZE} (always
 * considered visible) and the hidden- range case (overlay anchored entirely in
 * hidden rows or columns is invisible).
 */
final class SheetOverlayVisibility implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetOverlayVisibility(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    boolean isVisible(SheetOverlayWrapper overlay) {
        var anchor = overlay.getAnchor();

        // Need special handling for XSSFClientAnchor anchors of type
        // DONT_MOVE_AND_RESIZE.
        // See https://github.com/vaadin/flow-components/issues/3261
        if (AnchorType.DONT_MOVE_AND_RESIZE.equals(anchor.getAnchorType())
                && anchor instanceof XSSFClientAnchor) {
            // Since there's no way to know if an arbitrary x/y coordinate is
            // inside the current viewport, always return true for these
            // anchors.
            return true;
        }

        int col1 = anchor.getCol1();
        int col2 = anchor.getCol2();
        int row1 = anchor.getRow1();
        int row2 = anchor.getRow2();

        // type=2 doesn't size with cells
        final boolean isType2 = (col2 == 0 && row2 == 0);

        if (!isType2) {
            // to ensure compatibility with grouping/hidden columns
            if (isColumnRangeHidden(col1, col2)
                    || isRowRangeHidden(row1, row2)) {
                return false;
            }
        }

        int horizontalSplit = spreadsheet.getLastFrozenColumn();
        int verticalSplit = spreadsheet.getLastFrozenRow();
        int firstRow = spreadsheet.getFirstRow();
        int lastRow = spreadsheet.getLastRow();
        int firstColumn = spreadsheet.getFirstColumn();
        int lastColumn = spreadsheet.getLastColumn();

        boolean visibleInArea1 = horizontalSplit > 0 && verticalSplit > 0
                && overlay.isVisible(1, 1, verticalSplit, horizontalSplit);
        boolean visibleInArea2 = horizontalSplit > 0
                && overlay.isVisible(firstRow, 1, lastRow, horizontalSplit);
        boolean visibleInArea3 = verticalSplit > 0
                && overlay.isVisible(1, firstColumn, verticalSplit, lastColumn);
        boolean visibleInArea4 = overlay.isVisible(firstRow, firstColumn,
                lastRow, lastColumn);

        return visibleInArea1 || visibleInArea2 || visibleInArea3
                || visibleInArea4;
    }

    private boolean isRowRangeHidden(int row1, int row2) {
        for (int row = row1; row <= row2; row++) {
            if (!spreadsheet.isRowHidden(row)) {
                return false;
            }
        }
        return true;
    }

    private boolean isColumnRangeHidden(int col1, int col2) {
        for (int col = col1; col <= col2; col++) {
            if (!spreadsheet.isColumnHidden(col)) {
                return false;
            }
        }
        return true;
    }
}
