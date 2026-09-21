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
    void shift_rowInsideAffectedRange_movesByTheOffset() {
        // A button inside [first=5, last=10] moves with its row: 7 + 3 = 10.
        // The old test asked for row > last as well, so a button inside the
        // range stayed where it was while its row moved out from under it -
        // flow-components#3223.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref7 = new CellReference("Sheet1", 7, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref7);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(binding).detach(button1);
        verify(button1).setCellReference(argThat(
                ref -> ref != null && ref.getRow() == 10 && ref.getCol() == 1
                        && "Sheet1".equals(ref.getSheetName())));
        verify(registry).replaceAll(
                argThat(updated -> updated.values().contains(button1)
                        && updated.size() == 1));
    }

    @Test
    void shift_survivingButtons_goBackOntoTheClient() {
        // Every button is detached at the top because its row index is
        // stale. The ones that survive have a current one by the end, so
        // they have to go back - otherwise the shift simply removes them.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref7 = new CellReference("Sheet1", 7, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref7);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(binding).detach(button1);
        verify(binding).attach(button1);
    }

    @Test
    void shift_removedButtons_doNotGoBack() {
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref6 = new CellReference("Sheet1", 6, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref6);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, -3);

        verify(binding).detach(button1);
        verify(binding, never()).attach(button1);
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
    void shift_rowBelowAffectedRange_isUnaffected() {
        // A button at row 11 is past the end of [first=5, last=10], so
        // nothing above it moved and neither does it.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref11 = new CellReference("Sheet1", 11, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref11);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(button1, never()).setCellReference(any());
        verify(registry)
                .replaceAll(argThat(updated -> updated.get(ref11) == button1));
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

        // button1 unaffected (row 0 is above first=5).
        verify(button1, never()).setCellReference(any());
        // button2 at row 15 is past last=10, so nothing above it moved.
        verify(button2, never()).setCellReference(any());
        // button3 at row 6 was one of the removed rows 5..7.
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
    void shift_zeroOffset_leavesEveryReferenceAlone_butStillDetachesAndReplaces() {
        // n=0 means no rows moved, so no button's reference changes. It
        // still walks the remap (no early return), and the buttons come
        // back on the client at the end.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref7 = new CellReference("Sheet1", 7, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref7);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 0);

        verify(binding).detach(button1);
        verify(button1, never()).setCellReference(any());
        verify(registry)
                .replaceAll(argThat(updated -> updated.get(ref7) == button1));
        verify(binding).attach(button1);
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
