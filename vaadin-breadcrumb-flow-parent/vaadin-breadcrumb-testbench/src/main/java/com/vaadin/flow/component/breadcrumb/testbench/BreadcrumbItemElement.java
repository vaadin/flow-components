/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.breadcrumb.testbench;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-breadcrumb-item&gt;</code> element.
 */
@Element("vaadin-breadcrumb-item")
public class BreadcrumbItemElement extends TestBenchElement {

    /**
     * Gets the text content of this breadcrumb item.
     *
     * @return the text content
     */
    public String getText() {
        return getPropertyString("textContent").trim();
    }

    /**
     * Checks whether this item is the current (active) breadcrumb item.
     *
     * @return {@code true} if the item is current
     */
    public boolean isCurrent() {
        return hasAttribute("current");
    }

    /**
     * Gets the path (href) of this breadcrumb item.
     *
     * @return the path, or {@code null} if not set
     */
    public String getPath() {
        return getDomAttribute("path");
    }

    /**
     * Clicks the link inside this breadcrumb item to navigate.
     *
     * @throws NoSuchElementException
     *             if the item does not contain a link
     */
    public void clickLink() {
        WebElement anchorElement;
        try {
            anchorElement = getWrappedElement().getShadowRoot()
                    .findElement(By.cssSelector("a[part='link']"));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(
                    "Breadcrumb item does not contain a link", e);
        }
        // click() on elements in shadow DOM does not work with Chrome driver
        executeScript("arguments[0].click();", anchorElement);
    }
}
