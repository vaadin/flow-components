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
import java.util.Collections;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Adjusts row heights so rows that contain custom components are at least
 * {@code minimumRowHeightForComponents} points tall, and restores their
 * previous (POI-derived or hidden) height when components are removed. Owns the
 * per-call memory of which rows currently have components so the restore step
 * can identify the rows to clean up.
 */
final class CustomComponentRowSizer implements Serializable {

    private final Spreadsheet spreadsheet;
    private Set<Integer> currentRowsWithComponents;

    CustomComponentRowSizer(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void applyHeightsFor(Set<Integer> nextRowsWithComponents) {
        // Components can be on rows setMaxRows added past the loaded ones.
        int lastRow = Math.max(lastOf(nextRowsWithComponents),
                lastOf(currentRowsWithComponents));
        float[] updatedHeights = RowSizeOperations.heightsCovering(spreadsheet,
                lastRow);
        int minHeight = spreadsheet.getMinimumRowHeightForComponents();

        // Grow rows that now host a component
        for (Integer row : nextRowsWithComponents) {
            if (spreadsheet.isRowHidden(row)) {
                continue;
            }
            if (updatedHeights[row] < minHeight) {
                updatedHeights[row] = minHeight;
            }
        }
        // Restore rows that previously hosted a component but no longer do
        if (currentRowsWithComponents != null) {
            Sheet activeSheet = spreadsheet.getActiveSheet();
            for (Integer row : currentRowsWithComponents) {
                if (nextRowsWithComponents.contains(row)) {
                    continue;
                }
                if (spreadsheet.isRowHidden(row)) {
                    updatedHeights[row] = 0;
                } else {
                    Row r = activeSheet.getRow(row);
                    updatedHeights[row] = r == null
                            ? activeSheet.getDefaultRowHeightInPoints()
                            : r.getHeightInPoints();
                }
            }
        }
        spreadsheet.setRowH(updatedHeights);
        this.currentRowsWithComponents = nextRowsWithComponents;
    }

    private static int lastOf(Set<Integer> rows) {
        return rows == null || rows.isEmpty() ? -1 : Collections.max(rows);
    }
}
