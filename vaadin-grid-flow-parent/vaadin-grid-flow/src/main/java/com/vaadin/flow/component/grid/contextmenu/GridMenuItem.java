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

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

/**
 * Item component used inside {@link GridContextMenu} and {@link GridSubMenu}.
 * This component can be created and added to a menu overlay with
 * {@link HasGridMenuItems#addItem(String, ComponentEventListener)} and similar
 * methods.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class GridMenuItem<T> extends
        MenuItemBase<GridContextMenu<T>, GridMenuItem<T>, GridSubMenu<T>> {

    private final SerializableRunnable contentReset;

    /**
     * Creates a new instance using the context menu and its reset callback.
     *
     * @param contextMenu
     *            the context menu, not {@code null}
     * @param contentReset
     *            the callback to reset the context menu, not {@code null}
     */
    public GridMenuItem(GridContextMenu<T> contextMenu,
            SerializableRunnable contentReset) {
        super(contextMenu);
        Objects.requireNonNull(contextMenu);
        Objects.requireNonNull(contentReset);
        this.contentReset = contentReset;
    }

    /**
     * Adds the given click listener for this menu item. The fired
     * {@link GridContextMenu.GridContextMenuItemClickEvent} contains
     * information of which item inside the Grid was targeted when the context
     * menu was opened.
     *
     * @param clickListener
     *            the click listener to add
     * @return a handle for removing the listener
     */
    public Registration addMenuItemClickListener(
            ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>> clickListener) {
        return getElement().addEventListener("click", event -> {
            clickListener.onComponentEvent(
                    new GridContextMenu.GridContextMenuItemClickEvent<T>(this,
                            true));
        });
    }

    @Override
    protected GridSubMenu<T> createSubMenu() {
        return new GridSubMenu<>(this, contentReset);
    }

}
