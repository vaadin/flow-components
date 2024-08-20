/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.component.charts.ui.MainView;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractParallelTest;

public abstract class AbstractTBTest extends AbstractParallelTest {

    @Override
    public void setup() throws Exception {
        super.setup();
        driver.get(getTestUrl(getView()));
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

    /**
     * Overriding the way how test path is calculated. In Charts we prepend a
     * fragment like `/vaadin-charts/area/`
     */
    @Override
    protected String getDeploymentPath(Class<?> viewClass) {
        return "/" + viewClass.getCanonicalName()
                .replace(MainView.EXAMPLE_BASE_PACKAGE, "vaadin-charts/")
                .replace(".", "/");
    }

    protected abstract Class<? extends AbstractChartExample> getView();
}
