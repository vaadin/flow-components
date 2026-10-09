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

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Bulk-rebuilds the in-memory {@link MergedRegion} list from the active sheet's
 * current POI state. Called whenever a structural operation (row delete, sheet
 * load, etc.) may have changed the set of merged regions in POI without going
 * through {@link MergedRegionOperations}, leaving the cache out of date.
 */
final class MergedRegionSynchronizer implements Serializable {

    private final Spreadsheet spreadsheet;
    private final MergedRegionOperations operations;

    MergedRegionSynchronizer(Spreadsheet spreadsheet,
            MergedRegionOperations operations) {
        this.spreadsheet = spreadsheet;
        this.operations = operations;
    }

    /**
     * Rebuild the cached region list to match POI's current state.
     */
    void rebuildFromPoi() {
        int poiRegionCount = spreadsheet.getActiveSheet().getNumMergedRegions();
        if (poiRegionCount == 0) {
            spreadsheet.setMergedRegions(null);
            return;
        }
        ArrayList<MergedRegion> rebuilt = new ArrayList<>();
        for (int i = 0; i < poiRegionCount; i++) {
            CellRangeAddress poiRegion = spreadsheet.getActiveSheet()
                    .getMergedRegion(i);
            try {
                MergedRegion region = new MergedRegion();
                region.col1 = poiRegion.getFirstColumn() + 1;
                region.col2 = poiRegion.getLastColumn() + 1;
                region.row1 = poiRegion.getFirstRow() + 1;
                region.row2 = poiRegion.getLastRow() + 1;
                region.id = spreadsheet.mergedRegionCounter++;
                rebuilt.add(i, region);
            } catch (IndexOutOfBoundsException ioobe) {
                // POI reports a region that wasn't in the cache; route it
                // through the full add path so the cascade runs.
                operations.addRegionToPoiAndCache(poiRegion);
            }
        }
        while (poiRegionCount < rebuilt.size()) {
            rebuilt.remove(rebuilt.size() - 1);
        }
        spreadsheet.setMergedRegions(rebuilt);
    }
}
