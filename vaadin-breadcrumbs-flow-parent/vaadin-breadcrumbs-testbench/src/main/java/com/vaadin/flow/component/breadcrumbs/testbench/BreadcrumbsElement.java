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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-breadcrumbs&gt;</code>
 * element.
 */
@Element("vaadin-breadcrumbs")
public class BreadcrumbsElement extends TestBenchElement {

    /**
     * Gets all breadcrumb items in the trail.
     *
     * @return list of all breadcrumb item elements
     */
    public List<BreadcrumbsItemElement> getItems() {
        return $(BreadcrumbsItemElement.class).all();
    }

    /**
     * Gets the current item — the item that represents the current page. The
     * current item is the last item without a path and carries the
     * {@code current} state attribute.
     *
     * @return the current breadcrumb item element, or {@code null} if none
     */
    public BreadcrumbsItemElement getCurrentItem() {
        return getItems().stream().filter(BreadcrumbsItemElement::isCurrent)
                .findFirst().orElse(null);
    }

    /**
     * Gets the breadcrumb item whose visible text matches the given string.
     *
     * @param text
     *            the text to search for
     * @return the matching breadcrumb item element, or {@code null} if not
     *         found
     */
    public BreadcrumbsItemElement getItemByText(String text) {
        return getItems().stream().filter(item -> text.equals(item.getText()))
                .findFirst().orElse(null);
    }

    /**
     * Gets the breadcrumb item whose {@code path} attribute matches the given
     * path.
     *
     * @param path
     *            the path to search for
     * @return the matching breadcrumb item element, or {@code null} if not
     *         found
     */
    public BreadcrumbsItemElement getItemByPath(String path) {
        return getItems().stream().filter(item -> path.equals(item.getPath()))
                .findFirst().orElse(null);
    }

    /**
     * Returns {@code true} if one or more items are currently collapsed into
     * the overflow overlay. Reads the {@code has-overflow} state attribute.
     *
     * @return {@code true} if the breadcrumbs has overflow, {@code false}
     *         otherwise
     */
    public boolean hasOverflow() {
        return hasAttribute("has-overflow");
    }

    /**
     * Gets the overflow button element from the shadow DOM.
     *
     * @return the shadow-DOM element with {@code part="overflow-button"}
     * @throws NoSuchElementException
     *             if the overflow button is not found in the shadow DOM
     */
    public TestBenchElement getOverflowButton() {
        WebElement button = getWrappedElement().getShadowRoot()
                .findElement(By.cssSelector("[part='overflow-button']"));
        return (TestBenchElement) wrapElement(button, getCommandExecutor());
    }

    /**
     * Opens the overflow overlay by clicking the overflow button and waits
     * until the overlay element reports that it is open.
     */
    public void openOverflowOverlay() {
        WebElement button = getWrappedElement().getShadowRoot()
                .findElement(By.cssSelector("[part='overflow-button']"));
        // click() on elements in shadow DOM does not work with Chrome driver
        executeScript("arguments[0].click();", button);
        waitUntil(driver -> getOverflowOverlay().hasAttribute("opened"));
    }

    /**
     * Gets the {@code <vaadin-breadcrumbs-overlay>} element.
     *
     * @return the overlay element
     */
    public TestBenchElement getOverflowOverlay() {
        return $("vaadin-breadcrumbs-overlay").first();
    }

    /**
     * Gets the breadcrumb item elements that are currently shown inside the
     * open overflow overlay (i.e. items with {@code slot="overlay"}).
     *
     * @return list of breadcrumb item elements inside the open overlay
     */
    public List<TestBenchElement> getOverflowItems() {
        return $(BreadcrumbsItemElement.class).withAttribute("slot", "overlay")
                .all().stream().map(el -> (TestBenchElement) el).toList();
    }
}
