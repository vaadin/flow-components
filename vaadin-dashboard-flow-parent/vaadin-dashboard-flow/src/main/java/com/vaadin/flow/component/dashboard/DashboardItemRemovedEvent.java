/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Widget or section removed event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemRemovedListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-removed")
public class DashboardItemRemovedEvent extends ComponentEvent<Dashboard> {

    private final Component removedItem;

    /**
     * Creates a dashboard item removed event.
     *
     * @param source
     *            Dashboard that contains the item that was removed
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public DashboardItemRemovedEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.item.nodeid") int nodeId) {
        super(source, fromClient);
        this.removedItem = getRemovedItem(source, nodeId);
    }

    /**
     * Returns the removed item
     *
     * @return the removed item
     */
    public Component getRemovedItem() {
        return removedItem;
    }

    private static Component getRemovedItem(Dashboard dashboard, int nodeId) {
        return dashboard.getChildren().map(item -> {
            if (nodeId == item.getElement().getNode().getId()) {
                return item;
            }
            if (item instanceof DashboardSection section) {
                return section.getWidgets().stream()
                        .filter(sectionItem -> nodeId == sectionItem
                                .getElement().getNode().getId())
                        .findAny().orElse(null);
            }
            return null;
        }).filter(Objects::nonNull).findAny().orElse(null);
    }
}
