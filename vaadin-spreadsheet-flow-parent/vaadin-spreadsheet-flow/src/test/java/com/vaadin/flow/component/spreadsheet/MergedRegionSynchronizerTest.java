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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * Rebuilding the client's list of merged regions from POI with
 * {@link MergedRegionSynchronizer}.
 *
 * <p>
 * Pins the {@code rebuildFromPoi} contract: read POI's regions, build a fresh
 * {@code MergedRegion} DTO for each with 1-based coordinates and a
 * monotonically-incrementing id, and call {@code setMergedRegions(rebuilt)}.
 * The empty-sheet case short-circuits to {@code setMergedRegions(null)}.
 *
 * <p>
 * Mocks the parent {@link Spreadsheet}. The {@link Sheet} is a POI sheet, spied
 * on.
 */
class MergedRegionSynchronizerTest {

    @Mock
    private Spreadsheet spreadsheet;

    private Sheet sheet;

    private MergedRegionSynchronizer sync;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sheet = Mockito.spy(new XSSFWorkbook().createSheet());
        when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        sync = new MergedRegionSynchronizer(spreadsheet);
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

    @Test
    void rebuildFromPoi_emptySheet_setsMergedRegionsToNull() {
        // Pin the short-circuit: when POI has zero regions, the synchronizer
        // calls setMergedRegions(null) and returns immediately — it does NOT
        // pass an empty ArrayList. Load-bearing because null vs empty is
        // observable on the serialized property mirror to the client.
        sync.rebuildFromPoi();

        verify(spreadsheet).setMergedRegions(isNull());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rebuildFromPoi_populatedSheet_buildsFullList_with1BasedCoords() {
        // Pin: every POI region is reflected into the rebuilt list with
        // 1-based coordinates (POI's 0-based -> +1). The rebuilt list size
        // matches POI's count exactly.
        poiHas(range(0, 0, 1, 1), range(5, 5, 7, 7));

        sync.rebuildFromPoi();

        ArgumentCaptor<ArrayList<MergedRegion>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setMergedRegions(captor.capture());
        ArrayList<MergedRegion> rebuilt = captor.getValue();
        assertEquals(2, rebuilt.size());

        MergedRegion first = rebuilt.get(0);
        assertEquals(1, first.col1);
        assertEquals(1, first.row1);
        assertEquals(2, first.col2);
        assertEquals(2, first.row2);

        MergedRegion second = rebuilt.get(1);
        assertEquals(6, second.col1);
        assertEquals(6, second.row1);
        assertEquals(8, second.col2);
        assertEquals(8, second.row2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void rebuildFromPoi_assignsMonotonicSequentialIds() {
        // Pin: ids come from spreadsheet.mergedRegionCounter via direct
        // post-increment. Two POI regions → ids 0 and 1 (counter starts at
        // 0 on a fresh mock). The synchronizer does not reset the counter
        // — a subsequent rebuild would continue minting fresh ids, as
        // MergedRegionOperations does (ids are monotonic across the
        // spreadsheet's lifetime).
        poiHas(range(0, 0, 0, 1), range(1, 1, 1, 2));

        sync.rebuildFromPoi();

        ArgumentCaptor<ArrayList<MergedRegion>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setMergedRegions(captor.capture());
        ArrayList<MergedRegion> rebuilt = captor.getValue();
        assertEquals(0, rebuilt.get(0).id);
        assertEquals(1, rebuilt.get(1).id);
    }

    @Test
    @SuppressWarnings("unchecked")
    void rebuildFromPoi_singleRegion_buildsListOfOne() {
        poiHas(range(2, 3, 4, 5));

        sync.rebuildFromPoi();

        ArgumentCaptor<ArrayList<MergedRegion>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setMergedRegions(captor.capture());
        ArrayList<MergedRegion> rebuilt = captor.getValue();
        assertEquals(1, rebuilt.size());
        // POI range(r1=2, c1=3, r2=4, c2=5) → 1-based (col1=4, row1=3,
        // col2=6, row2=5).
        MergedRegion only = rebuilt.get(0);
        assertEquals(4, only.col1);
        assertEquals(3, only.row1);
        assertEquals(6, only.col2);
        assertEquals(5, only.row2);
    }

    @Test
    void rebuildFromPoi_doesNotConsultExistingCache() {
        // Pin: the synchronizer is "POI is the source of truth" — it never
        // calls spreadsheet.getMergedRegions(). The rebuild is a one-way
        // POI→cache propagation. This is the load-bearing distinction
        // between rebuildFromPoi and the per-region add/remove path.
        poiHas(range(0, 0, 0, 1));

        sync.rebuildFromPoi();

        verify(spreadsheet, never()).getMergedRegions();
    }

    @Test
    void rebuildFromPoi_readsPoisRegionsAllAtOnce() {
        // XSSF finds a region by its index by going through those before it:
        // read one by one, 20,000 regions took 0.7 s whenever rows moved.
        poiHas(range(0, 0, 1, 1), range(5, 5, 7, 7), range(9, 0, 9, 3));

        sync.rebuildFromPoi();

        verify(sheet, never()).getMergedRegion(anyInt());
        verify(sheet, times(1)).getMergedRegions();
    }
}
