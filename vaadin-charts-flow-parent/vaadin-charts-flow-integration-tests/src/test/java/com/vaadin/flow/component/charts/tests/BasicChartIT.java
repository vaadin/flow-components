/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-charts/area/area-chart")
public class BasicChartIT extends AbstractChartIT {

    @Test
    public void Chart_TitleDisplayed() {
        final TestBenchElement chart = getChartElement();
        final WebElement title = chart.$("*").withClassName("highcharts-title")
                .first();
        Assert.assertTrue(title.getText().contains("First Chart for Flow"));
    }

    @Test
    public void Chart_TitleCanBeChanged() {
        final TestBenchElement chart = getChartElement();
        final WebElement title = chart.$("*").withClassName("highcharts-title")
                .first();
        Assert.assertTrue(title.getText().contains("First Chart for Flow"));

        final WebElement changeTitleButton = findElement(By.id("change_title"));
        changeTitleButton.click();

        final WebElement titleChanged = chart.$("*")
                .withClassName("highcharts-title").first();
        Assert.assertTrue(titleChanged.getText()
                .contains("First Chart for Flow - title changed"));
    }

    @Test
    public void Chart_SeriesNameIsSet() {
        final TestBenchElement chart = getChartElement();
        final WebElement series = chart.$("*")
                .withClassName("highcharts-legend-item").first();
        Assert.assertTrue(series.getText().contains("Tokyo"));
    }

    @Test
    public void Chart_LabelDisplayed() {
        final TestBenchElement chart = getChartElement();
        waitUntil(driver -> {
            List<TestBenchElement> labels = chart.$("*")
                    .withClassName("highcharts-label").all();
            return !labels.isEmpty()
                    && labels.stream().map(TestBenchElement::getText)
                            .anyMatch("Sample label"::equals);
        });
    }
}
