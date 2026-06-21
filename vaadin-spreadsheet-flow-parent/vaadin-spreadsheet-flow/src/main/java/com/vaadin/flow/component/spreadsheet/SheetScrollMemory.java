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
 * Records the current scroll position into the per-sheet vertical/horizontal
 * scroll-position arrays before a sheet switch, so the client can restore the
 * right scroll offset when the user returns to a previously visited sheet.
 *
 * <p>
 * Both {@link SheetChangeDispatcher#onSheetSelected} and
 * {@link SheetChangeDispatcher#onNewSheetCreated} call into this class before
 * the actual sheet change.
 */
final class SheetScrollMemory implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetScrollMemory(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Stash the given scroll position against the sheet at the given visible
     * index.
     */
    void rememberForSheetAtIndex(int oldIndex, int scrollLeft, int scrollTop) {
        int[] vertical = Arrays.copyOf(spreadsheet.getVerticalScrollPositions(),
                spreadsheet.getVerticalScrollPositions().length);
        vertical[oldIndex] = scrollTop;
        spreadsheet.setVerticalScrollPositions(vertical);

        int[] horizontal = Arrays.copyOf(
                spreadsheet.getHorizontalScrollPositions(),
                spreadsheet.getHorizontalScrollPositions().length);
        horizontal[oldIndex] = scrollLeft;
        spreadsheet.setHorizontalScrollPositions(horizontal);
    }
}
