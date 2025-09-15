/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.column.ColumnWithZooming;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.testbench.TestBenchElement;

public class ChartZoomingIT extends AbstractTBTest {

    private ChartElement chart;
    private String[] chartXAxisLabels;
    private String[] chartYAxisLabels;

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return ColumnWithZooming.class;
    }

    @Before
    public void init() {
        chart = getChartElement();
        chartXAxisLabels = getChartLabels("x");
        chartYAxisLabels = getChartLabels("y");
    }

    private String[] getChartLabels(String axis) {
        var axisLabels = chart.$(TestBenchElement.class)
                .withClassName("highcharts-" + axis + "axis-labels").first();
        return axisLabels.getChildren().stream().map(label -> label.getText())
                .toArray(String[]::new);
    }

    @Test
    public void zoomingXSet_zoomInChart_zoomedInX() {
        $("button").id("zoom-X").click();
        scrollChart();
        assertZoomedInX(true);
        assertZoomedInY(false);
    }

    @Test
    public void zoomingYSet_zoomInChart_noZoomingX() {
        $("button").id("zoom-Y").click();
        scrollChart();
        assertZoomedInX(false);
        assertZoomedInY(true);
    }

    @Test
    public void zoomingXYSet_zoomInChart_zoomedInX() {
        $("button").id("zoom-XY").click();
        scrollChart();
        assertZoomedInX(true);
        assertZoomedInY(true);
    }

    private void assertZoomedInX(boolean zoomedIn) {
        var chartXAxisLabelsAfterZoom = getChartLabels("x");

        // Assert that the X axis labels have changed after zooming in
        // (indicating that the chart has zoomed in on the X axis)
        if (zoomedIn) {
            Assert.assertFalse(
                    Arrays.equals(chartXAxisLabels, chartXAxisLabelsAfterZoom));
        } else {
            Assert.assertArrayEquals(chartXAxisLabels,
                    chartXAxisLabelsAfterZoom);
        }
    }

    private void assertZoomedInY(boolean zoomedIn) {
        var chartYAxisLabelsAfterZoom = getChartLabels("y");

        // Assert that the X axis labels have changed after zooming in
        // (indicating that the chart has zoomed in on the Y axis)
        if (zoomedIn) {
            Assert.assertFalse(
                    Arrays.equals(chartYAxisLabels, chartYAxisLabelsAfterZoom));
        } else {
            Assert.assertArrayEquals(chartYAxisLabels,
                    chartYAxisLabelsAfterZoom);
        }
    }

    private void scrollChart() {
        var zoomingElement = chart.$(TestBenchElement.class)
                .withClassName("highcharts-plot-background").first();
        System.out.println(zoomingElement.getAttribute("class"));
        var scrollOrigin = WheelInput.ScrollOrigin.fromElement(zoomingElement);
        new Actions(getDriver()).scrollFromOrigin(scrollOrigin, -500, -500)
                .perform();
    }
}
