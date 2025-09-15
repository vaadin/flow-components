/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.other.DynamicChangingChart;

public class DynamicChangingChartIT extends AbstractTBTest {
    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return DynamicChangingChart.class;
    }

    @Test
    public void setConfiguration_changes_chart() {
        findElement(By.id("set_funnel_button")).click();
        assertTitle("Sales funnel");
        findElement(By.id("set_polar_button")).click();
        assertTitle("Polar Chart");
        findElement(By.id("set_line_button")).click();
        assertTitle("Solar Employment Growth by Sector, 2010-2016");
    }

    private void assertTitle(String expectedTitle) {
        waitUntil(e -> getChartElement().$("*")
                .withAttributeContaining("class", "highcharts-title")
                .withText(expectedTitle).exists(), 2);
    }
}
