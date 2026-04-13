/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

public abstract class AbstractChartIT extends AbstractComponentIT {

    @Override
    public void setup() throws Exception {
        super.setup();
        open();
    }

    protected ChartElement getChartElement() {
        return $(ChartElement.class).waitForFirst();
    }

    protected TestBenchElement getElementFromShadowRoot(
            TestBenchElement shadowRootOwner, String selector) {
        return shadowRootOwner.$(selector).first();
    }

    protected TestBenchElement getElementFromShadowRoot(
            TestBenchElement shadowRootOwner, String selector, int index) {
        ElementQuery<TestBenchElement> elements = shadowRootOwner.$(selector);
        if (elements.all().size() > index) {
            return elements.get(index);
        }

        throw new AssertionError(
                "Could not find required element in the shadowRoot");
    }
}
