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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-side-nav-item&gt;</code>
 * element.
 */
@Element("vaadin-side-nav-item")
public class SideNavItemElement extends TestBenchElement {

    public void navigate() {
        click(1, 1);
    }

    public List<SideNavItemElement> getItems() {
        // get only the direct vaadin-side-nav-item of this vaadin-side-nav
        return wrapElements(findElements(By.xpath("vaadin-side-nav-item")),
                getCommandExecutor())
                .stream()
                .map(testBenchElement -> testBenchElement
                        .wrap(SideNavItemElement.class))
                .collect(Collectors.toList());
    }

    public boolean isExpanded() {
        return hasAttribute("expanded");
    }

    public boolean isActive() {
        return hasAttribute("active");
    }

    public String getLabel() {
        final WebElement unnamedSlot = getWrappedElement().getShadowRoot()
                .findElement(By.cssSelector("slot:not([name])"));
        return (String) executeScript(
                "return arguments[0].assignedNodes()[0].textContent;",
                unnamedSlot);
    }

    public void clickExpandButton() {
        final WebElement element = getWrappedElement().getShadowRoot()
                .findElement(By.cssSelector("button[part='toggle-button']"));
        executeScript("arguments[0].click();", element);
    }
}
