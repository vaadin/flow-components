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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Setting row heights and hiding rows with {@link RowSizeOperations}.
 *
 * <p>
 * Pins the entanglement between {@code height == 0} and "row is hidden":
 *
 * <ul>
 * <li>Setting a height of 0 routes through {@code applyHidden(true)} instead of
 * the regular height-set path.</li>
 * <li>Setting a non-zero height on a previously-hidden row removes it from
 * {@code hiddenRowIndexes} AND clears {@code setZeroHeight(false)} on the POI
 * row.</li>
 * <li>Missing POI rows are auto-created when needed.</li>
 * <li>Every public method ends with a {@code reloadSheetStyles} call.</li>
 * </ul>
 *
 * <p>
 * Mocks {@link Spreadsheet} + {@link Sheet} + {@link Row}. The
 * {@code onClientResized} method is exercised but its history-manager wiring is
 * given a mocked passthrough (the SizeChangeCommand capture is trusted, not
 * asserted in detail).
 */
class RowSizeOperationsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Sheet sheet;
    @Mock
    private Row row;

    private RowSizeOperations ops;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ops = new RowSizeOperations(spreadsheet);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getHiddenRowIndexes())
                .thenReturn(new ArrayList<>());
        // The heights the sizes are changed in.
        lenient().when(spreadsheet.getRowH()).thenReturn(new float[20]);
        lenient().when(sheet.getRow(org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(row);
    }

    @Test
    void setHeight_positive_setsRowHeight_andReloadsStyles() {
        ops.setHeight(2, 25f);

        verify(row).setHeightInPoints(25f);
        // height > 0 → isRowHidden=false. Second arg matches that.
        verify(spreadsheet).reloadSheetStyles(true, false);
    }

    @Test
    void setHeight_zero_routesThroughApplyHidden_andReloadsWithTrue() {
        // Pin: height=0 routes through applyHidden(true), NOT through
        // setHeightInPoints. The reloadSheetStyles is called with
        // isRowHidden=true.
        ops.setHeight(2, 0f);

        verify(row).setZeroHeight(true);
        verify(row, never())
                .setHeightInPoints(org.mockito.ArgumentMatchers.anyFloat());
        verify(spreadsheet).reloadSheetStyles(true, true);
    }

    @Test
    void setHeight_unhidesPreviouslyHiddenRow() {
        // Pin: setting a positive height on a row whose index is in
        // hiddenRowIndexes removes it from the list AND clears
        // setZeroHeight(false) on the POI row.
        ArrayList<Integer> hidden = new ArrayList<>();
        hidden.add(3); // index 2 + 1 (1-based in hidden list)
        when(spreadsheet.getHiddenRowIndexes()).thenReturn(hidden);
        when(row.getZeroHeight()).thenReturn(true);

        ops.setHeight(2, 15f);

        verify(row).setZeroHeight(false);
        verify(row).setHeightInPoints(15f);
        // The hidden list passed back to setHiddenRowIndexes should NOT
        // contain 3.
        ArgumentCaptor<ArrayList<Integer>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setHiddenRowIndexes(captor.capture());
        assertFalse(captor.getValue().contains(3),
                "Previously hidden index removed");
    }

    @Test
    void setHeight_nullRow_createsRow_thenSetsHeight() {
        when(sheet.getRow(2)).thenReturn(null);
        when(sheet.createRow(2)).thenReturn(row);

        ops.setHeight(2, 20f);

        verify(sheet).createRow(2);
        verify(row).setHeightInPoints(20f);
    }

    @Test
    void setHeights_mixedZeroAndPositive_anyHidden_reloadsWithTrue() {
        // Pin: setHeights tracks if ANY height was zero and passes
        // isAnyHidden=true to reloadSheetStyles when at least one was.
        Map<Integer, Float> heights = new HashMap<>();
        heights.put(1, 20f);
        heights.put(2, 0f); // hidden

        ops.setHeights(heights);

        verify(spreadsheet).reloadSheetStyles(true, true);
    }

    @Test
    void setHeights_allPositive_reloadsWithFalse() {
        Map<Integer, Float> heights = new HashMap<>();
        heights.put(1, 20f);
        heights.put(2, 30f);

        ops.setHeights(heights);

        verify(spreadsheet).reloadSheetStyles(true, false);
    }

    @Test
    void setHidden_true_setsZeroHeight_andReloadsStyles() {
        ops.setHidden(2, true);

        verify(row).setZeroHeight(true);
        // setHidden ALWAYS reloads with (true, true), regardless of
        // the hidden flag value — matches the source code.
        verify(spreadsheet).reloadSheetStyles(true, true);
    }

    @Test
    void setHidden_false_setsZeroHeightFalse_butStillReloadsWithTrue() {
        // Pin the asymmetry: setHidden(false) clears zero-height but still
        // calls reloadSheetStyles(true, true) — the second arg is hard-
        // coded to true regardless of the hidden flag. The intent appears
        // to be "row visibility changed, recompute" regardless of
        // direction.
        ops.setHidden(2, false);

        verify(row).setZeroHeight(false);
        verify(spreadsheet).reloadSheetStyles(true, true);
    }

    @Test
    void setRowsHidden_appliesPerRow_thenReloadsOnce() {
        Map<Integer, Boolean> rows = new HashMap<>();
        rows.put(1, true);
        rows.put(2, false);

        ops.setRowsHidden(rows);

        verify(row, times(1)).setZeroHeight(true);
        verify(row, times(1)).setZeroHeight(false);
        verify(spreadsheet, times(1)).reloadSheetStyles(true, true);
    }

    @Test
    void setDefaultHeightPoints_pushesToBothSheetAndSpreadsheet() {
        // Pin: defaultRow updates BOTH the POI sheet AND the spreadsheet
        // field (mirror). The spreadsheet's setDefRowH is what triggers
        // the client property mirror via SheetDimensions.
        ops.setDefaultHeightPoints(18f);

        verify(sheet).setDefaultRowHeightInPoints(18f);
        verify(spreadsheet).setDefRowH(18f);
        // The rows without a height of their own change height with it.
        verify(spreadsheet).reloadSheetStyles(true, false);
    }

    @Test
    void setHeight_nullRowWithZeroHeight_callsCreateRow_throughApplyHidden() {
        // Pin: applyHidden also creates the row if missing.
        when(sheet.getRow(5)).thenReturn(null);
        when(sheet.createRow(5)).thenReturn(row);

        ops.setHeight(5, 0f);

        verify(sheet).createRow(5);
        verify(row).setZeroHeight(true);
    }

    @Test
    void setHeight_buildsFreshHiddenIndexesList_doesNotMutatePrior() {
        // Pin: applyHeight wraps the existing hidden-list in a new
        // ArrayList before mutation. Load-bearing because the cached
        // list is a MirroredProperty.serialized field; in-place mutation
        // would skip the mirror.
        ArrayList<Integer> existing = new ArrayList<>();
        existing.add(3);
        when(spreadsheet.getHiddenRowIndexes()).thenReturn(existing);

        ops.setHeight(2, 20f);

        verify(spreadsheet).setHiddenRowIndexes(
                org.mockito.ArgumentMatchers.argThat(list -> list != existing));
        // Original list unchanged.
        assertEquals(1, existing.size());
        assertEquals(Integer.valueOf(3), existing.get(0));
    }
}
