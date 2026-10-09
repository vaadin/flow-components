/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * The heights {@link CustomComponentRowSizer} gives the rows that hold custom
 * components.
 *
 * <p>
 * Three behaviors pinned:
 *
 * <ul>
 * <li>Grow: rows that now host a component get bumped to
 * {@code minimumRowHeightForComponents} if smaller. Hidden rows are
 * skipped.</li>
 * <li>Restore: rows that previously hosted a component but no longer do are
 * reset to either {@code 0} (hidden) or to the POI row's height (with the
 * active sheet's default-row-height fallback when the row is null in POI).</li>
 * <li>Memory: {@code currentRowsWithComponents} stores the latest input so the
 * next call can compute the restore set. Untouched state means the first call
 * has no restore to do.</li>
 * </ul>
 */
class CustomComponentRowSizerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Sheet activeSheet;

    private CustomComponentRowSizer sizer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sizer = new CustomComponentRowSizer(spreadsheet);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(activeSheet);
        lenient().when(spreadsheet.getMinimumRowHeightForComponents())
                .thenReturn(50);
        lenient().when(spreadsheet.isRowHidden(anyInt())).thenReturn(false);
        lenient().when(activeSheet.getDefaultRowHeightInPoints())
                .thenReturn(15f);
    }

    @Test
    void firstCall_growsRowsWithComponents_belowMinimum() {
        // Pin: a fresh sizer with no prior state grows rows whose current
        // height is below the minimum.
        float[] heights = { 10f, 20f, 30f, 40f };
        when(spreadsheet.getRowH()).thenReturn(heights);

        sizer.applyHeightsFor(new HashSet<>(java.util.Arrays.asList(0, 2)));

        float[] expected = { 50f, 20f, 50f, 40f };
        assertArrayEquals(expected, captureUpdatedHeights());
    }

    @Test
    void firstCall_doesNotShrinkRowsAboveMinimum() {
        // Pin: rows already taller than the minimum are left alone.
        float[] heights = { 100f, 200f };
        when(spreadsheet.getRowH()).thenReturn(heights);

        sizer.applyHeightsFor(new HashSet<>(java.util.Arrays.asList(0, 1)));

        assertArrayEquals(new float[] { 100f, 200f }, captureUpdatedHeights());
    }

    @Test
    void firstCall_skipsHiddenRows_evenIfTheyHaveAComponent() {
        // Pin: hidden rows are not grown — keeping them hidden matters
        // more than showing the component.
        float[] heights = { 10f, 20f };
        when(spreadsheet.getRowH()).thenReturn(heights);
        when(spreadsheet.isRowHidden(0)).thenReturn(true);

        sizer.applyHeightsFor(Collections.singleton(0));

        // Row 0 stays at 10f despite being in the components set.
        assertArrayEquals(new float[] { 10f, 20f }, captureUpdatedHeights());
    }

    @Test
    void secondCall_restoresPreviousRow_thatNoLongerHostsAComponent() {
        // Pin the restore branch: after the first call sets row 0 to the
        // minimum, the next call without row 0 in the components set
        // restores its POI height.
        float[] firstHeights = { 10f, 20f };
        when(spreadsheet.getRowH()).thenReturn(firstHeights);
        sizer.applyHeightsFor(Collections.singleton(0));

        Row row0 = mock(Row.class);
        when(activeSheet.getRow(0)).thenReturn(row0);
        when(row0.getHeightInPoints()).thenReturn(12.5f);
        // Second call: row 0 no longer has a component.
        float[] secondHeights = { 50f, 20f }; // reflects first-call state
        when(spreadsheet.getRowH()).thenReturn(secondHeights);

        sizer.applyHeightsFor(Collections.emptySet());

        // Row 0 restored to its POI height (12.5).
        ArgumentCaptor<float[]> captor = ArgumentCaptor.forClass(float[].class);
        verify(spreadsheet, org.mockito.Mockito.atLeastOnce())
                .setRowH(captor.capture());
        float[] last = captor.getAllValues()
                .get(captor.getAllValues().size() - 1);
        assertEquals(12.5f, last[0]);
        assertEquals(20f, last[1]);
    }

    @Test
    void secondCall_restoresHiddenPriorRow_toZero() {
        // Pin: when restoring a row that's hidden, the height is set to 0
        // (matches POI's hidden-row representation).
        float[] firstHeights = { 10f };
        when(spreadsheet.getRowH()).thenReturn(firstHeights);
        sizer.applyHeightsFor(Collections.singleton(0));

        // Now row 0 is hidden when the restore runs.
        when(spreadsheet.isRowHidden(0)).thenReturn(true);
        float[] secondHeights = { 50f };
        when(spreadsheet.getRowH()).thenReturn(secondHeights);

        sizer.applyHeightsFor(Collections.emptySet());

        ArgumentCaptor<float[]> captor = ArgumentCaptor.forClass(float[].class);
        verify(spreadsheet, org.mockito.Mockito.atLeastOnce())
                .setRowH(captor.capture());
        float[] last = captor.getAllValues()
                .get(captor.getAllValues().size() - 1);
        assertEquals(0f, last[0]);
    }

    @Test
    void secondCall_restoresMissingRow_toDefaultRowHeightFallback() {
        // Pin: a row that doesn't exist in POI on restore uses the sheet's
        // defaultRowHeightInPoints. Important because rows that had
        // components may have been deleted from POI without being deleted
        // from the height array.
        float[] firstHeights = { 10f };
        when(spreadsheet.getRowH()).thenReturn(firstHeights);
        sizer.applyHeightsFor(Collections.singleton(0));

        when(activeSheet.getRow(0)).thenReturn(null);
        float[] secondHeights = { 50f };
        when(spreadsheet.getRowH()).thenReturn(secondHeights);

        sizer.applyHeightsFor(Collections.emptySet());

        ArgumentCaptor<float[]> captor = ArgumentCaptor.forClass(float[].class);
        verify(spreadsheet, org.mockito.Mockito.atLeastOnce())
                .setRowH(captor.capture());
        float[] last = captor.getAllValues()
                .get(captor.getAllValues().size() - 1);
        assertEquals(15f, last[0]); // defaultRowHeightInPoints
    }

    @Test
    void rowsThatStayInBothCalls_areNotRestored() {
        // Pin: a row that's in BOTH calls stays at the minimum height —
        // not restored. The restore branch only runs for rows that left
        // the components set.
        float[] firstHeights = { 10f, 10f };
        when(spreadsheet.getRowH()).thenReturn(firstHeights);
        sizer.applyHeightsFor(new HashSet<>(java.util.Arrays.asList(0, 1)));

        float[] secondHeights = { 50f, 50f };
        when(spreadsheet.getRowH()).thenReturn(secondHeights);
        sizer.applyHeightsFor(Collections.singleton(0));

        ArgumentCaptor<float[]> captor = ArgumentCaptor.forClass(float[].class);
        verify(spreadsheet, org.mockito.Mockito.atLeastOnce())
                .setRowH(captor.capture());
        float[] last = captor.getAllValues()
                .get(captor.getAllValues().size() - 1);
        assertEquals(50f, last[0]); // still in components → still 50
        // Row 1 was in the prior set but not in this set → restored.
    }

    @Test
    void firstCallMemory_canBeQueriedByPassingEmptySetSecond() {
        // Pin: the memory of currentRowsWithComponents is stored from the
        // first call so the second can compute the diff. Hands-off check:
        // call apply([0]), then apply([]); the second call must run the
        // restore branch for row 0.
        float[] heights = { 10f };
        when(spreadsheet.getRowH()).thenReturn(heights);
        sizer.applyHeightsFor(Collections.singleton(0));

        float[] heightsAfter = { 50f };
        when(spreadsheet.getRowH()).thenReturn(heightsAfter);
        Row row0 = mock(Row.class);
        when(activeSheet.getRow(0)).thenReturn(row0);
        when(row0.getHeightInPoints()).thenReturn(18f);

        sizer.applyHeightsFor(Collections.emptySet());

        // If memory hadn't been stored, row 0 wouldn't be restored.
        ArgumentCaptor<float[]> captor = ArgumentCaptor.forClass(float[].class);
        verify(spreadsheet, org.mockito.Mockito.atLeastOnce())
                .setRowH(captor.capture());
        assertEquals(18f, captor.getAllValues().get(1)[0]);
    }

    // ----- helpers ----------------------------------------------------------

    private float[] captureUpdatedHeights() {
        ArgumentCaptor<float[]> captor = ArgumentCaptor.forClass(float[].class);
        verify(spreadsheet).setRowH(captor.capture());
        return captor.getValue();
    }

    private static <T> Set<T> setOf(T... values) {
        return new HashSet<>(java.util.Arrays.asList(values));
    }
}
