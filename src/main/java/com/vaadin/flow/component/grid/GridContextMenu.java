/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.MenuItem;

/**
 * Server-side component for {@code <vaadin-context-menu>} to be used with
 * {@link Grid}.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class GridContextMenu<T> extends ContextMenuBase<GridContextMenu<T>> {

    /**
     * Creates an empty context menu to be used with a Grid.
     */
    public GridContextMenu() {
        super();
    }

    /**
     * Creates an empty context menu with the given target component.
     * 
     * @param target
     *            the target component for this context menu
     * @see #setTarget(Component)
     */
    public GridContextMenu(Grid<T> target) {
        this();
        setTarget(target);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if the given target is not an instance of {@link Grid}
     */
    @Override
    public void setTarget(Component target) {
        if (!(target instanceof Grid<?>)) {
            throw new IllegalArgumentException(
                    "Only an instance of Grid can be used as the target for GridContextMenu. "
                            + "Use ContextMenu for any other component.");
        }
        super.setTarget(target);
    }

    /**
     * Adds a new item component with the given text content and click listener
     * to the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     * 
     * @param text
     *            the text content for the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(Component, ComponentEventListener)
     * @see #add(Component...)
     */
    public MenuItem addItem(String text,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener) {
        MenuItem menuItem = addItem(text);
        addMenuItemClickListener(menuItem, clickListener);
        return menuItem;
    }

    /**
     * Adds a new item component with the given component and click listener to
     * the context menu overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     * 
     * @param component
     *            the component inside the new item
     * @param clickListener
     *            the handler for clicking the new item, can be {@code null} to
     *            not add listener
     * @return the added {@link MenuItem} component
     * @see #addItem(String, ComponentEventListener)
     * @see #add(Component...)
     */
    public MenuItem addItem(Component component,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener) {
        MenuItem menuItem = addItem(component);
        addMenuItemClickListener(menuItem, clickListener);
        return menuItem;
    }

    private void addMenuItemClickListener(MenuItem menuItem,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener) {
        if (clickListener != null) {
            menuItem.getElement().addEventListener("click", event -> {
                clickListener.onComponentEvent(
                        new GridContextMenuItemClickEvent<T>(menuItem, true));
            });
        }
    }

    /**
     * Event that is fired when a {@link MenuItem} is clicked inside a
     * {@link GridContextMenu}.
     * 
     * @author Vaadin Ltd.
     */
    public static class GridContextMenuItemClickEvent<T>
            extends ComponentEvent<MenuItem> {

        private Grid<T> grid;

        GridContextMenuItemClickEvent(MenuItem source, boolean fromClient) {
            super(source, fromClient);
            grid = (Grid<T>) ((MenuItem) getSource()).getContextMenu()
                    .getTarget();
        }

        /**
         * Gets the Grid that the context menu is connected to.
         * 
         * @return the Grid that the context menu is connected to.
         */
        public Grid<T> getGrid() {
            return grid;
        }

        /**
         * Gets the item in the Grid that was the target of the context-click,
         * or an empty {@code Optional} if the context-click didn't target any
         * item in the Grid (eg. if targeting a header).
         * 
         * @return the target item of the context-click
         */
        public Optional<T> getItem() {
            return Optional.ofNullable(grid.getContextMenuTargetItem());
        }
    }

}
