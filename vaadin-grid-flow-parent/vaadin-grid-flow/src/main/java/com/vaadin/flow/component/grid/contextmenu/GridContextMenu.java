/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.List;
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

import tools.jackson.databind.node.ObjectNode;

/**
 * Server-side component for {@code <vaadin-context-menu>} to be used with
 * {@link Grid}.
 *
 * @author Vaadin Ltd.
 * @since 3.0
 */
@SuppressWarnings("serial")
public class GridContextMenu<T> extends
        ContextMenuBase<GridContextMenu<T>, GridMenuItem<T>, GridSubMenu<T>>
        implements HasGridMenuItems<T> {

    private SerializablePredicate<T> dynamicContentHandler;
    private SerializablePredicate<DynamicContentContext<T>> dynamicContentProvider;

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

    /**
     * @since 4.0
     */
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
     * The context of a context-click on a {@link Grid}, passed to the
     * {@link #setDynamicContentProvider(SerializablePredicate) dynamic content
     * provider}.
     *
     * @param item
     *            the item in the Grid that was the target of the context-click,
     *            or {@code null} if the context-click didn't target any item
     *            (eg. if targeting a header)
     * @param column
     *            the column in the Grid that was the target of the
     *            context-click, or {@code null} if the context-click didn't
     *            target any application column (eg. selection column)
     * @param <T>
     *            the grid bean type
     * @since 25.4
     */
    public record DynamicContentContext<T>(T item,
            Grid.Column<T> column) implements Serializable {
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
     * Gets the grid context menus that have the given grid as their target, in
     * the order their target was set. Plain {@code ContextMenu} instances
     * targeting the grid are not included.
     *
     * @param grid
     *            the target grid
     * @param <T>
     *            the grid bean type
     * @return an unmodifiable snapshot of the grid context menus targeting the
     *         grid, empty if there are none
     * @see Grid#getContextMenus()
     * @since 25.4
     */
    public static <T> List<GridContextMenu<T>> getContextMenus(Grid<T> grid) {
        return getContextMenus(grid, GridContextMenu.class);
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
                    """
                            Only an instance of Grid can be used as the target for GridContextMenu. \
                            Use ContextMenu for any other component.\
                            """);
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
     * @since 4.0
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
     *         menu, or {@code null} if not specified or if a dynamic content
     *         provider was set with
     *         {@link #setDynamicContentProvider(SerializablePredicate)}.
     * @since 4.1
     * @deprecated Use {@link #getDynamicContentProvider()} together with
     *             {@link #setDynamicContentProvider(SerializablePredicate)},
     *             which also receives the clicked column.
     */
    @Deprecated(since = "25.4", forRemoval = true)
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
     * <p>
     * Replaces any provider set with
     * {@link #setDynamicContentProvider(SerializablePredicate)}.
     * </p>
     *
     * @param dynamicContentHandler
     *            the callback function that will be executed before opening the
     *            context menu.
     * @since 4.1
     * @deprecated Use {@link #setDynamicContentProvider(SerializablePredicate)}
     *             instead, which also receives the clicked column.
     */
    @Deprecated(since = "25.4", forRemoval = true)
    public void setDynamicContentHandler(
            SerializablePredicate<T> dynamicContentHandler) {
        this.dynamicContentHandler = dynamicContentHandler;
        this.dynamicContentProvider = null;
    }

    /**
     * Gets the callback function that is executed before the context menu is
     * opened to dynamically provide its contents.
     *
     * @return the callback function that is executed before opening the context
     *         menu, or {@code null} if not specified or if a handler was set
     *         with {@link #setDynamicContentHandler(SerializablePredicate)}.
     * @see #setDynamicContentProvider(SerializablePredicate)
     * @since 25.4
     */
    public SerializablePredicate<DynamicContentContext<T>> getDynamicContentProvider() {
        return dynamicContentProvider;
    }

    /**
     * Sets a callback that is executed before the context menu is opened to
     * dynamically provide its contents.
     *
     * <p>
     * The callback receives a {@link DynamicContentContext} with the clicked
     * item and column. This is useful when the context menu items depend on
     * what was clicked, for example:
     * </p>
     *
     * <pre>
     * contextMenu.setDynamicContentProvider(context -&gt; {
     *     contextMenu.removeAll();
     *     Person person = context.item();
     *     if (context.column() == nameColumn) {
     *         contextMenu.addItem("Call", e -&gt; call(person));
     *     } else if (context.column() == addressColumn) {
     *         contextMenu.addItem("Show on map", e -&gt; showOnMap(person));
     *     }
     *     return true;
     * });
     * </pre>
     *
     * <p>
     * The boolean return value of this callback specifies if the context menu
     * will be opened.
     * </p>
     * <p>
     * Replaces any handler set with
     * {@link #setDynamicContentHandler(SerializablePredicate)}.
     * </p>
     *
     * @param dynamicContentProvider
     *            the callback function that will be executed before opening the
     *            context menu, or {@code null} to remove it
     * @since 25.4
     */
    public void setDynamicContentProvider(
            SerializablePredicate<DynamicContentContext<T>> dynamicContentProvider) {
        this.dynamicContentProvider = dynamicContentProvider;
        this.dynamicContentHandler = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @since 25.0
     */
    @SuppressWarnings("removal")
    @Override
    protected boolean onBeforeOpenMenu(ObjectNode eventDetail) {
        Grid<T> grid = (Grid<T>) getTarget();
        String key = eventDetail.get("key").asString();

        if (dynamicContentProvider != null) {
            final T item = grid.getDataCommunicator().getKeyMapper().get(key);
            final Grid.Column<T> column = getColumnByInternalId(grid,
                    eventDetail.get("internalColumnId").asString());
            return dynamicContentProvider
                    .test(new DynamicContentContext<>(item, column));
        }

        if (getDynamicContentHandler() != null) {
            final T item = grid.getDataCommunicator().getKeyMapper().get(key);
            return getDynamicContentHandler().test(item);
        }

        return super.onBeforeOpenMenu(eventDetail);
    }

    private static <T> Grid.Column<T> getColumnByInternalId(Grid<T> grid,
            String internalId) {
        return grid.getColumns().stream()
                .filter(column -> internalId
                        .equals(column.getElement().getProperty("_flowId")))
                .findFirst().orElse(null);
    }
}
