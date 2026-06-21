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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Orchestrates loading of cell comments (and invalid-formula markers) for the
 * currently visible cells. Resets the four state maps and walks each viewport
 * pane via {@link CellCommentRangeScanner}.
 *
 * <p>
 * Owns the {@code topLeftCellCommentsLoaded} latch — the top-left frozen pane
 * is only re-scanned once per spreadsheet lifecycle, since frozen rows and
 * columns don't scroll out of view.
 */
final class CellCommentLoader implements Serializable {

    private final Spreadsheet spreadsheet;
    private final CellCommentRangeScanner scanner;
    private boolean topLeftLoaded;

    CellCommentLoader(Spreadsheet spreadsheet,
            CellCommentRangeScanner scanner) {
        this.spreadsheet = spreadsheet;
        this.scanner = scanner;
    }

    void resetTopLeftLoaded() {
        this.topLeftLoaded = false;
    }

    void load() {
        if (spreadsheet.getFirstColumn() == -1) {
            // Spreadsheet not loaded. This method will be called again.
            return;
        }

        // Reset the four state maps to empty (preserving the property-write
        // semantics) before scanning the panes.
        spreadsheet.setCellComments(new HashMap<>());
        spreadsheet.setCellCommentAuthors(new HashMap<>());
        spreadsheet.setVisibleCellComments(new ArrayList<>());
        spreadsheet.setInvalidFormulaCells(new HashSet<>());

        int verticalSplit = spreadsheet.getLastFrozenRow();
        int horizontalSplit = spreadsheet.getLastFrozenColumn();
        int firstRow = spreadsheet.getFirstRow();
        int lastRow = spreadsheet.getLastRow();
        int firstColumn = spreadsheet.getFirstColumn();
        int lastColumn = spreadsheet.getLastColumn();

        if (verticalSplit > 0 && horizontalSplit > 0 && !topLeftLoaded) {
            scanner.scan(1, 1, verticalSplit, horizontalSplit);
            topLeftLoaded = true;
        }
        if (verticalSplit > 0) {
            scanner.scan(1, firstColumn, verticalSplit, lastColumn);
        }
        if (horizontalSplit > 0) {
            scanner.scan(firstRow, 1, lastRow, horizontalSplit);
        }
        scanner.scan(firstRow, firstColumn, lastRow, lastColumn);
    }
}
