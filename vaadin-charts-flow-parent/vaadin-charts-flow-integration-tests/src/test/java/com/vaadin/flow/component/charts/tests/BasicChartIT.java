/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.area.AreaChart;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
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
