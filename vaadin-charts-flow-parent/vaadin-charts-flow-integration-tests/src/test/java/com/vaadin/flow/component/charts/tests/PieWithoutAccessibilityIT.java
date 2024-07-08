/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.pie.PieWithoutAccessibility;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class PieWithoutAccessibilityIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return PieWithoutAccessibility.class;
    }

    @Test
    public void setSize_showChart_DimentionsAreSet() {
        ChartElement chart = getChartElement();
        assertFalse("Accessibility should be disabled",
                chart.getPropertyBoolean("configuration", "userOptions",
                        "accessibility", "enabled"));
        assertFalse(chart.$("title").exists());
    }
}
