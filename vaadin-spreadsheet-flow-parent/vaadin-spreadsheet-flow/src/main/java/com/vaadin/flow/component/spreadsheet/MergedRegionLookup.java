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
import java.util.List;
import java.util.function.Supplier;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;

/**
 * Read-only queries over the currently active sheet's merged regions.
 * Implements the frozen client-side {@link MergedRegionContainer} contract.
 *
 * <p>
 * Backed by a supplier of the live region list so the lookup always sees the
 * latest state without holding a stale reference. Owns no data of its own.
 */
@SuppressWarnings("serial")
final class MergedRegionLookup implements MergedRegionContainer, Serializable {

    private final Supplier<List<MergedRegion>> regionsSupplier;

    MergedRegionLookup(Supplier<List<MergedRegion>> regionsSupplier) {
        this.regionsSupplier = regionsSupplier;
    }

    @Override
    public MergedRegion getMergedRegionStartingFrom(int column, int row) {
        List<MergedRegion> regions = regionsSupplier.get();
        if (regions == null) {
            return null;
        }
        for (MergedRegion region : regions) {
            if (region.col1 == column && region.row1 == row) {
                return region;
            }
        }
        return null;
    }

    @Override
    public MergedRegion getMergedRegion(int column, int row) {
        List<MergedRegion> regions = regionsSupplier.get();
        if (regions == null) {
            return null;
        }
        for (MergedRegion region : regions) {
            if (region.col1 <= column && region.row1 <= row
                    && region.col2 >= column && region.row2 >= row) {
                return region;
            }
        }
        return null;
    }
}
