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
import java.util.Arrays;

/**
 * Remembers where each sheet was scrolled to when another sheet was shown, so
 * that the client can scroll it back there when it is shown again.
 * <p>
 * The offsets are kept by POI sheet index, hidden sheets included, so that they
 * stay with their sheets when other sheets are hidden, shown again or deleted.
 * The client gets them in the order of the visible sheets.
 */
final class SheetScrollMemory implements Serializable {

    private final Spreadsheet spreadsheet;

    /** Offsets by POI sheet index. A sheet past the end is at the top. */
    private int[] tops = new int[0];
    private int[] lefts = new int[0];

    SheetScrollMemory(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /** Forgets every offset, for a workbook being loaded. */
    void clear() {
        tops = new int[0];
        lefts = new int[0];
    }

    /**
     * Remembers the given offsets for the sheet at the given visible index, and
     * sends them to the client. An index that is not a visible sheet's is
     * ignored.
     */
    void rememberForSheetAtIndex(int visibleIndex, int scrollLeft,
            int scrollTop) {
        if (visibleIndex < 0
                || visibleIndex >= spreadsheet.getNumberOfVisibleSheets()) {
            return;
        }
        int poiIndex = spreadsheet.getVisibleSheetPOIIndex(visibleIndex);
        if (poiIndex >= tops.length) {
            tops = Arrays.copyOf(tops, poiIndex + 1);
            lefts = Arrays.copyOf(lefts, poiIndex + 1);
        }
        tops[poiIndex] = scrollTop;
        lefts[poiIndex] = scrollLeft;
        publish();
    }

    /**
     * Forgets the offsets of the sheet at the given POI index, which has been
     * deleted: the sheets after it have moved up one index.
     */
    void sheetDeleted(int poiIndex) {
        if (poiIndex >= 0 && poiIndex < tops.length) {
            tops = withoutIndex(tops, poiIndex);
            lefts = withoutIndex(lefts, poiIndex);
        }
    }

    /** Sends the client the offsets of the visible sheets, in their order. */
    void publish() {
        int count = spreadsheet.getNumberOfVisibleSheets();
        int[] visibleTops = new int[count];
        int[] visibleLefts = new int[count];
        for (int i = 0; i < count; i++) {
            int poiIndex = spreadsheet.getVisibleSheetPOIIndex(i);
            if (poiIndex < tops.length) {
                visibleTops[i] = tops[poiIndex];
                visibleLefts[i] = lefts[poiIndex];
            }
        }
        spreadsheet.workbookModel().verticalScrollPositions.set(visibleTops);
        spreadsheet.workbookModel().horizontalScrollPositions.set(visibleLefts);
    }

    private static int[] withoutIndex(int[] values, int index) {
        int[] result = new int[values.length - 1];
        System.arraycopy(values, 0, result, 0, index);
        System.arraycopy(values, index + 1, result, index,
                values.length - index - 1);
        return result;
    }
}
