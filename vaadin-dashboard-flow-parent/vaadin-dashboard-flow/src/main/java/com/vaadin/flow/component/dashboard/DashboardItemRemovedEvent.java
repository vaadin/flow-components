/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;

/**
 * Fired when an item was removed.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemRemovedListener(ComponentEventListener)
 */
public class DashboardItemRemovedEvent extends ComponentEvent<Dashboard> {

    private final Component item;

    private final List<Component> items;

    /**
     * Creates a dashboard item removed event.
     *
     * @param source
     *            Dashboard that contains the item that was removed
     * @param fromClient
     *            {@code true} if the event originated from the client side,
     *            {@code false} otherwise
     * @param item
     *            The removed item
     * @param items
     *            The root level items of the dashboard
     */
    public DashboardItemRemovedEvent(Dashboard source, boolean fromClient,
            Component item, List<Component> items) {
        super(source, fromClient);
        this.item = item;
        this.items = items;
    }

    /**
     * Returns the removed item
     *
     * @return the removed item
     */
    public Component getItem() {
        return item;
    }

    /**
     * Returns the root level items of the dashboard
     *
     * @return the root level items of the dashboard
     */
    public List<Component> getItems() {
        return items;
    }
}
