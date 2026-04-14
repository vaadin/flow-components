/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-charts/lineandscatter/line-with-timezone")
public class TimezoneIT extends AbstractChartIT {

    ChartElement chart;

    @Before
    public void init() {
        chart = getChartElement();
    }

    @Test
    public void noTimezoneDefined_labelBasedOnEpoch() {
        assertFirstLabelValue("Jan 1");
    }

    @Test
    public void timezoneAmericaNewYork_labelBasedOnTimezone() {
        $("button").id("America_New_York").click();
        assertFirstLabelValue("07:00 PM");
    }

    @Test
    public void timezoneEuropeHelsinki_labelBasedOnTimezone() {
        $("button").id("Europe_Helsinki").click();
        assertFirstLabelValue("02:00 AM");
    }

    @Test
    public void timezoneUTC_labelBasedOnTimezone() {
        // Change timezone to Europe/Helsinki and then to UTC to ensure
        // that timezone is actually changed and not just set to default
        $("button").id("Europe_Helsinki").click();
        $("button").id("UTC").click();
        assertFirstLabelValue("Jan 1");
    }

    private void assertFirstLabelValue(final String expectedValue) {
        var firstAxisLabel = chart.$(TestBenchElement.class)
                .withClassName("highcharts-xaxis-labels").first()
                .findElement(By.tagName("text"));
        Assert.assertEquals(firstAxisLabel.getText(), expectedValue);
    }
}
