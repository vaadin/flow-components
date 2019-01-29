/*
 * Copyright 2000-2017 Vaadin Ltd.
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
