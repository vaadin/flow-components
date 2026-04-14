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

import com.vaadin.flow.component.charts.examples.pie.PieWithClassName;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-charts/pie/pie-with-class-name")
public class ChartStyleIT extends AbstractChartIT {

    @Test
    public void addClassName_showChart_classIsPresent() {
        ChartElement chart = getChartElement();
        Assert.assertTrue(
                chart.getClassNames().contains(PieWithClassName.CLASS_NAME));
    }
}
