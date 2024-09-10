/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardItemReorderEndEvent;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class DashboardDragDropTest extends DashboardTestBase {
    private Dashboard dashboard;

    private JsonArray itemsArray;

    @Before
    @Override
    public void setup() {
        super.setup();
        dashboard = new Dashboard();
        dashboard.add(new DashboardWidget(), new DashboardWidget());
        DashboardSection section = dashboard.addSection();
        section.add(new DashboardWidget(), new DashboardWidget());
        getUi().add(dashboard);
        fakeClientCommunication();
        itemsArray = getItemsArray(dashboard.getChildren().toList());
    }

    @Test
    public void reorderWidget_orderIsUpdated() {
        assertRootLevelItemReorder(0, 1);
    }

    @Test
    public void reorderSection_orderIsUpdated() {
        assertRootLevelItemReorder(2, 1);
    }

    @Test
    public void reorderWidgetInSection_orderIsUpdated() {
        assertSectionWidgetReorder(2, 0, 1);
    }

    @Test
    public void invalidNumberOfRootLevelItemsInEvent_itemsAreUnchanged() {
        JsonObject newItem = Json.createObject();
        newItem.put("nodeid", 50);
        itemsArray.set(itemsArray.length(), newItem);
        assertInvalidItemsAreDisregarded();
    }

    @Test
    public void invalidNumberOfWidgetsInSectionInEvent_itemsAreUnchanged() {
        JsonObject sectionItem = itemsArray.get(2);
        JsonArray sectionItems = sectionItem.getArray("items");
        JsonObject newItem = Json.createObject();
        sectionItems.set(sectionItems.length(), newItem);
        assertInvalidItemsAreDisregarded();
    }

    @Test
    public void invalidRootLevelItemNodeIdInEvent_itemsAreUnchanged() {
        JsonObject item = itemsArray.get(0);
        item.put("nodeid", -2);
        assertInvalidItemsAreDisregarded();
    }

    @Test
    public void duplicateRootLevelItemNodeIdInEvent_itemsAreUnchanged() {
        JsonObject item1 = itemsArray.get(0);
        JsonObject item2 = itemsArray.get(1);
        item2.put("nodeid", item1.getNumber("nodeid"));
        assertInvalidItemsAreDisregarded();
    }

    @Test
    public void invalidWidgetInSectionNodeIdInEvent_itemsAreUnchanged() {
        JsonObject sectionItem = itemsArray.get(2);
        JsonArray sectionItems = sectionItem.getArray("items");
        JsonObject item = sectionItems.get(0);
        item.put("nodeid", -2);
        assertInvalidItemsAreDisregarded();
    }

    @Test
    public void duplicateWidgetInSectionNodeIdInEvent_itemsAreUnchanged() {
        JsonObject sectionItem = itemsArray.get(2);
        JsonArray sectionItems = sectionItem.getArray("items");
        JsonObject item1 = sectionItems.get(0);
        JsonObject item2 = sectionItems.get(1);
        item2.put("nodeid", item1.getNumber("nodeid"));
        assertInvalidItemsAreDisregarded();
    }

    @Test
    public void duplicateNodeIdForRootLevelItemAndWidgetInSectionInEvent_itemsAreUnchanged() {
        JsonObject rootLevelItem = itemsArray.get(0);
        JsonObject sectionItem = itemsArray.get(2);
        JsonArray sectionItems = sectionItem.getArray("items");
        JsonObject itemInSection = sectionItems.get(0);
        itemInSection.put("nodeid", rootLevelItem.getNumber("nodeid"));
        assertInvalidItemsAreDisregarded();
    }

    private void fireItemReorderEndEvent() {
        ComponentUtil.fireEvent(dashboard,
                new DashboardItemReorderEndEvent(dashboard, false, itemsArray));
    }

    private List<Integer> getSectionWidgetNodeIds(int sectionIndex) {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(sectionIndex);
        return section.getWidgets().stream()
                .map(component -> component.getElement().getNode().getId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Integer> getRootLevelNodeIds() {
        return dashboard.getChildren()
                .map(component -> component.getElement().getNode().getId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Integer> getExpectedSectionWidgetNodeIds(int sectionIndex,
            int initialIndex, int finalIndex) {
        List<Integer> expectedSectionWidgetNodeIds = getSectionWidgetNodeIds(
                sectionIndex);
        int nodeId = expectedSectionWidgetNodeIds.get(initialIndex);
        expectedSectionWidgetNodeIds.remove((Object) nodeId);
        expectedSectionWidgetNodeIds.add(finalIndex, nodeId);
        return expectedSectionWidgetNodeIds;
    }

    private List<Integer> getExpectedRootLevelItemNodeIds(int initialIndex,
            int finalIndex) {
        List<Integer> expectedRootLevelNodeIds = getRootLevelNodeIds();
        int nodeId = expectedRootLevelNodeIds.get(initialIndex);
        expectedRootLevelNodeIds.remove((Object) nodeId);
        expectedRootLevelNodeIds.add(finalIndex, nodeId);
        return expectedRootLevelNodeIds;
    }

    private void reorderSectionWidget(int sectionIndex, int initialIndex,
            int finalIndex) {
        JsonObject sectionItem = itemsArray.get(sectionIndex);
        JsonArray sectionItems = sectionItem.getArray("items");
        sectionItem.put("items",
                reorderItemInJsonArray(initialIndex, finalIndex, sectionItems));
    }

    private void reorderRootLevelItem(int initialIndex, int finalIndex) {
        itemsArray = reorderItemInJsonArray(initialIndex, finalIndex,
                itemsArray);
    }

    private void assertSectionWidgetReorder(int sectionIndex, int initialIndex,
            int finalIndex) {
        reorderSectionWidget(sectionIndex, initialIndex, finalIndex);
        List<Integer> expectedSectionWidgetNodeIds = getExpectedSectionWidgetNodeIds(
                sectionIndex, initialIndex, finalIndex);
        fireItemReorderEndEvent();
        Assert.assertEquals(expectedSectionWidgetNodeIds,
                getSectionWidgetNodeIds(sectionIndex));
    }

    private void assertRootLevelItemReorder(int initialIndex, int finalIndex) {

        reorderRootLevelItem(initialIndex, finalIndex);
        List<Integer> expectedRootLevelNodeIds = getExpectedRootLevelItemNodeIds(
                initialIndex, finalIndex);
        fireItemReorderEndEvent();
        Assert.assertEquals(expectedRootLevelNodeIds, getRootLevelNodeIds());
    }

    private void assertInvalidItemsAreDisregarded() {
        reorderRootLevelItem(0, 1);
        List<Integer> initialRootLevelNodeIds = getRootLevelNodeIds();
        fireItemReorderEndEvent();
        Assert.assertEquals(initialRootLevelNodeIds, getRootLevelNodeIds());
    }

    private static JsonArray reorderItemInJsonArray(int initialIndex,
            int finalIndex, JsonArray initialArray) {
        JsonObject itemToMove = initialArray.get(initialIndex);
        initialArray.remove(initialIndex);
        JsonArray newArray = Json.createArray();
        for (int i = 0; i < finalIndex; i++) {
            JsonObject currentItem = initialArray.get(i);
            newArray.set(i, currentItem);
        }
        newArray.set(finalIndex, itemToMove);
        for (int i = finalIndex; i < initialArray.length(); i++) {
            JsonObject currentItem = initialArray.get(i);
            newArray.set(i + 1, currentItem);
        }
        return newArray;
    }
}
