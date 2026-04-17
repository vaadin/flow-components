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

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-breadcrumb&gt;</code>
 * element.
 */
@Element("vaadin-breadcrumb")
public class BreadcrumbElement extends TestBenchElement {

    /**
     * Gets all breadcrumb items.
     *
     * @return list of breadcrumb item elements
     */
    public List<BreadcrumbItemElement> getItems() {
        return $(BreadcrumbItemElement.class).all();
    }

    /**
     * Gets the current (active) breadcrumb item.
     *
     * @return the current item, or {@code null} if none is marked as current
     */
    public BreadcrumbItemElement getCurrentItem() {
        return $(BreadcrumbItemElement.class).withAttribute("current").first();
    }

    /**
     * Checks whether the breadcrumb is in overflow mode, i.e., some items are
     * hidden behind an overflow button.
     *
     * @return {@code true} if the breadcrumb has overflow
     */
    public boolean hasOverflow() {
        return hasAttribute("overflow");
    }

    /**
     * Clicks the overflow button to show hidden breadcrumb items.
     *
     * @throws NoSuchElementException
     *             if the overflow button is not present
     */
    public void clickOverflowButton() {
        WebElement overflowButton;
        try {
            overflowButton = getWrappedElement().getShadowRoot()
                    .findElement(org.openqa.selenium.By
                            .cssSelector("[part='overflow-button']"));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(
                    "Breadcrumb does not contain an overflow button", e);
        }
        // click() on elements in shadow DOM does not work with Chrome driver
        executeScript("arguments[0].click();", overflowButton);
    }
}
