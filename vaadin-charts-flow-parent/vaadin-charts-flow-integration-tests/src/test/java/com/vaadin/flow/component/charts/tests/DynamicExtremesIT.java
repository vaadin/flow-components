/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.dynamic.DynamicExtremes;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class DynamicExtremesIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return DynamicExtremes.class;
    }

    @Test
    public void axisFunction_toggleExtremesPoint_pointHidden() {
        ChartElement chart = getChartElement();
        WebElement toggleExtremesButton = findElement(
                By.id("toggleExtremesButton"));
        int initialVisiblePointsCount = chart.getVisiblePoints().size();
        toggleExtremesButton.click();
        assertNotEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
        toggleExtremesButton.click();
        assertEquals(initialVisiblePointsCount,
                chart.getVisiblePoints().size());
    }
}
