/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.contextmenu;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

/**
 * A common interface for components that can have {@link GridMenuItem}s with
 * click listeners inside them.
 *
 * @see GridContextMenu
 * @see GridSubMenu
 *
 * @author Vaadin Ltd.
 */
interface HasGridMenuItems<T> extends Serializable {

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link GridMenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link GridMenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the
     * {@link GridContextMenu#add(Component...)} method.
     *
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link GridMenuItem} component
     * @see #addItem(Component, ComponentEventListener)
     * @see GridContextMenu#add(Component...)
     * @see GridSubMenu#add(Component...)
     */
    GridMenuItem<T> addItem(String text,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener);

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link GridMenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link GridMenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the
     * {@link GridContextMenu#add(Component...)} method.
     *
     * @param component
     *            the component inside the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link GridMenuItem} component
     * @see #addItem(String, ComponentEventListener)
     * @see GridContextMenu#add(Component...)
     * @see GridSubMenu#add(Component...)
     */
    GridMenuItem<T> addItem(Component component,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener);
}
