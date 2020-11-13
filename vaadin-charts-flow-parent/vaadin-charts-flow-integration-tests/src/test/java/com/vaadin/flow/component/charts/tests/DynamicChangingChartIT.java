package com.vaadin.flow.component.charts.tests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.other.DynamicChangingChart;
import com.vaadin.flow.component.charts.testbench.ChartElement;

public class DynamicChangingChartIT extends AbstractTBTest {
    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return DynamicChangingChart.class;
    }

    @Test
    @org.junit.Ignore("Unstable test when migrated to mono-repo")
    public void setConfiguration_changes_chart() {
        ChartElement chart = getChartElement();
        findElement(By.id("set_funnel_button")).click();
        assertTitle(chart, "Sales funnel");
        findElement(By.id("set_polar_button")).click();
        assertTitle(chart, "Polar Chart");
        findElement(By.id("set_line_button")).click();
        assertTitle(chart, "Solar Employment Growth by Sector, 2010-2016");
    }

    private void assertTitle(ChartElement chart, String expectedTitle) {
        WebElement title = getElementFromShadowRoot(chart, By.className("highcharts-title"));
        waitUntil(e -> expectedTitle.equals(title.getText()), 2);
    }
}
