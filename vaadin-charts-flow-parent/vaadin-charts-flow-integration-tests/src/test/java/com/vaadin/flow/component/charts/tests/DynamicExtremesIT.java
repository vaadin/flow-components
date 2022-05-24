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

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.dynamic.DynamicExtremes;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

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
