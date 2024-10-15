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

/**
 * Widget resize end event of {@link Dashboard}.
 *
 * @author Vaadin Ltd.
 * @see Dashboard#addItemResizeEndListener(ComponentEventListener)
 */
@DomEvent("dashboard-item-resize-end")
public class DashboardItemResizeEndEvent extends ComponentEvent<Dashboard> {

    private final DashboardWidget resizedWidget;

    private final int colspan;

    private final int rowspan;

    /**
     * Creates a dashboard item resize end event.
     *
     * @param source
     *            Dashboard that contains the widget that was dragged
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param nodeId
     *            Node ID the resized widget
     * @param colspan
     *            New colspan of the resized widget
     * @param rowspan
     *            New rowspan of the resized widget
     */
    public DashboardItemResizeEndEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.item.nodeid") int nodeId,
            @EventData("event.detail.item.colspan") int colspan,
            @EventData("event.detail.item.rowspan") int rowspan) {
        super(source, fromClient);
        this.resizedWidget = source.getWidgets().stream()
                .filter(child -> nodeId == child.getElement().getNode().getId())
                .findAny().orElse(null);
        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    /**
     * Returns the resized widget
     *
     * @return the resized widget
     */
    public DashboardWidget getResizedWidget() {
        return resizedWidget;
    }

    /**
     * Returns the new colspan of the resized item
     *
     * @return new colspan of the resized item
     */
    public int getColspan() {
        return colspan;
    }

    /**
     * Returns the new rowspan of the resized item
     *
     * @return new rowspan of the resized item
     */
    public int getRowspan() {
        return rowspan;
    }
}
