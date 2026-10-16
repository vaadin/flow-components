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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Applies the cell-level side effects that follow when a region is merged or
 * unmerged: style invalidation on the affected cells (and the row/column just
 * past the region), marking the now-hidden cells for removal from the client
 * cache when merging, marking the newly-visible cells for re-send when
 * unmerging, and reloading custom border styles on the sheet.
 *
 * <p>
 * Both {@link MergedRegionOperations} and {@link MergedRegionSynchronizer}
 * drive the same cascade through this class.
 */
final class MergedRegionCascade implements Serializable {

    private final Spreadsheet spreadsheet;

    MergedRegionCascade(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Cascade after a region was added: every cell inside the region (except
     * the top-left anchor) is marked for removal from the client cache because
     * the merge hides it; every cell along the region's outer edge has its
     * style invalidated so its borders re-render.
     */
    void afterMerge(MergedRegion region) {
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int r = region.row1; r <= (region.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            if (row == null) {
                continue;
            }
            for (int c = region.col1; c <= (region.col2 + 1); c++) {
                Cell cell = row.getCell(c - 1);
                if (cell == null) {
                    continue;
                }
                spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(cell,
                        false);
                if ((c != region.col1 || r != region.row1) && c <= region.col2
                        && r <= region.row2) {
                    spreadsheet.getCellValueManager().markCellForRemove(cell);
                }
            }
        }
        spreadsheet.getSpreadsheetStyleFactory()
                .loadCustomBorderStylesToState();
    }

    /**
     * Cascade after a region was removed: every cell inside the region (now
     * visible again) is marked for update so its value re-sends to the client,
     * and styles on the same area are invalidated so borders re-render. Cells
     * that don't yet exist on the row get their cached style cleared
     * explicitly.
     */
    void afterUnmerge(MergedRegion region) {
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int r = region.row1; r <= (region.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            if (row == null) {
                continue;
            }
            for (int c = region.col1; c <= (region.col2 + 1); c++) {
                Cell cell = row.getCell(c - 1);
                if (cell != null) {
                    spreadsheet.getSpreadsheetStyleFactory()
                            .cellStyleUpdated(cell, false);
                    spreadsheet.getCellValueManager().markCellForUpdate(cell);
                } else {
                    // 0-based
                    spreadsheet.getSpreadsheetStyleFactory()
                            .clearCellStyle(r - 1, c - 1);
                }
            }
        }
        spreadsheet.getSpreadsheetStyleFactory()
                .loadCustomBorderStylesToState();
    }
}
