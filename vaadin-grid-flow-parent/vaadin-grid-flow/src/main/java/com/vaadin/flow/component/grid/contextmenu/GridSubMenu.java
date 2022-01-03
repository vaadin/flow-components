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

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * API that allows adding content into the sub menus of a
 * {@link GridContextMenu} to create hierarchical menus. Get it by calling
 * {@link GridMenuItem#getSubMenu()} on the item component that should open the
 * sub menu. Sub menu will be rendered only if content has been added inside it.
 *
 * @author Vaadin Ltd.
 */
public class GridSubMenu<T>
        extends SubMenuBase<GridContextMenu<T>, GridMenuItem<T>, GridSubMenu<T>>
        implements HasGridMenuItems<T> {

    private final SerializableRunnable contentReset;

    /**
     * Creates a new instance of submenu using the associated
     * {@code parentMenuItem} (item which opens the submenu) and reset context
     * menu callback.
     *
     * @param parentMenuItem
     *            the associated menu item, not {@code null}
     * @param contentReset
     *            the context menu reset callback, not {@code null}
     */
    public GridSubMenu(GridMenuItem<T> parentMenuItem,
            SerializableRunnable contentReset) {
        super(parentMenuItem);
        Objects.requireNonNull(parentMenuItem);
        this.contentReset = contentReset;
    }

    @Override
    public GridMenuItem<T> addItem(String text,
            ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>> clickListener) {
        GridMenuItem<T> menuItem = addItem(text);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @Override
    public GridMenuItem<T> addItem(Component component,
            ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>> clickListener) {
        GridMenuItem<T> menuItem = addItem(component);
        if (clickListener != null) {
            menuItem.addMenuItemClickListener(clickListener);
        }
        return menuItem;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected MenuManager<GridContextMenu<T>, GridMenuItem<T>, GridSubMenu<T>> createMenuManager() {
        SerializableBiFunction itemFactory = (menu,
                reset) -> new GridMenuItem<>((GridContextMenu<?>) menu,
                        (SerializableRunnable) reset);
        return new MenuManager(getParentMenuItem().getContextMenu(),
                contentReset, itemFactory, GridMenuItem.class,
                getParentMenuItem());
    }

}
