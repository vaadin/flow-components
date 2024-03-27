/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.grid.Grid;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

/**
 * Drag start event of {@link Grid} rows.
 *
 * @param <T>
 *            The Grid bean type.
 * @author Vaadin Ltd.
 * @see Grid#addDragStartListener(GridDragStartListener)
 */
@SuppressWarnings("serial")
@DomEvent("grid-dragstart")
public class GridDragStartEvent<T> extends ComponentEvent<Grid<T>> {

    private final List<T> draggedItems;

    /**
     * Creates a grid drag start event.
     *
     * @param source
     *            Component that was dragged.
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     *            side, <code>false</code> otherwise
     * @param details
     *            Event details from {@code detail}.
     */
    public GridDragStartEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail") JsonObject details) {
        super(source, fromClient);
        JsonArray items = details.getArray("draggedItems");

        draggedItems = new ArrayList<>();
        IntStream.range(0, items.length()).forEach(i -> {
            String itemKey = items.getObject(i).getString("key");
            T item = source.getDataCommunicator().getKeyMapper().get(itemKey);
            draggedItems.add(item);
        });
    }

    /**
     * Get the dragged row items.
     *
     * @return an unmodifiable list of items that are being dragged.
     */
    public List<T> getDraggedItems() {
        return Collections.unmodifiableList(draggedItems);
    }

}
