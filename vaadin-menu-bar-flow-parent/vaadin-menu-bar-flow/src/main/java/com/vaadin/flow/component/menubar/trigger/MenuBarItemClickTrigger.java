/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.trigger;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;

/**
 * Fires when a {@link MenuBar} item is clicked. Wire one or more
 * {@link com.vaadin.flow.component.trigger.internal.Action actions} to the
 * trigger to make them run, in order, inside the browser's click handler — so
 * downstream actions can invoke gesture-gated APIs (clipboard, fullscreen,
 * share, …) directly.
 * <p>
 * Accepts any item produced by {@link MenuBar#addItem(String) MenuBar.addItem}
 * (top-level button) or by {@code item.getSubMenu().addItem(...)} (item in a
 * sub-menu overlay).
 * <p>
 * Example:
 *
 * <pre>{@code
 * MenuItem copy = menuBar.addItem("Copy");
 * new MenuBarItemClickTrigger(copy)
 *         .triggers(new CopyTextToClipboardAction("hello"));
 * }</pre>
 */
public class MenuBarItemClickTrigger extends ClickTrigger {

    /**
     * Creates a click trigger on the given menu-bar item.
     *
     * @param menuItem
     *            the menu item to listen on, not {@code null}
     */
    public MenuBarItemClickTrigger(MenuItem menuItem) {
        super(menuItem);
    }
}
