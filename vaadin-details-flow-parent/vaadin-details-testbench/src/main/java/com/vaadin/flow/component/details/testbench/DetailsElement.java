/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.details.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-details")
public class DetailsElement extends TestBenchElement {

    /**
     * Returns summary element
     */
    public TestBenchElement getSummary() {
        return $(TestBenchElement.class).attribute("slot", "summary").first();
    }

    /**
     * Returns summary element as string
     */
    public String getSummaryText() {
        return getSummary().getText();
    }

    /**
     * Returns content element
     */
    public TestBenchElement getContent() {
        TestBenchElement contentPlaceholder = $(TestBenchElement.class)
                .attribute("part", "content").first();

        return (TestBenchElement) executeScript(
                "return arguments[0].firstElementChild.assignedNodes()[0];",
                contentPlaceholder);
    }

    /**
     * Whether the details are opened or not
     */
    public boolean isOpened() {
        return getPropertyBoolean("opened");
    }

    /**
     * Whether the component is enabled or not
     */
    public boolean isEnabled() {
        return !getPropertyBoolean("disabled");
    }

    /**
     * Returns a wrapper of the summary component
     */
    public TestBenchElement getSummaryWrapper() {
        return $(TestBenchElement.class).attribute("part", "summary").first();
    }

    /**
     * Expands or collapses the details
     */
    public void toggle() {
        getSummaryWrapper().click();
    }
}
