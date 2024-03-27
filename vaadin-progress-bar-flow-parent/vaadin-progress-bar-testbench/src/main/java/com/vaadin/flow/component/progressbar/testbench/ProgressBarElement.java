/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.progressbar.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-progress-bar&gt;</code>
 * element.
 */
@Element("vaadin-progress-bar")
public class ProgressBarElement extends TestBenchElement {

    /**
     * Gets the value.
     *
     * @return the value
     */
    public double getValue() {
        return getPropertyDouble("value");
    }
}