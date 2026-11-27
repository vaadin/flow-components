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

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-charts/other/declared-functions")
public class DeclaredFunctionsIT extends AbstractChartIT {

    @Test
    public void dataLabels_formattedByDeclaredFunction() {
        ChartElement chart = getChartElement();
        waitUntil(driver -> labels(chart, 0).size() == 3);
        Assert.assertEquals(List.of("2k", "4k", "8k"), labels(chart, 0));
    }

    @Test
    public void addSeries_dataLabelsFormattedByDeclaredFunction() {
        ChartElement chart = getChartElement();

        findElement(By.id("add-series")).click();

        waitUntil(driver -> labels(chart, 1).size() == 3);
        Assert.assertEquals(List.of("10%", "20%", "30%"), labels(chart, 1));
    }

    private List<String> labels(ChartElement chart, int seriesIndex) {
        return chart
                .$(".highcharts-data-labels.highcharts-series-" + seriesIndex
                        + " text")
                .all().stream()
                // The last child is the label text, after the outline copy
                .map(text -> text.getPropertyString("lastChild", "textContent"))
                .toList();
    }
}
