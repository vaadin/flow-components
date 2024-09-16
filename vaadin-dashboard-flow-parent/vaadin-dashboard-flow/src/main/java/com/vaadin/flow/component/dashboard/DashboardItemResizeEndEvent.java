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

    private final int nodeId;

    private final int colspan;

    private final int rowspan;

    /**
     * Creates a dashboard item reorder end event.
     *
     * @param source
     *            Dashboard that contains the item that was dragged
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param nodeId
     *            Node ID the resized item
     * @param colspan
     *            New colspan of the resized item
     * @param rowspan
     *            New rowspan of the resized item
     */
    public DashboardItemResizeEndEvent(Dashboard source, boolean fromClient,
            @EventData("event.detail.item.nodeid") int nodeId,
            @EventData("event.detail.item.colspan") int colspan,
            @EventData("event.detail.item.rowspan") int rowspan) {
        super(source, fromClient);
        this.nodeId = nodeId;
        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    /**
     * Returns the node ID of the resized item
     *
     * @return node ID of the resized item
     */
    public int getNodeId() {
        return nodeId;
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
