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

/**
 * Widget or section reorder start event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemReorderStartListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-reorder-start")
public class DashboardItemReorderStartEvent extends ComponentEvent<Dashboard> {

    /**
     * Creates a dashboard item reorder start event.
     *
     * @param source
     *            Dashboard that contains the item that was dragged
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public DashboardItemReorderStartEvent(Dashboard source,
            boolean fromClient) {
        super(source, fromClient);
    }
}
