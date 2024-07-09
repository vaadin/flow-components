/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
