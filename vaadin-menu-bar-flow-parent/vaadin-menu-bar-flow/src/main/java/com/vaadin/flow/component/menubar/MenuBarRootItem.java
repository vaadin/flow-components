/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.menubar;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.function.SerializableRunnable;

class MenuBarRootItem extends MenuItem {

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

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) {
            return;
        }
        super.setEnabled(enabled);
        menuBar.updateButtons();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible == isVisible()) {
            return;
        }
        super.setVisible(visible);
        menuBar.resetContent();
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
}
