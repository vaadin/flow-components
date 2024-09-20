/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Widget or section moved event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemMovedListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-moved-flow")
public class DashboardItemMovedEvent extends ComponentEvent<Dashboard> {

    private final List<Component> reorderedItems;

    private final Component reorderedItemsParent;

    /**
     * Creates a dashboard item moved event.
     *
     * @param source
     *            Dashboard that contains the item that was moved
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param items
     *            The ordered items represented by node IDs as a
     *            {@link JsonArray}
     */
    public DashboardItemMovedEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.items") JsonArray items,
            @EventData("event.detail.section") Integer sectionNodeId) {
        super(source, fromClient);
        if (sectionNodeId == null) {
            reorderedItemsParent = source;
            reorderedItems = getReorderedItemsList(items);
        } else {
            reorderedItemsParent = source.getChildren()
                    .filter(child -> sectionNodeId
                            .equals(child.getElement().getNode().getId()))
                    .map(DashboardSection.class::cast).findAny().orElseThrow();
            reorderedItems = getReorderedItemsList(
                    getSectionItems(items, sectionNodeId));
        }
    }

    /**
     * Returns the parent of the reordered items. Either a dashboard or a
     * section.
     *
     * @return the parent of the reordered items
     */
    public Component getReorderedItemsParent() {
        return reorderedItemsParent;
    }

    /**
     * Returns the list of the reordered item and its sibling items
     *
     * @return the list of the reordered item and its sibling items
     */
    public List<Component> getReorderedItems() {
        return reorderedItems;
    }

    private List<Component> getReorderedItemsList(
            JsonArray reorderedItemsFromClient) {
        Objects.requireNonNull(reorderedItemsFromClient);
        Map<Integer, Component> nodeIdToItems = reorderedItemsParent
                .getChildren()
                .collect(Collectors.toMap(
                        item -> item.getElement().getNode().getId(),
                        Function.identity()));
        List<Component> items = new ArrayList<>();
        for (int index = 0; index < reorderedItemsFromClient
                .length(); index++) {
            int nodeIdFromClient = (int) ((JsonObject) reorderedItemsFromClient
                    .get(index)).getNumber("nodeid");
            items.add(nodeIdToItems.get(nodeIdFromClient));
        }
        return items;
    }

    private static JsonArray getSectionItems(JsonArray items,
            int sectionNodeId) {
        for (int rootLevelIdx = 0; rootLevelIdx < items
                .length(); rootLevelIdx++) {
            JsonObject item = items.get(rootLevelIdx);
            int itemNodeId = (int) item.getNumber("nodeid");
            if (sectionNodeId == itemNodeId) {
                JsonObject sectionObj = items.get(rootLevelIdx);
                return sectionObj.getArray("items");
            }
        }
        return null;
    }
}
