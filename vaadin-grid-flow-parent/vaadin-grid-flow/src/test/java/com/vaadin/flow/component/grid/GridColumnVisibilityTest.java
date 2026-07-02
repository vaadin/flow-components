/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ObjectNode;

/**
 * Unit tests for the {@code hideable} flag, the visibility-gated data
 * generation and the {@link ColumnVisibilityChangedEvent}.
 */
class GridColumnVisibilityTest {

    @RegisterExtension
    final MockUIExtension uiExtension = new MockUIExtension();

    private Grid<String> grid;
    private Column<String> firstColumn;
    private Column<String> secondColumn;

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        firstColumn = grid.addColumn(str -> str.toUpperCase());
        secondColumn = grid.addColumn(str -> str.toLowerCase());
    }

    // hideable ---------------------------------------------------------------

    @Test
    void hideable_defaultsToFalse() {
        Assertions.assertFalse(firstColumn.isHideable());
    }

    @Test
    void setHideable_roundTrips() {
        firstColumn.setHideable(true);
        Assertions.assertTrue(firstColumn.isHideable());
        firstColumn.setHideable(false);
        Assertions.assertFalse(firstColumn.isHideable());
    }

    // hideable sync to hidden columns -----------------------------------------

    // Flow doesn't send property changes to invisible elements, so hideable is
    // pushed to hidden columns with JS through the grid element instead.

    @Test
    void setHideableOnHiddenColumn_sendsPropertyThroughGrid() {
        uiExtension.add(grid);
        firstColumn.setVisible(false);
        firstColumn.setHideable(true);

        uiExtension.fakeClientCommunication();

        List<JavaScriptInvocation> invocations = hideableSyncInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals(true,
                invocations.get(0).getParameters().get(2));
    }

    @Test
    void setHideableThenHideColumn_sameRoundTrip_sendsPropertyThroughGrid() {
        uiExtension.add(grid);
        // hideable is set while the column is still visible, but the column is
        // hidden within the same round trip, so the property change is not
        // synchronized normally.
        firstColumn.setHideable(true);
        firstColumn.setVisible(false);

        uiExtension.fakeClientCommunication();

        Assertions.assertEquals(1, hideableSyncInvocations().size());
    }

    @Test
    void setHideableOnVisibleColumn_synchronizesNormally_noJsNeeded() {
        uiExtension.add(grid);
        firstColumn.setHideable(true);

        uiExtension.fakeClientCommunication();

        Assertions.assertTrue(hideableSyncInvocations().isEmpty());
    }

    @Test
    void attachGridWithHiddenHideableColumn_sendsPropertyThroughGrid() {
        firstColumn.setVisible(false);
        firstColumn.setHideable(true);
        uiExtension.add(grid);

        uiExtension.fakeClientCommunication();

        List<JavaScriptInvocation> invocations = hideableSyncInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals(true,
                invocations.get(0).getParameters().get(2));
    }

    private List<JavaScriptInvocation> hideableSyncInvocations() {
        return uiExtension.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains(".hideable = "))
                .toList();
    }

    // per-item data ----------------------------------------------------------

    @Test
    void visibleColumns_generateData() {
        ObjectNode json = generateData("item");
        Assertions.assertTrue(json.has(firstColumn.getInternalId()));
        Assertions.assertTrue(json.has(secondColumn.getInternalId()));
    }

    @Test
    void hiddenColumn_omitsData_otherColumnsUnaffected() {
        firstColumn.setVisible(false);
        ObjectNode json = generateData("item");
        Assertions.assertFalse(json.has(firstColumn.getInternalId()));
        Assertions.assertTrue(json.has(secondColumn.getInternalId()));
    }

    @Test
    void shownAgain_generatesDataAgain() {
        firstColumn.setVisible(false);
        Assertions.assertFalse(
                generateData("item").has(firstColumn.getInternalId()));
        firstColumn.setVisible(true);
        Assertions.assertTrue(
                generateData("item").has(firstColumn.getInternalId()));
    }

    @Test
    void hiddenColumn_omitsTooltipData() {
        firstColumn.setTooltipGenerator(item -> "tooltip-" + item);
        secondColumn.setTooltipGenerator(item -> "tooltip-" + item);

        ObjectNode tooltips = (ObjectNode) generateData("item")
                .get("gridtooltips");
        Assertions.assertTrue(tooltips.has(firstColumn.getInternalId()));

        firstColumn.setVisible(false);
        ObjectNode json = generateData("item");
        ObjectNode tooltipsHidden = (ObjectNode) json.get("gridtooltips");
        Assertions.assertFalse(tooltipsHidden.has(firstColumn.getInternalId()));
        Assertions.assertTrue(tooltipsHidden.has(secondColumn.getInternalId()));
    }

    @Test
    void hiddenColumn_omitsPartData() {
        firstColumn.setPartNameGenerator(item -> "part-first");
        secondColumn.setPartNameGenerator(item -> "part-second");

        ObjectNode part = (ObjectNode) generateData("item").get("part");
        Assertions.assertTrue(part.has(firstColumn.getInternalId()));

        firstColumn.setVisible(false);
        ObjectNode partHidden = (ObjectNode) generateData("item").get("part");
        Assertions.assertFalse(partHidden.has(firstColumn.getInternalId()));
        Assertions.assertTrue(partHidden.has(secondColumn.getInternalId()));
    }

    // visibility change event ------------------------------------------------

    @Test
    void setVisible_firesVisibilityChangedEvent() {
        AtomicReference<ColumnVisibilityChangedEvent<String>> captured = new AtomicReference<>();
        firstColumn.addVisibilityChangedListener(captured::set);

        firstColumn.setVisible(false);

        ColumnVisibilityChangedEvent<String> event = captured.get();
        Assertions.assertNotNull(event);
        Assertions.assertFalse(event.isVisible());
        Assertions.assertFalse(event.isFromClient());
        Assertions.assertEquals(firstColumn, event.getColumn());
        Assertions.assertFalse(firstColumn.isVisible());
    }

    @Test
    void setVisibleToSameValue_doesNotFireEvent() {
        AtomicReference<ColumnVisibilityChangedEvent<String>> captured = new AtomicReference<>();
        firstColumn.addVisibilityChangedListener(captured::set);

        firstColumn.setVisible(true); // already visible
        Assertions.assertNull(captured.get());
    }

    // client -> server sync --------------------------------------------------

    @Test
    void clientHidesColumn_updatesVisibilityAndFiresEventFromClient() {
        AtomicReference<ColumnVisibilityChangedEvent<String>> captured = new AtomicReference<>();
        firstColumn.addVisibilityChangedListener(captured::set);

        ComponentUtil.fireEvent(grid, new ColumnVisibilityChangeDomEvent(grid,
                true, firstColumn.getInternalId(), true));

        Assertions.assertFalse(firstColumn.isVisible());
        Assertions.assertNotNull(captured.get());
        Assertions.assertTrue(captured.get().isFromClient());
        Assertions.assertFalse(captured.get().isVisible());
    }

    @Test
    void clientShowsHiddenColumn_updatesVisibility() {
        firstColumn.setVisible(false);

        ComponentUtil.fireEvent(grid, new ColumnVisibilityChangeDomEvent(grid,
                true, firstColumn.getInternalId(), false));

        Assertions.assertTrue(firstColumn.isVisible());
    }

    @Test
    void clientEventForUnknownColumn_isIgnored() {
        // Should not throw for an unknown column id.
        ComponentUtil.fireEvent(grid, new ColumnVisibilityChangeDomEvent(grid,
                true, "no-such-column", true));
        Assertions.assertTrue(firstColumn.isVisible());
    }

    @SuppressWarnings("unchecked")
    private ObjectNode generateData(String item) {
        ObjectNode json = JacksonUtils.createObjectNode();
        CompositeDataGenerator<String> generator;
        try {
            Field field = Grid.class.getDeclaredField("gridDataGenerator");
            field.setAccessible(true);
            generator = (CompositeDataGenerator<String>) field.get(grid);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        generator.generateData(item, json);
        return json;
    }
}
