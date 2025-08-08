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
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a {@code <vaadin-menu-bar-submenu>} element.
 */
@Element("vaadin-menu-bar-submenu")
public class MenuBarSubMenuElement extends TestBenchElement {
    /**
     * Get the items of this submenu.
     *
     * @return List of menu items.
     */
    public List<MenuBarItemElement> getMenuItems() {
        TestBenchElement overlayContent = findElement(
                By.cssSelector(":scope > [slot='overlay']"));
        return overlayContent.$(MenuBarItemElement.class).all();
    }

    /**
     * Get the first menu item matching the text.
     *
     * @return Optional menu item.
     */
    public Optional<MenuBarItemElement> getMenuItem(String text) {
        return getMenuItems().stream()
                .filter(item -> item.getText().equals(text)).findFirst();
    }

    /**
     * Check if the submenu is open.
     *
     * @return {@code true} if submenu is open.
     */
    public boolean isOpen() {
        try {
            return hasAttribute("opened");
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Wait until the submenu is closed and its closing animation has finished.
     *
     * @throws TimeoutException
     *             if the submenu does not close
     */
    public void waitUntilClosed() {
        waitUntil(driver -> {
            try {
                return !(hasAttribute("opened") || hasAttribute("closing"));
            } catch (StaleElementReferenceException e) {
                return true;
            }
        });
    }
}
