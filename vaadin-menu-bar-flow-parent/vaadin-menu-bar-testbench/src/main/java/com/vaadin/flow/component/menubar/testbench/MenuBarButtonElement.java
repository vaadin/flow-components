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
package com.vaadin.flow.component.menubar.testbench;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a {@code <vaadin-menu-bar-button>} element.
 */
@Element("vaadin-menu-bar-button")
public class MenuBarButtonElement extends TestBenchElement {

    /**
     * Open the submenu of this button by clicking it. Returns the submenu.
     *
     * @return the submenu element
     * @throws NoSuchElementException
     *             if no submenu is opened for this button
     */
    public MenuBarSubMenuElement openSubMenu() {
        click();
        return getSubMenu();
    }

    /**
     * Gets the submenu currently opened for this button. Note that you must
     * hover or click this button beforehand for the menu to open.
     * Alternatively, you can use {@link #openSubMenu()} which both opens and
     * returns the submenu.
     *
     * @return the submenu element
     * @throws NoSuchElementException
     *             if no submenu is opened for this button
     */
    public MenuBarSubMenuElement getSubMenu() {
        waitForSubMenu();
        return getParent().getPropertyElement("_subMenu")
                .wrap(MenuBarSubMenuElement.class);
    }

    /**
     * Gets the menu items from the submenu that is currently opened for this
     * menu item.
     *
     * @return the list of menu items in the submenu
     * @throws NoSuchElementException
     *             if no submenu is opened for this button
     * @deprecated use {@link #openSubMenu()} or {@link #getSubMenu()} retrieve
     *             the submenu for this button, and then use
     *             {@link MenuBarSubMenuElement#getMenuItems()} to retrieve the
     *             items.
     */
    @Deprecated(since = "25.0", forRemoval = true)
    public List<MenuBarItemElement> getSubMenuItems() {
        return getSubMenu().getMenuItems();
    }

    /**
     * Check if the button has open sub menu.
     *
     * @return True if there is sub menu open
     */
    public boolean isExpanded() {
        return hasAttribute("expanded");
    }

    private void waitForSubMenu() {
        try {
            waitUntil(ExpectedConditions.attributeToBe(this, "aria-expanded",
                    "true"));
        } catch (TimeoutException e) {
            throw new NoSuchElementException(
                    "No submenu opened for this button.");
        }
    }
}
