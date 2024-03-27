/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.dynamic.DynamicChanges;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;

public class DynamicChangesIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return DynamicChanges.class;
    }

    @Test
    public void seriesFunction_addPoint_pointCreated() {
        ChartElement chart = getChartElement();
        int initialPointsCount = chart.getPoints().size();
        findElement(By.id("addPointButton")).click();
        assertEquals(initialPointsCount + 1,
                chart.$(".highcharts-point").all().size());
    }

    @Test
    public void pointFunction_removePoint_pointDeleted() {
        ChartElement chart = getChartElement();
        int initialPointsCount = chart.getPoints().size();
        findElement(By.id("removePointButton")).click();
        assertEquals(initialPointsCount - 1, chart.getPoints().size());
    }

}
