/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.dnd;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.grid.Grid;

/**
 * Drag end event of {@link Grid} rows.
 *
 * @param <T>
 *            The Grid bean type.
 * @author Vaadin Ltd.
 * @see Grid#addDragEndListener(GridDragEndListener)
 */
@SuppressWarnings("serial")
@DomEvent("grid-dragend")
public class GridDragEndEvent<T> extends ComponentEvent<Grid<T>> {

    /**
     * Creates a grid drag end event.
     *
     * @param source
     *            Component that was dragged.
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     */
    public GridDragEndEvent(Grid<T> source, boolean fromClient) {
        super(source, true);
    }

}
