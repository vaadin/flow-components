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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Attaching the pop-up buttons in view and detaching the others with
 * {@link PopupButtonRefresher}.
 *
 * <p>
 * Walks the registry and dispatches each button to attach / detach based on its
 * viewport visibility, skipping buttons whose cell reference belongs to a
 * different sheet.
 */
class PopupButtonRefresherTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private PopupButtonRegistry registry;
    @Mock
    private PopupButtonBinding binding;
    @Mock
    private Sheet activeSheet;

    private PopupButtonRefresher refresher;
    private PopupButton buttonA;
    private PopupButton buttonB;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        refresher = new PopupButtonRefresher(spreadsheet, registry, binding);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(activeSheet);
        lenient().when(activeSheet.getSheetName()).thenReturn("Sheet1");
        buttonA = mock(PopupButton.class);
        buttonB = mock(PopupButton.class);
    }

    @Test
    void refresh_emptyRegistry_doesNothing() {
        when(registry.values()).thenReturn(Collections.emptyList());

        refresher.refresh();

        verify(binding, never()).attach(any());
        verify(binding, never()).detach(any());
    }

    @Test
    void refresh_buttonOnDifferentSheet_skipped() {
        // Pin: a button whose cell reference's sheet name doesn't match
        // the active sheet is skipped — neither attached nor detached.
        // The refresher operates per-active-sheet.
        CellReference refOther = new CellReference("OtherSheet", 0, 0, true,
                true);
        when(buttonA.getCellReference()).thenReturn(refOther);
        when(registry.values()).thenReturn(Arrays.asList(buttonA));

        refresher.refresh();

        verify(binding, never()).attach(buttonA);
        verify(binding, never()).detach(buttonA);
    }

    @Test
    void refresh_buttonInViewport_attached() {
        // Pin: a button on the active sheet whose cell is visible gets
        // attached. The 1-based conversion in the refresher (+1 to col
        // and row) is exercised here.
        CellReference ref = new CellReference("Sheet1", 4, 2, true, true);
        when(buttonA.getCellReference()).thenReturn(ref);
        when(buttonA.getColumn()).thenReturn(2); // POI 0-based
        when(buttonA.getRow()).thenReturn(4);
        when(spreadsheet.isCellVisible(5, 3)).thenReturn(true);
        when(registry.values()).thenReturn(Arrays.asList(buttonA));

        refresher.refresh();

        verify(binding).attach(buttonA);
        verify(binding, never()).detach(buttonA);
    }

    @Test
    void refresh_buttonOutsideViewport_detached() {
        CellReference ref = new CellReference("Sheet1", 100, 100, true, true);
        when(buttonA.getCellReference()).thenReturn(ref);
        when(buttonA.getColumn()).thenReturn(100);
        when(buttonA.getRow()).thenReturn(100);
        when(spreadsheet.isCellVisible(101, 101)).thenReturn(false);
        when(registry.values()).thenReturn(Arrays.asList(buttonA));

        refresher.refresh();

        verify(binding).detach(buttonA);
        verify(binding, never()).attach(buttonA);
    }

    @Test
    void refresh_mixedButtons_eachDispatchedIndependently() {
        // Pin: buttons are evaluated one-by-one; the dispatch decision for
        // button A doesn't affect button B.
        CellReference refA = new CellReference("Sheet1", 0, 0, true, true);
        CellReference refB = new CellReference("Sheet1", 5, 5, true, true);
        when(buttonA.getCellReference()).thenReturn(refA);
        when(buttonA.getColumn()).thenReturn(0);
        when(buttonA.getRow()).thenReturn(0);
        when(buttonB.getCellReference()).thenReturn(refB);
        when(buttonB.getColumn()).thenReturn(5);
        when(buttonB.getRow()).thenReturn(5);
        when(spreadsheet.isCellVisible(1, 1)).thenReturn(true);
        when(spreadsheet.isCellVisible(6, 6)).thenReturn(false);
        when(registry.values()).thenReturn(Arrays.asList(buttonA, buttonB));

        refresher.refresh();

        verify(binding).attach(buttonA);
        verify(binding).detach(buttonB);
    }

    @Test
    void refresh_columnIsZeroBasedOnButton_convertedToOneBasedForIsCellVisible() {
        // Pin the +1 conversion explicitly. button.getColumn() = 0 → query
        // is isCellVisible(row+1, column+1) = isCellVisible(_, 1).
        CellReference ref = new CellReference("Sheet1", 0, 0, true, true);
        when(buttonA.getCellReference()).thenReturn(ref);
        when(buttonA.getColumn()).thenReturn(0);
        when(buttonA.getRow()).thenReturn(0);
        when(registry.values()).thenReturn(Arrays.asList(buttonA));

        refresher.refresh();

        // Verify isCellVisible was called with (1, 1), not (0, 0).
        verify(spreadsheet).isCellVisible(1, 1);
    }

}
