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
package com.vaadin.flow.component.menubar;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.function.SerializableRunnable;

@Tag("vaadin-menu-bar-item")
class MenuBarItem extends MenuItem {

    private final SerializableRunnable contentReset;
    final MenuBar menuBar;

    public MenuBarItem(MenuBar menuBar, SerializableRunnable contentReset) {
        super(null, contentReset);
        this.menuBar = menuBar;
        this.contentReset = contentReset;
    }

    @Override
    protected void ensureTooltipElement() {
        menuBar.ensureTooltipElement();
    }

    @Override
    protected MenuBarSubMenu createSubMenu() {
        return new MenuBarSubMenu(this, contentReset);
    }

    /**
     * Sets the menu item explicitly disabled or enabled. When disabled, menu
     * bar items are rendered as "dimmed" and prevented from being activated.
     * Disabled items remain focusable and hoverable, so they stay visible to
     * assistive technologies and can show a tooltip to explain why they are
     * disabled.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

}
