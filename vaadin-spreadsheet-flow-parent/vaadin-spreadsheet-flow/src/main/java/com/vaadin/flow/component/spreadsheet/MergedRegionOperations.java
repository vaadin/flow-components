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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Adds and removes a single merged region on the active sheet.
 *
 * <p>
 * {@link #addRegion} validates that the requested range does not overlap or
 * enclose an existing region (those are rejected with
 * {@link IllegalArgumentException}), discards any existing regions that lie
 * entirely inside the new range, mutates the POI sheet, mints a
 * {@link MergedRegion} DTO with the next id, and runs the merge cascade.
 * {@link #removeRegion} mirrors the inverse and runs the unmerge cascade.
 *
 * <p>
 * {@link MergedRegionCascade} owns the cell-level style + cache-marker side
 * effects; this class owns the validation, ordering, and the POI write.
 */
final class MergedRegionOperations implements Serializable {

    private final Spreadsheet spreadsheet;
    private final MergedRegionCascade cascade;

    MergedRegionOperations(Spreadsheet spreadsheet,
            MergedRegionCascade cascade) {
        this.spreadsheet = spreadsheet;
        this.cascade = cascade;
    }

    /**
     * Merges {@code range} into a new merged region. Discards any existing
     * regions that lie entirely inside it. Throws if it would overlap or
     * enclose any existing region.
     */
    void addRegion(CellRangeAddress range) {
        Sheet sheet = spreadsheet.getActiveSheet();
        // All at once: XSSF finds a region by its index, and counts them, by
        // going through them from the start, so merging cells on a sheet of
        // 20,000 regions took 3.5 s. The index is where the region is now
        // that those removed before it are gone.
        int index = 0;
        for (CellRangeAddress existing : sheet.getMergedRegions()) {
            int intersect = CellRangeUtil.intersect(range, existing);
            if (intersect == CellRangeUtil.INSIDE) {
                removeRegionFromPoiAndCache(index);
            } else if (intersect == CellRangeUtil.OVERLAP
                    || intersect == CellRangeUtil.ENCLOSES) {
                throw new IllegalArgumentException("An existing region "
                        + existing + " "
                        + (intersect == CellRangeUtil.OVERLAP ? "overlaps "
                                : "encloses ")
                        + "the given region " + range);
            } else {
                index++;
            }
        }
        addRegionToPoiAndCache(range);
        spreadsheet.getCellSelectionManager().mergedRegionAdded(range);
    }

    /**
     * Removes the merged region at the given POI index.
     */
    void removeRegion(int index) {
        CellRangeAddress removed = spreadsheet.getActiveSheet()
                .getMergedRegion(index);
        removeRegionFromPoiAndCache(index);
        spreadsheet.updateMarkedCells();
        spreadsheet.getCellSelectionManager().mergedRegionRemoved(removed);
    }

    /**
     * POI-add + DTO build + id mint + merge cascade + update flush.
     */
    private void addRegionToPoiAndCache(CellRangeAddress range) {
        Sheet sheet = spreadsheet.getActiveSheet();
        sheet.addMergedRegion(range);
        MergedRegion region = new MergedRegion();
        region.col1 = range.getFirstColumn() + 1;
        region.col2 = range.getLastColumn() + 1;
        region.row1 = range.getFirstRow() + 1;
        region.row2 = range.getLastRow() + 1;
        region.id = spreadsheet.mergedRegionCounter++;
        ArrayList<MergedRegion> updated = spreadsheet.getMergedRegions() != null
                ? new ArrayList<>(spreadsheet.getMergedRegions())
                : new ArrayList<>();
        updated.add(region);
        spreadsheet.setMergedRegions(updated);
        cascade.afterMerge(region);
        spreadsheet.updateMarkedCells();
    }

    private void removeRegionFromPoiAndCache(int index) {
        Sheet sheet = spreadsheet.getActiveSheet();
        sheet.removeMergedRegion(index);
        ArrayList<MergedRegion> updated = new ArrayList<>(
                spreadsheet.getMergedRegions());
        MergedRegion removed = updated.remove(index);
        spreadsheet.setMergedRegions(updated);
        cascade.afterUnmerge(removed);
    }
}
