/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-side-nav&gt;</code>
 * element.
 */

@Element("vaadin-side-nav")
public class SideNavElement extends TestBenchElement {

    public String getLabel() {
        return $("span").attributeContains("slot", "label").first().getText();
    }

    public boolean isCollapsible() {
        return hasAttribute("collapsible");
    }

    public void toggle() {
        final WebElement element;
        try {
            element = getWrappedElement()
                    .findElement(By.cssSelector("[slot=label]"));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(
                    "Nav does not contain a toggle button", e);
        }
        // click() on elements in shadow DOM does not work with Chrome driver
        executeScript("arguments[0].click();", element);
    }

    public List<SideNavItemElement> getItems() {
        return getItems(false);
    }

    public List<SideNavItemElement> getItems(boolean includeNestedItems) {
        return getItemsStream(includeNestedItems).collect(Collectors.toList());
    }

    public SideNavItemElement getSelectedItem() {
        return getItemsStream(true).filter(SideNavItemElement::isCurrent)
                .findAny().orElse(null);
    }

    public SideNavItemElement getItemByLabel(String label) {
        return getItemsStream(true)
                .filter(item -> label.equals(item.getLabel())).findAny()
                .orElse(null);
    }

    public SideNavItemElement getItemByPath(String path) {
        return getItemsStream(true).filter(item -> path.equals(item.getPath()))
                .findAny().orElse(null);
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
