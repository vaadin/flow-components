/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Item component used inside {@link ContextMenu} and {@link SubMenu}. This
 * component can be created and added to a menu overlay with
 * {@link HasMenuItems#addItem(String, ComponentEventListener)} and similar
 * methods.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class MenuItem extends MenuItemBase<ContextMenu, MenuItem, SubMenu>
        implements ClickNotifier<MenuItem> {

    private final SerializableRunnable contentReset;

    public MenuItem(ContextMenu contextMenu,
            SerializableRunnable contentReset) {
        super(contextMenu);
        this.contentReset = contentReset;
    }

    @Override
    protected SubMenu createSubMenu() {
        return new SubMenu(this, contentReset);
    }

}
