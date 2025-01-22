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
     * Sets the menu item explicitly disabled or enabled. When disabled, the
     * menu item is rendered as "dimmed" and prevents all user interactions
     * (mouse and keyboard).
     * <p>
     * Since disabled buttons (root-level items) are not focusable and cannot
     * react to hover events by default, it can cause accessibility issues by
     * making them entirely invisible to assistive technologies, and prevents
     * the use of Tooltips to explain why the action is not available. This can
     * be addressed with the feature flag {@code accessibleDisabledButtons},
     * which makes disabled buttons focusable and hoverable, while preventing
     * them from being triggered. To enable this feature flag, add the following
     * line to {@code src/main/resources/vaadin-featureflags.properties}:
     *
     * <pre>
     * com.vaadin.experimental.accessibleDisabledButtons = true
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
