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
import java.util.Objects;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-menu-bar&gt;</code>
 * element.
 */
@Element("vaadin-menu-bar")
public class MenuBarElement extends TestBenchElement {

    public static final String OVERLAY_TAG = "vaadin-menu-bar-overlay";

    /**
     * Gets the button elements wrapping the root level items. This does not
     * include the overflowing items which are rendered in a sub menu, nor the
     * overflow button which opens the sub menu.
     *
     * @return the button elements in the menu bar
     */
    public List<TestBenchElement> getButtons() {
        return $("vaadin-menu-bar-button").all().stream().filter(
                element -> !isOverflowButton(element) && isVisible(element))
                .collect(Collectors.toList());
    }

    /**
     * Gets the button which opens the sub menu of overflowing items, or
     * {@code null} if the overflow button is not visible.
     *
     * @return the button which opens the sub menu of overflowing items
     */
    public TestBenchElement getOverflowButton() {
        TestBenchElement overflowButton = $("[slot='overflow']").first();
        if (overflowButton == null || overflowButton.hasAttribute("hidden")) {
            return null;
        }
        return overflowButton;
    }

    private boolean isOverflowButton(TestBenchElement element) {
        return Objects.equals("overflow", element.getDomAttribute("slot"));
    }

    private boolean isVisible(TestBenchElement element) {
        return (boolean) executeScript(
                "return arguments[0].style.visibility !== 'hidden'", element);
    }

    /**
     * Get TestBenchElements representing sub menu items under the first sub
     * menu.
     *
     * @return List of TestBenchElements representing sub menu items.
     */
    public List<TestBenchElement> getSubMenuItems() {
        return getSubMenuItems(getSubMenu());
    }

    /**
     * Get TestBenchElements representing sub menu items under specific sub
     * menu.
     *
     * @param overlay
     *            The sub menu overlay from which items are being collected.
     * @return List of TestBenchElements representing sub menu items.
     */
    public List<TestBenchElement> getSubMenuItems(TestBenchElement overlay) {
        return overlay.$("vaadin-menu-bar-item").all();
    }

    /**
     * Get the sub menu overlay element.
     *
     * @return TestBenchElement for the first open sub menu.
     */
    public TestBenchElement getSubMenu() {
        waitForSubMenu();
        return (TestBenchElement) getDriver()
                .findElement(By.tagName(OVERLAY_TAG));
    }

    /**
     * Get all the open sub menu overlay elements.
     *
     * @return List of TestBenchElements representing currently open sub menus.
     */
    public List<TestBenchElement> getAllSubMenus() {
        waitForSubMenu();
        List<TestBenchElement> elements = new ArrayList<>();
        getDriver().findElements(By.tagName(OVERLAY_TAG))
                .forEach(element -> elements.add((TestBenchElement) element));
        return elements;
    }

    private void waitForSubMenu() {
        waitUntil(ExpectedConditions
                .presenceOfElementLocated(By.tagName(OVERLAY_TAG)));
    }

}
