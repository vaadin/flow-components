/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
