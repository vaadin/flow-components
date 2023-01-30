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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * API that allows adding content into the sub menus of a {@link MenuBar} to
 * create hierarchical menus. Get it by calling {@link MenuBarItem#getSubMenu()} on
 * the item component that should open the sub menu. Sub menu will be rendered
 * only if content has been added inside it.
 *
 * @author Vaadin Ltd.
 */
public class MenuBarSubMenu extends SubMenuBase<MenuBarMenu, MenuBarItem, MenuBarSubMenu>
        implements HasMenuItems<MenuBarMenu, MenuBarItem, MenuBarSubMenu> {

    private final SerializableRunnable contentReset;

    public MenuBarSubMenu(MenuBarItem parentMenuItem, SerializableRunnable contentReset) {
        super(parentMenuItem);
        this.contentReset = contentReset;
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
    protected MenuManager<MenuBarMenu, MenuBarItem, MenuBarSubMenu> createMenuManager() {
        return new MenuManager<>(getParentMenuItem().getContextMenu(),
                contentReset, MenuBarItem::new, MenuBarItem.class,
                getParentMenuItem());
    }
}
