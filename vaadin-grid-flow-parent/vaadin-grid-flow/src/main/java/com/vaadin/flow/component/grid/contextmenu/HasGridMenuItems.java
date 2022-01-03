/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
