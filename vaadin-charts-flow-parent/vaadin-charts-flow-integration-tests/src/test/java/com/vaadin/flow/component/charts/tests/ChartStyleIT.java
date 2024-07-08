/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.pie.PieWithClassName;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class ChartStyleIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return PieWithClassName.class;
    }

    @Test
    public void addClassName_showChart_classIsPresent() {
        ChartElement chart = getChartElement();
        assertTrue(chart.getClassNames().contains(PieWithClassName.CLASS_NAME));
    }
}
