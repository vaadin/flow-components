/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.area.AreaChart;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

public class BasicChartIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return AreaChart.class;
    }

    @Test
    public void Chart_TitleDisplayed() {
        final WebElement chart = getChartElement();
        final WebElement title = getElementFromShadowRoot(chart,
                By.className("highcharts-title"));
        assertTrue(title.getText().contains("First Chart for Flow"));
    }


    @Test

    public void Chart_TitleCanBeChanged() {
        final WebElement chart = getChartElement();
        final WebElement title = getElementFromShadowRoot(chart,
                By.className("highcharts-title"));
        assertTrue(title.getText().contains("First Chart for Flow"));

        final WebElement changeTitleButton = findElement(By.id("change_title"));
        changeTitleButton.click();

        final WebElement titleChanged = getElementFromShadowRoot(chart,
                By.className("highcharts-title"));
        assertTrue(titleChanged.getText()
                .contains("First Chart for Flow - title changed"));
    }

    @Test
    public void Chart_SeriesNameIsSet() {
        final WebElement chart = getChartElement();
        final WebElement series = getElementFromShadowRoot(chart,
                By.className("highcharts-legend-item"));
        assertTrue(series.getText().contains("Tokyo"));
    }

}
