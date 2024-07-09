/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Server-side component for {@code <vaadin-context-menu>}.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class ContextMenu extends ContextMenuBase<ContextMenu, MenuItem, SubMenu>
        implements HasMenuItems {

    /**
     * Creates an empty context menu.
     */
    public ContextMenu() {
    }

    /**
     * Creates an empty context menu with the given target component.
     *
     * @param target
     *            the target component for this context menu
     * @see #setTarget(Component)
     */
    public ContextMenu(Component target) {
        this();
        setTarget(target);
    }

    @Override
    public MenuItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return getMenuManager().addItem(text, clickListener);
    }

    @Override
    public MenuItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return getMenuManager().addItem(component, clickListener);
    }

    @Override
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager(
            SerializableRunnable contentReset) {
        return new MenuManager<>(this, contentReset, MenuItem::new,
                MenuItem.class, null);
    }

}
