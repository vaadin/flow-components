/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Menu component used inside {@link MenuBar} to show items in the overlay.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class MenuBarMenu extends ContextMenuBase<MenuBarMenu, MenuBarItem, MenuBarSubMenu>
        implements HasMenuItems<MenuBarMenu, MenuBarItem, MenuBarSubMenu> {

    /**
     * Creates an empty menu.
     */
    public MenuBarMenu() {
        super();
    }

    @Override
    public MenuBarItem addItem(String text,
            ComponentEventListener<ClickEvent<MenuBarItem>> clickListener) {
        return getMenuManager().addItem(text, clickListener);
    }

    @Override
    public MenuBarItem addItem(Component component,
            ComponentEventListener<ClickEvent<MenuBarItem>> clickListener) {
        return getMenuManager().addItem(component, clickListener);
    }

    @Override
    protected MenuManager<MenuBarMenu, MenuBarItem, MenuBarSubMenu> createMenuManager(
            SerializableRunnable contentReset) {
        return new MenuManager<>(this, contentReset, MenuBarItem::new,
                MenuBarItem.class, null);
    }
}
