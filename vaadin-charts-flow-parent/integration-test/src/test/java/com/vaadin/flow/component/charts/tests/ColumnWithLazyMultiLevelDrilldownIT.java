package com.vaadin.flow.component.charts.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.column.ColumnWithLazyMultiLevelDrilldown;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ColumnWithLazyMultiLevelDrilldownIT extends AbstractTBTest {

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return ColumnWithLazyMultiLevelDrilldown.class;
    }

    @Test
    public void test() throws IOException, AssertionError {
        final ChartElement chart = $(ChartElement.class).first();
        getFirstDrilldownPoint(chart).click();
        assertLogText("DrilldownEvent: Latin America and Caribbean");

        getFirstDrilldownPoint(chart).click();
        assertLogText("DrilldownEvent: Costa Rica");

        getDrillUpButton(chart).click();
        assertLogText("ChartDrillupEvent");
    }

    private WebElement getFirstDrilldownPoint(ChartElement chart) {
        return getElementFromShadowRoot(chart,
            By.cssSelector(".highcharts-drilldown-point"));
    }

    private WebElement getDrillUpButton(ChartElement chart) {
        return getElementFromShadowRoot(chart, By.cssSelector("button"));
    }

    private void assertLogText(String text) {
        assertTrue(String.format("Couldn't find text '%s' from the log.", text),
            logContainsText(text));
    }

    private boolean logContainsText(String string) {
        return $(TestBenchElement.class).id("log").getText().contains(string);
    }

}
