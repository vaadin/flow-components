/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.column.ColumnWithLazyMultiLevelDrilldownCallbackTests;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.testbench.TestBenchElement;

public class ColumnWithLazyMultiLevelDrilldownCallbackTestsIT
        extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getView() {
        return ColumnWithLazyMultiLevelDrilldownCallbackTests.class;
    }

    @Test
    public void test() throws AssertionError {
        ChartElement chart = $(ChartElement.class).first();
        waitUntil(e -> getElementFromShadowRoot(chart,
                ".highcharts-drilldown-point") != null);
        clickDrilldownPoint(chart, 0);
        // Can't drilldown with null callback
        assertEquals(0, getLogMessages().size());

        // Set new callback
        findElement(By.id("setNew")).click();
        // Showing nested drilldowns
        clickDrilldownPoint(chart, 0);
        assertLastLogText("DrilldownEvent: Item1");
        clickDrilldownPoint(chart, 1);
        assertLastLogText("DrilldownEvent: Item1_2");
        clickDrilldownPoint(chart, 1);
        assertLastLogText("DrilldownEvent: Item1_2_2");
        // Set same callback
        findElement(By.id("setSame")).click();
        getDrillUpButton(chart, "Item1_2").click();
        assertLastLogText("ChartDrillupEvent");
        System.out.println("clicking drilldown point before test failure");
        clickDrilldownPoint(chart, 0);
        assertLastLogText("DrilldownEvent: Item1_2_1");
        getDrillUpButton(chart, "Item1_2").click();
        assertLastLogText("ChartDrillupEvent");

        // Set callback to null
        findElement(By.id("setNull")).click();
        clickDrilldownPoint(chart, 0);
        // Can't drilldown with null callback
        assertLastLogText("ChartDrillupEvent");
        getDrillUpButton(chart, "Item1").click();
        // At Item1
        getDrillUpButton(chart, "Top").click();
        // At top level, no more drilldown buttons
        assertLastLogText("ChartDrillupEvent");

        // Can't drilldown with null callback
        clickDrilldownPoint(chart, 0);
        assertLastLogText("ChartDrillupEvent");
    }

    @Test
    public void drilldownSeriesWithLabelFormatter_formatterCallbackIsCalled() {
        ChartElement chart = $(ChartElement.class).first();

        // Set new callback
        findElement(By.id("setNew")).click();
        // Showing nested drilldowns
        clickDrilldownPoint(chart, 0);

        // Get the first label
        TestBenchElement label = getElementFromShadowRoot(chart,
                ".highcharts-data-labels.highcharts-series-0 tspan");
        assertEquals("ITEM1_1", label.getText());
    }

    private void clickDrilldownPoint(ChartElement chart, int index) {
        var button = getElementFromShadowRoot(chart,
                ".highcharts-drilldown-point", index);
        System.out.println("Clicking drilldown point: "
                + button.getAttribute("aria-label"));
        button.click();
    }

    private WebElement getDrillUpButton(ChartElement chart, String label) {
        var elements = getElementsFromShadowRoot(chart,
                ".highcharts-button.highcharts-breadcrumbs-button.highcharts-button-normal");
        for (TestBenchElement drillupButton : elements) {
            if (drillupButton.getText().contains(label)) {
                return drillupButton;
            }
        }
        return null;
    }

    private void assertLastLogText(String text) {
        final List<String> messages = getLogMessages();
        assertEquals(text, messages.get(messages.size() - 1));
    }

    private List<String> getLogMessages() {
        return findElements(By.tagName("li")).stream().map(e -> e.getText())
                .collect(Collectors.toList());
    }
}
