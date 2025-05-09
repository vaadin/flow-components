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

import com.vaadin.flow.function.SerializableRunnable;

class MenuBarRootItem extends MenuBarItem {

    private MenuBar menuBar;

    MenuBarRootItem(MenuBar menuBar, SerializableRunnable contentReset) {
        super(null, contentReset);
        this.menuBar = menuBar;
    }

    @Override
    public void setCheckable(boolean checkable) {
        if (checkable) {
            throw new UnsupportedOperationException(
                    "A root level item in a MenuBar can not be checkable");
        }
    }

    /**
     * Adds one or more theme names to this item. Multiple theme names can be
     * specified by using multiple parameters.
     * <p>
     * Note that the themes set via {@link MenuBar#setThemeName(String)} will be
     * overridden when using this method.
     *
     * @param themeNames
     *            the theme name or theme names to be added to the item
     */
    @Override
    public void addThemeNames(String... themeNames) {
        super.addThemeNames(themeNames);
        menuBar.updateButtons();
    }

    /**
     * Removes one or more theme names from this item. Multiple theme names can
     * be specified by using multiple parameters.
     *
     * @param themeNames
     *            the theme name or theme names to be removed from the item
     */
    @Override
    public void removeThemeNames(String... themeNames) {
        super.removeThemeNames(themeNames);
        menuBar.updateButtons();
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
        menuBar.updateButtons();
    }
}
