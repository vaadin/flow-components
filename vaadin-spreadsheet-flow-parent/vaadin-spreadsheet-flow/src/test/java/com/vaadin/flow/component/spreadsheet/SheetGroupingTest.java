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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * The grouping state {@link SheetGrouping} stores and mirrors to the client.
 *
 * <p>
 * Five property pairs (col-data, col-max, row-max, col-inversed, row-inversed)
 * plus one write-only setter (row-data). Pins the **server-side caching
 * asymmetry**: col-data is stored AND mirrored; row-data is mirrored but NOT
 * cached — there's no {@code getRowGroupingData} method because the field
 * doesn't exist. Preserved from the original Spreadsheet's design.
 */
class SheetGroupingTest {

    @Mock
    private Element element;

    private SheetGrouping grouping;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        grouping = new SheetGrouping(element);
    }

    @Test
    void freshGrouping_returnsZeroOrNullDefaults() {
        assertNull(grouping.getColGroupingData());
        assertEquals(0, grouping.getColGroupingMax());
        assertEquals(0, grouping.getRowGroupingMax());
        assertFalse(grouping.isColGroupingInversed());
        assertFalse(grouping.isRowGroupingInversed());
    }

    @Test
    void setColGroupingData_storesField_andMirrorsAsJson() {
        List<GroupingData> data = Arrays.asList(new GroupingData());

        grouping.setColGroupingData(data);

        assertSame(data, grouping.getColGroupingData());
        verify(element).setPropertyJson(eq("colGroupingData"),
                any(BaseJsonNode.class));
    }

    @Test
    void setRowGroupingData_writeOnly_noFieldStored() {
        // Pin the asymmetry between col-grouping (cached + mirrored) and
        // row-grouping (mirror-only). setRowGroupingData MUST NOT
        // populate the column cache as a side effect — calling it should
        // leave getColGroupingData() returning null. Without this guard
        // a refactor that consolidated the two setters into a shared
        // helper could silently flip the asymmetry.
        List<GroupingData> data = Collections.singletonList(new GroupingData());

        grouping.setRowGroupingData(data);

        verify(element).setPropertyJson(eq("rowGroupingData"),
                any(BaseJsonNode.class));
        assertNull(grouping.getColGroupingData(),
                "Setting row-grouping data must NOT populate the col cache");
    }

    @Test
    void setColGroupingMax_storesField_andMirrorsAsDouble() {
        grouping.setColGroupingMax(5);

        assertEquals(5, grouping.getColGroupingMax());
        verify(element).setProperty(eq("colGroupingMax"), eq(5d));
    }

    @Test
    void setRowGroupingMax_storesField_andMirrorsAsDouble() {
        grouping.setRowGroupingMax(3);

        assertEquals(3, grouping.getRowGroupingMax());
        verify(element).setProperty(eq("rowGroupingMax"), eq(3d));
    }

    @Test
    void setColGroupingInversed_storesField_andMirrorsAsBoolean() {
        grouping.setColGroupingInversed(true);

        assertEquals(true, grouping.isColGroupingInversed());
        verify(element).setProperty(eq("colGroupingInversed"), eq(true));
    }

    @Test
    void setRowGroupingInversed_storesField_andMirrorsAsBoolean() {
        grouping.setRowGroupingInversed(true);

        assertEquals(true, grouping.isRowGroupingInversed());
        verify(element).setProperty(eq("rowGroupingInversed"), eq(true));
    }

    @Test
    void settersAreIndependent_noCrossEffectsBetweenFields() {
        // Pin: each setter only touches its own field + its own mirror.
        grouping.setColGroupingMax(7);
        grouping.setRowGroupingMax(3);
        grouping.setColGroupingInversed(true);

        assertEquals(7, grouping.getColGroupingMax());
        assertEquals(3, grouping.getRowGroupingMax());
        assertEquals(true, grouping.isColGroupingInversed());
        assertFalse(grouping.isRowGroupingInversed());
    }

    @Test
    void mirrorFiresOnEverySet_notJustOnChange() {
        // Pin: the mirror is unconditional. Same value twice fires twice.
        grouping.setColGroupingMax(5);
        grouping.setColGroupingMax(5);

        verify(element, times(2)).setProperty(eq("colGroupingMax"), eq(5d));
    }

    @Test
    void setColGroupingData_null_storesNull_andStillMirrors() {
        // Null data → JSON "null" via the serializer. The field is set to
        // null, the mirror still fires.
        grouping.setColGroupingData(null);

        assertNull(grouping.getColGroupingData());
        verify(element).setPropertyJson(eq("colGroupingData"),
                any(BaseJsonNode.class));
    }
}
