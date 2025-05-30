/*
 * Copyright 2000-2025 Vaadin Ltd.
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
