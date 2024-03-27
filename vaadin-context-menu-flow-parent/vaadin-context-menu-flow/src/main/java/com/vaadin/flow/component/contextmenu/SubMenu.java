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
 * API that allows adding content into the sub menus of a {@link ContextMenu} to
 * create hierarchical menus. Get it by calling {@link MenuItem#getSubMenu()} on
 * the item component that should open the sub menu. Sub menu will be rendered
 * only if content has been added inside it.
 *
 * @author Vaadin Ltd.
 */
public class SubMenu extends SubMenuBase<ContextMenu, MenuItem, SubMenu>
        implements HasMenuItems {

    private final SerializableRunnable contentReset;

    public SubMenu(MenuItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem);
        this.contentReset = contentReset;
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
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager() {
        return new MenuManager<>(getParentMenuItem().getContextMenu(),
                contentReset, MenuItem::new, MenuItem.class,
                getParentMenuItem());
    }
}
