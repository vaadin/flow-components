/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.sidenav.testbench;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.By;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * A TestBench element representing a <code>&lt;vaadin-side-nav-item&gt;</code>
 * element.
 */
@Element("vaadin-side-nav-item")
public class SideNavItemElement extends TestBenchElement {

    public List<SideNavItemElement> getItems() {
        return getItems(false);
    }

    public List<SideNavItemElement> getItems(boolean includeNestedItems) {
        return getItemsStream(includeNestedItems).collect(Collectors.toList());
    }

    public String getLabel() {
        final WebElement unnamedSlot = getWrappedElement().getShadowRoot()
                .findElement(By.cssSelector("slot:not([name])"));
        return (String) executeScript(
                "return arguments[0].assignedNodes().filter(node => node.nodeType === Node.TEXT_NODE)[0].textContent;",
                unnamedSlot);
    }

    public String getPath() {
        return getAttribute("path");
    }

    public boolean isExpanded() {
        return hasAttribute("expanded");
    }

    public boolean isActive() {
        return hasAttribute("active");
    }

    @Override
    public void click() {
        click(1, 1);
    }

    public void navigate() {
        WebElement anchorElement;
        try {
            anchorElement = getWrappedElement().getShadowRoot()
                    .findElement((By.cssSelector("a")));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Item does not contain an anchor",
                    e);
        }
        // click() on elements in shadow DOM does not work with Chrome driver
        executeScript("arguments[0].click();", anchorElement);
    }

    public void toggle() {
        WebElement toggleButtonElement;
        try {
            toggleButtonElement = getWrappedElement().getShadowRoot()
                    .findElement(
                            By.cssSelector("button[part='toggle-button']"));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(
                    "Item does not contain a toggle button", e);
        }
        // click() on elements in shadow DOM does not work with Chrome driver
        executeScript("arguments[0].click();", toggleButtonElement);
    }

    private Stream<SideNavItemElement> getItemsStream(boolean includeChildren) {
        String xpathExp = includeChildren ? ".//vaadin-side-nav-item"
                : "vaadin-side-nav-item";
        return wrapElements(findElements(By.xpath(xpathExp)),
                getCommandExecutor()).stream()
                .map(testBenchElement -> testBenchElement
                        .wrap(SideNavItemElement.class));
    }
}
