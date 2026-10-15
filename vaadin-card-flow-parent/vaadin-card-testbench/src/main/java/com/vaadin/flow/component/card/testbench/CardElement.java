/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
    public List<TestBenchElement> getContents() {
        return findElements(By.cssSelector("div:not([slot])")).stream()
                .findFirst()
                .map(wrapper -> ((TestBenchElement) wrapper)
                        .getPropertyElements("children"))
                .orElse(Collections.emptyList());
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
        return getPropertyString("cardTitle");
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
    public List<TestBenchElement> getFooterContents() {
        return findElements(By.cssSelector("[slot='footer']")).stream()
                .map(el -> (TestBenchElement) el).toList();
    }

    private TestBenchElement getElementInSlot(String slotName) {
        return (TestBenchElement) findElements(
                By.cssSelector("[slot='%s']".formatted(slotName))).stream()
                .findFirst().orElse(null);
    }
}
