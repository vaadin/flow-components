/**
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class DashboardItemMoveTest extends DashboardTestBase {
    private Dashboard dashboard;

    private ArrayNode itemsArray;

    @BeforeEach
    @Override
    void setup() {
        super.setup();
        dashboard = getNewDashboard();
        dashboard.add(getNewWidget(), getNewWidget());
        dashboard.setEditable(true);
        DashboardSection section = dashboard.addSection();

        section.add(getNewWidget(), getNewWidget());
        ui.add(dashboard);
        ui.fakeClientCommunication();
        itemsArray = getItemsArray(dashboard.getChildren().toList());
    }

    @Test
    void moveWidget_orderIsUpdated() {
        assertRootLevelItemMoved(0, 1);
    }

    @Test
    void moveWidgetToSamePosition_orderIsNotUpdated() {
        assertRootLevelItemMoved(0, 0);
    }

    @Test
    void moveSection_orderIsUpdated() {
        assertRootLevelItemMoved(2, 1);
    }

    @Test
    void moveWidgetInSection_orderIsUpdated() {
        assertSectionWidgetMoved(2, 0, 1);
    }

    @Test
    void moveWidgetInSectionToSamePosition_orderIsNotUpdated() {
        assertSectionWidgetMoved(2, 0, 0);
    }

    @Test
    void setDashboardNotEditable_moveWidget_orderIsNotUpdated() {
        dashboard.setEditable(false);
        int movedWidgetNodeId = dashboard.getChildren().toList().get(0)
                .getElement().getNode().getId();
        List<Integer> expectedRootLevelNodeIds = getRootLevelNodeIds();
        moveRootLevelItem(0, 1);
        DashboardTestHelper.fireItemMovedEvent(dashboard, movedWidgetNodeId,
                itemsArray, null);
        Assertions.assertEquals(expectedRootLevelNodeIds,
                getRootLevelNodeIds());
    }

    @Test
    void moveWidget_noClientUpdate() {
        ui.dumpPendingJavaScriptInvocations();

        assertRootLevelItemMoved(0, 1);

        ui.fakeClientCommunication();

        Assertions.assertTrue(ui.dumpPendingJavaScriptInvocations().isEmpty());
    }

    @Test
    void moveWidget_eventCorrectlyFired() {
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
    void moveSection_eventCorrectlyFired() {
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
    void moveWidgetInSection_eventCorrectlyFired() {
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
    void setDashboardNotEditable_moveWidget_eventNotFired() {
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
    void changeWidgetMoveMode_eventCorrectlyFired() {
        Component movedItem = dashboard.getChildren().toList().get(0);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, true);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, false);
    }

    @Test
    void changeSectionMoveMode_eventCorrectlyFired() {
        Component movedItem = dashboard.getChildren().toList().get(2);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, true);
        assertItemMoveModeChangedEventCorrectlyFired(movedItem, false);
    }

    @Test
    void changeWidgetInSectionMoveMode_eventCorrectlyFired() {
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
        Assertions.assertEquals(1, listenerInvokedCount.get());
        Assertions.assertEquals(item, eventItem.get());
        Assertions.assertEquals(moveMode, eventIsMoveMode.get());
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
        Assertions.assertEquals(expectedListenerInvokedCount,
                listenerInvokedCount.get());
        if (expectedListenerInvokedCount > 0) {
            Assertions.assertEquals(expectedItem, eventItem.get());
            Assertions.assertEquals(expectedItems, eventItems.get());
            Assertions.assertEquals(Optional.ofNullable(expectedSection),
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
        ObjectNode sectionItem = (ObjectNode) itemsArray.get(sectionIndex);
        ArrayNode sectionItems = (ArrayNode) sectionItem.get("items");
        sectionItem.set("items",
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
        Assertions.assertEquals(expectedSectionWidgetNodeIds,
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
        Assertions.assertEquals(expectedRootLevelNodeIds,
                getRootLevelNodeIds());
    }

    private static ArrayNode moveItemInJsonArray(int initialIndex,
            int finalIndex, ArrayNode initialArray) {
        JsonNode itemToMove = initialArray.get(initialIndex);
        initialArray.remove(initialIndex);
        ArrayNode newArray = JacksonUtils.createArrayNode();
        for (int i = 0; i < finalIndex; i++) {
            JsonNode currentItem = initialArray.get(i);
            newArray.add(currentItem);
        }
        newArray.add(itemToMove);
        for (int i = finalIndex; i < initialArray.size(); i++) {
            JsonNode currentItem = initialArray.get(i);
            newArray.add(currentItem);
        }
        return newArray;
    }
}
