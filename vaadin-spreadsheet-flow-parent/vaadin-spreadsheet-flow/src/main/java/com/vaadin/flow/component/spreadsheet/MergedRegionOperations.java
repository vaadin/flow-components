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
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * The merged regions of the active sheet: merging and unmerging cells, and
 * giving the client the regions POI has.
 * <p>
 * The client is given a new list each time rather than the old one changed:
 * {@link MergedRegionLookup} keeps its index for as long as the list is the
 * same one of the same size.
 */
final class MergedRegionOperations implements Serializable {

    private final Spreadsheet spreadsheet;

    MergedRegionOperations(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Merges {@code range} into a new merged region, in place of the regions
     * inside it. Throws if a region overlaps or encloses it, before changing
     * anything.
     */
    void addRegion(CellRangeAddress range) {
        Sheet sheet = spreadsheet.getActiveSheet();
        // All at once: XSSF finds a region by its index, and counts them, by
        // going through them from the start, so merging cells on a sheet of
        // 20,000 regions took 3.5 s.
        List<CellRangeAddress> regions = sheet.getMergedRegions();
        for (CellRangeAddress existing : regions) {
            int intersect = CellRangeUtil.intersect(range, existing);
            if (intersect == CellRangeUtil.OVERLAP
                    || intersect == CellRangeUtil.ENCLOSES) {
                throw new IllegalArgumentException("An existing region "
                        + existing + " "
                        + (intersect == CellRangeUtil.OVERLAP ? "overlaps "
                                : "encloses ")
                        + "the given region " + range);
            }
        }
        // The index of a region is where it is now that those removed before
        // it are gone.
        int index = 0;
        for (CellRangeAddress existing : regions) {
            if (CellRangeUtil.intersect(range,
                    existing) == CellRangeUtil.INSIDE) {
                removeFromPoiAndClient(index);
            } else {
                index++;
            }
        }
        sheet.addMergedRegion(range);
        MergedRegion added = toClient(range);
        ArrayList<MergedRegion> updated = spreadsheet.getMergedRegions() != null
                ? new ArrayList<>(spreadsheet.getMergedRegions())
                : new ArrayList<>();
        updated.add(added);
        spreadsheet.setMergedRegions(updated);
        afterMerge(added);
        spreadsheet.updateMarkedCells();
        spreadsheet.getCellSelectionManager().mergedRegionAdded(range);
    }

    /** Removes the merged region at the given POI index. */
    void removeRegion(int index) {
        CellRangeAddress removed = spreadsheet.getActiveSheet()
                .getMergedRegion(index);
        removeFromPoiAndClient(index);
        spreadsheet.updateMarkedCells();
        spreadsheet.getCellSelectionManager().mergedRegionRemoved(removed);
    }

    /** Gives the client the regions of the active sheet, numbered from 0. */
    void load() {
        spreadsheet.mergedRegionCounter = 0;
        reload();
    }

    /**
     * Gives the client the regions of the active sheet again, as POI has them
     * after a change made to them directly, such as a row shift.
     */
    void reload() {
        // All at once: XSSF finds a region by its index by going through
        // those before it, so reading 20,000 regions one by one took 0.7 s.
        List<CellRangeAddress> poiRegions = spreadsheet.getActiveSheet()
                .getMergedRegions();
        if (poiRegions.isEmpty()) {
            spreadsheet.setMergedRegions(null);
            return;
        }
        ArrayList<MergedRegion> regions = new ArrayList<>(poiRegions.size());
        for (CellRangeAddress poiRegion : poiRegions) {
            regions.add(toClient(poiRegion));
        }
        spreadsheet.setMergedRegions(regions);
    }

    /** The region as the client has it: 1-based, with an id of its own. */
    private MergedRegion toClient(CellRangeAddress range) {
        MergedRegion region = new MergedRegion();
        region.col1 = range.getFirstColumn() + 1;
        region.col2 = range.getLastColumn() + 1;
        region.row1 = range.getFirstRow() + 1;
        region.row2 = range.getLastRow() + 1;
        region.id = spreadsheet.mergedRegionCounter++;
        return region;
    }

    private void removeFromPoiAndClient(int index) {
        spreadsheet.getActiveSheet().removeMergedRegion(index);
        ArrayList<MergedRegion> updated = new ArrayList<>(
                spreadsheet.getMergedRegions());
        MergedRegion removed = updated.remove(index);
        spreadsheet.setMergedRegions(updated);
        afterUnmerge(removed);
    }

    /**
     * Takes the cells a merge hides off the client, all but the first, and
     * styles again the cells of the region and those just past it, whose
     * borders are drawn on the merged cell now.
     */
    private void afterMerge(MergedRegion region) {
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
     * Gives the client the cells of an unmerged region again, and styles again
     * those and the cells just past them, whose borders are drawn on their own
     * cells again. A cell that does not exist has its borders cleared instead.
     */
    private void afterUnmerge(MergedRegion region) {
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
