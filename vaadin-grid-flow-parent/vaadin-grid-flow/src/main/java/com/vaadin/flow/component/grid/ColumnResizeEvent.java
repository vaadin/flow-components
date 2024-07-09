/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.grid.Grid.Column;

import elemental.json.JsonObject;

/**
 * Event fired when a Grid column is resized by the user.
 *
 * @param <T>
 *            the grid bean type
 *
 * @author Vaadin Ltd
 *
 * @see Grid#addColumnResizeListener(com.vaadin.flow.component.ComponentEventListener)
 *
 */
@DomEvent("column-drag-resize")
public class ColumnResizeEvent<T> extends ComponentEvent<Grid<T>> {

    private final String resizedColumnKey;

    /**
     * Creates a new column resize event.
     *
     * @param source
     *            the component that fired the event
     * @param fromClient
     *            <code>true</code> if the event was originally fired on the
     *            client, <code>false</code> if the event originates from
     *            server-side logic
     * @param resizedColumnKey
     *            internal id of the column that was the target of user's resize
     *            action
     *
     */
    public ColumnResizeEvent(Grid<T> source, boolean fromClient,
            @EventData("event.detail.resizedColumnKey") String resizedColumnKey) {
        super(source, fromClient);
        this.resizedColumnKey = resizedColumnKey;
    }

    /**
     * Returns the column that was the target of user's resize action
     *
     * @return resize action target column
     */
    public Column<T> getResizedColumn() {
        return getSource().getColumns().stream()
                .filter(col -> col.getInternalId().equals(resizedColumnKey))
                .findFirst().orElse(null);
    }

}
