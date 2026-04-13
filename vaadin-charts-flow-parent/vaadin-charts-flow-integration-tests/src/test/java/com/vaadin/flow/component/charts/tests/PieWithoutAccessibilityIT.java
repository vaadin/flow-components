/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-charts/pie/pie-without-accessibility")
public class PieWithoutAccessibilityIT extends AbstractChartIT {

    @Test
    public void setSize_showChart_DimentionsAreSet() {
        ChartElement chart = getChartElement();
        Assert.assertFalse("Accessibility should be disabled",
                chart.getPropertyBoolean("configuration", "userOptions",
                        "accessibility", "enabled"));
        Assert.assertFalse(chart.$("title").exists());
    }
}
