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
 * Moving and dropping pop-up buttons when rows shift, with
 * {@link PopupButtonRowShifter}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>Empty-registry early-return — no detaches, no replaceAll.</li>
 * <li>Detach-first contract: every button gets detached before the remap,
 * because its row index is now stale.</li>
 * <li>Three row-classification branches for
 * {@code shiftRows(startRow, endRow, n)}: row overwritten by the shift, row
 * shifted, row unaffected.</li>
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
        // Pin: a button at row 0 when shifting rows 5..10 is unaffected.
        // Its cell reference goes into the rebuilt map unchanged.
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
        // A button on one of the shifted rows 5..10 moves with its row:
        // 7 + 3 = 10.
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

        // Rows 8..12 move up by 3 and overwrite rows 5..7.
        shifter.shift(8, 12, -3);

        verify(binding).detach(button1);
        verify(binding, never()).attach(button1);
    }

    @Test
    void shift_rowRemoved_buttonDropped() {
        // Pin: the button of a row the shifted rows land on, and that is
        // not shifted itself, doesn't make it into the rebuilt map →
        // effectively dropped from the registry. binding.detach still
        // fires (the button must be cleaned up).
        when(registry.isEmpty()).thenReturn(false);
        // Rows 8..12 move up by 3 and overwrite rows 5..7.
        CellReference ref6 = new CellReference("Sheet1", 6, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref6);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(8, 12, -3);

        verify(binding).detach(button1);
        verify(button1, never()).setCellReference(any());
        // The rebuilt map is empty — the button was dropped.
        verify(registry).replaceAll(argThat(updated -> updated.isEmpty()));
    }

    @Test
    void shift_rowOverwrittenByADownwardShift_buttonDropped() {
        // Rows 5..10 move down by 3 and overwrite rows 11..13. The button
        // of row 12 goes with its row instead of moving along.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref12 = new CellReference("Sheet1", 12, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref12);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(button1, never()).setCellReference(any());
        verify(binding, never()).attach(button1);
        verify(registry).replaceAll(argThat(Map::isEmpty));
    }

    @Test
    void shift_rowBelowAffectedRange_isUnaffected() {
        // Rows 5..10 move down by 3 and land on 8..13. A button at row 14
        // is past them, so neither it nor its row moves.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref14 = new CellReference("Sheet1", 14, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref14);
        when(registry.values()).thenReturn(Arrays.asList(button1));

        shifter.shift(5, 10, 3);

        verify(button1, never()).setCellReference(any());
        verify(registry)
                .replaceAll(argThat(updated -> updated.get(ref14) == button1));
    }

    @Test
    void shift_mixedButtons_eachClassifiedIndependently() {
        // Pin: each button is classified by its own row. button1=unaffected,
        // button2=unaffected, button3=overwritten. The rebuilt map contains
        // button1 + button2 with their original keys — button3 dropped.
        when(registry.isEmpty()).thenReturn(false);
        CellReference ref0 = new CellReference("Sheet1", 0, 1, true, true);
        CellReference ref15 = new CellReference("Sheet1", 15, 1, true, true);
        CellReference ref6 = new CellReference("Sheet1", 6, 1, true, true);
        when(button1.getCellReference()).thenReturn(ref0);
        when(button2.getCellReference()).thenReturn(ref15);
        when(button3.getCellReference()).thenReturn(ref6);
        when(registry.values())
                .thenReturn(Arrays.asList(button1, button2, button3));

        shifter.shift(8, 12, -3); // overwrites rows 5..7

        // All three detached.
        verify(binding).detach(button1);
        verify(binding).detach(button2);
        verify(binding).detach(button3);

        // button1 unaffected (row 0 is above the shift).
        verify(button1, never()).setCellReference(any());
        // button2 at row 15 is past the shifted rows 8..12.
        verify(button2, never()).setCellReference(any());
        // button3 at row 6 was one of the overwritten rows 5..7.
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

        shifter.shift(8, 12, -3); // row 6 overwritten

        verify(registry).replaceAll(argThat(Map::isEmpty));
    }
}
