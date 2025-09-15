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
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;

/**
 * Fired when an item was moved.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemMovedListener(ComponentEventListener)
 */
public class DashboardItemMovedEvent extends ComponentEvent<Dashboard> {

    private final Component item;

    private final List<Component> items;

    private final DashboardSection section;

    /**
     * Creates a dashboard item moved event.
     *
     * @param source
     *            Dashboard that contains the item that was moved
     * @param fromClient
     *            {@code true} if the event originated from the client side,
     *            {@code false} otherwise
     * @param item
     *            The moved item
     * @param items
     *            The root level items of the dashboard
     * @param section
     *            The section that contains the moved item, {@code null} if the
     *            item is a direct child of the dashboard
     */
    public DashboardItemMovedEvent(Dashboard source, boolean fromClient,
            Component item, List<Component> items, DashboardSection section) {
        super(source, fromClient);
        this.item = item;
        this.items = items;
        this.section = section;
    }

    /**
     * Returns the moved item
     *
     * @return the moved item
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

    /**
     * Returns the section that contains the moved item, or an empty optional if
     * the item is a direct child of the dashboard
     *
     * @return the section that contains the moved item, or an empty optional if
     *         the item is a direct child of the dashboard
     */
    public Optional<DashboardSection> getSection() {
        return Optional.ofNullable(section);
    }
}
