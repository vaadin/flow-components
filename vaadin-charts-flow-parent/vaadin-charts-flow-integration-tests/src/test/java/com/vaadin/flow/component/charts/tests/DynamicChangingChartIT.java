package com.vaadin.flow.component.charts.tests;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.other.DynamicChangingChart;
import com.vaadin.flow.component.charts.testbench.ChartElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DynamicChangingChartIT extends AbstractTBTest {
    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return DynamicChangingChart.class;
    }

    @Test
    public void setConfiguration_changes_chart() {
        ChartElement chart = getChartElement();
        findElement(By.id("set_funnel_button")).click();
        assertTitle(chart,"Sales funnel");
        findElement(By.id("set_polar_button")).click();
        assertTitle(chart,"Polar Chart");
        findElement(By.id("set_line_button")).click();
        assertTitle(chart,"Solar Employment Growth by Sector, 2010-2016");
    }

    private void assertTitle(ChartElement chart, String expectedTitle) {
        WebElement shadowRoot = (WebElement) executeScript(
            "return arguments[0].shadowRoot", chart);
        Assert.assertEquals(expectedTitle,
            shadowRoot.findElement(By.className("highcharts-title")).getText());
    }
}
