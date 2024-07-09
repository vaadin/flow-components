/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.data.selection.SelectionModel;

/**
 * The server-side interface that controls Grid's selection state.
 *
 * @param <T>
 *            the grid bean type
 */
public interface GridSelectionModel<T> extends SelectionModel<Grid<T>, T> {

    /**
     * Handles the selection of an item that originates from the client.
     *
     * @param item
     *            the item being selected
     */
    void selectFromClient(T item);

    /**
     * Handles the deselection of an item that originates from the client.
     *
     * @param item
     *            the item being deselected
     */
    void deselectFromClient(T item);
}
