/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.charts.examples.dynamic.ReactiveTitleUpdate;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-charts/dynamic/reactive-title")
public class ReactiveTitleUpdateIT extends AbstractChartIT {

    @Test
    public void titleMutatedWithoutDrawChart_propagatesToClient() {
        TestBenchElement chart = getChartElement();

        waitUntil(driver -> ReactiveTitleUpdate.INITIAL_TITLE
                .equals(titleText(chart)));

        findElement(By.id("updateTitleButton")).click();

        waitUntil(driver -> ReactiveTitleUpdate.UPDATED_TITLE
                .equals(titleText(chart)));
    }

    private static String titleText(TestBenchElement chart) {
        var query = chart.$("*").withClassName("highcharts-title");
        if (query.all().isEmpty()) {
            return null;
        }
        return query.first().getText();
    }
}
