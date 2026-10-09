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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

/**
 * The row and column sizes {@link SheetDimensions} stores and mirrors to the
 * client.
 *
 * <p>
 * Per-sheet geometry state with six getter/setter pairs. Every setter is paired
 * with an Element property mirror — the value (or its serialized form for
 * arrays/lists) is written to {@code spreadsheet.getElement()} on every set.
 * Pins the field-name conventions, the serialization split (primitives go
 * through directly, arrays/lists via {@link Serializer#serialize}), and the
 * local field state that callers read via the getters.
 */
class SheetDimensionsTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element element;

    private SheetDimensions dims;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dims = new SheetDimensions(spreadsheet);
        lenient().when(spreadsheet.getElement()).thenReturn(element);
    }

    @Test
    void freshDimensions_returnsZeroOrNullDefaults() {
        // Pin the initial state of each field — float = 0.0, int = 0,
        // arrays + lists = null.
        assertEquals(0f, dims.getDefRowH());
        assertEquals(0, dims.getDefColW());
        assertNull(dims.getRowH());
        assertNull(dims.getColW());
        assertNull(dims.getHiddenColumnIndexes());
        assertNull(dims.getHiddenRowIndexes());
    }

    @Test
    void setDefRowH_storesValue_andMirrorsAsFloatProperty() {
        dims.setDefRowH(12.5f);

        assertEquals(12.5f, dims.getDefRowH());
        // Float property is mirrored via setProperty(String, double) —
        // Element auto-widens float to double for the numeric overload.
        verify(element).setProperty(eq("defRowH"), eq(12.5d));
    }

    @Test
    void setDefColW_storesValue_andMirrorsAsDoubleProperty() {
        dims.setDefColW(75);

        assertEquals(75, dims.getDefColW());
        // setProperty(String, double) overload — int widens to double.
        verify(element).setProperty(eq("defColW"), eq(75d));
    }

    @Test
    void setRowH_storesArray_andMirrorsSerializedString() {
        // Pin: arrays go through Serializer.serialize, which returns a
        // String. The setter then calls setProperty(String, String).
        float[] rowH = { 1.5f, 2.5f, 3.5f };
        dims.setRowH(rowH);

        assertSame(rowH, dims.getRowH());
        verify(element).setProperty(eq("rowH"), anyString());
    }

    @Test
    void setColW_storesArray_andMirrorsSerializedString() {
        int[] colW = { 50, 75, 100 };
        dims.setColW(colW);

        assertArrayEquals(colW, dims.getColW());
        verify(element).setProperty(eq("colW"), anyString());
    }

    @Test
    void setHiddenColumnIndexes_storesList_andMirrorsSerializedString() {
        ArrayList<Integer> indexes = new ArrayList<>();
        indexes.add(2);
        indexes.add(7);

        dims.setHiddenColumnIndexes(indexes);

        assertSame(indexes, dims.getHiddenColumnIndexes());
        verify(element).setProperty(eq("hiddenColumnIndexes"), anyString());
    }

    @Test
    void setHiddenRowIndexes_storesList_andMirrorsSerializedString() {
        ArrayList<Integer> indexes = new ArrayList<>();
        indexes.add(5);

        dims.setHiddenRowIndexes(indexes);

        assertSame(indexes, dims.getHiddenRowIndexes());
        verify(element).setProperty(eq("hiddenRowIndexes"), anyString());
    }

    @Test
    void setRowH_null_storesNull_andStillMirrors() {
        // Pin: setting null is a valid operation. The serialized form of
        // null is the JSON literal "null", which Element accepts.
        dims.setRowH(null);

        assertNull(dims.getRowH());
        verify(element).setProperty(eq("rowH"), anyString());
    }

    @Test
    void setHiddenColumnIndexes_emptyList_storesAndMirrors() {
        ArrayList<Integer> empty = new ArrayList<>();
        dims.setHiddenColumnIndexes(empty);

        assertSame(empty, dims.getHiddenColumnIndexes());
        verify(element).setProperty(eq("hiddenColumnIndexes"), anyString());
    }

    @Test
    void settersAreIndependent_noCrossEffectsBetweenFields() {
        // Pin: each setter only touches its own field + its own property
        // mirror. Setting defRowH doesn't disturb rowH (etc.).
        float[] rowH = { 1f, 2f };
        dims.setRowH(rowH);
        dims.setDefRowH(99f);

        // rowH still in place.
        assertSame(rowH, dims.getRowH());
        // defRowH updated.
        assertEquals(99f, dims.getDefRowH());
    }

    @Test
    void mirrorFiresOnEverySet_notJustOnChange() {
        // Pin: the mirror is unconditional. Setting the same value twice
        // fires the property write twice — no equality check. Matches the
        // simple-mirror contract; callers that want change-detection must
        // do it upstream.
        dims.setDefColW(50);
        dims.setDefColW(50);

        verify(element, org.mockito.Mockito.times(2)).setProperty(eq("defColW"),
                eq(50d));
    }
}
