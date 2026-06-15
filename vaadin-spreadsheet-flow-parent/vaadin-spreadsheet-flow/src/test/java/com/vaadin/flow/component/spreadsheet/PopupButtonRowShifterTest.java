/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Characterization tests for {@link PopupButtonRowShifter}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>Empty-registry early-return — no detaches, no replaceAll.</li>
 * <li>Detach-first contract: every button gets detached before the remap,
 * because its row index is now stale.</li>
 * <li>Three row-classification branches: row removed, row shifted (above range
 * changed), row unaffected.</li>
 * <li>`replaceAll` is called with the rebuilt map.</li>
 * </ul>
 */
class PopupButtonRowShifterTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private PopupButtonRegistry registry;
    @Mock
    private PopupButtonBinding binding;
    @Mock
    private Sheet activeSheet;

    private PopupButtonRowShifter shifter;
    private PopupButton button1;
    private PopupButton button2;
    private PopupButton button3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        shifter = new PopupButtonRowShifter(spreadsheet, registry, binding);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(activeSheet);
        lenient().when(activeSheet.getSheetName()).thenReturn("Sheet1");
        button1 = mock(PopupButton.class);
        button2 = mock(PopupButton.class);
        button3 = mock(PopupButton.class);
    }

    @Test
    void shift_emptyRegistry_noOp() {
        when(registry.isEmpty()).thenReturn(true);

        shifter.shift(1, 5, 2);

        verify(binding, never()).detach(any());
        verify(registry, never()).replaceAll(any());
    }

    @Test
    void shift_rowAboveAffectedRange_unaffected() {
        // Pin: a button at row 0 when shifting [first=5, last=10] is
        // unaffected. Its cell reference goes into the rebuilt map
        // unchanged.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref0 = new CellReference("Sheet1", 0, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref0);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        // Detached first.
        verify(binding).detach(button1);
        // Reference NOT updated.
        verify(button1, never()).setCellReference(any());
        // replaceAll called with original reference.
        verify(registry)
                .replaceAll(argThat(updated -> updated.get(ref0) == button1));
    }

    @Test
    void shift_rowShifted_setCellReferenceWithOffset() {
        // Pin: a button at row 11 when shifting [first=5, last=10] with
        // n=3 has row > last=10 AND row >= first=5 → number of rows above
        // changed. New row = 11 + 3 = 14. setCellReference called with
        // the new ref; the new ref becomes the key in the rebuilt map.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref11 = new CellReference("Sheet1", 11, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref11);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(binding).detach(button1);
        verify(button1).setCellReference(argThat(
                ref -> ref != null && ref.getRow() == 14 && ref.getCol() == 1
                        && "Sheet1".equals(ref.getSheetName())));
        verify(registry).replaceAll(
                argThat(updated -> updated.values().contains(button1)
                        && updated.size() == 1));
    }

    @Test
    void shift_rowRemoved_buttonDropped() {
        // Pin: when n is negative, a row in [first, first - n) was removed.
        // The button doesn't make it into the rebuilt map → effectively
        // dropped from the registry. binding.detach still fires (the
        // button must be cleaned up).
        when(registry.isEmpty()).thenReturn(false);
        // Shift first=5, last=10, n=-3 → rows 5..7 are removed.
        CellReference ref6 = new CellReference("Sheet1", 6, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref6);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, -3);

        verify(binding).detach(button1);
        verify(button1, never()).setCellReference(any());
        // The rebuilt map is empty — the button was dropped.
        verify(registry).replaceAll(argThat(updated -> updated.isEmpty()));
    }

    @Test
    void shift_rowInBetweenFirstAndLast_butNotShiftedOut_isUnaffected() {
        // Pin: a button at row 7 when shifting [first=5, last=10, n=3]:
        // - row >= first (7 >= 5) AND row > last (7 > 10) → false
        // - Therefore numberOfRowsAboveWasChanged returns false
        // - n is positive so rowWasRemoved returns false
        // → button is unaffected.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref7 = new CellReference("Sheet1", 7, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref7);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(button1, never()).setCellReference(any());
        verify(registry)
                .replaceAll(argThat(updated -> updated.get(ref7) == button1));
    }

    @Test
    void shift_mixedButtons_eachClassifiedIndependently() {
        // Pin: each button is classified by its own row. button1=unaffected,
        // button2=shifted, button3=removed. The rebuilt map contains
        // button1 (original key) + button2 (new key) — button3 dropped.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref0 = new CellReference("Sheet1", 0, 1, true, true);
        CellReference ref15 = new CellReference("Sheet1", 15, 1, true, true);
        CellReference ref6 = new CellReference("Sheet1", 6, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref0);
        when(button2.getCellReference()).thenReturn(ref15);
        when(button3.getCellReference()).thenReturn(ref6);
        when(registry.values())
                .thenReturn(Arrays.asList(button1, button2, button3));

        shifter.shift(5, 10, -3); // removes rows 5..7

        // All three detached.
        verify(binding).detach(button1);
        verify(binding).detach(button2);
        verify(binding).detach(button3);

        // button1 unaffected (row 0 < first 5).
        verify(button1, never()).setCellReference(any());
        // button2: row 15, first 5, last 10, n -3.
        // rowWasRemoved: n<0 && row>=first(true) && row < first-n=8 → 15 < 8
        // false
        // numberOfRowsAboveWasChanged: row>=first(true) && row>last(true) →
        // true
        // New row = 15 + -3 = 12.
        verify(button2).setCellReference(
                argThat(ref -> ref != null && ref.getRow() == 12));
        // button3 dropped.
        verify(button3, never()).setCellReference(any());

        // replaceAll contains button1 + button2 only.
        verify(registry).replaceAll(
                argThat((Map<CellReference, PopupButton> m) -> m.size() == 2
                        && m.containsValue(button1) && m.containsValue(button2)
                        && !m.containsValue(button3)));
    }

    @Test
    void shift_detachesBeforeReplaceAll() {
        // Pin the contract order: every detach happens BEFORE the
        // replaceAll. The registry mutation only fires once at the end.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref0 = new CellReference("Sheet1", 0, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref0);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(binding,
                registry);
        inOrder.verify(binding).detach(button1);
        inOrder.verify(registry).replaceAll(any());
    }

    @Test
    void shift_zeroOffset_isANoOpForButtons_butStillDetachesAndReplaces() {
        // Pin: n=0 means no rows moved. rowWasRemoved is false (n<0
        // required). numberOfRowsAboveWasChanged could still trigger:
        // row>=first && row>last. For row=15, first=5, last=10 → true.
        // newRow = 15 + 0 = 15. setCellReference called with effectively
        // the same row. Documents the contract that n=0 still walks
        // through the remap logic (no early-return).
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref15 = new CellReference("Sheet1", 15, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref15);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 0);

        verify(binding).detach(button1);
        // setCellReference called with row 15 (offset 0).
        verify(button1, times(1)).setCellReference(
                argThat(ref -> ref != null && ref.getRow() == 15));
        verify(registry).replaceAll(any());
    }

    @Test
    void shift_singleButtonEmptyRange_replaceAllStillCalled() {
        // Pin: even when no buttons survive, replaceAll fires with the
        // empty map. Registry is then equivalent to having all buttons
        // dropped.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref6 = new CellReference("Sheet1", 6, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref6);
        when(registry.values()).thenReturn(Collections.singletonList(button1));

        shifter.shift(5, 10, -3); // row 6 removed

        verify(registry).replaceAll(argThat(Map::isEmpty));
    }
}
