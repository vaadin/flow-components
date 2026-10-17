/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * Finding the merged region of a cell with {@link MergedRegionLookup}.
 *
 * <p>
 * Pins the two queries the lookup implements over a live region list:
 *
 * <ul>
 * <li>{@code getMergedRegionStartingFrom(col, row)} — exact match on
 * {@code (col1, row1)}.</li>
 * <li>{@code getMergedRegion(col, row)} — containment match
 * ({@code col1 ≤ col ≤ col2} and {@code row1 ≤ row ≤ row2}).</li>
 * </ul>
 *
 * <p>
 * Both methods consult the supplier each call so the lookup never holds a stale
 * snapshot — this is exercised by mutating the underlying list between lookups
 * in {@link #queries_seeMutationsOfTheBackingList}.
 *
 * <p>
 * No mocking framework is needed — the supplier indirection makes the
 * dependencies trivially injectable.
 */
class MergedRegionLookupTest {

    private static MergedRegion region(int id, int c1, int r1, int c2, int r2) {
        MergedRegion m = new MergedRegion(c1, r1, c2, r2);
        m.id = id;
        return m;
    }

    @Test
    void getMergedRegionStartingFrom_exactCornerMatch_returnsRegion() {
        // Pin: starting-from is strictly (col1, row1) — the top-left corner
        // of the merge. A query for any other cell in the merge does NOT
        // hit this method (that's getMergedRegion's job).
        MergedRegion m = region(1, 2, 3, 5, 7);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.singletonList(m));

        MergedRegion hit = lookup.getMergedRegionStartingFrom(2, 3);

        assertSame(m, hit);
    }

    @Test
    void getMergedRegionStartingFrom_innerCell_returnsNull() {
        // Pin: a cell inside the region but not at the top-left is NOT a
        // starting-from match. This is the load-bearing distinction
        // between the two query methods.
        MergedRegion m = region(1, 2, 3, 5, 7);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.singletonList(m));

        assertNull(lookup.getMergedRegionStartingFrom(3, 4));
    }

    @Test
    void getMergedRegionStartingFrom_noMatch_returnsNull() {
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.emptyList());
        assertNull(lookup.getMergedRegionStartingFrom(1, 1));
    }

    @Test
    void getMergedRegionStartingFrom_nullList_returnsNull() {
        // Pin the defensive null guard: a supplier that yields null does
        // not NPE — the lookup returns null cleanly. Matters because the
        // backing source may not be initialized before the first query.
        MergedRegionLookup lookup = new MergedRegionLookup(() -> null);
        assertNull(lookup.getMergedRegionStartingFrom(1, 1));
    }

    @Test
    void getMergedRegionStartingFrom_firstMatchWins() {
        // Pin iteration order semantics: the lookup walks the list and
        // returns the first match. If duplicate (col1, row1) regions exist
        // (a degenerate but technically possible state), the earlier one
        // wins. Documents the contract — callers that need uniqueness must
        // guarantee it upstream.
        MergedRegion a = region(1, 1, 1, 2, 2);
        MergedRegion b = region(2, 1, 1, 3, 3);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Arrays.asList(a, b));

        assertSame(a, lookup.getMergedRegionStartingFrom(1, 1));
    }

    // ----- getMergedRegion (containment) ------------------------------------

    @Test
    void getMergedRegion_topLeftCorner_returnsRegion() {
        MergedRegion m = region(1, 2, 3, 5, 7);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.singletonList(m));

        assertSame(m, lookup.getMergedRegion(2, 3));
    }

    @Test
    void getMergedRegion_bottomRightCorner_returnsRegion() {
        // Pin: containment is inclusive on all four bounds.
        MergedRegion m = region(1, 2, 3, 5, 7);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.singletonList(m));

        assertSame(m, lookup.getMergedRegion(5, 7));
    }

    @Test
    void getMergedRegion_interiorCell_returnsRegion() {
        MergedRegion m = region(1, 2, 3, 5, 7);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.singletonList(m));

        assertSame(m, lookup.getMergedRegion(3, 5));
    }

    @Test
    void getMergedRegion_cellJustOutside_returnsNull() {
        // Pin the boundary: col2+1 / row2+1 is OUTSIDE; the comparison is
        // <= not <.
        MergedRegion m = region(1, 2, 3, 5, 7);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Collections.singletonList(m));

        assertNull(lookup.getMergedRegion(6, 7));
        assertNull(lookup.getMergedRegion(5, 8));
        assertNull(lookup.getMergedRegion(1, 3));
        assertNull(lookup.getMergedRegion(2, 2));
    }

    @Test
    void getMergedRegion_firstContainingRegionWins() {
        // Pin: if regions overlap (a real Excel state — nested merges
        // before resolution), the lookup returns the first one in
        // iteration order that contains the cell. Callers that need
        // deterministic disambiguation must order the list upstream.
        MergedRegion outer = region(1, 1, 1, 10, 10);
        MergedRegion inner = region(2, 2, 2, 5, 5);
        MergedRegionLookup lookup = new MergedRegionLookup(
                () -> Arrays.asList(outer, inner));

        assertSame(outer, lookup.getMergedRegion(3, 3));
    }

    @Test
    void getMergedRegion_nullList_returnsNull() {
        MergedRegionLookup lookup = new MergedRegionLookup(() -> null);
        assertNull(lookup.getMergedRegion(1, 1));
    }

    @Test
    void queries_seeMutationsOfTheBackingList() {
        // Pin the "always-live" semantic: the lookup never caches the
        // list. Mutations between calls are visible. This is the reason
        // the constructor takes a Supplier rather than a List.
        List<MergedRegion> backing = new ArrayList<>();
        AtomicReference<List<MergedRegion>> ref = new AtomicReference<>(
                backing);
        SerializableSupplier<List<MergedRegion>> supplier = ref::get;
        MergedRegionLookup lookup = new MergedRegionLookup(supplier);

        // First call: empty list, returns null.
        assertNull(lookup.getMergedRegion(1, 1));

        // Mutate after construction; the lookup should see it.
        backing.add(region(1, 1, 1, 3, 3));
        assertNotNull(lookup.getMergedRegion(1, 1));

        // Swap to a different list entirely — also visible.
        ref.set(Collections.emptyList());
        assertNull(lookup.getMergedRegion(1, 1));
    }

    // The queries run for every cell of every range the server loads.

    @Test
    void queries_agreeWithAScanOfTheWholeList() {
        // Regions of every size, overlapping ones and some reaching across
        // many rows, so that the first one in the list has to win.
        Random random = new Random(42);
        List<MergedRegion> regions = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            int c1 = 1 + random.nextInt(30);
            int r1 = 1 + random.nextInt(400);
            int width = random.nextInt(4);
            int height = random.nextInt(10) == 0 ? random.nextInt(200)
                    : random.nextInt(3);
            regions.add(region(i, c1, r1, c1 + width, r1 + height));
        }
        MergedRegionLookup lookup = new MergedRegionLookup(() -> regions);

        for (int row = 1; row <= 620; row++) {
            for (int col = 1; col <= 36; col++) {
                assertSame(scanFor(regions, col, row),
                        lookup.getMergedRegion(col, row), col + "," + row);
                assertSame(scanStartingFrom(regions, col, row),
                        lookup.getMergedRegionStartingFrom(col, row),
                        col + "," + row);
            }
        }
    }

    private static MergedRegion scanFor(List<MergedRegion> regions, int col,
            int row) {
        for (MergedRegion r : regions) {
            if (r.col1 <= col && r.row1 <= row && r.col2 >= col
                    && r.row2 >= row) {
                return r;
            }
        }
        return null;
    }

    private static MergedRegion scanStartingFrom(List<MergedRegion> regions,
            int col, int row) {
        for (MergedRegion r : regions) {
            if (r.col1 == col && r.row1 == row) {
                return r;
            }
        }
        return null;
    }

    /** A list that counts how many of its elements are read. */
    private static final class CountingList extends ArrayList<MergedRegion> {
        private int reads;

        @Override
        public MergedRegion get(int index) {
            reads++;
            return super.get(index);
        }

        @Override
        public Iterator<MergedRegion> iterator() {
            Iterator<MergedRegion> it = super.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public MergedRegion next() {
                    reads++;
                    return it.next();
                }
            };
        }
    }

    @Test
    void queries_readTheListOnlyOnce() {
        // 20,000 merged regions made a screen of cells cost the server
        // seconds when every query read through all of them.
        CountingList regions = new CountingList();
        for (int i = 0; i < 1_000; i++) {
            regions.add(region(i, 1 + (i % 50) * 2, 1 + (i / 50) * 2,
                    2 + (i % 50) * 2, 2 + (i / 50) * 2));
        }
        MergedRegionLookup lookup = new MergedRegionLookup(() -> regions);
        assertNotNull(lookup.getMergedRegion(3, 3));
        int afterFirst = regions.reads;

        for (int row = 1; row <= 40; row++) {
            for (int col = 1; col <= 100; col++) {
                lookup.getMergedRegion(col, row);
                lookup.getMergedRegionStartingFrom(col, row);
            }
        }

        assertEquals(afterFirst, regions.reads);
    }

    @Test
    void aRegionAddedToTheList_isFound() {
        List<MergedRegion> regions = new ArrayList<>();
        regions.add(region(1, 1, 1, 2, 2));
        MergedRegionLookup lookup = new MergedRegionLookup(() -> regions);
        assertNull(lookup.getMergedRegion(5, 5));

        MergedRegion added = region(2, 5, 5, 6, 6);
        regions.add(added);

        assertSame(added, lookup.getMergedRegion(5, 5));
        assertSame(added, lookup.getMergedRegionStartingFrom(5, 5));
    }

    @Test
    void aListReplacedByAnotherOfTheSameSize_isIndexedAgain() {
        AtomicReference<List<MergedRegion>> ref = new AtomicReference<>(
                List.of(region(1, 1, 1, 2, 2)));
        MergedRegionLookup lookup = new MergedRegionLookup(ref::get);
        assertNotNull(lookup.getMergedRegion(1, 1));

        MergedRegion other = region(2, 5, 5, 6, 6);
        ref.set(List.of(other));

        assertNull(lookup.getMergedRegion(1, 1));
        assertSame(other, lookup.getMergedRegion(6, 6));
    }
}
