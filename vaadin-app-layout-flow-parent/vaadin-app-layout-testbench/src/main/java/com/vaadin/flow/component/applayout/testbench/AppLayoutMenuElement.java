package com.vaadin.flow.component.applayout.testbench;

/*
 * #%L
 * Vaadin App Layout Testbench API
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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

import com.vaadin.flow.component.tabs.testbench.TabsElement;

import java.util.List;

public class AppLayoutMenuElement extends TabsElement {

    public List<MenuItemElement> getMenuItems() {
        return this.$(MenuItemElement.class).all();
    }

    public MenuItemElement getMenuItemAt(int index) {
        return this.$(MenuItemElement.class).get(index);
    }

    public MenuItemElement getMenuItemWithTitle(String title) {
        return this.$(MenuItemElement.class).attribute("title", title).first();
    }

    public MenuItemElement getSelectedMenuItem() {
        return this.$(MenuItemElement.class).attribute("selected", "").first();
    }

    public int countMenuItems() {
        return getMenuItems().size();
    }

}
