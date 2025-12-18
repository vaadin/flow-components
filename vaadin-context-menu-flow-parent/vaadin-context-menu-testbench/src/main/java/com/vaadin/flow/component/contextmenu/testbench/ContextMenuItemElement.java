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
package com.vaadin.flow.component.contextmenu.testbench;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-context-menu-item&gt;</code> element.
 *
 * @author Vaadin Ltd
 *
 */
@Element("vaadin-context-menu-item")
public class ContextMenuItemElement extends TestBenchElement {

    /**
     * Open the submenu of this item by hovering over it. Returns the submenu
     * after it has opened, which can be used to access the items in the
     * submenu.
     *
     * @return the submenu element.
     * @throws NoSuchElementException
     *             if no submenu is opened for this item.
     */
    public ContextMenuElement openSubMenu() {
        hover();
        return getSubMenu();
    }

    /**
     * Check if the item is checked.
     *
     * @return {@code true} if the item is checked.
     */
    public boolean isChecked() {
        return hasAttribute("menu-item-checked");
    }

    private ContextMenuElement getMenu() {
        TestBenchElement listBox = getPropertyElement("parentElement");
        TestBenchElement overlayContent = listBox
                .getPropertyElement("parentElement");
        TestBenchElement menu = overlayContent
                .getPropertyElement("parentElement");

        return menu.wrap(ContextMenuElement.class);
    }

    private ContextMenuElement getSubMenu() {
        ContextMenuElement menu = getMenu();
        By submenuLocator = By.cssSelector(":scope > [slot='submenu'][opened]");

        try {
            // Wait for the submenu to be opened
            waitUntil(driver -> menu.findElement(submenuLocator));
        } catch (TimeoutException e) {
            throw new NoSuchElementException(
                    "No sub menu opened for this menu item.");
        }

        return menu.findElement(submenuLocator).wrap(ContextMenuElement.class);
    }
}
