/**
 * Copyright 2000-2024 Vaadin Ltd.
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
 * Fired when a widget was resized.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemResizedListener(ComponentEventListener)
 */
public class DashboardItemResizedEvent extends ComponentEvent<Dashboard> {

    private final DashboardWidget item;

    private final List<Component> items;

    /**
     * Creates a dashboard item resized event.
     *
     * @param source
     *            Dashboard that contains the widget that was resized
     * @param fromClient
     *            {@code true} if the event originated from the client side,
     *            {@code false} otherwise
     * @param item
     *            The resized widget
     * @param items
     *            The root level items of the dashboard
     */
    public DashboardItemResizedEvent(Dashboard source, boolean fromClient,
            DashboardWidget item, List<Component> items) {
        super(source, fromClient);
        this.item = item;
        this.items = items;
    }

    /**
     * Returns the resized widget
     *
     * @return the resized widget
     */
    public DashboardWidget getItem() {
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
