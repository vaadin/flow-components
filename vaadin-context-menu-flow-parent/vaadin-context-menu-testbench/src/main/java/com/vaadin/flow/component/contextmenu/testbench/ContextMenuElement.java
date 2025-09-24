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

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-context-menu&gt;</code>
 * element.
 *
 * @author Vaadin Ltd
 *
 */
@Element("vaadin-context-menu")
public class ContextMenuElement extends TestBenchElement {

    /**
     * Does a right click on the target using
     * {@link TestBenchElement#contextClick()} and returns the context menu that
     * is opened as a result.
     *
     * @param target
     *            the element that has the context menu
     * @return the opened context menu element
     * @throws NoSuchElementException
     *             if no context menu is opened
     */
    public static ContextMenuElement openByRightClick(TestBenchElement target) {
        target.contextClick();
        TestBenchElement body = wrapElement(
                target.getDriver().findElement(By.tagName("body")),
                target.getCommandExecutor());
        return body.$(ContextMenuElement.class).withAttribute("opened")
                .waitForFirst();
    }

    /**
     * Get the items of this context menu.
     *
     * @return List of menu items.
     */
    public List<ContextMenuItemElement> getMenuItems() {
        TestBenchElement overlayContent = findElement(
                By.cssSelector(":scope > [slot='overlay']"));
        return overlayContent.$(ContextMenuItemElement.class).all();
    }

    /**
     * Get the first menu item matching the text.
     *
     * @return Optional menu item.
     */
    public Optional<ContextMenuItemElement> getMenuItem(String text) {
        return getMenuItems().stream()
                .filter(item -> item.getText().equals(text)).findFirst();
    }

    /**
     * Check if the context menu is open.
     *
     * @return {@code true} if menu is open.
     */
    public boolean isOpen() {
        try {
            return hasAttribute("opened");
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    /**
     * Wait until the context menu is closed and its closing animation has
     * finished.
     *
     * @throws TimeoutException
     *             if the menu does not close
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
