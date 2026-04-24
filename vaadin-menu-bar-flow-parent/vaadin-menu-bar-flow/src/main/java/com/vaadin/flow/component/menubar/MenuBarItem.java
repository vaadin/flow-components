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
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.function.SerializableRunnable;

@Tag("vaadin-menu-bar-item")
class MenuBarItem extends MenuItem {

    private final SerializableRunnable contentReset;

    public MenuBarItem(ContextMenu contextMenu,
            SerializableRunnable contentReset) {
        super(contextMenu, contentReset);
        this.contentReset = contentReset;
    }

    @Override
    protected MenuBarSubMenu createSubMenu() {
        return new MenuBarSubMenu(this, contentReset);
    }

    /**
     * Sets the menu item explicitly disabled or enabled. When disabled, menu
     * bar items are rendered as "dimmed".
     * <p>
     * By default, disabled items are not focusable and don't react to hover. As
     * a result, they are hidden from assistive technologies, and it's not
     * possible to show a tooltip to explain why they are disabled. This can be
     * addressed by enabling several feature flags, which make disabled items
     * focusable and hoverable, while still preventing them from being
     * activated. To enable the feature flags, add the following lines to
     * {@code src/main/resources/vaadin-featureflags.properties}:
     *
     * <pre>
     * # Allow focus and hover interactions with disabled menu bar root items (buttons)
     * com.vaadin.experimental.accessibleDisabledButtons = true
     *
     * # Allow focus and hover interactions with disabled menu bar sub-menu items
     * com.vaadin.experimental.accessibleDisabledMenuItems = true
     * </pre>
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addClassName(String className) {
        super.addClassName(className);
        updateClassName();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void addClassNames(String... classNames) {
        super.addClassNames(classNames);
        updateClassName();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setClassName(String className) {
        super.setClassName(className);
        updateClassName();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setClassName(String className, boolean set) {
        super.setClassName(className, set);
        updateClassName();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean removeClassName(String className) {
        var result = super.removeClassName(className);
        updateClassName();
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void removeClassNames(String... classNames) {
        super.removeClassNames(classNames);
        updateClassName();
    }

    private void updateClassName() {
        getElement().executeJs(
                "window.Vaadin.Flow.menubarConnector.setClassName(this)");
    }
}
