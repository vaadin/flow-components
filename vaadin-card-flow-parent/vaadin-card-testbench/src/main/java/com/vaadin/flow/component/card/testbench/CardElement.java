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
package com.vaadin.flow.component.card.testbench;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-card&gt;</code> element.
 */
@Element("vaadin-card")
public class CardElement extends TestBenchElement {

    /**
     * Gets the content elements.
     *
     * @return the content elements
     */
    public List<TestBenchElement> getContentElements() {
        var contentWrapper = findElement(By.cssSelector("div:not([slot])"));
        if (contentWrapper == null) {
            return Collections.emptyList();
        }
        return contentWrapper.getPropertyElements("children");
    }

    /**
     * Gets the element in the title slot.
     *
     * @return the title element, or {@code null} if not found
     */
    public TestBenchElement getTitle() {
        return getElementInSlot("title");
    }

    /**
     * Gets the title property value.
     *
     * @return title property value
     */
    public String getStringTitle() {
        return getPropertyString("title");
    }

    /**
     * Gets the element in the subtitle slot.
     *
     * @return the subtitle element, or {@code null} if not found
     */
    public TestBenchElement getSubtitle() {
        return getElementInSlot("subtitle");
    }

    /**
     * Gets the element in the media slot.
     *
     * @return the media element, or {@code null} if not found
     */
    public TestBenchElement getMedia() {
        return getElementInSlot("media");
    }

    /**
     * Gets the element in the header slot.
     *
     * @return the title element, or {@code null} if not found
     */
    public TestBenchElement getHeader() {
        return getElementInSlot("header");
    }

    /**
     * Gets the element in the header prefix slot.
     *
     * @return the header prefix element, or {@code null} if not found
     */
    public TestBenchElement getHeaderPrefix() {
        return getElementInSlot("header-prefix");
    }

    /**
     * Gets the element in the header suffix slot.
     *
     * @return the header suffix element, or {@code null} if not found
     */
    public TestBenchElement getHeaderSuffix() {
        return getElementInSlot("header-suffix");
    }

    /**
     * Gets the elements in the footer slot.
     *
     * @return the footer elements
     */
    public List<TestBenchElement> getFooterElements() {
        return wrapElements(
                getWrappedElement()
                        .findElements(By.cssSelector("[slot='footer']")),
                getCommandExecutor());
    }

    private TestBenchElement getElementInSlot(String slotName) {
        return findElement(By.cssSelector("[slot='%s']".formatted(slotName)));
    }
}
