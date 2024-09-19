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
 * Widget or section reorder end event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemReorderEndListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-reorder-end-flow")
public class DashboardItemReorderEndEvent extends ComponentEvent<Dashboard> {

    private List<Component> reorderedItems;

    private JsonArray reorderedItemsFromClient;

    private HasWidgets reorderedItemsParent;

    /**
     * Creates a dashboard item reorder end event.
     *
     * @param source
     *            Dashboard that contains the item that was dragged
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param items
     *            The ordered items represented by node IDs as a
     *            {@link JsonArray}
     */
    public DashboardItemReorderEndEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.items") JsonArray items) {
        super(source, fromClient);
        setReorderedItemParent(source, items);
        setReorderedItems();
    }

    /**
     * Returns the parent of the reordered items. Either a dashboard or a
     * section.
     *
     * @return the parent of the reordered items
     */
    public HasWidgets getReorderedItemsParent() {
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

    private void setReorderedItemParent(Dashboard source,
            JsonArray itemsFromClient) {
        List<Component> serverItems = source.getChildren().toList();
        for (int rootLevelIdx = 0; rootLevelIdx < itemsFromClient
                .length(); rootLevelIdx++) {
            if (isNodeIdDifferentForIndex(itemsFromClient, serverItems,
                    rootLevelIdx)) {
                this.reorderedItemsParent = source;
                this.reorderedItemsFromClient = itemsFromClient;
                return;
            }
            if (serverItems
                    .get(rootLevelIdx) instanceof DashboardSection section
                    && isSectionItemReordered(section,
                            itemsFromClient.get(rootLevelIdx))) {
                this.reorderedItemsParent = section;
                this.reorderedItemsFromClient = ((JsonObject) itemsFromClient
                        .get(rootLevelIdx)).getArray("items");
                return;
            }
        }
    }

    private void setReorderedItems() {
        Map<Integer, Component> nodeIdToItems = ((Component) reorderedItemsParent)
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
        this.reorderedItems = items;
    }

    private static boolean isSectionItemReordered(DashboardSection section,
            JsonObject itemFromClient) {
        List<Component> sectionItems = section.getChildren().toList();
        JsonArray clientSectionItems = itemFromClient.getArray("items");
        for (int index = 0; index < clientSectionItems.length(); index++) {
            if (isNodeIdDifferentForIndex(clientSectionItems, sectionItems,
                    index)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNodeIdDifferentForIndex(JsonArray clientItems,
            List<Component> items, int index) {
        JsonObject itemFromClient = clientItems.get(index);
        int nodeIdFromClient = (int) itemFromClient.getNumber("nodeid");
        int nodeIdFromServer = items.get(index).getElement().getNode().getId();
        return nodeIdFromClient != nodeIdFromServer;
    }
}
