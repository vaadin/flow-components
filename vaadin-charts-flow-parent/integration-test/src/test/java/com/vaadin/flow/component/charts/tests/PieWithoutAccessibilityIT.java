/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.pie.PieWithoutAccessibility;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class PieWithoutAccessibilityIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
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
