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
 * Fired when an item enters or exits resize mode. Resize mode enables using
 * resize buttons or keyboard to resize items.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemResizeModeChangedListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-resize-mode-changed")
public class DashboardItemResizeModeChangedEvent
        extends ComponentEvent<Dashboard> {

    private final Component item;

    private final boolean resizeMode;

    /**
     * Creates a dashboard item resize mode changed event.
     *
     * @param source
     *            Dashboard that contains the item of which the resize mode
     *            state has changed
     * @param fromClient
     *            {@code true} if the event originated from the client side,
     *            {@code false} otherwise
     * @param itemId
     *            The ID of the item of which the resize mode state has changed
     * @param resizeMode
     *            Whether the item is in resize mode
     */
    public DashboardItemResizeModeChangedEvent(Dashboard source,
            boolean fromClient, @EventData("event.detail.item.id") int itemId,
            @EventData("event.detail.value") boolean resizeMode) {
        super(source, fromClient);
        this.item = source.getWidgets().stream().filter(
                widget -> itemId == widget.getElement().getNode().getId())
                .findAny().orElseThrow();
        this.resizeMode = resizeMode;
    }

    /**
     * Returns the item of which the resize mode state has changed;
     *
     * @return the item of which the resize mode state has changed
     */
    public Component getItem() {
        return item;
    }

    /**
     * Returns whether the item is in resize mode
     *
     * @return whether the item is in resize mode
     */
    public boolean isResizeMode() {
        return resizeMode;
    }
}
