/**
 * Copyright 2000-2025 Vaadin Ltd.
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
 * Fired when an item was selected.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemSelectedChangedListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-selected-changed")
public class DashboardItemSelectedChangedEvent
        extends ComponentEvent<Dashboard> {

    private final Component item;

    private final boolean selected;

    /**
     * Creates a dashboard item selected changed event.
     *
     * @param source
     *            Dashboard that contains the item of which the selected state
     *            has changed
     * @param fromClient
     *            {@code true} if the event originated from the client side,
     *            {@code false} otherwise
     * @param itemId
     *            The ID of the item of which the selected state has changed
     * @param selected
     *            Whether the item is selected
     */
    public DashboardItemSelectedChangedEvent(Dashboard source,
            boolean fromClient, @EventData("event.detail.item.id") int itemId,
            @EventData("event.detail.value") boolean selected) {
        super(source, fromClient);
        this.item = source.getItem(itemId);
        this.selected = selected;
    }

    /**
     * Returns the item of which the selected state has changed
     *
     * @return the item of which the selected state has changed
     */
    public Component getItem() {
        return item;
    }

    /**
     * Returns whether the item is selected
     *
     * @return whether the item is selected
     */
    public boolean isSelected() {
        return selected;
    }
}
