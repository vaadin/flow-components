package com.vaadin.flow.component.applayout;

/*
 * #%L
 * Vaadin App Layout for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;

/**
 * A menu item that executes an action.
 */
public class ActionMenuItem extends MenuItem {

    public ActionMenuItem(String title) {
        super(title);
    }

    public ActionMenuItem(Component icon) {
        super(icon);
    }

    public ActionMenuItem(Component icon, String title) {
        super(icon, title);
    }

    public ActionMenuItem(Component icon, String title,
                          ComponentEventListener<MenuItemClickEvent> action) {
        super(icon, title, action);
    }

    /**
     * Gets the action that would be executed if this menu item is selected.
     */
    public ComponentEventListener<MenuItemClickEvent> getAction() {
        return getListener();
    }

    /**
     * Sets the action to execute when menu item is selected.
     *
     * @param action
     */
    public void setAction(ComponentEventListener<MenuItemClickEvent> action) {
        setListener(action);
    }
}
