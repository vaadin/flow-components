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
package com.vaadin.flow.component.breadcrumbs.testbench;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-breadcrumbs-item&gt;</code> element.
 */
@Element("vaadin-breadcrumbs-item")
public class BreadcrumbsItemElement extends TestBenchElement {

    /**
     * Gets the visible text of this breadcrumb item.
     *
     * @return the item's text
     */
    @Override
    public String getText() {
        // The default implementation uses innerText, which adds a lot of
        // whitespace in some browsers.
        return getPropertyString("textContent");
    }

    /**
     * Gets the path this breadcrumb item links to.
     *
     * @return the path, or {@code null} for the current item, which has no path
     */
    public String getPath() {
        return getDomAttribute("path");
    }

    /**
     * Returns {@code true} if this item represents the current page.
     *
     * @return {@code true} if this is the current item, {@code false} otherwise
     */
    public boolean isCurrent() {
        return hasAttribute("current");
    }

    /**
     * Returns {@code true} if this item has a prefix component.
     *
     * @return {@code true} if a prefix component is present, {@code false}
     *         otherwise
     */
    public boolean hasPrefix() {
        return hasAttribute("has-prefix");
    }

    /**
     * Gets the prefix component of this breadcrumb item.
     *
     * @return the prefix component, or {@code null} if none is set
     */
    public TestBenchElement getPrefixComponent() {
        ElementQuery<TestBenchElement> prefix = $("*").withAttribute("slot",
                "prefix");
        return prefix.exists() ? prefix.single() : null;
    }

    /**
     * Navigates by activating this breadcrumb item's link.
     *
     * @throws NoSuchElementException
     *             if this item has no link, such as the current item
     */
    public void navigate() {
        WebElement anchor;
        try {
            anchor = getWrappedElement().getShadowRoot()
                    .findElement(By.cssSelector("a"));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Item does not contain an anchor",
                    e);
        }
        // click() on elements in shadow DOM does not work with Chrome driver.
        // Using executeScript also navigates items that are collapsed into the
        // overflow overlay, which are not visible on the page when the overflow
        // is not opened.
        executeScript("arguments[0].click();", anchor);
    }
}
