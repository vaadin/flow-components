/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * The CSS maps {@link SheetCssStyleMaps} stores and mirrors to the client.
 *
 * <p>
 * Six getter/setter pairs, each paired with a JSON-property mirror. Pure
 * storage + mirror — same pattern as {@code SheetDimensionsTest} and
 * {@code SheetGroupingTest}. The intentionally-excluded
 * {@code cellStyleToCSSStyle} field stays on the parent Spreadsheet (protected
 * for subclass compat), as documented in the source.
 */
class SheetCssStyleMapsTest {

    @Mock
    private Element element;

    private SheetCssStyleMaps maps;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        maps = new SheetCssStyleMaps(element);
    }

    @Test
    void freshMaps_allFieldsNull() {
        assertNull(maps.getRowIndexToStyleIndex());
        assertNull(maps.getColumnIndexToStyleIndex());
        assertNull(maps.getLockedColumnIndexes());
        assertNull(maps.getLockedRowIndexes());
        assertNull(maps.getConditionalFormattingStyles());
    }

    @Test
    void setRowIndexToStyleIndex_storesAndMirrorsAsJson() {
        HashMap<Integer, Integer> data = new HashMap<>();
        data.put(0, 5);
        data.put(1, 7);

        maps.setRowIndexToStyleIndex(data);

        assertSame(data, maps.getRowIndexToStyleIndex());
        verify(element).setPropertyJson(eq("rowIndexToStyleIndex"),
                any(BaseJsonNode.class));
    }

    @Test
    void setColumnIndexToStyleIndex_storesAndMirrors() {
        HashMap<Integer, Integer> data = new HashMap<>();
        data.put(0, 3);

        maps.setColumnIndexToStyleIndex(data);

        assertSame(data, maps.getColumnIndexToStyleIndex());
        verify(element).setPropertyJson(eq("columnIndexToStyleIndex"),
                any(BaseJsonNode.class));
    }

    @Test
    void setLockedColumnIndexes_storesAndMirrors() {
        Set<Integer> data = new HashSet<>();
        data.add(0);
        data.add(2);

        maps.setLockedColumnIndexes(data);

        assertSame(data, maps.getLockedColumnIndexes());
        verify(element).setPropertyJson(eq("lockedColumnIndexes"),
                any(BaseJsonNode.class));
    }

    @Test
    void setLockedRowIndexes_storesAndMirrors() {
        Set<Integer> data = new HashSet<>();
        data.add(1);

        maps.setLockedRowIndexes(data);

        assertSame(data, maps.getLockedRowIndexes());
        verify(element).setPropertyJson(eq("lockedRowIndexes"),
                any(BaseJsonNode.class));
    }

    @Test
    void setShiftedCellBorderStyles_mirrors() {
        ArrayList<String> data = new ArrayList<>();
        data.add(".col1.row1{border-right:1px;}");

        maps.setShiftedCellBorderStyles(data);

        verify(element).setPropertyJson(eq("shiftedCellBorderStyles"),
                any(BaseJsonNode.class));
    }

    @Test
    void setConditionalFormattingStyles_storesAndMirrors() {
        HashMap<Integer, String> data = new HashMap<>();
        data.put(1, "background:red;");

        maps.setConditionalFormattingStyles(data);

        assertSame(data, maps.getConditionalFormattingStyles());
        verify(element).setPropertyJson(eq("conditionalFormattingStyles"),
                any(BaseJsonNode.class));
    }

    @Test
    void setters_areIndependent_noCrossEffects() {
        HashMap<Integer, Integer> rowStyle = new HashMap<>();
        Set<Integer> lockedCol = new HashSet<>();

        maps.setRowIndexToStyleIndex(rowStyle);
        maps.setLockedColumnIndexes(lockedCol);

        assertSame(rowStyle, maps.getRowIndexToStyleIndex());
        assertSame(lockedCol, maps.getLockedColumnIndexes());
        assertNull(maps.getColumnIndexToStyleIndex());
        assertNull(maps.getLockedRowIndexes());
    }

    @Test
    void mirrorFiresOnEverySet_evenWithSameValue() {
        // Pin: unconditional mirror, like the sibling SheetDimensions
        // and SheetGrouping classes.
        HashMap<Integer, Integer> data = new HashMap<>();
        maps.setRowIndexToStyleIndex(data);
        maps.setRowIndexToStyleIndex(data);

        verify(element, times(2)).setPropertyJson(eq("rowIndexToStyleIndex"),
                any(BaseJsonNode.class));
    }

    @Test
    void setRowIndexToStyleIndex_null_storesNull_andMirrors() {
        maps.setRowIndexToStyleIndex(null);

        assertNull(maps.getRowIndexToStyleIndex());
        verify(element).setPropertyJson(eq("rowIndexToStyleIndex"),
                any(BaseJsonNode.class));
    }
}
