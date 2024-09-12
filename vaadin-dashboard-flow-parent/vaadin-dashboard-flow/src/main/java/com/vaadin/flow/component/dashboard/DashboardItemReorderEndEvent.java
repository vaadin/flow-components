/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import elemental.json.JsonArray;

/**
 * Widget or section reorder end event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemReorderEndListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-reorder-end-flow")
public class DashboardItemReorderEndEvent extends ComponentEvent<Dashboard> {

    private final JsonArray items;

    /**
     * Creates a dashboard item reorder end event.
     *
     * @param source
     *            Dashboard that contains the item that was dragged
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public DashboardItemReorderEndEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.items") JsonArray items) {
        super(source, fromClient);
        this.items = items;
    }

    /**
     * Returns the ordered items from the client side
     *
     * @return items the ordered items as a {@link JsonArray}
     */
    public JsonArray getItems() {
        return items;
    }
}
