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

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * Server-side component for {@code <vaadin-context-menu>} to be used with
 * {@link Grid}.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class GridContextMenu<T> extends
        ContextMenuBase<GridContextMenu<T>, GridMenuItem<T>, GridSubMenu<T>>
        implements HasGridMenuItems<T> {

    private SerializablePredicate<T> dynamicContentHandler;

    /**
     * Event that is fired when a {@link GridMenuItem} is clicked inside a
     * {@link GridContextMenu}.
     *
     * @author Vaadin Ltd.
     */
    public static class GridContextMenuItemClickEvent<T>
            extends ComponentEvent<GridMenuItem<T>> {

        private Grid<T> grid;
        private transient Optional<T> item;

        @SuppressWarnings("unchecked")
        GridContextMenuItemClickEvent(GridMenuItem<T> source,
                boolean fromClient) {
            super(source, fromClient);
            grid = (Grid<T>) getSource().getContextMenu().getTarget();
            item = Optional.ofNullable(grid.getDataCommunicator().getKeyMapper()
                    .get(grid.getElement()
                            .getProperty("_contextMenuTargetItemKey")));
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
            return item;
        }
    }

    public static class GridContextMenuOpenedEvent<T>
            extends OpenedChangeEvent<GridContextMenu<T>> {

        private final Grid<T> grid;
        private final transient Optional<T> item;
        private final transient Optional<String> columnId;

        @SuppressWarnings("unchecked")
        public GridContextMenuOpenedEvent(GridContextMenu<T> source,
                boolean fromClient) {
            super(source, fromClient);
            grid = (Grid<T>) getSource().getTarget();
            item = Optional.ofNullable(grid.getDataCommunicator().getKeyMapper()
                    .get(grid.getElement()
                            .getProperty("_contextMenuTargetItemKey")));
            columnId = Optional.ofNullable(grid.getElement()
                    .getProperty("_contextMenuTargetColumnId"));
        }

        /**
         * Gets the item in the Grid that was the target of the context-click,
         * or an empty {@code Optional} if the context-click didn't target any
         * item in the Grid (eg. if targeting a header).
         *
         * @return the target item of the context-click
         */
        public Optional<T> getItem() {
            return item;
        }

        /**
         * Gets the column ID in the Grid that was the target of the
         * context-click, or an empty {@code Optional} if the context-click
         * didn't target any application column in the Grid (eg. selection
         * column).
         *
         * @return the target item of the context-click
         */
        public Optional<String> getColumnId() {
            return columnId;
        }

    }

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
        if (target != null && !(target instanceof Grid<?>)) {
            throw new IllegalArgumentException(
                    "Only an instance of Grid can be used as the target for GridContextMenu. "
                            + "Use ContextMenu for any other component.");
        }
        super.setTarget(target);
    }

    @Override
    public GridMenuItem<T> addItem(String text,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener) {
        GridMenuItem<T> menuItem = getMenuManager().addItem(text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    public GridMenuItem<T> addItem(Component component,
            ComponentEventListener<GridContextMenuItemClickEvent<T>> clickListener) {
        GridMenuItem<T> menuItem = getMenuManager().addItem(component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected MenuManager<GridContextMenu<T>, GridMenuItem<T>, GridSubMenu<T>> createMenuManager(
            SerializableRunnable contentReset) {
        SerializableBiFunction itemFactory = (menu,
                reset) -> new GridMenuItem<>((GridContextMenu<?>) menu,
                        (SerializableRunnable) reset);
        return new MenuManager(this, contentReset, itemFactory,
                GridMenuItem.class, null);
    }

    /**
     * Adds a listener for the {@code opened-changed} events fired by the web
     * component.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    public Registration addGridContextMenuOpenedListener(
            ComponentEventListener<GridContextMenuOpenedEvent<T>> listener) {
        return super.addOpenedChangeListener(ev -> listener.onComponentEvent(
                new GridContextMenuOpenedEvent<>(ev.getSource(),
                        ev.isFromClient())));
    }

    /**
     * Gets the callback function that is executed before the context menu is
     * opened.
     *
     * <p>
     * The dynamic context handler allows for customizing the contents of the
     * context menu before it is open.
     * </p>
     *
     * @return the callback function that is executed before opening the context
     *         menu, or {@code null} if not specified.
     */
    public SerializablePredicate<T> getDynamicContentHandler() {
        return dynamicContentHandler;
    }

    /**
     * Sets a callback that is executed before the context menu is opened.
     *
     * <p>
     * This callback receives the clicked item (if any) as an input parameter
     * and further can dynamically modify the contents of the context menu. This
     * is useful in situations where the context menu items cannot be known in
     * advance and depend on the specific context (i.e. clicked row) and thus
     * can be configured dynamically.
     *
     * The boolean return value of this callback specifies if the context menu
     * will be opened.
     * </p>
     *
     * @param dynamicContentHandler
     *            the callback function that will be executed before opening the
     *            context menu.
     */
    public void setDynamicContentHandler(
            SerializablePredicate<T> dynamicContentHandler) {
        this.dynamicContentHandler = dynamicContentHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
        Grid<T> grid = (Grid<T>) getTarget();
        String key = eventDetail.getString("key");

        if (getDynamicContentHandler() != null) {
            final T item = grid.getDataCommunicator().getKeyMapper().get(key);
            return getDynamicContentHandler().test(item);
        }

        return super.onBeforeOpenMenu(eventDetail);
    }
}
