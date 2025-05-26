/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

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

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class DashboardItemMoveTest extends DashboardTestBase {
    private Dashboard dashboard;

    private JsonArray itemsArray;

    @Before
    @Override
    public void setup() {
        super.setup();
        dashboard = getNewDashboard();
        dashboard.add(getNewWidget(), getNewWidget());
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();

        section.add(getNewWidget(), getNewWidget());
        getUi().add(dashboard);
        fakeClientCommunication();
        itemsArray = getItemsArray(dashboard.getChildren().toList());
    }

    @Test
    public void moveWidget_orderIsUpdated() {
        assertRootLevelItemMoved(0, 1);
    }

    @Test
    public void moveWidgetToSamePosition_orderIsNotUpdated() {
        assertRootLevelItemMoved(0, 0);
    }

    @Test
    public void moveSection_orderIsUpdated() {
        assertRootLevelItemMoved(2, 1);
    }

    @Test
    public void moveWidgetInSection_orderIsUpdated() {
        assertSectionWidgetMoved(2, 0, 1);
    }

    @Test
    public void moveWidgetInSectionToSamePosition_orderIsNotUpdated() {
        assertSectionWidgetMoved(2, 0, 0);
    }

    @Test
    public void setDashboardNotEditable_moveWidget_orderIsNotUpdated() {
        dashboard.setEditable(false);
        int movedWidgetNodeId = dashboard.getChildren().toList().get(0)
                .getElement().getNode().getId();
        List<Integer> expectedRootLevelNodeIds = getRootLevelNodeIds();
        moveRootLevelItem(0, 1);
        DashboardTestHelper.fireItemMovedEvent(dashboard, movedWidgetNodeId,
                itemsArray, null);
        Assert.assertEquals(expectedRootLevelNodeIds, getRootLevelNodeIds());
    }

    @Test
    public void moveWidget_noClientUpdate() {
        getUi().getInternals().dumpPendingJavaScriptInvocations();

        assertRootLevelItemMoved(0, 1);

        fakeClientCommunication();

        Assert.assertTrue(getUi().getInternals()
                .dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    public void moveWidget_eventCorrectlyFired() {
        int initialIndex = 0;
        int finalIndex = 1;
        Component movedItem = dashboard.getChildren().toList()
                .get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.add(finalIndex, expectedItems.remove(initialIndex));
        Runnable itemMoveAction = () -> {
            moveRootLevelItem(initialIndex, finalIndex);
            DashboardTestHelper.fireItemMovedEvent(dashboard, movedItemNodeId,
                    itemsArray, null);
        };
        assertItemMovedEventCorrectlyFired(itemMoveAction, 1, movedItem,
                expectedItems, null);
    }

    @Test
    public void moveSection_eventCorrectlyFired() {
        int initialIndex = 2;
        int finalIndex = 1;
        Component movedItem = dashboard.getChildren().toList()
                .get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        List<Component> expectedItems = dashboard.getChildren()
                .collect(Collectors.toCollection(ArrayList::new));
        expectedItems.add(finalIndex, expectedItems.remove(initialIndex));
        Runnable itemMoveAction = () -> {
            moveRootLevelItem(initialIndex, finalIndex);
            DashboardTestHelper.fireItemMovedEvent(dashboard, movedItemNodeId,
                    itemsArray, null);
        };
        assertItemMovedEventCorrectlyFired(itemMoveAction, 1, movedItem,
                expectedItems, null);
    }

    @Test
    public void moveWidgetInSection_eventCorrectlyFired() {
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
            moveSectionWidget(sectionIndex, initialIndex, finalIndex);
            DashboardTestHelper.fireItemMovedEvent(dashboard, movedItemNodeId,
                    itemsArray, sectionNodeId);
        };
        assertItemMovedEventCorrectlyFired(itemMoveAction, 1, movedItem,
                expectedItems, section);
    }

    @Test
    public void setDashboardNotEditable_moveWidget_eventNotFired() {
        dashboard.setEditable(false);
        int initialIndex = 0;
        int finalIndex = 1;
        Component movedItem = dashboard.getChildren().toList()
                .get(initialIndex);
        int movedItemNodeId = movedItem.getElement().getNode().getId();
        Runnable itemMoveAction = () -> {
            moveRootLevelItem(initialIndex, finalIndex);
            DashboardTestHelper.fireItemMovedEvent(dashboard, movedItemNodeId,
                    itemsArray, null);
        };
        assertItemMovedEventCorrectlyFired(itemMoveAction, 0, null, null, null);
    }

    @Test
    public void changeWidgetMoveMode_eventCorrectlyFired() {
        Component movedItem = dashboard.getChildren().toList().get(0);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, true);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, false);
    }

    @Test
    public void changeSectionMoveMode_eventCorrectlyFired() {
        Component movedItem = dashboard.getChildren().toList().get(2);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, true);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, false);
    }

    @Test
    public void changeWidgetInSectionMoveMode_eventCorrectlyFired() {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(2);
        Component movedItem = section.getWidgets().get(0);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, true);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, false);
    }

    private void assertItemMoveModeChangedEventCorrectlyFired(Component item,
            boolean moveMode) {
        AtomicInteger listenerInvokedCount = new AtomicInteger(0);
        AtomicReference<Component> eventItem = new AtomicReference<>();
        AtomicReference<Boolean> eventIsMoveMode = new AtomicReference<>();
        dashboard.addItemMoveModeChangedListener(e -> {
            listenerInvokedCount.incrementAndGet();
            eventItem.set(e.getItem());
            eventIsMoveMode.set(e.isMoveMode());
            e.unregisterListener();
        });
        DashboardTestHelper.fireItemMoveModeChangedEvent(dashboard,
                item.getElement().getNode().getId(), moveMode);
        Assert.assertEquals(1, listenerInvokedCount.get());
        Assert.assertEquals(item, eventItem.get());
        Assert.assertEquals(moveMode, eventIsMoveMode.get());
    }

    private void assertItemMovedEventCorrectlyFired(Runnable itemMoveAction,
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

    private void moveSectionWidget(int sectionIndex, int initialIndex,
            int finalIndex) {
        JsonObject sectionItem = itemsArray.get(sectionIndex);
        JsonArray sectionItems = sectionItem.getArray("items");
        sectionItem.put("items",
                moveItemInJsonArray(initialIndex, finalIndex, sectionItems));
    }

    private void moveRootLevelItem(int initialIndex, int finalIndex) {
        itemsArray = moveItemInJsonArray(initialIndex, finalIndex, itemsArray);
    }

    private void assertSectionWidgetMoved(int sectionIndex, int initialIndex,
            int finalIndex) {
        DashboardSection section = (DashboardSection) dashboard.getChildren()
                .toList().get(sectionIndex);
        int sectionNodeId = section.getElement().getNode().getId();
        int movedWidgetNodeId = section.getWidgets().get(initialIndex)
                .getElement().getNode().getId();
        moveSectionWidget(sectionIndex, initialIndex, finalIndex);
        List<Integer> expectedSectionWidgetNodeIds = getExpectedSectionWidgetNodeIds(
                sectionIndex, initialIndex, finalIndex);
        DashboardTestHelper.fireItemMovedEvent(dashboard, movedWidgetNodeId,
                itemsArray, sectionNodeId);
        Assert.assertEquals(expectedSectionWidgetNodeIds,
                getSectionWidgetNodeIds(sectionIndex));
    }

    private void assertRootLevelItemMoved(int initialIndex, int finalIndex) {
        int movedItemNodeId = dashboard.getChildren().toList().get(initialIndex)
                .getElement().getNode().getId();
        moveRootLevelItem(initialIndex, finalIndex);
        List<Integer> expectedRootLevelNodeIds = getExpectedRootLevelItemNodeIds(
                initialIndex, finalIndex);
        DashboardTestHelper.fireItemMovedEvent(dashboard, movedItemNodeId,
                itemsArray, null);
        Assert.assertEquals(expectedRootLevelNodeIds, getRootLevelNodeIds());
    }

    private static JsonArray moveItemInJsonArray(int initialIndex,
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
