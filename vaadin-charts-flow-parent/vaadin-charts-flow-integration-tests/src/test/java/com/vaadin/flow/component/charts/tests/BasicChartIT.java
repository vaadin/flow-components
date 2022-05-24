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

import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.area.AreaChart;

import static org.junit.Assert.assertTrue;

public class BasicChartIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return AreaChart.class;
    }

    @Test
    public void Chart_TitleDisplayed() {
        final TestBenchElement chart = getChartElement();
        final WebElement title = chart.$("*")
                .attributeContains("class", "highcharts-title").first();
        assertTrue(title.getText().contains("First Chart for Flow"));
    }

    @Test

    public void Chart_TitleCanBeChanged() {
        final TestBenchElement chart = getChartElement();
        final WebElement title = chart.$("*")
                .attributeContains("class", "highcharts-title").first();
        assertTrue(title.getText().contains("First Chart for Flow"));

        final WebElement changeTitleButton = findElement(By.id("change_title"));
        changeTitleButton.click();

        final WebElement titleChanged = chart.$("*")
                .attributeContains("class", "highcharts-title").first();
        assertTrue(titleChanged.getText()
                .contains("First Chart for Flow - title changed"));
    }

    @Test
    public void Chart_SeriesNameIsSet() {
        final TestBenchElement chart = getChartElement();
        final WebElement series = chart.$("*")
                .attributeContains("class", "highcharts-legend-item").first();
        assertTrue(series.getText().contains("Tokyo"));
    }

}
