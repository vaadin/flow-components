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
 * Widget or section move mode state changed event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemMoveModeChangedListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-move-mode-changed")
public class DashboardItemMoveModeChangedEvent
        extends ComponentEvent<Dashboard> {

    private final Component item;

    private final boolean moveMode;

    /**
     * Creates a dashboard item move mode changed event.
     *
     * @param source
     *            Dashboard that contains the item of which the move mode state
     *            has changed
     * @param fromClient
     *            {@code true} if the event originated from the client side,
     *            {@code false} otherwise
     * @param itemId
     *            The ID of the item of which the move mode state has changed
     * @param moveMode
     *            Whether the item is in move mode
     */
    public DashboardItemMoveModeChangedEvent(Dashboard source,
            boolean fromClient, @EventData("event.detail.item.id") int itemId,
            @EventData("event.detail.value") boolean moveMode) {
        super(source, fromClient);
        this.item = source.getItem(itemId);
        this.moveMode = moveMode;
    }

    /**
     * Returns the item of which the move mode state has changed
     *
     * @return the item of which the move mode state has changed
     */
    public Component getItem() {
        return item;
    }

    /**
     * Returns whether the item is in move mode
     *
     * @return whether the item is in move mode
     */
    public boolean isMoveMode() {
        return moveMode;
    }
}
