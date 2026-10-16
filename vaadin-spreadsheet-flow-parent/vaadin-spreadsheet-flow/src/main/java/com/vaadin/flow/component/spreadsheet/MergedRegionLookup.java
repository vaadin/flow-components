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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;

/**
 * Read-only queries over the currently active sheet's merged regions.
 * Implements the frozen client-side {@link MergedRegionContainer} contract.
 *
 * <p>
 * Backed by a supplier of the live region list so the lookup always sees the
 * latest state without holding a stale reference.
 *
 * <p>
 * The queries run for every cell of every range loaded, so they go through an
 * index of the list rather than over all of it: with 20,000 merged regions a
 * screen of cells cost the server seconds. The index is built again when the
 * list is another one, or its size has changed - merges that come and go
 * replace the spreadsheet's list rather than change it.
 */
final class MergedRegionLookup implements MergedRegionContainer, Serializable {

    /** Rows per bucket of the index. */
    private static final int ROWS_PER_BUCKET = 64;

    private final Supplier<List<MergedRegion>> regionsSupplier;

    /** The list the index is of, and its size when it was built. */
    private transient List<MergedRegion> indexed;
    private transient int indexedSize;

    /** The regions over each bucket of rows, in the list's order. */
    private transient Map<Integer, List<MergedRegion>> byBucket;

    /** The first region in the list to start at each cell. */
    private transient Map<Long, MergedRegion> byStart;

    MergedRegionLookup(Supplier<List<MergedRegion>> regionsSupplier) {
        this.regionsSupplier = regionsSupplier;
    }

    @Override
    public MergedRegion getMergedRegionStartingFrom(int column, int row) {
        if (!index()) {
            return null;
        }
        return byStart.get(cellKey(column, row));
    }

    @Override
    public MergedRegion getMergedRegion(int column, int row) {
        if (!index()) {
            return null;
        }
        List<MergedRegion> bucket = byBucket.get(bucketOf(row));
        if (bucket == null) {
            return null;
        }
        for (MergedRegion region : bucket) {
            if (region.col1 <= column && region.row1 <= row
                    && region.col2 >= column && region.row2 >= row) {
                return region;
            }
        }
        return null;
    }

    /**
     * Makes sure the index is of the current list.
     *
     * @return {@code false} when there is no list
     */
    private boolean index() {
        List<MergedRegion> regions = regionsSupplier.get();
        if (regions == null) {
            return false;
        }
        if (regions == indexed && regions.size() == indexedSize) {
            return true;
        }
        Map<Integer, List<MergedRegion>> buckets = new HashMap<>();
        Map<Long, MergedRegion> starts = new HashMap<>();
        for (MergedRegion region : regions) {
            for (int bucket = bucketOf(region.row1); bucket <= bucketOf(
                    region.row2); bucket++) {
                buckets.computeIfAbsent(bucket, key -> new ArrayList<>())
                        .add(region);
            }
            starts.putIfAbsent(cellKey(region.col1, region.row1), region);
        }
        byBucket = buckets;
        byStart = starts;
        indexed = regions;
        indexedSize = regions.size();
        return true;
    }

    private static int bucketOf(int row) {
        return Math.floorDiv(row, ROWS_PER_BUCKET);
    }

    private static long cellKey(int column, int row) {
        return ((long) row << 32) | (column & 0xffffffffL);
    }
}
