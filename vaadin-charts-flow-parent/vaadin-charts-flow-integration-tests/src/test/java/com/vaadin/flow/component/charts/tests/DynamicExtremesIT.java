/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.demo.AbstractChartExample;
import com.vaadin.flow.component.charts.demo.examples.dynamic.DynamicExtremes;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertNotEquals;

public class DynamicExtremesIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return DynamicExtremes.class;
    }

    @Test
    public void axisFunction_toggleExtremesPoint_pointHidden() {
        ChartElement chart = getChartElement();
        int initialVisiblePointsCount = chart.getVisiblePoints().size();
        findElement(By.id("toggleExtremesButton")).click();
        assertNotEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
    }
}
