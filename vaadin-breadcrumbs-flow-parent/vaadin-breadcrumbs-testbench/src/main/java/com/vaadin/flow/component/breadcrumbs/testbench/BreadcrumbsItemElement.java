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
     * Gets the visible text of this breadcrumb item. Reads the text content
     * assigned to the default slot inside the shadow {@code [part="label"]}
     * element.
     *
     * @return the item's label text
     */
    @Override
    public String getText() {
        WebElement labelSlot = getWrappedElement().getShadowRoot()
                .findElement(By.cssSelector("[part='label'] slot:not([name])"));
        return (String) executeScript("return arguments[0].assignedNodes()"
                + ".map(node => (node.nodeType === Node.TEXT_NODE)"
                + "  ? node.textContent.trim() : '')"
                + ".filter(Boolean).join(' ')", labelSlot);
    }

    /**
     * Gets the value of the {@code path} attribute on this breadcrumb item.
     * Returns {@code null} for the current (non-link) item, which has no path.
     *
     * @return the path attribute value, or {@code null} if not set
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
     * Returns {@code true} if this item has content in the prefix slot.
     *
     * @return {@code true} if a prefix component is present, {@code false}
     *         otherwise
     */
    public boolean hasPrefix() {
        return hasAttribute("has-prefix");
    }

    /**
     * Gets the element slotted into the {@code prefix} slot of this breadcrumb
     * item.
     *
     * @return the prefix slot element, or {@code null} if no prefix is set
     */
    public TestBenchElement getPrefixSlotContent() {
        ElementQuery<TestBenchElement> prefix = $("*").withAttribute("slot",
                "prefix");
        return prefix.exists() ? prefix.first() : null;
    }

    /**
     * Clicks the anchor element in the shadow DOM of this breadcrumb item. This
     * only applies to items that have a {@code path} set; for current
     * (non-link) items the click is a no-op.
     */
    @Override
    public void click() {
        try {
            WebElement anchor = getWrappedElement().getShadowRoot()
                    .findElement(By.cssSelector("[part='link']"));
            // click() on elements in shadow DOM does not work with Chrome
            // driver
            executeScript("arguments[0].click();", anchor);
        } catch (NoSuchElementException e) {
            // Current item renders as <span part="nolink"> — nothing to click
        }
    }
}
