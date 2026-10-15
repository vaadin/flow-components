/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover.testbench;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-popover&gt;</code>
 * element.
 */
@Element("vaadin-popover")
public class PopoverElement extends TestBenchElement {

    @Override
    public SearchContext getContext() {
        // Find child elements inside the overlay,
        return getPropertyElement("_overlayElement");
    }

    /**
     * Checks whether the popover is shown.
     *
     * @return <code>true</code> if the popover is shown, <code>false</code>
     *         otherwise
     */
    public boolean isOpen() {
        try {
            return getPropertyBoolean("opened");
        } catch (StaleElementReferenceException e) {
            // The element is no longer even attached to the DOM
            // -> it's not open
            return false;
        }
    }
}
