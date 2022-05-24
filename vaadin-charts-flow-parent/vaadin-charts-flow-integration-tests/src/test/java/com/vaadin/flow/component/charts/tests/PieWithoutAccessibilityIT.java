/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
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
