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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-charts/dynamic/dynamic-extremes")
public class DynamicExtremesIT extends AbstractChartIT {

    @Test
    public void axisFunction_toggleExtremesPoint_pointHidden() {
        ChartElement chart = getChartElement();
        WebElement toggleExtremesButton = findElement(
                By.id("toggleExtremesButton"));
        int initialVisiblePointsCount = chart.getVisiblePoints().size();
        toggleExtremesButton.click();
        Assert.assertNotEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
        toggleExtremesButton.click();
        Assert.assertEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
    }
}
