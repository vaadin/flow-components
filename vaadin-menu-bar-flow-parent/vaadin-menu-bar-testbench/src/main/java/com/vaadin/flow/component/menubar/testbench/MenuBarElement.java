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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a {@code <vaadin-menu-bar>} element.
 */
@Element("vaadin-menu-bar")
public class MenuBarElement extends TestBenchElement {

    public static final String SUBMENU_TAG = "vaadin-menu-bar-submenu";

    /**
     * Gets the button elements wrapping the root level items. This does not
     * include the overflowing items which are rendered in a sub menu, nor the
     * overflow button which opens the sub menu.
     *
     * @return the button elements in the menu bar
     */
    public List<MenuBarButtonElement> getButtons() {
        return $(MenuBarButtonElement.class).all().stream().filter(
                element -> !isOverflowButton(element) && isVisible(element))
                .collect(Collectors.toList());
    }

    /**
     * Gets the button which opens the sub menu of overflowing items, or
     * {@code null} if the overflow button is not visible.
     *
     * @return the button which opens the sub menu of overflowing items
     */
    public MenuBarButtonElement getOverflowButton() {
        MenuBarButtonElement overflowButton = $(MenuBarButtonElement.class)
                .withAttribute("slot", "overflow").first();
        if (overflowButton == null || overflowButton.hasAttribute("hidden")) {
            return null;
        }
        return overflowButton;
    }

    private boolean isOverflowButton(TestBenchElement element) {
        return "overflow".equals(element.getAttribute("slot"));
    }

    private boolean isVisible(TestBenchElement element) {
        return (boolean) executeScript(
                "return arguments[0].style.visibility !== 'hidden'", element);
    }

    /**
     * Get TestBenchElements representing sub menu items under the first sub
     * menu.
     *
     * @return List of MenuBarItemElement representing sub menu items.
     */
    public List<MenuBarItemElement> getSubMenuItems() {
        return getSubMenuItems(getSubMenu());
    }

    /**
     * Get TestBenchElements representing sub menu items under specific sub
     * menu.
     *
     * @param subMenu
     *            The sub menu from which items are being collected.
     * @return List of MenuBarItemElement representing sub menu items.
     */
    public List<MenuBarItemElement> getSubMenuItems(TestBenchElement subMenu) {
        return subMenu.getPropertyElement("_listBox")
                .$(MenuBarItemElement.class).all();
    }

    /**
     * Get the sub menu element.
     *
     * @return TestBenchElement for the first sub menu in this menu bar
     */
    public TestBenchElement getSubMenu() {
        return getPropertyElement("_subMenu");
    }

    /**
     * Get all the opened sub menu elements.
     *
     * @return List of TestBenchElements representing all opened sub menus.
     */
    public List<TestBenchElement> getAllSubMenus() {
        List<TestBenchElement> elements = new ArrayList<>();
        findElements(By.tagName(SUBMENU_TAG)).forEach(element -> {
            if (element.getDomProperty("opened")
                    .equals(Boolean.TRUE.toString())) {
                elements.add((TestBenchElement) element);
            }
        });
        return elements;
    }

}
