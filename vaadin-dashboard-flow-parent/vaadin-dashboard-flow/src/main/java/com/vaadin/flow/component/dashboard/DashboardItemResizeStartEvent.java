/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Widget resize start event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemResizeStartListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-resize-start")
public class DashboardItemResizeStartEvent extends ComponentEvent<Dashboard> {

    private final Component resizedItem;

    /**
     * Creates a dashboard item reorder start event.
     *
     * @param source
     *            Dashboard that contains the item that was dragged
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param nodeId
     *            Node ID the resized item
     */
    public DashboardItemResizeStartEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.item.nodeid") int nodeId) {
        super(source, fromClient);
        this.resizedItem = source.getWidgets().stream()
                .filter(child -> nodeId == child.getElement().getNode().getId())
                .findAny().orElse(null);
    }

    /**
     * Returns the resized item
     *
     * @return the resized item
     */
    public Component getResizedItem() {
        return resizedItem;
    }
}
