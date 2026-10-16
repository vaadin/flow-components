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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Merging and unmerging cells with {@link MergedRegionOperations}.
 *
 * <p>
 * Pins validation rules (overlap rejected, enclose rejected, inside discarded),
 * the POI-write + cache-update + cascade-fire ordering, and the
 * {@code mergedRegionCounter} sequential-id mint on the parent
 * {@link Spreadsheet}.
 *
 * <p>
 * Mocks the {@link Spreadsheet}, the {@link MergedRegionCascade}, and the
 * {@link CellSelectionManager}. The {@link Sheet} is a POI sheet, spied on.
 * Uses an {@link AtomicReference} as the source-of-truth for the cached
 * merged-region list so the stateful getter/setter pair on the parent can be
 * reproduced without a real Spreadsheet instance.
 */
class MergedRegionOperationsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private MergedRegionCascade cascade;
    @Mock
    private CellSelectionManager selectionManager;

    private Sheet sheet;

    private AtomicReference<ArrayList<MergedRegion>> cachedRegions;
    private MergedRegionOperations operations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sheet = Mockito.spy(new XSSFWorkbook().createSheet());
        cachedRegions = new AtomicReference<>(null);
        when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        when(spreadsheet.getCellSelectionManager())
                .thenReturn(selectionManager);
        when(spreadsheet.getMergedRegions())
                .thenAnswer(inv -> cachedRegions.get());
        Mockito.doAnswer(inv -> {
            cachedRegions.set(inv.getArgument(0));
            return null;
        }).when(spreadsheet).setMergedRegions(any());

        operations = new MergedRegionOperations(spreadsheet, cascade);
    }

    private CellRangeAddress range(int r1, int c1, int r2, int c2) {
        return new CellRangeAddress(r1, r2, c1, c2);
    }

    /** Gives the sheet these merged regions, in this order. */
    private void poiHas(CellRangeAddress... regions) {
        for (CellRangeAddress region : regions) {
            sheet.addMergedRegion(region);
        }
        Mockito.clearInvocations(sheet);
    }

    /** Gives the sheet and the client the same regions, in this order. */
    private void bothHave(CellRangeAddress... regions) {
        poiHas(regions);
        ArrayList<MergedRegion> cached = new ArrayList<>();
        for (CellRangeAddress region : regions) {
            cached.add(new MergedRegion(region.getFirstColumn() + 1,
                    region.getFirstRow() + 1, region.getLastColumn() + 1,
                    region.getLastRow() + 1));
        }
        cachedRegions.set(cached);
    }

    private List<String> poiRegions() {
        return sheet.getMergedRegions().stream()
                .map(CellRangeAddress::formatAsString).toList();
    }

    private List<String> clientRegions() {
        return cachedRegions.get().stream()
                .map(region -> new CellRangeAddress(region.row1 - 1,
                        region.row2 - 1, region.col1 - 1, region.col2 - 1)
                        .formatAsString())
                .toList();
    }

    // ----- addRegion: happy path --------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void addRegion_onEmptySheet_mutatesPoi_buildsRegion_andFiresCascade() {
        CellRangeAddress r = range(0, 0, 1, 1);

        operations.addRegion(r);

        // POI mutated.
        verify(sheet).addMergedRegion(r);

        // Cache updated with a list containing one region; the region was
        // built from the range with 1-based coordinates and the mint id 0.
        ArgumentCaptor<ArrayList<MergedRegion>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setMergedRegions(captor.capture());
        ArrayList<MergedRegion> stored = captor.getValue();
        assertEquals(1, stored.size());
        MergedRegion built = stored.get(0);
        assertEquals(1, built.col1);
        assertEquals(1, built.row1);
        assertEquals(2, built.col2);
        assertEquals(2, built.row2);
        assertEquals(0, built.id);

        // Cascade fired with the same region instance.
        verify(cascade).afterMerge(built);
        // SelectionManager notified.
        verify(selectionManager).mergedRegionAdded(r);
        // updateMarkedCells fired exactly once.
        verify(spreadsheet, times(1)).updateMarkedCells();
    }

    @Test
    void addRegion_appendsToExistingCache_withoutMutatingPriorList() {
        // Pin: the operation builds a NEW ArrayList from the existing list
        // and appends; the prior list reference is not mutated in place.
        // This matters because the cache is a serialized property — any
        // in-place mutation would skip the mirror to the client.
        ArrayList<MergedRegion> existing = new ArrayList<>();
        MergedRegion seed = new MergedRegion(10, 10, 11, 11);
        seed.id = 5;
        existing.add(seed);
        cachedRegions.set(existing);

        operations.addRegion(range(0, 0, 1, 1));

        ArrayList<MergedRegion> stored = cachedRegions.get();
        assertEquals(2, stored.size());
        assertSame(seed, stored.get(0));
        // The setter received a list distinct from `existing` (defensive
        // new ArrayList<>(...) copy).
        assertTrue(stored != existing,
                "Operation must not mutate the prior cache list in place");
    }

    @Test
    @SuppressWarnings("unchecked")
    void addRegion_mintsSequentialIds_perCounter() {
        // Pin: mergedRegionCounter on the parent Spreadsheet is read via
        // direct field access (not a getter), so the post-increment ++
        // is observable from outside. Two adds → ids 0 and 1.

        operations.addRegion(range(0, 0, 1, 1));
        operations.addRegion(range(5, 5, 6, 6));

        ArgumentCaptor<ArrayList<MergedRegion>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet, times(2)).setMergedRegions(captor.capture());
        ArrayList<MergedRegion> firstSnapshot = captor.getAllValues().get(0);
        ArrayList<MergedRegion> secondSnapshot = captor.getAllValues().get(1);
        assertEquals(0, firstSnapshot.get(0).id);
        // Second snapshot is built fresh from the prior cache (which we
        // mirror via AtomicReference). The new region is appended at the
        // end with id 1.
        assertEquals(1, secondSnapshot.get(secondSnapshot.size() - 1).id);
    }

    // ----- addRegion: validation --------------------------------------------

    @Test
    void addRegion_overlappingExisting_throwsIllegalArgumentException() {
        // Pin: OVERLAP (partial intersection) is rejected. No POI mutation,
        // no cascade fire, no cache update.
        CellRangeAddress existing = range(0, 0, 2, 2);
        poiHas(existing);

        CellRangeAddress candidate = range(1, 1, 3, 3);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> operations.addRegion(candidate));
        assertTrue(ex.getMessage().contains("overlaps"));
        verify(sheet, never()).addMergedRegion(any());
        verify(cascade, never()).afterMerge(any());
    }

    @Test
    void addRegion_enclosingExisting_throwsIllegalArgumentException() {
        // Pin: ENCLOSES (the existing region wholly contains the candidate)
        // is rejected with a different message. Distinct from OVERLAP.
        CellRangeAddress existing = range(0, 0, 5, 5);
        poiHas(existing);

        CellRangeAddress candidate = range(1, 1, 2, 2);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> operations.addRegion(candidate));
        assertTrue(ex.getMessage().contains("encloses"));
        verify(sheet, never()).addMergedRegion(any());
    }

    @Test
    void addRegion_discardsExistingThatLiesInsideCandidate() {
        // Pin: an existing region wholly INSIDE the candidate is silently
        // removed first, then the candidate is added. POI sees a
        // removeMergedRegion(0) followed by addMergedRegion(candidate).
        CellRangeAddress existing = range(2, 2, 3, 3);
        CellRangeAddress candidate = range(0, 0, 5, 5);
        MergedRegion existingCached = new MergedRegion(2, 2, 3, 3);
        existingCached.id = 11;
        poiHas(existing);
        cachedRegions.set(new ArrayList<>(
                java.util.Collections.singletonList(existingCached)));

        operations.addRegion(candidate);

        InOrder inOrder = Mockito.inOrder(sheet);
        inOrder.verify(sheet).removeMergedRegion(0);
        inOrder.verify(sheet).addMergedRegion(candidate);
        // Capture the cascade args to pin coordinates AND enforce call
        // order via InOrder — a regression that swapped afterUnmerge and
        // afterMerge would otherwise pass plain verify(...) because each
        // method only fires once and Mockito does not order verifies
        // across separate calls.
        InOrder cascadeOrder = Mockito.inOrder(cascade);
        ArgumentCaptor<MergedRegion> unmerged = ArgumentCaptor
                .forClass(MergedRegion.class);
        ArgumentCaptor<MergedRegion> merged = ArgumentCaptor
                .forClass(MergedRegion.class);
        cascadeOrder.verify(cascade).afterUnmerge(unmerged.capture());
        cascadeOrder.verify(cascade).afterMerge(merged.capture());
        // The unmerge sees the EXISTING-inside region's coordinates
        // (cached as-is, no offset). The merge sees the CANDIDATE's
        // coordinates +1 (the SUT uses 1-based cache, 0-based POI).
        // Swapped wiring would fail here because the value ranges don't
        // overlap.
        assertEquals(2, unmerged.getValue().row1);
        assertEquals(3, unmerged.getValue().row2);
        assertEquals(2, unmerged.getValue().col1);
        assertEquals(3, unmerged.getValue().col2);
        assertEquals(1, merged.getValue().row1);
        assertEquals(6, merged.getValue().row2);
        assertEquals(1, merged.getValue().col1);
        assertEquals(6, merged.getValue().col2);
    }

    @Test
    void addRegion_discardsEveryRegionInside_keepingTheOthersInOrder() {
        // A1:B2 and A4:B5 lie inside A1:C6, D1:E2 and H1:H2 do not.
        bothHave(range(0, 0, 1, 1), range(0, 3, 1, 4), range(3, 0, 4, 1),
                range(0, 7, 1, 7));

        operations.addRegion(range(0, 0, 5, 2));

        assertEquals(List.of("D1:E2", "H1:H2", "A1:C6"), poiRegions());
        assertEquals(List.of("D1:E2", "H1:H2", "A1:C6"), clientRegions());
    }

    @Test
    void addRegion_nextToTheOthers_keepsThemAll() {
        bothHave(range(0, 0, 1, 1), range(0, 3, 1, 4));

        operations.addRegion(range(0, 2, 1, 2));

        assertEquals(List.of("A1:B2", "D1:E2", "C1:C2"), poiRegions());
        assertEquals(List.of("A1:B2", "D1:E2", "C1:C2"), clientRegions());
    }

    // ----- removeRegion -----------------------------------------------------

    @Test
    void removeRegion_capturesRange_thenMutatesPoi_andFiresUnmergeCascade() {
        CellRangeAddress existing = range(0, 0, 1, 1);
        poiHas(existing);
        MergedRegion cached = new MergedRegion(1, 1, 2, 2);
        cached.id = 42;
        cachedRegions.set(
                new ArrayList<>(java.util.Collections.singletonList(cached)));

        operations.removeRegion(0);

        InOrder inOrder = Mockito.inOrder(sheet, cascade, spreadsheet,
                selectionManager);
        inOrder.verify(sheet).removeMergedRegion(0);
        inOrder.verify(cascade).afterUnmerge(cached);
        inOrder.verify(spreadsheet).updateMarkedCells();
        inOrder.verify(selectionManager).mergedRegionRemoved(existing);

        ArrayList<MergedRegion> after = cachedRegions.get();
        assertTrue(after.isEmpty(),
                "Cache list must shrink by exactly one entry");
    }

    @Test
    void removeRegion_byIndex_pinsIndexEqualityBetweenPoiAndCache() {
        // Pin a non-obvious invariant: the integer index passed in is used
        // BOTH as the POI removeMergedRegion(index) argument AND as the
        // ArrayList.remove(index) on the cache. The caller must guarantee
        // the POI and cache orderings are aligned — the operation
        // doesn't verify alignment.
        CellRangeAddress existing = range(5, 5, 6, 6);
        poiHas(range(0, 0, 1, 1), range(2, 2, 3, 3), existing);
        ArrayList<MergedRegion> cache = new ArrayList<>();
        cache.add(new MergedRegion(0, 0, 1, 1));
        cache.add(new MergedRegion(2, 2, 3, 3));
        MergedRegion targetCache = new MergedRegion(5, 5, 6, 6);
        cache.add(targetCache);
        cachedRegions.set(cache);

        operations.removeRegion(2);

        verify(sheet).removeMergedRegion(2);
        verify(cascade).afterUnmerge(eq(targetCache));
        ArrayList<MergedRegion> after = cachedRegions.get();
        assertEquals(2, after.size());
    }
}
