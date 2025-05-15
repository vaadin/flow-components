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

import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a {@code <vaadin-menu-bar-button>} element.
 */
@Element("vaadin-menu-bar-button")
public class MenuBarButtonElement extends TestBenchElement {

    /**
     * Get the sub menu overlay element linked to this menu button.
     *
     * @return TestBenchElement for the open sub menu.
     */
    public TestBenchElement getSubMenu() {
        waitForSubMenu();
        return getPropertyElement("__overlay");
    }

    /**
     * Get TestBenchElements representing sub menu items under this button.
     *
     * @return List of MenuBarItemElement representing sub menu items.
     */
    public List<MenuBarItemElement> getSubMenuItems() {
        return getSubMenu().$(MenuBarItemElement.class).all();
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
        waitUntil(ExpectedConditions.attributeToBe(this, "aria-expanded",
                "true"));
    }

}
