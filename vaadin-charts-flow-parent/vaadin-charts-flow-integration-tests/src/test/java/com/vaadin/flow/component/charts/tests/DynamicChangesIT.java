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
import org.openqa.selenium.By;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-charts/dynamic/dynamic-changes")
public class DynamicChangesIT extends AbstractChartIT {

    @Test
    public void seriesFunction_addPoint_pointCreated() {
        ChartElement chart = getChartElement();
        int initialPointsCount = chart.getPoints().size();
        findElement(By.id("addPointButton")).click();
        Assert.assertEquals(initialPointsCount + 1,
                chart.$(".highcharts-point").all().size());
    }

    @Test
    public void pointFunction_removePoint_pointDeleted() {
        ChartElement chart = getChartElement();
        int initialPointsCount = chart.getPoints().size();
        findElement(By.id("removePointButton")).click();
        Assert.assertEquals(initialPointsCount - 1, chart.getPoints().size());
    }

}
