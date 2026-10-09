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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * How {@link SheetScrollMemory} remembers the scroll position of each sheet.
 *
 * <p>
 * Pins the defensive-copy semantic on both arrays: each call reads the current
 * arrays, makes a fresh copy via {@code Arrays.copyOf}, updates the target
 * index, and writes the new array back. In-place mutation would skip the
 * {@code MirroredProperty} client mirror.
 */
class SheetScrollMemoryTest {

    @Mock
    private Spreadsheet spreadsheet;

    private SheetScrollMemory memory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        memory = new SheetScrollMemory(spreadsheet);
        lenient().when(spreadsheet.getVerticalScrollPositions())
                .thenReturn(new int[] { 0, 0, 0 });
        lenient().when(spreadsheet.getHorizontalScrollPositions())
                .thenReturn(new int[] { 0, 0, 0 });
    }

    @Test
    void rememberForSheetAtIndex_setsBothArraysAtTargetIndex() {
        memory.rememberForSheetAtIndex(1, 50, 75);

        ArgumentCaptor<int[]> verticalCaptor = ArgumentCaptor
                .forClass(int[].class);
        verify(spreadsheet)
                .setVerticalScrollPositions(verticalCaptor.capture());
        assertArrayEquals(new int[] { 0, 75, 0 }, verticalCaptor.getValue());

        ArgumentCaptor<int[]> horizontalCaptor = ArgumentCaptor
                .forClass(int[].class);
        verify(spreadsheet)
                .setHorizontalScrollPositions(horizontalCaptor.capture());
        assertArrayEquals(new int[] { 0, 50, 0 }, horizontalCaptor.getValue());
    }

    @Test
    void rememberForSheetAtIndex_doesNotMutatePriorArrays() {
        // Pin defensive-copy: the arrays passed to set* are NOT the same
        // reference as the arrays returned by get*. The original arrays
        // are untouched (which would matter if any caller is holding a
        // reference to them).
        int[] originalVertical = { 100, 200, 300 };
        int[] originalHorizontal = { 10, 20, 30 };
        when(spreadsheet.getVerticalScrollPositions())
                .thenReturn(originalVertical);
        when(spreadsheet.getHorizontalScrollPositions())
                .thenReturn(originalHorizontal);

        memory.rememberForSheetAtIndex(0, 99, 88);

        ArgumentCaptor<int[]> verticalCaptor = ArgumentCaptor
                .forClass(int[].class);
        verify(spreadsheet)
                .setVerticalScrollPositions(verticalCaptor.capture());
        assertNotSame(originalVertical, verticalCaptor.getValue(),
                "Vertical array must be a fresh copy");
        // Original arrays unchanged.
        assertArrayEquals(new int[] { 100, 200, 300 }, originalVertical);
        assertArrayEquals(new int[] { 10, 20, 30 }, originalHorizontal);
    }

    @Test
    void rememberForSheetAtIndex_preservesOtherIndices() {
        int[] originalVertical = { 11, 22, 33 };
        int[] originalHorizontal = { 44, 55, 66 };
        when(spreadsheet.getVerticalScrollPositions())
                .thenReturn(originalVertical);
        when(spreadsheet.getHorizontalScrollPositions())
                .thenReturn(originalHorizontal);

        memory.rememberForSheetAtIndex(1, 999, 888);

        ArgumentCaptor<int[]> verticalCaptor = ArgumentCaptor
                .forClass(int[].class);
        verify(spreadsheet)
                .setVerticalScrollPositions(verticalCaptor.capture());
        // index 1 updated, others preserved.
        assertArrayEquals(new int[] { 11, 888, 33 }, verticalCaptor.getValue());

        ArgumentCaptor<int[]> horizontalCaptor = ArgumentCaptor
                .forClass(int[].class);
        verify(spreadsheet)
                .setHorizontalScrollPositions(horizontalCaptor.capture());
        assertArrayEquals(new int[] { 44, 999, 66 },
                horizontalCaptor.getValue());
    }

    @Test
    void rememberForSheetAtIndex_indexZero_works() {
        memory.rememberForSheetAtIndex(0, 5, 6);

        ArgumentCaptor<int[]> verticalCaptor = ArgumentCaptor
                .forClass(int[].class);
        verify(spreadsheet)
                .setVerticalScrollPositions(verticalCaptor.capture());
        assertArrayEquals(new int[] { 6, 0, 0 }, verticalCaptor.getValue());
    }
}
