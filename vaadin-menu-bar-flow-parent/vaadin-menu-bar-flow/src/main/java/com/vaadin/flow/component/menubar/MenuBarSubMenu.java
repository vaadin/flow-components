/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.menubar;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.function.SerializableRunnable;

class MenuBarSubMenu extends SubMenu {

    private final SerializableRunnable contentReset;

    public MenuBarSubMenu(MenuItem parentMenuItem,
            SerializableRunnable contentReset) {
        super(parentMenuItem, contentReset);
        this.contentReset = contentReset;
    }

    @Override
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager() {
        return new MenuManager<>(null, contentReset, MenuBarItem::new,
                MenuItem.class, getParentMenuItem());
    }
}
