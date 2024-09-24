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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dashboard.Dashboard;
import com.vaadin.flow.component.dashboard.DashboardSection;
import com.vaadin.flow.component.dashboard.DashboardWidget;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class DashboardDragReorderTest extends DashboardTestBase {
    private Dashboard dashboard;

    private JsonArray itemsArray;

    @Before
    @Override
    public void setup() {
        super.setup();
        dashboard = new Dashboard();
        dashboard.add(new DashboardWidget(), new DashboardWidget());
        dashboard.setEditable(true);
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
    public void reorderWidgetToSamePosition_orderIsNotUpdated() {
        assertRootLevelItemReorder(0, 0);
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
    public void reorderWidgetInSectionToSamePosition_orderIsNotUpdated() {
        assertSectionWidgetReorder(2, 0, 0);
    }

    @Test
    public void setDashboardNotEditable_reorderWidget_orderIsNotUpdated() {
        dashboard.setEditable(false);
        int movedWidgetNodeId = dashboard.getChildren().toList().get(0)
                .getElement().getNode().getId();
        List<Integer> expectedRootLevelNodeIds = getRootLevelNodeIds();
        reorderRootLevelItem(0, 1);
        fireItemMovedEvent(movedWidgetNodeId);
        Assert.assertEquals(expectedRootLevelNodeIds, getRootLevelNodeIds());
    }

    @Test
    public void reorderWidget_eventCorrectlyFired() {
        int initialIndex = 0;
        int finalIndex = 1;
        Component movedItem = dashboard.getChildren().toList()
                .get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.add(finalIndex, expectedItems.remove(initialIndex));
        Runnable itemMoveAction = () -> {
            reorderRootLevelItem(initialIndex, finalIndex);
            fireItemMovedEvent(movedItemNodeId);
        };
        assertEventCorrectlyFired(itemMoveAction, 1, movedItem, expectedItems,
                null);
    }

    @Test
    public void reorderSection_eventCorrectlyFired() {
        int initialIndex = 2;
        int finalIndex = 1;
        Component movedItem = dashboard.getChildren().toList()
                .get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.add(finalIndex, expectedItems.remove(initialIndex));
        Runnable itemMoveAction = () -> {
            reorderRootLevelItem(initialIndex, finalIndex);
            fireItemMovedEvent(movedItemNodeId);
        };
        assertEventCorrectlyFired(itemMoveAction, 1, movedItem, expectedItems,
                null);
    }

    @Test
    public void reorderWidgetInSection_eventCorrectlyFired() {
        int sectionIndex = 2;
        int initialIndex = 0;
        int finalIndex = 1;
        List<Component> expectedItems = dashboard.getChildren().toList();
        DashboardSection section = (DashboardSection) expectedItems
                .get(sectionIndex);
        int sectionNodeId = section.getElement().getNode().getId();
        Component movedItem = section.getWidgets().get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        Runnable itemMoveAction = () -> {
            reorderSectionWidget(sectionIndex, initialIndex, finalIndex);
            fireItemMovedEvent(movedItemNodeId, sectionNodeId);
        };
        assertEventCorrectlyFired(itemMoveAction, 1, movedItem, expectedItems,
                section);
    }

    @Test
    public void setDashboardNotEditable_reorderWidget_eventNotFired() {
        dashboard.setEditable(false);
        int initialIndex = 0;
        int finalIndex = 1;
        Component movedItem = dashboard.getChildren().toList()
                .get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        Runnable itemMoveAction = () -> {
            reorderRootLevelItem(initialIndex, finalIndex);
            fireItemMovedEvent(movedItemNodeId);
        };
        assertEventCorrectlyFired(itemMoveAction, 0, null, null, null);
    }

    private void assertEventCorrectlyFired(Runnable itemMoveAction,
            int expectedListenerInvokedCount, Component expectedItem,
            List<Component> expectedItems, DashboardSection expectedSection) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventItem = new AtomicReference<>();
        AtomicReference<List<Component>> eventItems = new AtomicReference<>();
        AtomicReference<Optional<DashboardSection>> eventSection = new AtomicReference<>();
        dashboard.addItemMovedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventItem.set(e.getItem());
            eventItems.set(e.getItems());
            eventSection.set(e.getSection());
            e.unregisterListener();
        });
        itemMoveAction.run();
        Assert.assertEquals(expectedListenerInvokedCount,
                listenerInvokedCount.get());
        if (expectedListenerInvokedCount > 0) {
            Assert.assertEquals(expectedItem, eventItem.get());
            Assert.assertEquals(expectedItems, eventItems.get());
            Assert.assertEquals(Optional.ofNullable(expectedSection),
                    eventSection.get());
        }
    }

    private void fireItemMovedEvent(int itemNodeId) {
        fireItemMovedEvent(itemNodeId, null);
    }

    private void fireItemMovedEvent(int itemNodeId, Integer sectionNodeId) {
        JsonObject eventData = Json.createObject();
        eventData.put("event.detail.item", itemNodeId);
        eventData.put("event.detail.items", itemsArray);
        if (sectionNodeId != null) {
            eventData.put("event.detail.section", sectionNodeId);
        }
        DomEvent itemMovedDomEvent = new DomEvent(dashboard.getElement(),
                "dashboard-item-moved-flow", eventData);
        dashboard.getElement().getNode().getFeature(ElementListenerMap.class)
                .fireEvent(itemMovedDomEvent);
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
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(sectionIndex);
        int sectionNodeId = section.getElement().getNode().getId();
        int movedWidgetNodeId = section.getWidgets().get(initialIndex)
                .getElement().getNode().getId();
        reorderSectionWidget(sectionIndex, initialIndex, finalIndex);
        List<Integer> expectedSectionWidgetNodeIds = getExpectedSectionWidgetNodeIds(
                sectionIndex, initialIndex, finalIndex);
        fireItemMovedEvent(movedWidgetNodeId, sectionNodeId);
        Assert.assertEquals(expectedSectionWidgetNodeIds,
                getSectionWidgetNodeIds(sectionIndex));
    }

    private void assertRootLevelItemReorder(int initialIndex, int finalIndex) {
        int movedItemNodeId = dashboard.getChildren().toList().get(initialIndex)
                .getElement().getNode().getId();
        reorderRootLevelItem(initialIndex, finalIndex);
        List<Integer> expectedRootLevelNodeIds = getExpectedRootLevelItemNodeIds(
                initialIndex, finalIndex);
        fireItemMovedEvent(movedItemNodeId);
        Assert.assertEquals(expectedRootLevelNodeIds, getRootLevelNodeIds());
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
