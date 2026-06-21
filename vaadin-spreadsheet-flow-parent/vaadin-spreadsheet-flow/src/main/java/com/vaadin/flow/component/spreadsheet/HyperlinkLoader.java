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
import java.util.HashMap;

/**
 * Orchestrates loading of hyperlink tooltips for the currently visible cells.
 * Resets the tooltips map, then walks each viewport pane via
 * {@link HyperlinkRangeScanner}.
 */
final class HyperlinkLoader implements Serializable {

    private final Spreadsheet spreadsheet;
    private final HyperlinkRangeScanner scanner;

    HyperlinkLoader(Spreadsheet spreadsheet, HyperlinkRangeScanner scanner) {
        this.spreadsheet = spreadsheet;
        this.scanner = scanner;
    }

    void load() {
        spreadsheet.setHyperlinksTooltips(new HashMap<>());

        int verticalSplit = spreadsheet.getLastFrozenRow();
        int horizontalSplit = spreadsheet.getLastFrozenColumn();
        int firstRow = spreadsheet.getFirstRow();
        int lastRow = spreadsheet.getLastRow();
        int firstColumn = spreadsheet.getFirstColumn();
        int lastColumn = spreadsheet.getLastColumn();

        if (verticalSplit > 0 && horizontalSplit > 0) {
            scanner.scan(1, 1, verticalSplit, horizontalSplit);
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
